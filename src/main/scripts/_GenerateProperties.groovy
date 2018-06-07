/*******************************************************************************
Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsInit")

import grails.util.BuildSettings
import groovy.io.FileType
import java.math.MathContext
import java.math.RoundingMode
import org.apache.commons.lang.StringUtils

/**
 * This script will take a properties bundle and generate a 'dummy' bundle with markup that will
 * allow us identify a property value as being localized or more importantly, something that is not localized.
 */

def getNewFileName(file, locale) {
    def absolutePathOfFile = file.absolutePath
    def newFileName = absolutePathOfFile.replace(".properties", "_" + locale + ".properties")
    return newFileName
}

def writeToFile(fileName, generatedProps) {
    def destFile = new File( fileName )
    destFile.delete()

    generatedProps.each { line ->
        destFile.append(line)
        destFile.append("\n")
    }
}

def shouldFormatKey(key) {
    def shouldFormat = true;

    if (key.indexOf(".format") >= 0
            || "default.language.direction".equals(key)) {
        shouldFormat = false
    }
    return shouldFormat
}


def generateNewProp( line, params ) {
    def isProperty = {
        return it.contains( "=" )
    }

    if (isProperty( line ) ) {

        def key = StringUtils.substringBefore( line, "=" )
        def value = StringUtils.substringAfter( line, "=" )

        if (key == "default.language.locale") {
            return "default.language.locale=" + params.targetLocale
        }
        else if (shouldFormatKey( key )) {

            def calculateBufferSize = { word ->
                def factor = 0.0

                switch (word.size()) {
                    case 1..5:
                        factor = 1
                        break
                    case 6..12:
                        factor = 0.8
                        break
                    case 13..20:
                        factor = 0.6
                        break
                    case 21..30:
                        factor = 0.4
                        break
                    case 31..50:
                        factor = 0.2
                        break
                    default:
                        factor = 0.1
                }

               return word.size() * factor
            }

            def numberOfCharactersToAdd = 0.0

            def words = value.tokenize( " " )
            words.each {
                numberOfCharactersToAdd += calculateBufferSize( it )
            }

            def buffer = { num, character ->

                // We will always round up no matter the first precision.
                def mc = new MathContext( 1, RoundingMode.UP )
                def rounded = num.round( mc )

                def output = ""

                rounded.times {
                    output += "@"
                }

                return output
            }

            def bufferCharacter = buffer( (numberOfCharactersToAdd / 2), "@" )

            return "$key=$bufferCharacter$value$bufferCharacter"
        }
        else {
            return line
        }
    }
    else {
        // Return the line as is and do nothing.  We are only generating properties.
        return line
    }
}

/**
 * This will generate the properties files.  Eventually it'll allow you to pass in an arguments map.
 * @param args
 * @return
 */
def generatePropertiesFile( args = null ) {

    def params = getParamsMap( args )


    def dir = BuildSettings.BASE_DIR
    def generatedProps = []
    dir.traverse(type: FileType.FILES, nameFilter: params.filter) { source ->
        println "Generating props for " + source.canonicalFile
        source.eachLine { line ->
            generatedProps << generateNewProp(line, params)
        }

        def newFile = getNewFileName(source, params.targetLocale)

        println "Writing to file '$newFile'"
        def destFile = new File(newFile)

        // Delete the existing file if it is there
        destFile.delete()

        generatedProps.each { line ->
            destFile.append(line)
            destFile.append("\n")
        }
    }
}

def getParamsMap( args = null ) {

    def defaultMap = [
            dummyCharacter: "@",
            filter: "messages.properties",
            targetLocale: "en_PS" // English Pseduo
    ]

    // TODO:  Allow us to override the params map from the command line.

    return defaultMap
}


target(generateProperties: "Transform properties files to required format") {
    Number startTime = System.currentTimeMillis()
    println "Start Generation"
    generatePropertiesFile( args )
    println "Generation Complete in " + (System.currentTimeMillis() - startTime) + "ms"
}