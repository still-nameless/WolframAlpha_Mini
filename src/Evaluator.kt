import java.util.*
import javax.swing.text.MutableAttributeSet
import kotlin.math.*

class Evaluator() {

    val equations : MutableList<Pair<Expr.Equation,Expr.Equation>> = mutableListOf()

    private fun setUpEvaluation(input: String) {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        var isLeftHandside = true
        var leftHandsideEquation : MutableList<Expr> = mutableListOf()
        var rightHandsideEquation : MutableList<Expr> = mutableListOf()

        while (true) {
            val x : Expr? = parser.parseExpr()
            if(x != null && x !is Expr.Splitter) {
                if (x == Expr.Equals) isLeftHandside = false
                else if (isLeftHandside)
                    leftHandsideEquation.add(x)
                else
                    rightHandsideEquation.add(x)
            }
            else if (x is Expr.Splitter) {
                equations.add(Pair(Expr.Equation(leftHandsideEquation),Expr.Equation(rightHandsideEquation)))
                leftHandsideEquation = mutableListOf()
                rightHandsideEquation = mutableListOf()
                isLeftHandside = true
            }
            else {
                equations.add(Pair(Expr.Equation(leftHandsideEquation),Expr.Equation(rightHandsideEquation)))
                break
            }
        }
    }

    fun evaluate(input : String){
        setUpEvaluation(input)
        var newEquation : Pair<Expr.Equation,Expr.Equation>
        for (equation in equations){
            val evalLeftEquation = evaluate(removeMinus(equation.first.exprs))
            val evalRightEquation = evaluate(removeMinus(equation.second.exprs))
            newEquation = seperateVariables(evalLeftEquation,evalRightEquation)
            sortByVariableName(newEquation.first.exprs)
            equations[equations.indexOf(equation)] = newEquation
        }
    }

    private fun evaluate(input : MutableList<Expr>) : MutableList<Expr>{
        val result : MutableList<Expr> = mutableListOf()
        if(input.size == 1 && input.first() is Expr.Number) return input
        for (expr in input) {
            when (expr) {
                is Expr.Bracketed -> {
                    val body = evaluate(removeMinus(expr.exprs))
                    result.add(checkBracketed(body))
                }
                is Expr.Function -> {
                    val body = evaluate(removeMinus(expr.exprs))
                    body.forEach { result.add(applyFunction(expr, it as Expr.Number)) }
                }
                else -> result.add(expr)
            }
        }
        return evaluateEquation(toPostfixNotation(result))
    }

    private fun checkBracketed(input : MutableList<Expr>) : Expr {
        if (input.size == 1) return input[0]
        return Expr.Bracketed(input)
    }

    private fun seperateVariables(leftList : MutableList<Expr>, rightList : MutableList<Expr>) : Pair<Expr.Equation,Expr.Equation>{
        var newPair : Pair<MutableList<Expr>,MutableList<Expr>> = Pair(leftList, rightList)
        var tempList : MutableList<Expr> = leftList
        var index = 0
        while (index < tempList.size){
            when (tempList[index]){
                is Expr.Number -> {
                    newPair =  bringFromLeftToRight(leftList,rightList,tempList[index])
                    tempList = newPair.first
                    index = 0
                }
                else -> index++
            }
        }
        index = 0
        while (index < newPair.second.size){
            when (newPair.second[index]){
                is Expr.Variable -> {
                    newPair = bringFromRightToLeft(newPair.first,newPair.second,newPair.second[index])
                    index = 0
                }
                else -> index++
            }
        }
        return Pair(Expr.Equation(newPair.first),Expr.Equation(newPair.second))
    }

    private fun bringFromLeftToRight(fromList : MutableList<Expr>, toList : MutableList<Expr>, expr : Expr) : Pair<MutableList<Expr>,MutableList<Expr>> {
        var newLeft : MutableList<Expr> = mutableListOf()
        var newRight : MutableList<Expr> = mutableListOf()
        if (expr is Expr.Number){
            fromList.add(Expr.Addition())
            fromList.add(Expr.Number(-expr.number))
            newLeft = evaluateEquation(toPostfixNotation(fromList))
            if (toList.isNotEmpty()){
                toList.add(Expr.Addition())
                toList.add(Expr.Number(-expr.number))
                newRight = evaluateEquation(toPostfixNotation(toList))
            }
        }
        return Pair(filterZeroCoefficients(newLeft),filterZeroCoefficients(newRight))
    }

    private fun bringFromRightToLeft(fromList : MutableList<Expr>, toList : MutableList<Expr>, expr : Expr) : Pair<MutableList<Expr>,MutableList<Expr>> {
        var newLeft : MutableList<Expr> = mutableListOf()
        var newRight : MutableList<Expr> = mutableListOf()
        if (expr is Expr.Variable){
            fromList.add(Expr.Addition())
            fromList.add(Expr.Variable(-expr.number,expr.name))
            newLeft = evaluateEquation(toPostfixNotation(fromList))
            if (toList.isNotEmpty()){
                toList.add(Expr.Addition())
                toList.add(Expr.Variable(-expr.number,expr.name))
                newRight = evaluateEquation(toPostfixNotation(toList))
            }
        }
        return Pair(filterZeroCoefficients(newLeft),filterZeroCoefficients(newRight))
    }

