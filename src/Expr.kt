sealed class Expr {

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    data class Number (val number : Double) : Expr()
    data class BoundVariable (val number: Double, val name: Char) : Expr()
    data class Function (val binder : String, val exprs: List<Expr>) : Expr()
    data class Bracketed (val exprs: List<Expr>) : Expr()
    data class Equation (val expressions: MutableList<Expr>) : Expr()

    open class Operators(val precedence: Int) : Expr() {
    }

    class Addition() : Operators(1)
    class Subtraction () : Operators(1)
    class Multiplication () : Operators(2)
    class Division () : Operators(2)

    class Dummy : Expr()
}
