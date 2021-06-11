class PIterator<T>(private val iter : Iterator<T>) {

    private var lookahead : T? = null
    fun next() : T {
        lookahead?.let { lookahead = null; return it }
        return iter.next()
    }

    fun peek() : T {
        val token = next()
        lookahead = token
        return token
    }

    fun hasNext(): Boolean {
        return lookahead != null || iter.hasNext()
    }
}