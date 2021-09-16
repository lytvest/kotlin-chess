import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.kmem.clamp
import com.soywiz.korge.annotations.KorgeExperimental
import com.soywiz.korge.component.docking.dockedTo
import com.soywiz.korge.component.onStageResized
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onDown
import com.soywiz.korge.input.onMouseDrag
import com.soywiz.korge.input.onUp
import com.soywiz.korge.scene.MaskTransition
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.tween.tweenAsync
import com.soywiz.korge.ui.UIButton
import com.soywiz.korge.ui.uiScrollable
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.TransitionFilter
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.BitmapSlice
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.color.RGBAf
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.async.launch
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.ScaleMode
import com.soywiz.korma.interpolation.Easing
import kotlin.math.round

class StartScene(val loader: ResourceLoader, val settings: SettingProfile): Scene() {
    var boardContainer: BoardContainer? = null

    @KorgeExperimental
    override suspend fun Container.sceneInit() {

        val self = this
        val bgBitmap = loader.bitmap("bg.jpg")!!
        image(bgBitmap).size(WIDTH, HEIGHT).addTo(this)

        val nameBitmap = loader.bitmap("name.png")!!
        image(nameBitmap).addTo(this).apply {
            xy(40,40)
            size(WIDTH - 80.0, height / width * (WIDTH - 80.0))
        }
        val settingContainer = container {
            roundRect(
                WIDTH - 100.0,
                HEIGHT - 220.0,
                30.0,
                30.0,
                RGBA(0, 0, 0, 170),
                RGBA(0, 0, 0, 240),
                3.0
            ).addTo(this)
            val text1 = text("Difficulty: ${settings.difficulty + 1}", 30.0){
                xy(20, 14)
            }
            roundRect(WIDTH - 135, 5, 5, 5, RGBA(255,255,255,170)).addTo(this) {
                xy(20,80)
            }
            val start = 20.0
            val end = WIDTH - 165.0
            val len = (end - start) / 4.0
            roundRect(50,50, 30,30, RGBA(255,255,255, 200), RGBA(255,255,255,240), 2.0){
                xy(start + len * 2, 60.0)
                onMouseDrag { info ->
                    x -= info.deltaDx
                    x = x.clamp(start, end)
                    settings.difficulty = round(((x - start) / len)).toInt()
                    text1.text = "Difficulty: ${settings.difficulty + 1}"
                    if (info.end){
                        launch {
                            tweenAsync(
                                this@roundRect::x[start + len * settings.difficulty],
                                time = 0.3.seconds,
                                easing = Easing.EASE_OUT
                            )
                        }
                    }
                }
            }.addTo(this)
            xy(50, 10)
            visible = false
        }
        val settingBitmap = loader.sliceBitmap("settings.png")!!
        val settingPressedBitmap = loader.sliceBitmap("settings-pressed.png")!!

        val settingButton = image(settingBitmap).addTo(this).apply {
            size(100, 100)
            xy(40, HEIGHT - 110)
            onDown {
                bitmap = settingPressedBitmap
            }
            onUp {
                bitmap = settingBitmap
            }
            onClick {
                settingContainer.visible = !settingContainer.visible
                tweenAsync(settingContainer::alpha[1.0], time = 0.3.seconds)
            }
        }

        val profileBitmap = loader.sliceBitmap("user.png")!!
        val profilePressedBitmap = loader.sliceBitmap("user-pressed.png")!!
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


        val playBitmap = loader.sliceBitmap("play-button.png")!!
        val playPressedBitmap = loader.sliceBitmap("play-button-pressed.png")!!
        val playStopBitmap = loader.sliceBitmap("stop.png")!!
        val playStopPressedBitmap = loader.sliceBitmap("stop-pressed.png")!!
        val playButton = image(playBitmap).addTo(this).apply {
            size(200, 200)
            xy(WIDTH / 2 - 100, HEIGHT - 210 )
            onClick {
                if (boardContainer == null) {
                    boardContainer =
                        BoardContainer(loader, coroutineContext, settings, (WIDTH - 60) / 8.0).addTo(self)
                    boardContainer!!.xy(30, 30)
                    tweenAsync(
                        boardContainer!!::x[WIDTH, 30],
                        time = 300.milliseconds,
                        easing = Easing.EASE_IN
                    )
                    bitmap = playStopBitmap
                    settingContainer.visible = false
                    tweenAsync(
                        settingButton::y[HEIGHT + 10.0],
                        time = 300.milliseconds,
                        easing = Easing.EASE_IN
                    )
                    tweenAsync(
                        profileButton::y[HEIGHT + 10.0],
                        time = 300.milliseconds,
                        easing = Easing.EASE_IN
                    )
                } else {
                    self.removeChild(boardContainer)
                    bitmap = playBitmap
                    boardContainer = null
                    tweenAsync(
                        settingButton::y[HEIGHT - 110],
                        time = 300.milliseconds,
                        easing = Easing.EASE_IN
                    )

                    tweenAsync(
                        profileButton::y[HEIGHT - 110],
                        time = 300.milliseconds,
                        easing = Easing.EASE_IN
                    )
                }
            }
            onDown {
                bitmap = if (boardContainer == null) playPressedBitmap else playStopPressedBitmap
            }
            onUp {
                bitmap = if (boardContainer == null) playBitmap else playStopBitmap
            }
        }

    }

}