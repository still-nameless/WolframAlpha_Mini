class Parser(private val tokens : Lexer) {

    private var equation = Expr.Equation(mutableListOf())

    fun parseExpr() : Expr? {
        return when (val t : Token = tokens.next()) {
            is Token.Literals.NUMBER_LIT, is Token.Literals.VARIABLE_LIT -> parseNumberVariables(t)
            is Token.Functions.SIN, Token.Functions.COS, Token.Functions.TAN, Token.Functions.LOG,
               Token.Functions.SQRT -> parseFunctions(t)
            is Token.Operators.ADDITION, Token.Operators.SUBTRACTION, Token.Operators.MULTIPLICATION,
               Token.Operators.DIVISION -> null
            is Token.Symbols.LPAREN -> parseBracketedExpression()
            is Token.ControlTokens.EOF, Token.ControlTokens.SPLITTER -> null
            else -> throw Exception("Unexpected Token $t!")
        }
    }

    private fun <A>parseNumberVariables(token: A) : Expr = when {
        token is Token.Literals.NUMBER_LIT && tokens.peek() is Token.Literals.VARIABLE_LIT -> Expr.BindedVariables(
            token.n,(tokens.next() as Token.Literals.VARIABLE_LIT).c)
        token is Token.Literals.VARIABLE_LIT && tokens.peek() is Token.Literals.NUMBER_LIT -> Expr.BindedVariables(
            (tokens.next() as Token.Literals.NUMBER_LIT).n, token.c)
        token is Token.Literals.NUMBER_LIT -> parseNumbers(token)
        token is Token.Literals.VARIABLE_LIT -> parseVariables(token)
        else -> throw Exception("Coding monkeys at work!")
    }

    private inline fun <reified A>parseFunctions(token : A) : Expr {
        expectNext<Token.Symbols.LPAREN>()
        val body = iterateTokensTillRightParenthesis()
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Function(token.toString(), body)
    }

    private fun parseBracketedExpression() : Expr{
        val body = iterateTokensTillRightParenthesis()
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Bracketed(body)
    }

    private fun iterateTokensTillRightParenthesis() : List<Expr>{
        val body : MutableList<Expr> = mutableListOf()
        while (tokens.peek() != Token.Symbols.RPAREN) {
            val expr : Expr? = parseExpr()
            if (expr != null) body.add(expr) else break
        }
        return body
    }

    private fun parseVariables(t : Token.Literals.VARIABLE_LIT) : Expr = Expr.Variable(t.c)

    private fun parseNumbers(t : Token.Literals.NUMBER_LIT) : Expr = Expr.Number(t.n)

    private fun parseOperator() : Operator? {
        return when (tokens.peek()){
            Token.Operators.ADDITION -> Operator.Addition
            Token.Operators.SUBTRACTION -> Operator.Subtraction
            Token.Operators.MULTIPLICATION -> Operator.Multiplication
            Token.Operators.DIVISION -> Operator.Division
            else -> null
        }
    }

    private inline fun <reified A>expectNext(): A {
        val next : Token = tokens.next()
        if (next !is A) {
            throw Exception("Unexpected token: $next")
        }
        return next
    }


    /*
        is Token.Literals.NUMBER_LIT -> {
                        equation.add(Token.Literals.BINDED_VAR_LIT((pElement() as Token.Literals.NUMBER_LIT).n,
                            (equation.last() as Token.Literals.VARIABLE_LIT).c))
                        repeat(2){
                            equation.removeAt(equation.lastIndex-1)
                        }
                    }
        is Token.Literals.NUMBER_LIT -> {
                when (pElement()) {
                    is Token.Literals.VARIABLE_LIT -> {
                        equation.add(Token.Literals.BINDED_VAR_LIT((equation.last() as Token.Literals.NUMBER_LIT).n,
                            (pElement() as Token.Literals.VARIABLE_LIT).c))
                        repeat(2) {
                            equation.removeAt(equation.lastIndex - 1)
                        }
                    }
                    is Token.Literals.BINDED_VAR_LIT -> {
                        equation.add(Token.Literals.BINDED_VAR_LIT((pElement() as Token.Literals.BINDED_VAR_LIT).n * (equation.last() as Token.Literals.NUMBER_LIT).n,
                            (pElement() as Token.Literals.BINDED_VAR_LIT).c))
                        repeat(2) {
                            equation.removeAt(equation.lastIndex - 1)
                        }
                    }
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


    private val pElement  = { equation[equation.lastIndex - 1] }
     */
}