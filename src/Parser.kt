import java.util.*
import kotlin.math.*

class Parser(private val tokens : Lexer) {

    var equation = Expr.Equation(mutableListOf())

    fun parseExpr(): Expr? {
        if(tokens.peek() == null) return equation
        var t : Token = tokens.next()
        return when (t) {
            is Token.Literals.NUMBER_LIT, is Token.Literals.VARIABLE_LIT -> parseNumberVariables(t)
            is Token.Functions.SIN, Token.Functions.COS, Token.Functions.TAN, Token.Functions.LOG,
            Token.Functions.SQRT -> applyFunction(
                t,
                parseToPostfixNotation((parseFunctions(t) as Expr.Function).exprs)
            ) // maybe curry? ;)
            is Token.Operators.ADDITION, Token.Operators.SUBTRACTION, Token.Operators.MULTIPLICATION,
            Token.Operators.DIVISION -> parseOperator(t)
            is Token.Operators.SUBTRACTION -> {
                val tempToken = tokens.next()
                if(tempToken is Token.Literals.VARIABLE_LIT) {
                    Expr.BoundVariable(-1.0, tempToken.c)
                } else if(tempToken is Token.Literals.NUMBER_LIT){
                    Expr.Number(-tempToken.n)
                } else if(tempToken is Token.Symbols.LPAREN) {
                    multiplySome(-1.0,
                        evaluateBracketedExpression(parseToPostfixNotation((parseBracketedExpression() as Expr.Bracketed).exprs)) as MutableList<Expr>
                    ).forEach { equation.expressions.add(it)}
                    return null
                }
                else {
                    t = tempToken
                    parseOperator(t)
                }
            }
            is Token.Symbols.LPAREN -> evaluateBracketedExpression(parseToPostfixNotation((parseBracketedExpression() as Expr.Bracketed).exprs))[0]
            is Token.ControlTokens.EOF, Token.ControlTokens.SPLITTER -> null
            else -> throw Exception("Unexpected Token $t!")
        }
    }

    private fun multiplySome(number : Double, list : MutableList<Expr>) : List<Expr>{
        for (i in 0 until list.size){
            when(val tempExpr = list[i]){
                is Expr.Number -> list[i] = Expr.Number(tempExpr.number * number)
                is Expr.BoundVariable -> list[i] = Expr.BoundVariable(tempExpr.number * number, tempExpr.name)
                else -> continue
            }
        }
        return list
    }

    private fun <A> applyFunction(token: A, expr: Expr.Number): Expr {
        return when (token) {
            is Token.Functions.SIN -> Expr.Number(sin(expr.number))
            is Token.Functions.COS -> Expr.Number(cos(expr.number))
            is Token.Functions.TAN -> Expr.Number(tan(expr.number))
            is Token.Functions.SQRT -> Expr.Number(sqrt(expr.number))
            is Token.Functions.LOG -> Expr.Number(ln(expr.number))
            else -> throw Exception("Applying '$token' - function failed!")
        }
    }

    private fun parseToPostfixNotation(list: List<Expr>): MutableList<Expr> {
        val output: MutableList<Expr> = mutableListOf()
        val operatorStack: Stack<Expr> = Stack()
        for (expr: Expr in list) {
            if (expr is Expr.Number || expr is Expr.BoundVariable) {
                output.add(expr)
            } else if (expr is Expr.Addition || expr is Expr.Subtraction || expr is Expr.Multiplication || expr is Expr.Division) {
                while (operatorStack.isNotEmpty() && comparePrecedenceOfOperators(expr, operatorStack.peek()) <= 0) {
                    output.add(operatorStack.pop())
                }
                operatorStack.add(expr)
            } else if (expr is Expr.Function) {
                parseToPostfixNotation(expr.exprs)
            } else if (expr is Expr.Bracketed) {
                parseToPostfixNotation(expr.exprs)
            }
        }
        while (operatorStack.isNotEmpty()) {
            output.add(operatorStack.pop())
        }
        return output
    }

    private fun evaluateBracketedExpression(input : List<Expr>) : List<Expr>{
        val numberStack : Stack<Expr> = Stack()
        val variableStack : Stack<Expr> = Stack()
        val output : MutableList<Expr> = mutableListOf()
        var containsBoundVariable : Boolean = false

        if(input.size <= 1) return input

        for (expr in input) {
            when (expr){
                is Expr.Number -> numberStack.push(expr)
                is Expr.BoundVariable -> {
                    numberStack.push(Expr.Dummy())
                    variableStack.push(expr)
                }
                is Expr.Operators -> {
                    var op2 = numberStack.pop()
                    var op1 = numberStack.pop()

                    if ((op1 is Expr.Dummy || op2 is Expr.Dummy) && (expr is Expr.Addition || expr is Expr.Subtraction)) {
                        if (op2 is Expr.Dummy) {
                            op2 = variableStack.pop()
                            output.add(Expr.Addition())
                            output.add(op2)
                            if (op1 is Expr.Number) numberStack.push(op1)
                            else {
                                if(expr is Expr.Subtraction) output.add(expr) else output.add(Expr.Addition())
                                output.add(op1)
                            }
                        }
                        else {
                            op1 = variableStack.pop()
                            output.add(Expr.Addition())
                            output.add(op1)
                            if (op2 is Expr.Number) numberStack.push(op2)
                            else {
                                if(expr is Expr.Subtraction) output.add(expr) else output.add(Expr.Addition())
                                output.add(op2)
                            }
                        }
                    }
                    else {
                        numberStack.push(executeOperation(op1, op2, expr))
                    }
                }
                else -> throw Exception("Something went terribly wrong!")
            }
        }
        // warum dummy auf numberSTack -> makes no sense
        if ((numberStack.peek() as Expr.Number).number < 0) output.add(Expr.Subtraction()) else output.add(Expr.Addition())
        output.add(Expr.Number(abs((numberStack.pop() as Expr.Number).number)))
        return output
    }

