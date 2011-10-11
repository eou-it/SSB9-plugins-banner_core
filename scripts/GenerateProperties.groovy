/** *****************************************************************************
  Copyright 2008-2011 SunGard Higher Education. All Rights Reserved.

  This copyrighted software contains confidential and proprietary information of
  SunGard Higher Education and its subsidiaries. Any use of this software is
  limited solely to SunGard Higher Education licensees, and is further subject to
  the terms and conditions of one or more written license agreements between
  SunGard Higher Education and the licensee in question. SunGard, Banner and
  Luminis are either registered trademarks or trademarks of SunGard Higher
  Education in the U.S.A. and/or other regions and/or countries.
 ****************************************************************************** */

/**
 * Gant script that generates a psuedo properties file for debugging i18n issues.
 */

includeTargets << new File( "${basedir}/scripts/_GenerateProperties.groovy" )

target (generatePropertiesMain:'''Generates a psuedo properties file for debugging i18n issues.

Examples:
grails generate-locale-props sourceFile=<source file> targetLocale=<locale>'
sourceFile: A properties file to dummy translate.  Default is messages.properties * Optional
targetLocale: A dummy locale determining the name of the output file.  Default is 'en_PS' * Optional

E.g.: grails generate-locale-props sourceFile=messages.properties targetLocale=en_PS

''') {
    depends(generateProperties)
}

setDefaultTarget('generatePropertiesMain')