import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.korge.*
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onMouseDrag
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Point
import kotlin.math.min
import com.soywiz.korge.tween.get
import com.soywiz.korgw.GameWindow
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.SizeInt
import com.soywiz.korma.interpolation.Easing
import kotlin.coroutines.CoroutineContext
import kotlin.math.round
import kotlin.reflect.KClass

const val WIDTH = 550
const val HEIGHT = 800

suspend fun main() = Korge(Korge.Config(module = MyModule))

object MyModule : Module() {
    override val mainScene: KClass<out Scene> = StartScene::class
    override val bgcolor: RGBA = Colors["#1254ab"]
    override val size: SizeInt = SizeInt(WIDTH, HEIGHT)
    override val windowSize: SizeInt = SizeInt(550, 800)
    override val quality: GameWindow.Quality = GameWindow.Quality.QUALITY
    override val title: String = "Invisible chess"

    override suspend fun AsyncInjector.configure() {
        mapInstance(MyDependency("HELLO WORLD"))
        mapPrototype { StartScene(get()) }
    }
}

class MyDependency(val value: String)


suspend fun loadChessImages(): Map<Char, Bitmap> {
    val map = mutableMapOf<Char, Bitmap>()
    val names = "PRNBQK"
    for (ch in names) {
        map[ch] = resourcesVfs["chess/w$ch.png"].readBitmap()
    }
    for (ch in names) {
        map[ch.lowercaseChar()] = resourcesVfs["chess/b$ch.png"].readBitmap()
    }
    return map
}


