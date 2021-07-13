class Parser(private val tokens : Lexer) {

    fun parseExpr(): Expr? {
        return when (val t: Token = tokens.next()) {
            is Token.Literals.NUMBER_LIT, is Token.Literals.VARIABLE_LIT -> parseNumberVariables(t)
            is Token.Constants.PI -> parseConstants(t)
            is Token.Functions.SIN, Token.Functions.COS, Token.Functions.TAN, Token.Functions.LOG,
            Token.Functions.SQRT, Token.Functions.EXP -> parseFunctions(t)
            is Token.Operators.ADDITION, Token.Operators.SUBTRACTION, Token.Operators.MULTIPLICATION,
            Token.Operators.DIVISION -> parseOperator(t)
            is Token.Symbols.LPAREN -> parseBracketedExpression()
            is Token.Symbols.EQUALS -> Expr.Equals
            is Token.ControlTokens.SPLITTER -> Expr.Splitter
            is Token.ControlTokens.EOF -> null
            else -> throw Exception("Unexpected Token: '$t'!")
        }
    }

    private fun <A> parseNumberVariables(token: A): Expr = when {
        token is Token.Literals.NUMBER_LIT && tokens.peek() is Token.Literals.VARIABLE_LIT -> Expr.Variable(
            token.n, (tokens.next() as Token.Literals.VARIABLE_LIT).c
        )
        token is Token.Literals.VARIABLE_LIT && tokens.peek() is Token.Literals.NUMBER_LIT -> Expr.Variable(
            (tokens.next() as Token.Literals.NUMBER_LIT).n, token.c
        )
        token is Token.Literals.NUMBER_LIT -> parseNumbers(token)
        token is Token.Literals.VARIABLE_LIT -> parseVariables(token)
        else -> throw Exception("Unknown token: '$token'!")
    }

    private fun parseConstants(token: Token) : Expr{
        return when (token){
            is Token.Constants.PI -> Expr.Number(Math.PI)
            else -> throw Exception("Unknown Constant: '$token'!")
        }
    }

    private fun parseFunctions(token: Token): Expr {
        expectNext<Token.Symbols.LPAREN>()
        val body : MutableList<Expr> = iterateTokensTillRightParenthesis()
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Function(token.toString(), body)
    }

    private fun parseOperator(token: Token): Expr {
        return when (token) {
            is Token.Operators.ADDITION -> Expr.Addition()
            is Token.Operators.SUBTRACTION -> Expr.Subtraction()
            is Token.Operators.MULTIPLICATION -> Expr.Multiplication()
            is Token.Operators.DIVISION -> Expr.Division()
            else -> throw Exception("Unknown token: '$token'!")
        }
    }

    private fun parseBracketedExpression(): Expr {
        val body : MutableList<Expr> = iterateTokensTillRightParenthesis()
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Bracketed(body)
    }

    private fun iterateTokensTillRightParenthesis(): MutableList<Expr> {
        val body: MutableList<Expr> = mutableListOf()
        while (tokens.peek() != Token.Symbols.RPAREN) {
            val expr: Expr? = parseExpr()
            if (expr != null) body.add(expr) else break
        }
        return body
    }

    private inline fun <reified A> expectNext(): A {
        val next: Token = tokens.next()
        if (next !is A) {
            throw Exception("Unexpected token: '$next'!")
        }
        return next
    }

    private fun parseVariables(t: Token.Literals.VARIABLE_LIT): Expr = Expr.Variable(1.0, t.c)

    private fun parseNumbers(t: Token.Literals.NUMBER_LIT): Expr = Expr.Number(t.n)
}