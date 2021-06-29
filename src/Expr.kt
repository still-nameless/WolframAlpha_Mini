sealed class Expr {

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    data class Number (val number : Double) : Expr()
    data class Variable (val number: Double, val name: Char) : Expr()
    data class Function (val binder : String, val exprs: MutableList<Expr>) : Expr()
    data class Bracketed (val exprs: MutableList<Expr>) : Expr()
    data class Equation (val exprs: MutableList<Expr>) : Expr()
    data class PartialEquation (val exprs : MutableList<Expr>) : Expr()
    open class Operators(val precedence: Int) : Expr() {
    }

    class Addition() : Operators(1)
    class Subtraction () : Operators(1)
    class Multiplication () : Operators(2)
    class Division () : Operators(2)

    class Dummy : Expr()
}
