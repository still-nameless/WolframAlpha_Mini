sealed class Expr {

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    data class Number (val number : Double) : Expr()
    data class Variable (val number: Double, val name: Char) : Expr()
    data class Function (val binder : String, val exprs: MutableList<Expr>) : Expr()
    data class Bracketed (val exprs: MutableList<Expr>) : Expr()
    data class Equation (val exprs: MutableList<Expr>) : Expr()
    open class Operator(val precedence: Int) : Expr() {
    }

    class Addition() : Operator(1)
    class Subtraction () : Operator(1)
    class Multiplication () : Operator(2)
    class Division () : Operator(2)

    object Dummy : Expr()
    object Equals : Expr()
    object Splitter : Expr()
}
