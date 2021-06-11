fun main(){

    val input : String = "6x + 3 = 9"
    val lexer : Lexer = Lexer(input)
    test("7x + 2y + sin(3245) + sqrt(5) = 5, 2x + y = 65")
}

fun test(input: String) {
    println("Lexing: $input")
    val lexer = Lexer(input)
    while (lexer.peek() != Token.EOF) {
        println(lexer.next())
    }
    println(lexer.next())
}