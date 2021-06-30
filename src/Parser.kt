import java.util.*
import kotlin.math.*

class Parser(private val tokens : Lexer) {

    fun parseExpr(): Expr? {
        return when (val t: Token = tokens.next()) {
            is Token.Literals.NUMBER_LIT, is Token.Literals.VARIABLE_LIT -> parseNumberVariables(t)
            is Token.Constants.PI -> parseConstants(t)
            is Token.Functions.SIN, Token.Functions.COS, Token.Functions.TAN, Token.Functions.LOG,
            Token.Functions.SQRT, Token.Functions.EXP -> applyFunction(
                t, evaluateFunction(parseToPostfixNotation(removeMinus((parseFunctions(t) as Expr.Function).exprs)))
            ) // maybe curry? ;)
            is Token.Operators.ADDITION, Token.Operators.SUBTRACTION, Token.Operators.MULTIPLICATION,
            Token.Operators.DIVISION -> parseOperator(t)
            is Token.Symbols.LPAREN -> {
                val a = (parseBracketedExpression() as Expr.Bracketed).exprs
                val b = removeMinus(a)
                val c = parseToPostfixNotation(b)
                val d = evaluateBracketedExpression(c)
                Expr.PartialEquation(d) //evaluateBracketedExpression(parseToPostfixNotation(removeMinus((parseBracketedExpression() as Expr.Bracketed).exprs))))
            }
            is Token.ControlTokens.EOF, Token.ControlTokens.SPLITTER -> null
            else -> throw Exception("Unexpected Token $t!")
        }
    }

    private fun parseConstants(token: Token) : Expr{
        return when (token){
            is Token.Constants.PI -> Expr.Number(Math.PI)
            else -> throw Exception("Unknown Constant '$token'!")
        }
    }

    private fun <A> applyFunction(token: A, expr: Expr.Number): Expr {
        return when (token) {
            is Token.Functions.SIN -> Expr.Number(sin(expr.number))
            is Token.Functions.COS -> Expr.Number(cos(expr.number))
            is Token.Functions.TAN -> Expr.Number(tan(expr.number))
            is Token.Functions.SQRT -> Expr.Number(sqrt(expr.number))
            is Token.Functions.LOG -> Expr.Number(ln(expr.number))
            is Token.Functions.EXP -> Expr.Number(exp(expr.number))
            else -> throw Exception("Applying '$token' - function failed!")
        }
    }

    private fun removeMinus(list: MutableList<Expr>) : MutableList<Expr> {
        val newEquation : MutableList<Expr> = mutableListOf()
        var i = 0
        while (i < list.size) {
            if (i == 0) {
                if (list[i] is Expr.Subtraction) {
                    newEquation.add(Expr.Number(-1.0))
                    newEquation.add(Expr.Multiplication())
                    i++
                }
            }
            else {
                if (list[i] is Expr.Subtraction) {
                    if (list[i-1] is Expr.Number || list[i-1] is Expr.Variable) {
                        newEquation.add(Expr.Addition())
                        newEquation.add(Expr.Number(-1.0))
                        newEquation.add(Expr.Multiplication())
                        i++
                    }
                }
            }
            newEquation.add(list[i])
            i++
        }
        return newEquation
    }

    private fun parseToPostfixNotation(list : MutableList<Expr>) : MutableList<Expr> {
        val output : MutableList<Expr> = mutableListOf()
        val operatorStack : Stack<Expr> = Stack()
        var modifiedInput : MutableList<Expr> = list
        var i = 0
        while (i < modifiedInput.size) {
            val expr = modifiedInput[i]
            if (expr is Expr.Number){
                output.add(expr)
            }
            else if(expr is Expr.Variable) {
                output.add(expr)
            }
            else if (expr is Expr.PartialEquation){
                modifiedInput = parseToPostfixNotation((multiplyOutBracket(modifiedInput,i,output)).exprs)
                output.clear()
                operatorStack.clear()
                modifiedInput.forEach { output.add(it) }
                i = 0
                //parseToPostfixNotation(expr.exprs).forEach { output.add(it) }
            }
            else if (expr is Expr.Addition || expr is Expr.Subtraction || expr is Expr.Multiplication || expr is Expr.Division) {
                while (operatorStack.isNotEmpty() && comparePrecedenceOfOperators(expr,operatorStack.peek()) <= 0){
                    output.add(operatorStack.pop())
                }
                operatorStack.add(expr)
            }
            else if(expr is Expr.Function){
                parseToPostfixNotation(expr.exprs).forEach { output.add(it) }
            }
            i++
        }
        while (operatorStack.isNotEmpty()){
            output.add(operatorStack.pop())
        }
        return output
    }


