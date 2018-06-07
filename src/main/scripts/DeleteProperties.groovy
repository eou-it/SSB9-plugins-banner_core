/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsInit")

import grails.util.BuildSettings
import groovy.io.FileType
import java.math.MathContext
import java.math.RoundingMode
import org.apache.commons.lang.StringUtils

/**
 * This script will delete previously generated properties files used for debugging i18n issues.
 **/
def getNewFileName(file, locale) {
    def absolutePathOfFile = file.absolutePath
    def newFileName = absolutePathOfFile.replace(".properties", "_" + locale + ".properties")
    return newFileName
}


/**
 * This will delete the properties files.  Eventually it'll allow you to pass in an arguments map.
 * @param args
 * @return
 */
def deletePropertiesFiles( args = null ) {

    def params = getParamsMap( args )

    def dir = BuildSettings.BASE_DIR

    dir.traverse(type: FileType.FILES, nameFilter: params.filter) { source ->

        def newFile = getNewFileName(source, params.targetLocale)

        println "Deleting file '$newFile'"
        def destFile = new File(newFile)

        // Delete the existing file if it is there
        destFile.delete()
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


target (main:'''Deletes psuedo properties files used for debugging i18n issues.

Examples:
grails delete-properties''') 
{	
    Number startTime = System.currentTimeMillis()
    println "Start Deletion"

	// Turn args into a map
	def argMap = [:]
	args?.split("\n")?.each {
        def key = StringUtils.substringBefore( it, "=" )
        def value = StringUtils.substringAfter( it, "=" )
		argMap[key] = value
	}

    deletePropertiesFiles( argMap )

    println "Deletion Complete in " + (System.currentTimeMillis() - startTime) + "ms"
}

setDefaultTarget('main')
