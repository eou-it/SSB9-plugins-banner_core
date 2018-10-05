description "Create Dummy Arabic Keys", "grails dummy-arabic"

def generateFakeProperty(def fromThis, def arabicCharacterList) {
    String arabicString = ""
    Integer wordNumber = 0
    def random = new Random()
    def workingWordList = fromThis.split(" ")
    Boolean openBracket = false
    workingWordList.each { it ->
        if (it.contains("{")) {
            arabicString += "" + wordNumber + it + " "
        }
        else {
            String workingWord = wordNumber
            def charCount = getCharCountFor(it)
            for (int i=0;i<charCount;i++)
            {
                workingWord += arabicCharacterList[random.nextInt(10)]
            }
            arabicString += workingWord + " "
        }
        wordNumber++
    }
    if (arabicString.endsWith(" ")) {
        arabicString = arabicString.substring(0, arabicString.length() - 1)
    }
    return arabicString
}


Integer getCharCountFor(def thisString) {
    Integer origSize = thisString.size()
    if (origSize < 6) {
        return origSize * 2
    }
    else if (origSize < 13) {
        return origSize * 1.8
    }
    else if (origSize < 21) {
        return origSize * 1.6
    }
    else if (origSize < 31) {
        return origSize * 1.4
    }
    else if (origSize < 50) {
        return origSize * 1.2
    }
    else {
        return origSize * 1.1
    }
}


def toUnicode(def thisString) {
    def returnString = ""
    thisString.each{ c ->
        returnString += String.format ("\\u%04x", (int)c)
    }
    return returnString
}


Boolean ignore(thisProperty, ignoreList) {
    def ignoreThis = false
    ignoreList.each{ it ->
        if (thisProperty.toLowerCase().startsWith(it.toLowerCase())) {
            ignoreThis = true
        }
    }
    return ignoreThis
}

def processPlugin(plugin, mainArabicProperties, arabicCharacterList, ignoreList) {

    // Load up the main arabic properties file from the grails app

    def arabicProperties = new Properties()
    def needSaudiFile = false

    // Load in the main properties file
    def messagesProperties = new Properties()
    File messageFile = new File(plugin, "grails-app/i18n/messages.properties")
    if (messageFile.exists()) {
        needSaudiFile = true
        messageFile.withInputStream { stream ->
            messagesProperties.load(stream)
        }
    }

    messagesProperties.propertyNames().each { propertyName ->
        if (!ignore(propertyName, ignoreList) && !arabicProperties.getProperty(propertyName)) {
            arabicProperties.setProperty(propertyName,
                    generateFakeProperty(messagesProperties.getProperty(propertyName), arabicCharacterList))
        }
    }

    if (needSaudiFile) {
        println "Generating new grails-app/i18n/messages_ar_SA.properties for plugin: " + plugin

        File saudiArabicFile = new File(plugin, "grails-app/i18n/messages_ar_SA.properties")
        saudiArabicFile.write("#GENERATED FILE!!! DO NOT CHECK IN!!!\n")
        saudiArabicFile.write("default.language.direction=rtl\n")

        arabicProperties.propertyNames().each { propertyName ->
            String arabicString = propertyName + "=" + toUnicode(arabicProperties.getProperty(propertyName))
            saudiArabicFile.append(arabicString)
            saudiArabicFile.append("\n")
        }
    }



}

def mainMethod() {
    def ignoreList = ["default.name.format", "default.date.format", "default.birthdate.format", "default.dateEntry.format", "js.datepicker.dateFormat", "default.calendar", "default.language.direction"]
    def dummyArabicCharacters = ["\u0645", "\u0639", "\u0644", "\u062D", "\u062F", "\u0625", "\u0626", "\u0627", "\u0628", "\u0639"]
    def mainArabicProperties = new Properties()
    File messageFile11 = new File("grails-app/i18n/messages.properties")
    if (messageFile11.exists()) {
        messageFile11.withInputStream { stream ->
            mainArabicProperties.load(stream)
        }
    }

    mainArabicProperties.propertyNames().each { propertyName ->
        if (!ignore(propertyName, ignoreList) && !mainArabicProperties.getProperty(propertyName)) {
            mainArabicProperties.setProperty(propertyName, generateFakeProperty(mainArabicProperties.getProperty(propertyName), dummyArabicCharacters))
        }
    }

    // Find our list of plugins
    File plugins = new File("plugins")
    plugins.eachFile { pgn ->
        processPlugin(pgn, mainArabicProperties, dummyArabicCharacters, ignoreList)
    }

    println "Generating new grails-app/i18n/messages_ar_SA.properties"
    File saudiArabicFile = new File("grails-app/i18n/messages_ar_SA.properties")
    saudiArabicFile.write("#GENERATED FILE!!! DO NOt CHECK IN!!!\n")
    saudiArabicFile.write("default.language.direction=rtl\n")

    mainArabicProperties.propertyNames().each { propertyName ->
        def arabicString = propertyName + "=" + toUnicode(mainArabicProperties.getProperty(propertyName))
        saudiArabicFile.append(arabicString)
        saudiArabicFile.append("\n")
    }
}

mainMethod()