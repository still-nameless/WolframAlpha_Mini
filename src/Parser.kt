import java.util.*
import kotlin.math.*

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

    private fun <A>applyFunction(token : A, expr : Expr.Number) : Expr {
        return when(token) {
            is Token.Functions.SIN -> Expr.Number(sin(expr.number))
            is Token.Functions.COS -> Expr.Number(cos(expr.number))
            is Token.Functions.TAN -> Expr.Number(tan(expr.number))
            is Token.Functions.SQRT -> Expr.Number(sqrt(expr.number))
            is Token.Functions.LOG -> Expr.Number(ln(expr.number))
            else -> throw Exception("Applying '$token' - function failed!")
        }
    }

    private fun parseToPostfixNotation(list : List<Expr>) : Expr.Number {
        val output : MutableList<Expr> = mutableListOf()
        val operatorStack : Stack<Expr> = Stack()
        for (expr in list) {
            if (expr is Expr.Number || expr is Expr.BoundVariable){
                output.add(expr)
            }
            else if (expr is Expr.Addition || expr is Expr.Subtraction || expr is Expr.Multiplication || expr is Expr.Division) {
                while (operatorStack.isNotEmpty() && comparePrecedenceOfOperators(expr,operatorStack.peek()) <= 0){
                    output.add(operatorStack.pop())
                }
                operatorStack.add(expr)
            }
            else if(expr is Expr.Function){
                parseToPostfixNotation(expr.exprs)
            }
        }
        while (operatorStack.isNotEmpty()){
            output.add(operatorStack.pop())
        }
        return evaluateFunction(output)
    }

    private fun evaluateFunction(list : List<Expr>): Expr.Number {
        val variableStack : Stack<Expr> = Stack()
        for (expr in list){
            if(expr is Expr.Number) {
                variableStack.push(expr)
            }
            else {
                val op1 = variableStack.pop() as Expr.Number
                val op2 = variableStack.pop() as Expr.Number
                variableStack.push(executeOperation(op1,op2,expr))
            }
        }
        return variableStack.pop() as Expr.Number
    }

    private fun <A,B>executeOperation(op1 : A, op2 : A, op : B) : Expr?{
        if (op1 is Expr.Number && op2 is Expr.Number){
            return when (op){
                is Expr.Addition -> Expr.Number(op1.number + op2.number)
                is Expr.Subtraction -> Expr.Number(op1.number - op2.number)
                is Expr.Multiplication -> Expr.Number(op1.number * op2.number)
                is Expr.Division -> Expr.Number(op1.number / op2.number)
                else -> throw Exception("Illegal operator: '$op'!")
            }
        }
        throw Exception("Unknown operand: '$op1'")
    }

    /**
     *     0 -> precedences are equal
     *     1 -> precedence of operator1 is greater than precedence of operator2
     *    -1 -> precedence of operator2 is greater than precedence of operator1
     */
    private fun <A,B>comparePrecedenceOfOperators(op1: A, op2: B) : Int{
        if (op2 == null) return Int.MIN_VALUE
        if(op1 is Expr.Operators && op2 is Expr.Operators) {
           return op1.precedence.compareTo(op2.precedence)
        }
        else
            throw Exception("Mashalla falsches Token diese hehe")
    }

    private fun <A>parseNumberVariables(token: A) : Expr = when {
        token is Token.Literals.NUMBER_LIT && tokens.peek() is Token.Literals.VARIABLE_LIT -> Expr.BoundVariable(
            token.n,(tokens.next() as Token.Literals.VARIABLE_LIT).c)
        token is Token.Literals.VARIABLE_LIT && tokens.peek() is Token.Literals.NUMBER_LIT -> Expr.BoundVariable(
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

    private fun parseOperator(token : Token) : Expr? {
        return when (token){
            is Token.Operators.ADDITION -> Expr.Addition()
            is Token.Operators.SUBTRACTION -> Expr.Subtraction()
            is Token.Operators.MULTIPLICATION -> Expr.Multiplication()
            is Token.Operators.DIVISION -> Expr.Division()
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
        -------- evaluateExpr BEGIN ---------
        val variableStack : Stack<Expr> = Stack()
        for (expr in list){
            if(expr is Expr.Number || expr is Expr.BoundVariable) {
                variableStack.push(expr)
            }
            else if(expr is Expr.Multiplication) {
                val op1 = variableStack.pop()
                val op2 = variableStack.pop()
                if(op1 is Expr.Number && op2 is Expr.Number)
                    variableStack.push(Expr.Number(op1.number * op2.number))
                else if(op1 is Expr.BoundVariable && op2 is Expr.Number)
                    variableStack.push(Expr.BoundVariable(op1.number * op2.number, op1.name))
                else if(op1 is Expr.Number && op2 is Expr.BoundVariable)
                    variableStack.push(Expr.BoundVariable(op1.number * op2.number, op2.name))
                else if(op1 is Expr.BoundVariable && op2 is Expr.BoundVariable)
                    throw Exception("This is not possible in a linear equation!")
            }
        }
        -------- evaluateExpr END ---------





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