class BoardContainer(
    val fugures: Map<Char, Bitmap>,
    val coroutineContext: CoroutineContext,
    val sizeItem: Double = min(WIDTH, HEIGHT) / 8.0
) : Container() {
    val white = Colors["#f0d9b5"]
    val whitePressed = Colors["#829769"]
    val black = Colors["#b58863"]
    val blackPressed = Colors["#646f40"]
    var board = Board.started()
    val figureImages = mutableMapOf<Pair<Int, Int>, Image>()
    val rectsBoard = mutableMapOf<Pair<Int, Int>, SolidRect>()
    val figuresExit = mutableListOf<Image>()
    var isDragger = false

    init {

        for (i in 0..7)
            for (j in 0..7) {
                val rect = SolidRect(sizeItem, sizeItem, if ((j + i) % 2 != 0) black else white)
                rect.xy(i * sizeItem, j * sizeItem)
                addChild(rect)
                rectsBoard[i to j] = rect
                rect.onClick {
                    clickTo(i to j)
                }
            }
        for (i in 0..7) {
            text("${8 - i}", textSize = 25.0, color = if (i % 2 != 0) black else white)
                .xy(8 * sizeItem - 15.0, 1 + i * sizeItem)
                .addTo(this)
        }
        val letters = "abcdefgh"
        for (i in 0..7) {
            text(letters[i] + "", textSize = 25.0, color = if (i % 2 != 0) black else white)
                .xy(3 + i * sizeItem, 8 * sizeItem - 30.0)
                .addTo(this)
        }
        createFigures()
    }

    var oldPoint: Pair<Int, Int>? = null

    fun tweenAlpha(img: Image, end: Double, time: TimeSpan = 300.milliseconds) {
        launch(coroutineContext) {
            img.tween(
                img::alpha[img.alpha, end],
                easing = Easing.EASE_OUT,
                time = time
            )
        }
    }

    var isAiTurn = false
    fun nextCourse() {
        if (isAiTurn) {
            isAiTurn = false
            return
        }
        isAiTurn = true

        val aiTurn = board.aiTurn() ?: return
        println("ai turn $aiTurn")
        clickTo(aiTurn.first)
        clickTo(aiTurn.second)

        val seeFigures = board.getSeeFigures()
        for (iter in figureImages) {
            val x = iter.key.first
            val y = iter.key.second
            val img = iter.value
            if (board.isBlack(x, y)) {
                if (seeFigures.contains(iter.key)) {
                    tweenAlpha(img, 1.0, 1.5.seconds)
                } else {
                    tweenAlpha(img, 0.1, 1.seconds)
                }
            }
        }
    }

    private var endGameShow = false
    fun endGame() {
        if (endGameShow)
            return
        endGameShow = true
        showBanner(if (board.winWhite) "winner.png" else "lose.png")
    }

    private fun showBanner(name: String) {
        launch(coroutineContext) {
            val bannerBitmap = resourcesVfs[name].readBitmap()
            val banner = image(bannerBitmap).addTo(this).apply {
                xy(40, 40)
                size(WIDTH - 80.0, height / width * (WIDTH - 80.0))
            }
            banner.tween(banner::x[WIDTH, 40], time = 300.milliseconds, easing = Easing.EASE_IN)
        }
    }

    fun clickTo(p: Pair<Int, Int>) {
        println("click tp $p [${board.arr[p.first, p.second]}] [$oldPoint]")
        if (board.endGame)
            endGame()
        if (oldPoint == null) {
            if (board.arr[p.first, p.second] != '_') {
                swapColors(p)
                oldPoint = p
            }
        } else {
            oldPoint?.apply {
                val (x, y) = this
                swapColors(this)
                if (board.allMoves(x, y).contains(p)) {
                    println("move ${board.arr[x, y]}")
                    board.move(x, y, p.first, p.second)
                    figureImages[x to y]?.let { fig ->
                        moveFigure(fig, p, this)
                    }
                    oldPoint = null
                    nextCourse()
                } else if (isDragger) {
                    isDragger = false
                    figureImages[x to y]?.let { fig ->
                        moveFigure(fig, this, null)
                    }
                }
                oldPoint = null
            }

        }
    }

    private fun moveFigure(
        fig: Image,
        p: Pair<Int, Int>,
        old: Pair<Int, Int>?
    ) {
        old?.let { oldn ->
            figureImages[p]?.let {
                figuresExit.add(it)
                launch(coroutineContext) {
                    it.tween(
                        it::x[it.x, sizeItem * 0.4 * figuresExit.size],
                        it::y[it.y, 8 * sizeItem],
                        easing = Easing.EASE_IN,
                        time = 300.milliseconds
                    )
                }
            }
            figureImages.remove(oldn)
        }

        figureImages[p] = fig
        launch(coroutineContext) {
            fig.tween(
                fig::x[fig.x, p.first * sizeItem],
                fig::y[fig.y, p.second * sizeItem],
                easing = Easing.EASE_IN,
                time = 300.milliseconds
            )
        }
    }

    private fun swapColors(p: Pair<Int, Int>) {
        rectsBoard[p]?.let { it.color = swapColor(it.color) }
        board.allMoves(p.first, p.second).forEach {
            rectsBoard[it]?.let { rect ->
                rect.color = swapColor(rect.color)
            }
        }
    }

    fun swapColor(color: RGBA): RGBA {
        return when (color) {
            white -> whitePressed
            black -> blackPressed
            whitePressed -> white
            blackPressed -> black
            else -> Colors.RED
        }
    }

    fun createFigures() {
        board.arr.each { px, py, ch ->
            fugures[ch]?.let { img ->
                image(img).apply {
                    position(px * sizeItem, py * sizeItem)
                    size(sizeItem, sizeItem)
                    figureImages[px to py] = this
                    onClick {
                        clickTo(toPoint(pos))
                    }
                    onMouseDrag {
                        if (it.start)
                            isDragger = true
                        xy(x - it.deltaDx, y - it.deltaDy)
                        if (it.start || it.end)
                            clickTo(toPoint(pos))
                    }
                }.addTo(this)
            }
        }
    }

    fun toPoint(pos: Point): Pair<Int, Int> {
        return round(pos.x / sizeItem).toInt() to round(pos.y / sizeItem).toInt()
    }
}