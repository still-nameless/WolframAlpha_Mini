fun main(){
    /*
    val x = mutableListOf<String>(
    "28x + 2y + 4 - 3 + sin(2 + 4) + (7y - 3x * 2) = 3x - 3x + 5x",
    "(-1 + 17 + 34x + 2z +4  + 54 + 8z)",
    "(-1x + 2y + 3z + 4a + 2)",
    "(7x + (2x) + (3x + 5x))",
    "((1)*(2)*(3) + 3y*(3 + 2))",
    "(((1)*(2x + 2) + 3x) / (cos(PI) * 5 * (4)))",
    "((2x + 2)/(2 * 1))",
    "(2 / (2x + 1))")
    for (i in x){
        testParser(i)
    }
     */

    // Klammern werden einfach aufgelöst => 2x + 1 = 3x, einfach falsch
    //val input = "(2x + 2)/(2 * 1) = 3x" Hier müssen nochmal Partial Equations benutzt werden

    //val input = "7 * (3*2-1) + 2x + 0*y = 5x + 65"
    val input = "x + y = 1, 2x + 4y = 4"
    val evaluator = Evaluator()
    evaluator.setUpEvaluation(input)
    val n = evaluator.equations.size
    val EQS = EquationSolver()
    val matrix = Array(n) { DoubleArray(n) }
    val solutions = DoubleArray(n)

    for (i in 0 until n){
        for (j in 0 until evaluator.equations[i].first.exprs.size){
            val element = evaluator.equations[i].first.exprs[j]
            if (element is Expr.Variable)
                matrix[i][j] = element.number
            else
                continue
        }
        solutions[i] = (evaluator.equations[i].second.exprs[0] as Expr.Number).number
    }
    val solutionVector = EQS.performGaussianElimination(matrix,solutions)

    solutionVector.forEach { println(it) }
    //testParser(input)
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