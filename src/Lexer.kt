class Lexer(input: String) {

    val equation = mutableListOf<Token>()
    val inputString: String = input
    private var iterator : PIterator<Char> = PIterator(input.iterator())
    private var lookahead: Token? = null
    private val isDecimalPoint = {c: Char -> c == '.'}
    private val pElement  = { equation[equation.lastIndex - 1] }

    init {
        while (peek() != Token.EOF){
            equation.add(next())
            fixEquation()
        }
    }

    private fun fixEquation(){
        if(equation.size < 2) return
        when (equation.last()){
            // Adding multiplication signs / determining binded variables
            is Token.VARIABLE_LIT -> {
                when (pElement()){
                    is Token.NUMBER_LIT -> {
                        equation.add(Token.BINDED_VAR_LIT((pElement() as Token.NUMBER_LIT).n,
                            (equation.last() as Token.VARIABLE_LIT).c))
                        repeat(2){
                            equation.removeAt(equation.lastIndex-1)
                        }
                    }
                    is Token.RPAREN -> {
                        insertMultiplicationToken()
                    }
                }
            }
            is Token.NUMBER_LIT -> {
                when (pElement()) {
                    is Token.VARIABLE_LIT -> {
                        equation.add(Token.BINDED_VAR_LIT((equation.last() as Token.NUMBER_LIT).n,
                            (pElement() as Token.VARIABLE_LIT).c))
                        repeat(2) {
                            equation.removeAt(equation.lastIndex - 1)
                        }
                    }
                    is Token.BINDED_VAR_LIT -> {
                        equation.add(Token.BINDED_VAR_LIT((pElement() as Token.BINDED_VAR_LIT).n * (equation.last() as Token.NUMBER_LIT).n,
                            (pElement() as Token.BINDED_VAR_LIT).c))
                        repeat(2) {
                            equation.removeAt(equation.lastIndex - 1)
                        }
                    }
                }
            }
            is Token.LPAREN -> {
                when (pElement()) {
                    is Token.VARIABLE_LIT -> {
                        insertMultiplicationToken()
                    }
                    is Token.NUMBER_LIT -> {
                        insertMultiplicationToken()
                    }
                }
            }
        }
    }

    private fun insertMultiplicationToken() {
        val lastToken = equation.last()
        equation[equation.lastIndex] = Token.MULTIPLICATION
        equation.add(lastToken)
    }

    private fun next() : Token {
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
        return Token.NUMBER_LIT(result.toDouble())
    }

    private fun ident(c: Char): Token {
        var result = c.toString()
        while (iterator.hasNext() && iterator.peek().isLetter()) {
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