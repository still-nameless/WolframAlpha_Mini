fun main(){
    val input : String = ".453"
    testParser(input)

    testGaussianAlgorithm()
}

fun testParser(input : String){
    println("Parsing: $input")
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    println(parser.parseExpr())
}

fun testGaussianAlgorithm(){
    val matrix : Array<Array<Double>> = arrayOf(
        arrayOf(-1.0,1.0),
        arrayOf(-4.0,1.0)
    )
    val solutions : Array<Double> = arrayOf(2.0,-4.0)
    val solutionVector : Array<Double> = EquationSolver().GaussianElimination(matrix, solutions)

    solutionVector.forEach { println(it) }
}
