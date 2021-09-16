import kotlin.random.Random


fun Board.getBestCourse(mins:Boolean = true): Pair<Int, Board.Turn>? {
    var bestScore = Int.MAX_VALUE
    val courses = mutableListOf<Board.Turn>()
    getAllCourse(colorCourse).forEach {
        val nextBoard = copy()
        nextBoard.move(it.start, it.end)
        var sc = nextBoard.score()
        if(!mins)
            sc = -sc
        if (sc == bestScore)
            courses.add(it)
        if (sc < bestScore){
            bestScore = sc
            courses.clear()
            courses.add(it)
        }
    }
    if (courses.isEmpty())
        return null
    return (if(mins) bestScore else -bestScore) to courses[Random.nextInt(courses.size)]
}

fun Board.calculateRec(gl:Int, me: Boolean = true): Pair<Int, Board.Turn>? {
    if(gl == 0) return getBestCourse(me)
    var bestScore = Int.MAX_VALUE

    val courses = mutableListOf<Board.Turn>()
    getAllCourse(colorCourse).map{
        val nextBoard = copy()
        nextBoard.move(it.start, it.end)
        var sc1 = nextBoard.score()
        if(!me)
            sc1 = -sc1
        if (sc1 == bestScore)
            courses.add(it)
        if (sc1 < bestScore){
            bestScore = sc1
            courses.clear()
            courses.add(it)
        }
        val res = nextBoard.calculateRec(gl - 1, !me)
        res?.let { pair ->
            var sc = pair.first
            if (!me)
                sc = -sc
            if (sc == bestScore)
                courses.add(it)
            if (sc < bestScore) {
                bestScore = sc
                courses.clear()
                courses.add(it)
            }
        }
    }
    if (courses.isEmpty())
        return null
    return (if(me) bestScore else -bestScore) to courses[Random.nextInt(courses.size)]
}

fun Board.findBestTurn(count: Int, isMins: Boolean = true): Pair<Int, Board.Turn?> {
    if (count == 0)
        return currentBest(isMins)

    val score = score()
    //println("count=$count, score=${score}")
    //dump()
    //println()

    val turns = getAllCourse(colorCourse).map {
        val board = copy()
        board.move(it.start, it.end)
        val nscore = board.score()
        if ((isMins && nscore > score) || (!isMins && nscore < score))
            board.currentBest(!isMins).first to it
        else
            board.findBestTurn(count - 1, !isMins).first to it
    }

    val best = getMinOrMax(isMins, turns) ?: 0 to null

    //println("best for $count, $best")
    return best
}

private fun Board.currentBest(isMins: Boolean): Pair<Int, Board.Turn?> {
    val turns = getAllCourse(colorCourse).map {
        val board = copy()
        board.move(it.start, it.end)
        board.score() to it
    }
    return getMinOrMax(isMins, turns) ?: 0 to null
}

private fun getMinOrMax(isMins: Boolean, turns: List<Pair<Int, Board.Turn>>): Pair<Int, Board.Turn>? {
    val elem = if (isMins) turns.minByOrNull { it.first } else turns.maxByOrNull { it.first }
    return elem?.let { e ->
        val list = turns.filter { it.first == e.first }
        list[Random.nextInt(list.size)]
    }
}