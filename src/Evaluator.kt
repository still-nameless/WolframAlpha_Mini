class Evaluator(equation : List<Token>) {

    private val simpEquation = mutableListOf<Token>()
    private var lookahead: Token? = null
    private var iterator : PIterator<Token> = PIterator(equation.iterator())

    init {
        while (peek() != Token.ControlTokens.EOF){
            simpEquation.add(next())
            simplifyEquation()
        }
    }

    private fun simplifyEquation() {
        // Determine function parenthesis
        when (simpEquation.last()){

        }
    }

    private fun next() : Token {
        lookahead?.let { lookahead = null; return it }
        if (!iterator.hasNext()) {
            return Token.ControlTokens.EOF
        }
        return when (val c = iterator.next()) {

            else -> Token.Literals.NUMBER_LIT(2.0)
        }
    }

    private fun findClosingParanthesis(){

    }

    private fun peek(): Token {
        val token = next()
        lookahead = token
        return token
    }
}