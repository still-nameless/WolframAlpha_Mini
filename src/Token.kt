
sealed class Token {

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    // Constants
    object Constants : Token() {
        object PI : Token()
    }

    // Mathematical Functions
    object Functions : Token() {
        object COS : Token()
        object SIN : Token()
        object TAN : Token()
        object LOG : Token()
        object SQRT : Token()
        object EXP : Token()
    }

    // Symbols
    object Symbols : Token() {
        object RPAREN : Token()
        object LPAREN : Token()
        object EQUALS : Token()
    }

    // Operators
    object Operators : Token() {
        object ADDITION : Token()
        object SUBTRACTION : Token()
        object MULTIPLICATION : Token()
        object DIVISION : Token()
    }

    // Literals
    object Literals : Token() {
        data class VARIABLE_LIT(val c : Char) : Token()
        data class NUMBER_LIT(val n : Double) : Token()
    }

    // Control Token
    object ControlTokens : Token() {
        object EOF : Token()
        object SPLITTER : Token()
    }
}