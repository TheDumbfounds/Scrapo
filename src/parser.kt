import com.sun.corba.se.impl.io.TypeMismatchException
import org.openqa.selenium.By

class Parser {

    val testingData: MutableList<String> = mutableListOf()

    fun parse(tokens: MutableList<Token>){

        val generator: Generator = Generator()

        fun addTestingValue(value: String){
            testingData.add(value)
        }

        fun userSpecifiedIndex(i: Int) : Boolean {
            return (tokens[i + 1].value + tokens[i + 3].value == "[]")
        }

        var i = 0
        while (i < tokens.size-1){

            fun handleFunctionCallWithIndex(keyword: String){
                var index: Int = 0
                if (userSpecifiedIndex(i)){
                    if (tokens[i+2].type == TokenType.INT){
                        index = tokens[i+2].value.toInt()
                        i += 4
                    } else {
                        throw IndexNotIntegerException("The index '${tokens[i+2].value}' is not an integer!")
                    }
                } else {
                    index = 0
                    i++
                    addTestingValue("CLICK")
                }

                if (tokens[i+1].type != TokenType.STRING){
                    throw TypeMismatchException("The Keyword '$keyword[index]' needs to be followed by a string")
                }

                val selector = if (tokens[i].value == "CLASS") By.className(tokens[i+1].value)
                    else By.id(tokens[i+1].value)
                when (keyword){
                    "CLICK" -> {
                        generator.clickOn(selector, generator.driver!!, index)
                        addTestingValue("CLICK[$index]-${tokens[i+1].value}")
                    }
                    "TYPE" -> {
                        generator.typeInto(selector, generator.driver!!, index)
                        addTestingValue("TYPE[$index]-${tokens[i+1].value}")
                    }
                }

            }

            when (tokens[i].type){
                TokenType.WORD -> {
                    when (tokens[i].value){
                        "OPEN" -> { // SYNTAX: OPEN "URL"
                            addTestingValue("OPEN-${tokens[i+1].value}")
                            generator.openWebsite(generator.driver!!, tokens[i+1].value)
                            i++
                        }
                        "CLOSE" -> { // SYNTAX: CLOSE
                            addTestingValue("CLOSE")
                            generator.closeWebbrowser(generator.driver!!)
                        }
                        "CLICK" -> { // SYNTAX: CLICK[INDEX] || CLICK
                            handleFunctionCallWithIndex(tokens[i].value)
                            i++
                        }
                        "TYPE" -> {
                            handleFunctionCallWithIndex(tokens[i].value)
                        }
                        "WAIT" -> {
                            i++
                            addTestingValue("WAIT-${tokens[i].value}")
                            val value = tokens[i].value
                            try {
                                val time: Long = value.toLong()
                                generator.waitTime(time)
                            } catch (ex: Exception){
                                throw TypeMismatchException("The value '$value' specified along 'WAIT'" +
                                        " is not a LongType.")
                            }
                        }
                        else -> {
                            if (!isConstant(tokens[i].value)){
                                addTestingValue("KEYWORD-NOT-FOUND")
                                throw KeywordNotFoundException("'${tokens[i].value}' could not be found!")
                            }
                        }
                    }
                }
                TokenType.OPERATOR -> {
                    // TODO: Organize to words
                    if (tokens[i-1].value == "BROWSER"){
                        addTestingValue("SET-BROWSER-${tokens[i+1].value}")
                        generator.setWebdriver(tokens[i+1].value)
                    }
                }
                TokenType.STRING -> {

                }
                TokenType.GROUPING_SYMBOL -> {

                }
                TokenType.INT -> {

                }
            }
            i++
        }


    }

    fun isConstant(word: String) : Boolean {
        // TODO: Modularize to check for other constants
        if (word == "BROWSER"){
            return true
        }
        return false
    }


}
