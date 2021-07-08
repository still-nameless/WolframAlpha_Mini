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

    val input = "a - 5b + 4c + d= 7, 2a + b -3c -d = 1, 3a - 4b + c - 5d = 0, a + b + c + d = 1"
    val input2 = "a = sin( 4 * (3+9 / sqrt(4)) - 3*9 + cos(24/2) + 23 * tan(17) + PI*5)"
    val evaluator = Evaluator()
    evaluator.evaluate(input)
    val n = evaluator.equations.size
    val EQS = EquationSolver()
    val matrix = Array(n) { DoubleArray(n) }
    val solutions = DoubleArray(n)

    for (i in 0 until n){
        var index = 0
        for (j in 0 until evaluator.equations[i].first.exprs.size){
            val element = evaluator.equations[i].first.exprs[j]
            if (element is Expr.Variable)
                matrix[i][index++] = element.number
            else
                continue
        }
        solutions[i] = (evaluator.equations[i].second.exprs[0] as Expr.Number).number
    }
    val solutionVector = EQS.performGaussianElimination(matrix,solutions)

    solutionVector.forEach { println(String.format("%.3f",it)) }
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