import kotlin.math.exp

class Parser(private val tokens : Lexer) {

    fun parseExpr() : Expr? {
        return when (val t : Token = tokens.next()){
            is Token.Literals.NUMBER_LIT, is Token.Literals.VARIABLE_LIT -> parseNumberVariables(t, tokens.next())
            is Token.Functions.SIN -> parseFunctions(t)
            is Token.ControlTokens.EOF -> null
            else -> throw Exception("Unexpected Token $t!")
        }
    }

    private fun <A,B>parseNumberVariables(t1: A, t2 : B) : Expr = when {
        t1 is Token.Literals.NUMBER_LIT && t2 is Token.Literals.VARIABLE_LIT -> Expr.BindedVariables(t1.n,t2.c)
        t1 is Token.Literals.VARIABLE_LIT && t2 is Token.Literals.NUMBER_LIT ->  Expr.BindedVariables(t2.n, t1.c)
        t1 is Token.Literals.NUMBER_LIT -> parseNumbers(t1)
        t1 is Token.Literals.VARIABLE_LIT -> parseVariables(t1)
        else -> throw Exception("Coding monkeys at work!")
    }

    //private fun parseFunctions(t : Token) : Expr = Expr.Function(t.toString(),Expr.Number(2.0))
    private inline fun <reified A>parseFunctions(token : A) : Expr {
        expectNext<Token.Symbols.LPAREN>()
        val body = mutableListOf<Expr>()
        while (tokens.peek() != Token.Symbols.RPAREN) { // Rechte Klammer könnte Probleme werfen
            val expr : Expr? = parseExpr()
            // Crasht beim Aufruf von parseNumberVariables wegen des tokens.next()
            if (expr != null)
                body.add(expr)
            else
                break
        }
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Function(token.toString(), body)
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