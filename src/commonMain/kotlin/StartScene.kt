import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.component.docking.dockedTo
import com.soywiz.korge.component.onStageResized
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onDown
import com.soywiz.korge.input.onUp
import com.soywiz.korge.scene.MaskTransition
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.TransitionFilter
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.ScaleMode
import com.soywiz.korma.interpolation.Easing

class StartScene(private val dependency: MyDependency): Scene() {

    lateinit var images : Map<Char, Bitmap>
    var boardContainer: BoardContainer? = null

    override suspend fun Container.sceneInit() {

        val self = this
        val bgBitmap = resourcesVfs["bg.jpg"].readBitmap()
        image(bgBitmap).size(WIDTH, HEIGHT).addTo(this)

        val nameBitmap = resourcesVfs["name.png"].readBitmap()
        image(nameBitmap).addTo(this).apply {
            xy(40,40)
            size(WIDTH - 80.0, height / width * (WIDTH - 80.0))
        }
        val playBitmap = resourcesVfs["play-button.png"].readBitmap().slice()
        val playPressedBitmap = resourcesVfs["play-button-pressed.png"].readBitmap().slice()
        val playStopBitmap = resourcesVfs["stop.png"].readBitmap().slice()
        val playStopPressedBitmap = resourcesVfs["stop-pressed.png"].readBitmap().slice()
        val playButton = image(playBitmap).addTo(this).apply {
            size(200, 200)
            xy(WIDTH / 2 - 100, HEIGHT - 210 )
            onClick {
                if (boardContainer == null) {
                    boardContainer =
                        BoardContainer(images, coroutineContext, (WIDTH - 60) / 8.0).addTo(self)
                    boardContainer!!.xy(30, 30)
                    boardContainer.tween(
                        boardContainer!!::x[WIDTH, 30],
                        time = 300.milliseconds,
                        easing = Easing.EASE_IN
                    )
                    bitmap = playStopBitmap
                } else {
                    self.removeChild(boardContainer)
                    bitmap = playBitmap
                    boardContainer = null
                }
            }
            onDown {
                bitmap = if (boardContainer == null) playPressedBitmap else playStopPressedBitmap
            }
            onUp {
                bitmap = if (boardContainer == null) playBitmap else playStopBitmap
            }
        }

        val settingBitmap = resourcesVfs["settings.png"].readBitmap().slice()
        val settingPressedBitmap = resourcesVfs["settings-pressed.png"].readBitmap().slice()
        val settingButton = image(settingBitmap).addTo(this).apply {
            size(100, 100)
            xy(40, HEIGHT - 110)
            onDown {
                bitmap = settingPressedBitmap
            }
            onUp {
                bitmap = settingBitmap
            }
        }

        val profileBitmap = resourcesVfs["user.png"].readBitmap().slice()
        val profilePressedBitmap = resourcesVfs["user-pressed.png"].readBitmap().slice()
        val profileButton = image(profileBitmap).addTo(this).apply {
            size(100, 100)
            xy(WIDTH - 140, HEIGHT - 110)
            onDown {
                bitmap = profilePressedBitmap
            }
            onUp {
                bitmap = profileBitmap
            }
        }
        images = loadChessImages()
    }

}