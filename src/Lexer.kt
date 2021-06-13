class Lexer(input: String) {

    val iterator : PIterator<Char> = PIterator(input.iterator())
    var lookahead: Token? = null

    fun next() : Token {
        lookahead?.let { lookahead = null; return it }
        consumeWhitespace()
        if (!iterator.hasNext()) {
            return Token.EOF
        }
        return when (val c = iterator.next()) {
            '(' -> Token.LPAREN
            ')' -> Token.RPAREN
            '+' -> Token.ADDITION
            '-' -> Token.SUBTRACTION
            '*' -> Token.MULTIPLICATION
            '/' -> Token.DIVISION
            '=' -> Token.EQUALS
            ',',';' -> Token.SPLITTER

            else -> when {
                c.isJavaIdentifierStart() -> ident(c)
                c.isDigit() || isDecimalPoint(c) -> number(c)
                else -> throw Exception("Unexpected Character: '$c'")
            }
        }
    }

    public fun peek(): Token {
        val token = next()
        lookahead = token
        return token
    }

    private fun number(c: Char): Token {
        var result = c.toString()
        while (iterator.hasNext() && (iterator.peek().isDigit() || isDecimalPoint(iterator.peek()))) {
            result += iterator.next()
        }
        try {
            result.toDouble()
        } catch (exp : Exception){
            throw Exception("Could not convert '${result}${iterator!!.next()}' into a number!")
        }
        return Token.NUMBER_LIT(result.toDouble())
    }

    private fun ident(c: Char): Token {
        var result = c.toString()
        while (iterator.hasNext() && iterator.peek().isJavaIdentifierPart()) {
            result += iterator.next()
        }
        return when (result) {
            "sin" -> Token.SIN
            "cos" -> Token.COS
            "tan" -> Token.TAN
            "sqrt" -> Token.SQRT
            "log" -> Token.LOG
            else -> when(result.length) {
                1 -> Token.VARIABLE_LIT(result.single())
                else -> throw Exception("Unknown function '${result}'")
            }
        }
    }

    private fun consumeWhitespace() {
        while (iterator.hasNext()) {
            val c = iterator.peek()
            if (!c.isWhitespace()) break
            iterator!!.next()
        }
    }

    private val isDecimalPoint = {c: Char -> c == '.'}
}