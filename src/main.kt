// Binded_Variablen mit multiplikativen numbern zusammenfügen (Evtl. Evaluation)
// Klammersetzung
// Mehrere Gleichungen erkennen
// LGS lösen
// fertig


fun main(){
    val input : String = "7x + 5 = 3" // <=> 7x = -2
    val list = Lexer(input).equation
    list.forEach{println(it)}
}
