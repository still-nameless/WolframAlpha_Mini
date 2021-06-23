import java.lang.Exception
import kotlin.math.abs

class EquationSolver() {
    private val epsilon : Double = 1e-15

    fun GaussianElimination(matrix : Array<Array<Double>>, solutions : Array<Double>) : Array<Double> {
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

        val solutionVector : Array<Double>  = Array(n){0.0}
        for (i : Int in n - 1 downTo 0){
            var sum : Double = 0.0
            for (j : Int in i + 1 until n){
                sum += matrix[i][j] * solutionVector[j]
            }
            solutionVector[i] = (solutions[i] - sum) / matrix[i][i]
        }
        return solutionVector
    }

    private fun swap2D(matrix : Array<Array<Double>>, i : Int , j : Int){
        val temp : Array<Double> = matrix[i]
        matrix[i] = matrix[j]
        matrix[j] = temp
    }

    private fun swap1D(matrix: Array<Double>, i : Int, j : Int){
        val temp : Double = matrix[i]
        matrix[i] = matrix[j]
        matrix[j] = temp
    }
}