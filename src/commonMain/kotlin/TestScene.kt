import com.soywiz.klock.DateTime
import com.soywiz.klock.seconds
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.tween.tweenAsync
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.injector
import com.soywiz.korio.async.launchAsap

class TestScene(val loader: ResourceLoader, val settings: SettingProfile) : Scene() {

    /**
     * DO NOT BLOCK. This is called as a main method of the scene.
     * This is called after [sceneInit].
     * This method doesn't need to complete as long as it suspends.
     * Its underlying job will be automatically closed on the [sceneAfterDestroy].
     * No need to call super.
     */
    override suspend fun Container.sceneMain() {
        val r1 = solidRect(100, 100, Colors.GREENYELLOW).addTo(this).xy(100, 100)
        val r2 = solidRect(100, 100, Colors.RED).addTo(this).xy(400, 100)
        r1.tween(r1::x[400.0], r2::x[0.0], time = 5.seconds)

        calcFactorial()
        injector.dump()
    }

    suspend fun Container.calcFactorial() {
        launchAsap {
            val st = DateTime.now()

            println(factorial(900_000_000L))

            val end = DateTime.now()
            println("time: ${end - st}")
            text("time: ${end - st}").xy(
                WIDTH / 2.0,
                HEIGHT / 2.0
            )
        }
    }

    tailrec fun factorial(x: Long, curr: Long = 1): Long =
        if (x <= 1) curr else factorial(x - 1, curr * x)
}