    private fun <A,B,C>executeOperationWithVariables(op1 : A, op2 : B, op : C) : Expr {

        if(op1 is Expr.Number && op2 is Expr.BoundVariable) {
            return when (op){
                is Expr.Multiplication -> Expr.BoundVariable(op1.number * op2.number, op2.name)
                is Expr.Division -> Expr.BoundVariable(op1.number / op2.number, op2.name)
                else -> throw Exception("Illegal operator: '$op'!")
            }
        }
        else if(op1 is Expr.BoundVariable && op2 is Expr.Number){
            return when (op){
                is Expr.Multiplication -> Expr.BoundVariable(op1.number * op2.number, op1.name)
                is Expr.Division -> Expr.BoundVariable(op1.number / op2.number, op1.name)
                else -> throw Exception("Illegal operator: '$op'!")
            }
        }
        else throw Exception("ERROR")
    }

    private fun evaluateFunction(list: List<Expr>): Expr.Number {
        val variableStack: Stack<Expr> = Stack()
        for (expr in list) {
            if (expr is Expr.Number) {
                variableStack.push(expr)
            } else {
                val op2 = variableStack.pop() as Expr.Number
                val op1 = variableStack.pop() as Expr.Number
                variableStack.push(executeOperation(op1, op2, expr))
            }
        }
        return variableStack.pop() as Expr.Number
    }

    private fun <A, B> executeOperation(op1: A, op2: A, op: B): Expr {
        if (op1 is Expr.Number && op2 is Expr.Number) {
            return when (op) {
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
    private fun <A, B> comparePrecedenceOfOperators(op1: A, op2: B): Int {
        if (op2 == null) return Int.MIN_VALUE
        if (op1 is Expr.Operators && op2 is Expr.Operators) {
            return op1.precedence.compareTo(op2.precedence)
        } else
            throw Exception("Mashalla falsches Token diese hehe")
    }

    private fun <A> parseNumberVariables(token: A): Expr = when {
        token is Token.Literals.NUMBER_LIT && tokens.peek() is Token.Literals.VARIABLE_LIT -> Expr.BoundVariable(
            token.n, (tokens.next() as Token.Literals.VARIABLE_LIT).c
        )
        token is Token.Literals.VARIABLE_LIT && tokens.peek() is Token.Literals.NUMBER_LIT -> Expr.BoundVariable(
            (tokens.next() as Token.Literals.NUMBER_LIT).n, token.c
        )
        token is Token.Literals.NUMBER_LIT -> parseNumbers(token)
        token is Token.Literals.VARIABLE_LIT -> parseVariables(token)
        else -> throw Exception("Unknown token: '$token'")
    }

    private inline fun <reified A> parseFunctions(token: A): Expr {
        expectNext<Token.Symbols.LPAREN>()
        val body = iterateTokensTillRightParenthesis()
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Function(token.toString(), body)
    }

    private fun parseBracketedExpression(): Expr {
        val body = iterateTokensTillRightParenthesis()
        expectNext<Token.Symbols.RPAREN>()
        return Expr.Bracketed(body)
    }

    private fun iterateTokensTillRightParenthesis(): List<Expr> {
        val body: MutableList<Expr> = mutableListOf()
        while (tokens.peek() != Token.Symbols.RPAREN) {
            val expr: Expr? = parseExpr()
            if (expr != null) body.add(expr) else break
        }
        return body
    }

    private fun parseVariables(t: Token.Literals.VARIABLE_LIT): Expr = Expr.BoundVariable(1.0, t.c)

    private fun parseNumbers(t: Token.Literals.NUMBER_LIT): Expr = Expr.Number(t.n)

    private fun parseOperator(token: Token): Expr? {
        return when (token) {
            is Token.Operators.ADDITION -> Expr.Addition()
            is Token.Operators.SUBTRACTION -> Expr.Subtraction()
            is Token.Operators.MULTIPLICATION -> Expr.Multiplication()
            is Token.Operators.DIVISION -> Expr.Division()
            else -> null
        }
    }

    private inline fun <reified A> expectNext(): A {
        val next: Token = tokens.next()
        if (next !is A) {
            throw Exception("Unexpected token: $next")
        }
        return next
    }
}