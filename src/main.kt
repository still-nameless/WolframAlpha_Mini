fun main(){
    val input : String = "sin(2x)"
    testParser(input)
    //testGaussianAlgorithm()
}

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
