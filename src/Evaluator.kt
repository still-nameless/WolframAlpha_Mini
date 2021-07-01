import java.util.*
import kotlin.math.*

class Evaluator() {

    val equations : MutableList<Pair<Expr.Equation,Expr.Equation>> = mutableListOf()

    fun setUpEvaluation(input: String) {
        val lexer = Lexer(input)
        val parser = Parser(lexer)
        var isLeftHandside = true
        val leftHandsideEquation : MutableList<Expr> = mutableListOf()
        val rightHandsideEquation : MutableList<Expr> = mutableListOf()

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
                leftHandsideEquation.clear()
                rightHandsideEquation.clear()
                isLeftHandside = true
            }
            else {
                equations.add(Pair(Expr.Equation(leftHandsideEquation),Expr.Equation(rightHandsideEquation)))
                break
            }
        }
        evaluate()
    }


}