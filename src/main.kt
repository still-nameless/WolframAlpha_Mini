fun main(){
    // 7x - 5 + 3
    // -7x
    val input : String = "-(7x + 2 * 5 + 2x)" //"3*(7x + 3y)"   "2 + 3 - 7 * 2"
    val input2 : String = "sin(2+7*4-1)"
    testParser3(input)
    //testGaussianAlgorithm()
}

/**
 *      (2x + 7
 *
 */



/** evaluate boundedVariables
 *      1. Fall -> BoundedVariable + Number
 *      2. Fall -> BoundedVariable - Number
 *      3. Fall -> BoundedVariable * Number
 *      4. Fall -> BoundedVariable / Number
 *      5. Fall -> BoundedVariable + BoundedVariable
 *      6. Fall -> BoundedVariable - BoundedVariable
 *      7. Fall -> BoundedVariable + BoundedVariable (same binder)
 *      8. Fall -> BoundedVariable - BoundedVariable (same binder)
 *      9. Fall -> BoundedVariable * BoundedVariable (same binder)
 *     10. Fall -> BoundedVariable / BoundedVariable (same binder)
 */

fun testParser(input : String){
    println("Parsing: $input")
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    while (true){
        val x : Expr? = parser.parseExpr()
        if (x != null)
            println(x)
        else
            break
    }
    println("fertig")
}

fun testParser2(input : String){
    println("Parsing: $input")
    val res = mutableListOf<Expr>()
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    while (true){
        val x : Expr? = parser.parseExpr()
        if (x != null)
            res.add(x)
        else
            break
    }
    res.forEach{ println(it) }
    println("fertig")
}

fun testParser3(input : String){
    println("Parsing: $input")
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    while (true){
        val x : Expr = parser.parseExpr() ?: break
    }
    parser.equation.expressions.forEach { println(it) }
}

fun testGaussianAlgorithm(){
    val matrix : Array<Array<Double>> = arrayOf(
        arrayOf(-1.0,1.0),
        arrayOf(-4.0,1.0)
    )
    val solutions : Array<Double> = arrayOf(2.0,-4.0)
    val equationSolver = EquationSolver()
    val solutionVector : Array<Double> = equationSolver.performGaussianElimination(matrix, solutions)

    solutionVector.forEach { println(it) }
}
