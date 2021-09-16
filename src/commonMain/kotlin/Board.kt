import com.soywiz.kds.Array2

typealias PairInt = Pair<Int, Int>

class Board(val arr: Array2<Char>, val difficulty: Int) {
    var colorCourse = 'P'
    var pEndBlack = 'q'
    var pEndWhite = 'Q'
    var endGame = false
    var winWhite = false


    fun copy(): Board = Board(arr.clone(), difficulty).let{
        it.colorCourse = colorCourse
        it.endGame = endGame
        it.winWhite = winWhite
        it
    }

    fun move(start: PairInt, end: PairInt): Turn? {
        if (!allMoves(start).contains(end))
            return null
        if (arr[end] in "kK"){
            endGame = true
            winWhite = arr[end] == 'k'
        }
        arr[end] = arr[start]
        arr[start] = '_'
        colorCourse = nextColorCourse()
        if (arr[end] in "pP" && (end.second == 0 || end.second == 7)){
            arr[end] = if (arr[end] == 'p') pEndBlack else pEndWhite
            return Turn(start, end, arr[end])
        }
        return Turn(start, end)
    }

    fun aiTurn(): Turn?{
        return findBestTurn(difficulty).second
    }

    fun getAllFigures(color: Char = 'p') : List<PairInt> {
        val res = mutableListOf<PairInt>()
        arr.each { x, y, ch -> if (ch isFriend color && ch != '_') res.add(x to y)}
        return res
    }

    fun getAllCourse(color: Char = 'p'): List<Turn> {
        return getAllFigures(color).flatMap { pair ->
            allMoves(pair).map { Turn(pair, it) }
        }
    }

    fun getSeeFigures(color: Char = 'P') : List<PairInt> {
        val res =  getAllFigures(color).flatMap { allMoves(it) }.filter { pair ->
            arr[pair] != '_'
        }
        colorCourse = nextColorCourse()
        val res2 = getAllFigures(swapColor(color)).map { it to allMoves(it) }.filter { r ->
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


    fun allMoves(pair: PairInt): Set<PairInt> {
        if(arr[pair] == '_' || arr[pair] isEnemy colorCourse || endGame)
            return setOf()
        return when (arr[pair].uppercaseChar()) {
            'K' -> kindMoves(pair)
            'P' -> pawnMoves(pair)
            'N' -> knightMovies(pair)
            'B' -> bishopMoves(pair)
            'R' -> rookMoves(pair)
            'Q' -> queenMoves(pair)
            else -> listOf()
        }.toSet()
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

    fun rookMoves(pair: PairInt): List<PairInt> {
        val x = pair.first
        val y = pair.second
        val res = mutableListOf<PairInt>()


        checkTo(x, y, -1, 0, res)
        checkTo(x, y, 1, 0, res)
        checkTo(x, y, 0, -1, res)
        checkTo(x, y, 0, 1, res)

        return res
    }

    fun bishopMoves(pair: PairInt): List<PairInt> {
        val x = pair.first
        val y = pair.second
        val res = mutableListOf<PairInt>()

        checkTo(x, y, -1, -1, res)
        checkTo(x, y, -1, 1, res)
        checkTo(x, y, 1, -1, res)
        checkTo(x, y, 1, 1, res)

        return res
    }
    fun queenMoves(pair: PairInt): List<PairInt> {
        return rookMoves(pair) + bishopMoves(pair)
    }

    fun knightMovies(pair: PairInt): List<PairInt> {
        val x = pair.first
        val y = pair.second
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

    fun kindMoves(pair: PairInt): List<PairInt> {
        val x = pair.first
        val y = pair.second
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

    fun pawnMoves(pair: PairInt): List<PairInt> {
        val x = pair.first
        val y = pair.second
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
    fun dump(){
        arr.dump()
    }

    fun score(): Int{
        var res = 0
        arr.each{ x, y, ch ->
            val s = when(ch.uppercaseChar()){
                'K' -> 900
                'Q' -> 90
                'R' -> 60
                'B' -> 30
                'N' -> 30
                'P' -> 10
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
        fun from(s: String, difficulty: Int): Board =
            Board(Array2(s.split("/").map { parseLine(it) }.toList()), difficulty)

        fun started(difficulty: Int): Board = from(base, difficulty)
        fun parseLine(s: String): List<Char> =
            s.flatMap { ch ->
                if (ch.isDigit()) List(ch.toString().toInt()) { '_' } else listOf(ch)
            }

    }
    data class Turn(val start: PairInt, val end: PairInt, val figure: Char? = null)

}

operator fun Array2<Char>.get(pair: PairInt) =
    this[pair.first, pair.second]

operator fun Array2<Char>.set(pair: PairInt, value: Char) {
    this[pair.first, pair.second] = value
}