    private fun isFactorInFrontBrackets(list : MutableList<Expr>, index : Int) :Boolean {
        if (list.size < 3) return false
        return index > 1 && list[index-1] is Expr.Multiplication && (list[index-2] is Expr.Number || list[index-2] is Expr.Variable || list[index-2] is Expr.PartialEquation)
    }

    private fun isFactorBehindBrackets(list : MutableList<Expr>, index : Int) : Boolean {
        if (list.size < 3) return false
        return index < list.size - 2 && list[index+1] is Expr.Multiplication && (list[index+2] is Expr.Number || list[index+2] is Expr.Variable || list[index+2] is Expr.PartialEquation)
    }

    private fun multiplyOutBracket(input : MutableList<Expr>, index : Int) : MutableList<Expr>{
        if (input.size <= 2) {
            return (input[0] as Expr.PartialEquation).exprs
        }
        if (isFactorInFrontBrackets(input,index) && isFactorBehindBrackets(input,index)){
            val factor = executeOperationWithVariables(input[index-2],input[index+2],Expr.Multiplication())
            return Expr.PartialEquation((
                    input.subList(0, index-2)
                  + applyFactor(input,index,2,factor)
                  + input.subList(index+3, input.size)) as MutableList<Expr>
            )
        }
        else if (isFactorInFrontBrackets(input,index)){
            val result : MutableList<Expr> = mutableListOf(Expr.PartialEquation(applyFactor(input,index,-2)))
            input.subList(0, index-2).forEach { result.add(it) }
            return result
        }
        else if (isFactorBehindBrackets(input,index)) {
            val result : MutableList<Expr> = mutableListOf()
            result.add(Expr.PartialEquation(applyFactor(input, index, 2)))
            input.subList(index+3, input.size).forEach { result.add(it) }
            return result
        }
        return Expr.PartialEquation(output)
    }

    private fun applyFactor(input : MutableList<Expr>, index : Int, offSet : Int, factor : Expr? = null) : MutableList<Expr>{
        val output : MutableList<Expr> = mutableListOf()
        for (expr in (input[index] as Expr.PartialEquation).exprs){
            if (expr is Expr.Variable || expr is Expr.Number){
                if (factor == null)
                    output.add(executeOperationWithVariables(input[index+offSet],expr,Expr.Multiplication()))
                else
                    output.add(executeOperationWithVariables(factor,expr,Expr.Multiplication()))
                output.add(Expr.Addition())
            }
        }
        output.removeLast()
        return output
    }

    private fun evaluateBracketedExpression(input : MutableList<Expr>) : MutableList<Expr>{
        val numberStack : Stack<Expr> = Stack()
        val variableStack : Stack<Expr> = Stack()
        val operatorStack : Stack<Expr> = Stack()
        val output : MutableList<Expr> = mutableListOf()

        if(input.size <= 1) return input

        for (expr in input) {
            when (expr){
                is Expr.Number -> numberStack.push(expr)
                is Expr.Variable -> {
                    numberStack.push(Expr.Dummy)
                    variableStack.push(expr)
                }
                is Expr.Operators -> {
                    operatorStack.push(expr)
                    val op2 = numberStack.pop()
                    val op1 = numberStack.pop()

                    if ((op1 is Expr.Dummy || op2 is Expr.Dummy) && (expr is Expr.Addition || expr is Expr.Subtraction)) {
                        if (op2 is Expr.Dummy) {
                            makeDummiesGreatAgain(op1,variableStack,numberStack,output)
                        }
                        else {
                            makeDummiesGreatAgain(op2,variableStack,numberStack,output)
                        }
                    }
                    else if (op1 is Expr.Dummy && op2 is Expr.Dummy && expr is Expr.Multiplication){
                        throw Exception("This is not a linear equation!")
                    }
                    else if ((op1 is Expr.Dummy || op2 is Expr.Dummy) && (expr is Expr.Multiplication || expr is Expr.Division)){
                        val res = executeOperationWithVariables(op1, op2, expr, variableStack)
                        if (res is Expr.Number) numberStack.push(res)
                        else {
                            numberStack.push(Expr.Dummy)
                            variableStack.push(res)
                        }
                    }
                    else {
                        numberStack.push(executeOperationWithVariables(op1, op2, expr))
                    }
                }
                else -> throw Exception("Something went terribly wrong!")
            }
        }
        if (numberStack.isNotEmpty() && numberStack.peek() !is Expr.Dummy)
            output.add(numberStack.pop())
        else if (output.size > 1)
            output.removeLast()
        else if (variableStack.peek() != null)
            output.add(variableStack.pop())
        return sumpUpVariables(output)
    }

