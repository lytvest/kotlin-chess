import com.soywiz.korge.*
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korim.bitmap.BitmapSlice
import com.soywiz.korim.bitmap.slice
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.SizeInt
import kotlin.reflect.KClass

const val WIDTH = 550
const val HEIGHT = 800

suspend fun main() = Korge(Korge.Config(module = MyModule))

object MyModule : Module() {
    override val mainScene: KClass<out Scene> = LoaderScene::class
    override val bgcolor: RGBA = Colors["#1254ab"]
    override val windowSize: SizeInt = SizeInt(WIDTH, HEIGHT)
    override val size: SizeInt = SizeInt(WIDTH, HEIGHT)
    override val title: String = "Invisible chess"

    override suspend fun AsyncInjector.configure() {
        mapInstance(MyDependency("HELLO WORLD"))
        mapSingleton { ResourceLoader(context = get()) }
        mapSingleton { SettingProfile() }
        mapPrototype { StartScene(get(), get()) }
        mapPrototype { LoaderScene(get()) }
    }
}

class MyDependency(val value: String)





