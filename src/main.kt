fun main(){

    val input : String = "6x + 3 = 9"
    val lexer : Lexer = Lexer(input)
    val parser : Parser = Parser()
    val evaluator : Evaluator = Evaluator()
    val EquationSolver : EquationSolver = EquationSolver()
}