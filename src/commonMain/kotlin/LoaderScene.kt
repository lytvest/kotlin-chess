import com.soywiz.klock.seconds
import com.soywiz.korge.scene.MaskTransition
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.TransitionFilter
import com.soywiz.korim.color.Colors
import kotlin.math.round

class LoaderScene(val loader: ResourceLoader) : Scene() {
    override suspend fun Container.sceneInit() {
        val W = views.virtualWidth
        val H = views.virtualHeight
        println("$W, $H")
        val loadText = text("Loading...          ", 40.0).apply {
            position(W / 2 - width / 2, H / 2.0)
            println("text($width, $height)")
        }
        addFixedUpdater(0.2.seconds) {
            loadText.text = "Loading... ${round(loader.percent() * 100)} %"
        }
    }

    override suspend fun Container.sceneMain() {

        val list = listOf(
            "bg.jpg",
            "name.png", "play-button.png", "play-button-pressed.png",
            "stop.png", "stop-pressed.png", "settings.png", "settings-pressed.png",
            "user.png", "user-pressed.png", "winner.png", "lose.png", "chess/wP.png",
            "chess/bP.png",
            "chess/wR.png",
            "chess/bR.png",
            "chess/wN.png",
            "chess/bN.png",
            "chess/wB.png",
            "chess/bB.png",
            "chess/wQ.png",
            "chess/bQ.png",
            "chess/wK.png",
            "chess/bK.png",
        )

        loader.readBitmaps(list)
        loader.await()
        sceneContainer.changeTo<StartScene>(
            time = 0.3.seconds,
            transition = MaskTransition(TransitionFilter.Transition.SWEEP)
        )

    }
}