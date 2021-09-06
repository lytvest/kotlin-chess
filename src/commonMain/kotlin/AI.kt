import kotlin.random.Random


fun Board.getBestCourse(mins:Boolean = true): Pair<Int, Pair<PairInt, PairInt>>? {
    var bestScore = Int.MAX_VALUE
    val courses = mutableListOf<Pair<PairInt, PairInt>>()
    getAllCourse(colorCourse).forEach {
        val s = it.first
        val e = it.second
        val nextBoard = copy()
        nextBoard.move(s.first, s.second, e.first, e.second)
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

fun Board.calculateRec(gl:Int, me: Boolean = true): Pair<Int, Pair<PairInt, PairInt>>? {
    if(gl == 0) return getBestCourse(me)
    var bestScore = Int.MAX_VALUE

    val courses = mutableListOf<Pair<PairInt, PairInt>>()
    getAllCourse(colorCourse).map{
        val s = it.first
        val e = it.second
        val nextBoard = copy()
        nextBoard.move(s.first, s.second, e.first, e.second)
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

