
fun main(){
    val testCases : MutableList<String> = mutableListOf(
    "a - 7 = b + sin(PI) * c,exp(log(sqrt(4)))*a + b - 3c = 2,3 + a + 5b = -c * sin(200)"
    ,"a = sin( 4 * (3+9 / sqrt(4)) - 3*9 + cos(24/2) + 23 * tan(17) + PI*5)"
    ,"5x - y - z - 1 = 4x + 3y -z,3x - z - 1 = 5x + 2y + 5z + 1,2z - 2*(y-3x) - 5 = -5x - 3"
    ,"tan(sin(PI)/cos(2*PI)) + PI * a = 1"
    ,"(((1)*(2 + 2) + 3) / (cos(PI) * 5 * (4)))*a = 1"
    ,"2*v + 2*w + 4*x + 5*y + 6*z = 7,2*v + 2*w + 3*x + 3*y + 2*z = 12,1*v + 2*w + 1*x + 5*y + 8*z = -5,2*v + 4*w + 1*x + 5*y + 8*z = -5,2*v + 2*w + 1*x + 5*y + 8*z = -5")

    testCases.forEach { EquationSolver().solveEquation(it) }
}