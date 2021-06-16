class Lexer(input: String) {

    val equation = mutableListOf<Token>()
    var iterator : PIterator<Char> = PIterator(input.iterator())
    private var lookahead: Token? = null
    private val isDecimalPoint = {c: Char -> c == '.'}
    private val pElement  = { equation[equation.lastIndex - 1] }

    init {
        while (peek() != Token.ControlTokens.EOF){
            equation.add(next())
            fixEquation()
        }
    }

    private fun fixEquation(){
        if(equation.size < 2) return
        when (equation.last()){
            // Adding multiplication signs
            is Token.Literals.VARIABLE_LIT -> {
                when (pElement()){
                    is Token.Symbols.RPAREN -> insertMultiplicationToken()
                    is Token.Literals.NUMBER_LIT -> insertMultiplicationToken()
                }
            }
            is Token.Symbols.LPAREN -> {
                when (pElement()) {
                    is Token.Literals.VARIABLE_LIT -> insertMultiplicationToken()
                    is Token.Literals.NUMBER_LIT -> insertMultiplicationToken()
                }
            }
            is Token.Literals.NUMBER_LIT -> {
                when (pElement()) {
                    is Token.Literals.VARIABLE_LIT -> insertMultiplicationToken()
                }
            }
        }
    }

    private fun insertMultiplicationToken() {
        val lastToken = equation.last()
        equation[equation.lastIndex] = Token.Operators.MULTIPLICATION
        equation.add(lastToken)
    }

    private fun next() : Token {
        lookahead?.let { lookahead = null; return it }
        consumeWhitespace()
        if (!iterator.hasNext()) {
            return Token.ControlTokens.EOF
        }
        return when (val c = iterator.next()) {
            '(' -> Token.Symbols.LPAREN
            ')' -> Token.Symbols.RPAREN
            '+' -> Token.Operators.ADDITION
            '-' -> Token.Operators.SUBTRACTION
            '*' -> Token.Operators.MULTIPLICATION
            '/' -> Token.Operators.DIVISION
            '=' -> Token.Symbols.EQUALS
            ',',';' -> Token.ControlTokens.SPLITTER

            else -> when {
                c.isJavaIdentifierStart() -> ident(c)
                c.isDigit() || isDecimalPoint(c) -> number(c)
                else -> throw Exception("Unexpected Character: '$c'")
            }
        }
    }

    private fun peek(): Token {
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
            throw Exception("Could not convert '${result}${iterator.next()}' into a number!")
        }
        return Token.Literals.NUMBER_LIT(result.toDouble())
    }

    private fun ident(c: Char): Token {
        var result = c.toString()
        while (iterator.hasNext() && iterator.peek().isLetter()) {
            result += iterator.next()
        }
        return when (result) {
            "sin" -> Token.Functions.SIN
            "cos" -> Token.Functions.COS
            "tan" -> Token.Functions.TAN
            "sqrt" -> Token.Functions.SQRT
            "log" -> Token.Functions.LOG
            else -> when(result.length) {
                1 -> Token.Literals.VARIABLE_LIT(result.single())
                else -> throw Exception("Unknown Expression '${result}'")
            }
        }
    }

    private fun consumeWhitespace() {
        while (iterator.hasNext()) {
            val c = iterator.peek()
            if (!c.isWhitespace()) break
            iterator.next()
        }
    }
}