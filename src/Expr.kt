sealed class Expr {
    data class Variable (val name: Char): Expr()
    data class Number (val number : Double) : Expr()
    data class BindedVariables (val number: Double, val name: Char) : Expr()
    data class Function (val binder : String, val exprs: List<Expr>) : Expr()
    data class Bracketed (val exprs: List<Expr>) : Expr()
    data class Equation (val expressions: List<Expr>) : Expr()
}

enum class Operator{
    Addition, Subtraction, Multiplication, Division
}
