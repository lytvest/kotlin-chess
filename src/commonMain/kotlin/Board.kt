import com.soywiz.kds.Array2
import kotlin.random.Random

typealias PairInt = Pair<Int, Int>

class Board(val arr: Array2<Char>) {
    var colorCourse = 'P'
    var pEbdBlack = 'q'
    var pEbdWhite = 'Q'
    var endGame = false
    var winWhite = false


    fun copy(): Board = Board(arr.clone()).let{
        it.colorCourse = colorCourse
        it.endGame = endGame
        it.winWhite = winWhite
        it
    }

    fun move(sx: Int, sy: Int, ex: Int, ey: Int) {
        if (arr[ex,ey] in "kK"){
            endGame = true
            winWhite = arr[ex, ey] == 'k'
        }
        arr[ex, ey] = arr[sx, sy]
        arr[sx, sy] = '_'
        colorCourse = nextColorCourse()
    }

    fun aiTurn(): Pair<PairInt, PairInt>?{
        return calculateRec(2)?.second
    }

    fun getAllFigures(color: Char = 'p') : List<PairInt> {
        val res = mutableListOf<PairInt>()
        arr.each { x, y, ch -> if (ch isFriend color && ch != '_') res.add(x to y)}
        return res
    }

    fun getAllCourse(color: Char = 'p'): List<Pair<PairInt, PairInt>> {
        return getAllFigures(color).flatMap { pair ->
            allMoves(pair.first, pair.second).map { pair to it }
        }
    }

    fun getSeeFigures(color: Char = 'P') : List<PairInt> {
        val res =  getAllFigures(color).flatMap { allMoves(it.first, it.second) }.filter { (x, y) ->
            arr[x,y] != '_'
        }
        colorCourse = nextColorCourse()
        val res2 = getAllFigures(swapColor(color)).map { it to allMoves(it.first, it.second) }.filter { r ->
            r.second.count { arr[it.first, it.second] != '_' } > 0
        }.map { it.first }
        colorCourse = nextColorCourse()
        return res + res2
    }

    fun isWhile(x:Int, y:Int): Boolean = arr[x,y] isFriend 'P'
    fun isBlack(x:Int, y:Int): Boolean = arr[x,y] isFriend 'p'

    private fun nextColorCourse(): Char =
        swapColor(colorCourse)
    private fun swapColor(ch: Char): Char =
        if (ch == 'P') 'p' else 'P'


    fun allMoves(x: Int, y: Int): List<PairInt> {
        if(arr[x,y] == '_' || arr[x,y] isEnemy colorCourse || endGame)
            return listOf()
        return when (arr[x, y].uppercaseChar()) {
            'K' -> kindMoves(x, y)
            'P' -> pawnMoves(x, y)
            'N' -> knightMovies(x, y)
            'B' -> bishopMoves(x, y)
            'R' -> rookMoves(x, y)
            'Q' -> queenMoves(x, y)
            else -> listOf()
        }
    }

    infix fun Char.isEnemy(ch: Char) =
        this.isUpperCase() != ch.isUpperCase()

    infix fun Char.isFriend(ch: Char) =
        this.isUpperCase() == ch.isUpperCase()

    private fun checkTo(x: Int, y: Int, dx: Int, dy: Int, res: MutableList<PairInt>) {
        var nx = x
        var ny = y
        while (true) {
            nx += dx
            ny += dy
            if (nx in 0..7 && ny in 0..7) {
                if (arr[nx, ny] == '_')
                    res.add(PairInt(nx, ny))
                else {
                    if (arr[nx, ny] isEnemy arr[x, y])
                        res.add(PairInt(nx, ny))
                    return
                }
            } else return
        }
    }

    fun rookMoves(x: Int, y: Int): List<PairInt> {
        val res = mutableListOf<PairInt>()


        checkTo(x, y, -1, 0, res)
        checkTo(x, y, 1, 0, res)
        checkTo(x, y, 0, -1, res)
        checkTo(x, y, 0, 1, res)

        return res
    }

    fun bishopMoves(x: Int, y: Int): List<PairInt> {
        val res = mutableListOf<PairInt>()

        checkTo(x, y, -1, -1, res)
        checkTo(x, y, -1, 1, res)
        checkTo(x, y, 1, -1, res)
        checkTo(x, y, 1, 1, res)

        return res
    }
    fun queenMoves(x: Int, y: Int): List<PairInt> {
        return rookMoves(x,y) + bishopMoves(x,y)
    }

    fun knightMovies(x: Int, y: Int): List<PairInt> {
        val res = mutableListOf<PairInt>()
        val list = listOf(
            x + 1 to y + 2,
            x + 2 to y + 1,
            x + 2 to y - 1,
            x + 1 to y - 2,
            x - 1 to y - 2,
            x - 2 to y - 1,
            x - 2 to y + 1,
            x - 1 to y + 2,
        )
        list.forEach { (px, py) ->
            if (px in 0..7 && py in 0..7 && (arr[x, y] isEnemy arr[px, py] || arr[px, py] == '_'))
                res.add(PairInt(px, py))
        }
        return res
    }

    fun kindMoves(x: Int, y: Int): List<PairInt> {
        val res = mutableListOf<PairInt>()
        val list = listOf(
            x - 1 to y - 1,
            x to y - 1,
            x + 1 to y - 1,
            x - 1 to y,
            x + 1 to y,
            x - 1 to y + 1,
            x to y + 1,
            x + 1 to y + 1
        )
        list.forEach { (px, py) ->
            if (px in 0..7 && py in 0..7 && (arr[x, y] isEnemy arr[px, py] || arr[px, py] == '_'))
                res.add(PairInt(px, py))
        }
        return res
    }

    fun pawnMoves(x: Int, y: Int): List<PairInt> {
        val res = mutableListOf<PairInt>()

        fun pm(w: Int, line: Int, letter: Boolean) {
            if (!(y + 1 in 0..7 && y - 1 in 0..7))
                return
            if (arr[x, y + w] == '_')
                res.add(PairInt(x, y + w))
            if (y == line && arr[x, y + 2 * w] == '_')
                res.add(PairInt(x, y + 2 * w))
            if (x - 1 >= 0 && (arr[x - 1, y + w].isUpperCase() == letter) && arr[x - 1, y + w] != '_') {
                res.add(PairInt(x - 1, y + w))
            }
            if (x + 1 < 8 && (arr[x + 1, y + w].isUpperCase() == letter) && arr[x + 1, y + w] != '_') {
                res.add(PairInt(x + 1, y + w))
            }
        }

        if (arr[x, y] == 'p') {
            pm(1, 1, true)
        }
        else if (arr[x, y] == 'P')
            pm(-1, 6, false)
        return res
    }

    fun score(): Int{
        var res = 0
        arr.each{ x, y, ch ->
            val s = when(ch.uppercaseChar()){
                'K' -> 100
                'Q' -> 8
                'R' -> 6
                'B' -> 3
                'N' -> 3
                'P' -> 1
                else -> 0
            }
            if (ch.isUpperCase())
                res += s
            else
                res -= s
        }
        return res
    }

    companion object {
        const val base = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
        fun from(s: String): Board =
            Board(Array2(s.split("/").map { parseLine(it) }.toList()))

        fun started(): Board = from(base)
        fun parseLine(s: String): List<Char> =
            s.flatMap { ch ->
                if (ch.isDigit()) List(ch.toString().toInt()) { '_' } else listOf(ch)
            }

    }
}