fun main(){
    val input : String = "(4 * 6x + 4y * 2 * 5)"
    val input2 : String = "(-1 + 17 + 34x + 2z +4  + 54 + 8z)"
    //testRemoveMinus()
    testParser(input)
    //testGaussianAlgorithm()

}

/**
 *
 *          2x 8 + 3z -
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

    println("fertig")
}

fun stringToList() : MutableList<Expr> {
    val input = "-(-3+6-3x)"
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val res = mutableListOf<Expr>()
    while (true){
        val x : Expr? = parser.parseExpr()
        if (x != null)
            res.add(x)
        else
            break
    }
    return res
}

fun testRemoveMinus() {
    val list : MutableList<Expr> = mutableListOf(Expr.Subtraction(), Expr.Bracketed(mutableListOf(Expr.Subtraction(), Expr.Number(3.0), Expr.Addition(), Expr.Number(6.0), Expr.Subtraction(), Expr.Variable(2.0,'x'))))
    val lexer = Lexer("")
    val parser = Parser(lexer)
    parser.removeMinus(list).forEach { println(it) }
}

//      - ( - 3 + 6

//      Expr.Number()
//      Expr.Variable()
//      Expr.Addition()
//      Expr.Subtraction()
//      Expr.Multiplication()
//      Expr.Division()

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