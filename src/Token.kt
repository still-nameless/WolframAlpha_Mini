
sealed class Token {

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    // Mathematical Expressions
    object COS : Token()
    object SIN : Token()
    object TAN : Token()
    object LOG : Token()
    object SQRT : Token()

    // Symbols
    object LPAREN : Token()
    object RPAREN : Token()
    object EQUALS : Token()

    // Operators
    object ADDITION : Token()
    object SUBTRACTION : Token()
    object MULTIPLICATION : Token()
    object DIVISION : Token()

    // Literals
    data class VARIABLE_LIT(val c : Char) : Token()
    data class NUMBER_LIT(val n : Double) : Token()
    data class BINDED_VAR_LIT(val n : Double, val c: Char) : Token()

    // Control Token
    object EOF : Token()
    object SPLITTER : Token()
}