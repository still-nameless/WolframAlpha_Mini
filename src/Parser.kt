class Parser(val tokens : Lexer) {


    /*
        is Token.Literals.NUMBER_LIT -> {
                        equation.add(Token.Literals.BINDED_VAR_LIT((pElement() as Token.Literals.NUMBER_LIT).n,
                            (equation.last() as Token.Literals.VARIABLE_LIT).c))
                        repeat(2){
                            equation.removeAt(equation.lastIndex-1)
                        }
                    }
        is Token.Literals.NUMBER_LIT -> {
                when (pElement()) {
                    is Token.Literals.VARIABLE_LIT -> {
                        equation.add(Token.Literals.BINDED_VAR_LIT((equation.last() as Token.Literals.NUMBER_LIT).n,
                            (pElement() as Token.Literals.VARIABLE_LIT).c))
                        repeat(2) {
                            equation.removeAt(equation.lastIndex - 1)
                        }
                    }
                    is Token.Literals.BINDED_VAR_LIT -> {
                        equation.add(Token.Literals.BINDED_VAR_LIT((pElement() as Token.Literals.BINDED_VAR_LIT).n * (equation.last() as Token.Literals.NUMBER_LIT).n,
                            (pElement() as Token.Literals.BINDED_VAR_LIT).c))
                        repeat(2) {
                            equation.removeAt(equation.lastIndex - 1)
                        }
                    }
                }
            }
     */
}