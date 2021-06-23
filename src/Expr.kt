sealed class Expr {
    data class Variable (val number: Double = 1.0, val name: String): Expr()
    data class Number (val number : Double) : Expr()
    data class Function (val binder : String, val expr: Expr)
    data class Equation (val expressions: List<Expr>) : Expr()
}
