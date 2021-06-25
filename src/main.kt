fun main(){
    val input : String = "3*(7x + 3y)"
    testParser(input)
    //testGaussianAlgorithm()
}

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
