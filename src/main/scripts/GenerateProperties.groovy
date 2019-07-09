/*******************************************************************************
 Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import groovy.io.FileType
import org.apache.commons.lang.StringUtils

import java.math.MathContext
import java.math.RoundingMode

/**
 * This script will take a properties bundle and generate a 'dummy' bundle.
 * This dummy bundle will allow us identify a property value as being localized
 * or more importantly, something that is not localized.
 **/
description "Generate a psuedo properties file for debugging i18n issues", "grails generate-properties filter=messages.properties targetLocale=en_PS"


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

    if (key.indexOf(".format") >= 0 ||
	    key.indexOf(".dateFormat") >= 0 ||
	    "default.language.direction".equals(key)) {
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

            def calculateBufferSize = { valueString ->
                def factor = 0.0

                switch (valueString.size()) {
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

               return valueString.size() * factor
            }

			def numberOfCharactersToAdd = calculateBufferSize(value)

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
    def generatedProps
	String filename

    def dirPath = System.getProperty('user.dir')

    new File(dirPath).traverse(type: FileType.FILES, nameFilter: params.filter) { source ->
		filename = source.canonicalFile
		if ( StringUtils.contains(filename,"i18n_core.git") ) {
		    generatedProps = []
            source.eachLine { line ->
                generatedProps << line
            }
		} else {
	        generatedProps = []
            source.eachLine { line ->
                generatedProps << generateNewProp(line, params)
            }
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


def getParamsMap( argMap = [:] ) {

	def paramMap = [:]

    def defaultMap = [
            dummyCharacter: "@",
            filter: "messages.properties",
            targetLocale: "en_PS" // English Pseduo
    ]

	paramMap.putAll( defaultMap )
	paramMap.putAll( argMap )

    return paramMap
}

//'Generates a psuedo properties file for debugging i18n issues.
//
//Examples:
//grails generate-properties filter=<source file> targetLocale=<locale>'
//filter: A properties file to dummy translate.
//targetLocale: A dummy locale determining the name of the output file.
//
//E.g.: grails generate-properties filter=messages.properties targetLocale=en_PS
//
//'''

def mainMethod() {
    Number startTime = System.currentTimeMillis()
    println "Start Generation"

    // Turn args into a map
    def argMap = [:]

    args?.each {
        def key = StringUtils.substringBefore( it, "=" )
        def value = StringUtils.substringAfter( it, "=" )
        argMap[key] = value
    }

    generatePropertiesFile( argMap )

    println "Generation Complete in " + (System.currentTimeMillis() - startTime) + "ms"
}

mainMethod()