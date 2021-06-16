fun main(){
    val input : String = "sin(2) + 7x * c8 - (2)b + e(7)= 4" // <=> 7x = -2
    val list = Lexer(input).equation
    val evalList = Evaluator(list)
    list.forEach{println(it)}
}
