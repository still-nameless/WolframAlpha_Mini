fun main(){
    val input : String = "(28x + 2y + 4 - 3 + sin(2 + 4) + (7y - 3x * 2))"
    val input2 : String = "(-1 + 17 + 34x + 2z +4  + 54 + 8z)"
    val input3 : String = "(-1x + 2y + 3z + 4a + 2)"
    val input4 : String = "(-(3x + 3))" // Klammer wird nicht richtig aufgelöst
    val input5 : String = "sin(PI)"
    testParser(input4)
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