import java.lang.Exception
import kotlin.math.abs

class EquationSolver() {

    private val epsilon : Double = 1e-15

    fun solveEquation(input : String){
        val evaluator = Evaluator()
        evaluator.evaluate(input)

        val n = evaluator.equations.size
        val matrix = Array(n) { DoubleArray(n) }
        val solutions = DoubleArray(n)
        val variableList = getAllVariables(evaluator.equations).distinct()
        if (variableList.size > evaluator.equations.size) throw Exception("Can not solve linear system of equations with " +
                "${variableList.size} variables and only ${evaluator.equations.size} equations!")

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
        printSolution(performGaussianElimination(matrix,solutions),evaluator.equations)
    }

    private fun printSolution(solutionVector : DoubleArray, equations : MutableList<Pair<Expr.Equation,Expr.Equation>>) {
        val variableList = getAllVariables(equations).distinct()
        println("Lösungen für das Gleichungssystem lauten:")
        for (i in variableList.indices){
            println("${variableList[i]}: ${String.format("%.2f",solutionVector[i])}")
        }
    }

    private fun performGaussianElimination(matrix : Array<DoubleArray>, solutions : DoubleArray) : DoubleArray {
        val n : Int = solutions.size

        for (i : Int in 0 until n){
            var max : Int = i
            for (j : Int in i + 1 until n){
                if (abs(matrix[j][i]) > abs(matrix[max][i])){
                    max = j
                }
            }
            swap2D(matrix,i,max)
            swap1D(solutions,i,max)

            if (abs(matrix[i][i]) <= epsilon){
                throw Exception("Matrix is singular!")
            }

            for (j : Int in i + 1 until n){
                val current : Double = matrix[j][i] / matrix[i][i]
                solutions[j] -= current * solutions[i]
                for (k : Int  in i until n){
                    matrix[j][k] -= current * matrix[i][k]
                }
            }
        }

        val solutionVector = DoubleArray(n)
        for (i : Int in n - 1 downTo 0){
            var sum = 0.0
            for (j : Int in i + 1 until n){
                sum += matrix[i][j] * solutionVector[j]
            }
            solutionVector[i] = (solutions[i] - sum) / matrix[i][i]
        }
        return solutionVector
    }

    private fun swap2D(matrix : Array<DoubleArray>, i : Int , j : Int){
        val temp : DoubleArray = matrix[i]
        matrix[i] = matrix[j]
        matrix[j] = temp
    }

    private fun swap1D(matrix: DoubleArray, i : Int, j : Int){
        val temp : Double = matrix[i]
        matrix[i] = matrix[j]
        matrix[j] = temp
    }

    private fun getAllVariables(equations: MutableList<Pair<Expr.Equation, Expr.Equation>>) : MutableList<Char>{
        val variableList : MutableList<Char> = mutableListOf()
        equations.forEach{ it ->
            it.first.exprs.filterIsInstance<Expr.Variable>().forEach { variableList.add(it.name) }
        }
        return variableList
    }
}