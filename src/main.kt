fun main(){
    val input : String = "+/34"
    val list = Lexer(input).equation
    list.forEach{println(it)}
}
