import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.async.asyncAsap
import com.soywiz.korio.async.launch
import com.soywiz.korio.async.launchAsap
import com.soywiz.korio.file.VfsFile
import com.soywiz.korio.file.std.resourcesVfs
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class ResourceLoader(val files: VfsFile = resourcesVfs, val context: CoroutineContext) {
    val map: MutableMap<String, Any> = mutableMapOf()
    var old = launch(context){}
    var countLoaded = 0
    var countLoading = 0

    fun read(names: List<String>, load: suspend VfsFile.() -> Any ){
        countLoading += names.size
        old = launch(context) {
            old.join()
            for (name in names) {
                map[name] = files[name].load()
                countLoaded += 1
            }
        }
    }

    fun isCompleted() = old.isCompleted

    operator fun <T> get(name: String): T? {
        return map[name] as T?
    }

    fun readBitmaps(names:List<String>) = read(names) { readBitmap() }

    fun bitmap(name: String) = get<Bitmap>(name)
    fun sliceBitmap(name: String) = get<Bitmap>(name)?.slice()

    fun percent(): Double = countLoaded.toDouble() / countLoading
    fun resetPercent() {
        countLoading -= countLoaded
        countLoaded = 0
    }

    suspend fun await() {
        old.join()
    }


}