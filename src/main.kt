fun main(){
    val input : String = ".453"
    testParser(input)
}

fun testParser(input : String){
    println("Parsing: $input")
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    println(parser.parseExpr())
}

// (7x + 3) + 5x
