sealed class Token {

    // Mathematical Expressions
    object COS : Token()
    object SIN : Token()
    object TAN : Token()
    object LOG : Token()
    object SQRT : Token()

    // Symbols
    object LPAREN : Token()
    object RPAREN : Token()
    object DECIMAL_POINT : Token()

    // Operators
    object ADDITION : Token()
    object SUBTRACTION : Token()
    object MULTIPLICATION : Token()
    object DIVISION : Token()

    // Literals
    data class CHARACTER_LIT(val c : Char) : Token()
    data class NUMBER_LIT(val n : Int) : Token()

    // Control Token
    object EOF : Token()
    object SPLITTER : Token()
}