    private fun filterZeroCoefficients(input: MutableList<Expr>) : MutableList<Expr>{
        var index = 0
        while (index < input.size){
            when (val element = input[index]){
                is Expr.Number -> {
                    if (element.number == 0.0 && index != input.size-1) {
                        repeat(2){
                            input.removeAt(index)
                        }
                    }
                    else if (element.number == 0.0) {
                        repeat(2){
                            input.removeAt(input.size-1)
                        }
                    }
                    else
                        index++
                }
                is Expr.Variable -> {
                    if (element.number == 0.0 && index != input.size-1) {
                        repeat(2){
                            input.removeAt(index)
                        }
                    }
                    else if (element.number == 0.0){
                        repeat(2){
                            input.removeAt(input.size-1)
                        }
                    }
                    else index++
                }
                else -> index++
            }
        }
        return input
    }

    private fun sortByVariableName(input : MutableList<Expr>){
        for (i in 0 until input.size) {
            val element1 = input[i]
            if (element1 !is Expr.Variable) continue
            for (j in i + 1 until input.size) {
                val element2 = input[j]
                if (element2 !is Expr.Variable) continue
                if (element1.name > element2.name) {
                    val expr = input[i]
                    input[i] = input[j]
                    input[j] = expr
                }
            }
        }
    }

    private fun applyFunction(function: Expr.Function, expr: Expr.Number): Expr.Number {
        return when (function.binder.toUpperCase()) {
            "SIN"  -> Expr.Number(sin(expr.number))
            "COS"  -> Expr.Number(cos(expr.number))
            "TAN"  -> Expr.Number(tan(expr.number))
            "SQRT" -> Expr.Number(sqrt(expr.number))
            "LOG"  -> Expr.Number(ln(expr.number))
            "EXP"  -> Expr.Number(exp(expr.number))
            else -> throw Exception("Applying '$function' - function failed!")
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

    private fun toPostfixNotation(list : MutableList<Expr>) : MutableList<Expr> {
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
            else if (expr is Expr.Bracketed){
                modifiedInput = multiplyOutBracket(modifiedInput,i)
                output.clear()
                operatorStack.clear()
                i = 0
                continue
            }
            else if (expr is Expr.Addition || expr is Expr.Subtraction || expr is Expr.Multiplication || expr is Expr.Division) {
                while (operatorStack.isNotEmpty() && comparePrecedenceOfOperators(expr,operatorStack.peek()) <= 0){
                    output.add(operatorStack.pop())
                }
                operatorStack.add(expr)
            }
            else if(expr is Expr.Function){
                toPostfixNotation(expr.exprs).forEach { output.add(it) }
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
        return index > 1 && (list[index-1] is Expr.Multiplication || list[index-1] is Expr.Division) && (list[index-2] is Expr.Number || list[index-2] is Expr.Variable || list[index-2] is Expr.Bracketed)
    }

    private fun isFactorBehindBrackets(list : MutableList<Expr>, index : Int) : Boolean {
        if (list.size < 3) return false
        return index < list.size - 2 && (list[index+1] is Expr.Multiplication || list[index+1] is Expr.Division) && (list[index+2] is Expr.Number || list[index+2] is Expr.Variable || list[index+2] is Expr.Bracketed)
    }

    private fun multiplyOutBracket(input : MutableList<Expr>, index : Int) : MutableList<Expr>{
        if (input.size <= 2) {
            return (input[0] as Expr.Bracketed).exprs
        }
        if (isFactorInFrontBrackets(input,index)){
            return createResultList(input,index,-2,-2,1,input[index -1])
        }
        else if (isFactorBehindBrackets(input,index)) {
            return createResultList(input, index, 2, 0, 3,input[index+1])
        }
        else{
            val result : MutableList<Expr> = mutableListOf()
            val expr : Expr = input[index]
            input.subList(0,index).forEach { result.add(it) }
            if (expr is Expr.Bracketed) {
                for (i in 0 until expr.exprs.size) {
                    result.add(expr.exprs[i])
                }
            }
            input.subList(index+1,input.size).forEach { result.add(it) }
            return result
        }
    }

    private fun createResultList(input : MutableList<Expr>, index: Int, offSet: Int, moveIndex1 : Int, moveIndex2 : Int, operator : Expr) : MutableList<Expr>{
        val result : MutableList<Expr> = mutableListOf()
        input.subList(0, index + moveIndex1).forEach { result.add(it) }
        result.add(Expr.Bracketed(applyFactor(input,index,offSet,operator)))
        input.subList(index + moveIndex2, input.size).forEach { result.add(it) }
        return result
    }

    private fun applyFactor(input : MutableList<Expr>, index : Int, offSet : Int, operator: Expr) : MutableList<Expr>{
        val output : MutableList<Expr> = mutableListOf()
        for (expr in (input[index] as Expr.Bracketed).exprs){
            if (expr is Expr.Variable || expr is Expr.Number){
                if (operator is Expr.Multiplication || operator is Expr.Division && offSet > 0)
                    output.add(executeOperationWithVariables(expr, input[index+offSet], operator))
                else
                    output.add(executeOperationWithVariables(input[index+offSet], expr, operator))
                output.add(Expr.Addition())
            }
        }
        output.removeLast()
        return output
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
                //is Expr.Division -> Expr.Variable(operand1.number / operand2.number, operand2.name)
                else -> throw Exception("ja das ist einfach keine lineare equation oder? - marc")
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

    private fun evaluateEquation(input : MutableList<Expr>) : MutableList<Expr>{
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
        if (output.last() is Expr.Addition)
            output.removeLast()
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
}