    private fun sumpUpVariables(list : MutableList<Expr>) : MutableList<Expr> {
        var tempList : MutableList<Expr> = list
        val summedUpList: MutableList<Expr> = mutableListOf()
        val variables: MutableList<Char> = mutableListOf()
        val output: MutableList<Expr> = mutableListOf()

        for (expr in tempList) {
            if (expr is Expr.Variable && !variables.contains(expr.name))
                variables.add(expr.name)
        }
        for (i in variables.indices) {
            val filteredList = tempList.filter { if (it is Expr.Variable) it.name == variables[i] else false }
            summedUpList.add(Expr.Variable(filteredList.sumOf { (it as Expr.Variable).number }, variables[i]))
        }
        var i = 0
        while (i < tempList.size) {
            val element = tempList[i]
            if (element !is Expr.Variable)
                if (element is Expr.Addition && output.last() is Expr.Addition) {
                    i++
                    continue
                }
                else {
                    output.add(element)
                    tempList.removeAt(i--)
                }
            else {
                summedUpList.find { element.name == (it as Expr.Variable).name }?.let { output.add(it) }
                tempList = tempList.filter { if (it is Expr.Variable) element.name != it.name else true } as MutableList<Expr>
                i = 0
                continue
            }
            i++
        }
        return output
    }

    private fun makeDummiesGreatAgain(operand : Expr, variableStack: Stack<Expr>, numberStack: Stack<Expr>, output: MutableList<Expr>) {
        repeat(variableStack.size){
            val op1 = variableStack.pop()
            output.add(op1)
            output.add(Expr.Addition())
        }
        if (operand is Expr.Number)
            numberStack.push(operand)
        else if (operand is Expr.Dummy)
            numberStack.add(Expr.Dummy)
    }

    private fun executeOperationWithVariables(op1 : Expr, op2 : Expr, op : Expr, variableStack: Stack<Expr>? = null) : Expr {
        var operand1 : Expr = op1
        var operand2 : Expr = op2
        if (op2 is Expr.Dummy && variableStack != null)
            operand2 = variableStack.pop()
        if(op1 is Expr.Dummy && variableStack != null)
            operand1 = variableStack.pop()
        if (op1 is Expr.Number && op2 is Expr.Number) {
            return when (op) {
                is Expr.Addition -> Expr.Number(op1.number + op2.number)
                is Expr.Subtraction -> Expr.Number(op1.number - op2.number)
                is Expr.Multiplication -> Expr.Number(op1.number * op2.number)
                is Expr.Division -> Expr.Number(op1.number / op2.number)
                else -> throw Exception("Illegal operator: '$op'!")
            }
        }
        if(operand1 is Expr.Number && operand2 is Expr.Variable) {
            return when (op){
                is Expr.Multiplication -> Expr.Variable(operand1.number * operand2.number, operand2.name)
                is Expr.Division -> Expr.Variable(operand1.number / operand2.number, operand2.name)
                else -> throw Exception("Illegal operator: '$op'!")
            }
        }
        else if(operand1 is Expr.Variable && operand2 is Expr.Number){
            return when (op){
                is Expr.Multiplication -> Expr.Variable(operand1.number * operand2.number, operand1.name)
                is Expr.Division -> Expr.Variable(operand1.number / operand2.number, operand1.name)
                else -> throw Exception("Illegal operator: '$op'!")
            }
        }
        else if(operand1 is Expr.Variable && operand2 is Expr.Variable && operand1.name == operand2.name){
            if (op is Expr.Division) {
                return Expr.Number(operand1.number / operand2.number)
            }
            else
                throw Exception("Illegal operator: '$op'!")
        }
        else throw Exception("This is not a linear equation!")
    }

    private fun evaluateFunction(list: List<Expr>): Expr.Number {
        val variableStack: Stack<Expr> = Stack()
        for (expr in list) {
            if (expr is Expr.Number || expr is Expr.Variable) {
                variableStack.push(expr)
            } else {
                val op2 = variableStack.pop()
                val op1 = variableStack.pop()
                if (op1 is Expr.Variable || op2 is Expr.Variable)
                    variableStack.push(executeOperationWithVariables(op1,op2,expr,variableStack))
                else
                    variableStack.push(executeOperationWithVariables(op1,op2,expr))
            }
        }
        return variableStack.pop() as Expr.Number
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
        token is Token.Literals.NUMBER_LIT && tokens.peek() is Token.Literals.VARIABLE_LIT -> Expr.Variable(
            token.n, (tokens.next() as Token.Literals.VARIABLE_LIT).c
        )
        token is Token.Literals.VARIABLE_LIT && tokens.peek() is Token.Literals.NUMBER_LIT -> Expr.Variable(
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

    private fun iterateTokensTillRightParenthesis(): MutableList<Expr> {
        val body: MutableList<Expr> = mutableListOf()
        while (tokens.peek() != Token.Symbols.RPAREN) {
            val expr: Expr? = parseExpr()
            if (expr != null) body.add(expr) else break
        }
        return body
    }

    private fun parseVariables(t: Token.Literals.VARIABLE_LIT): Expr = Expr.Variable(1.0, t.c)

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