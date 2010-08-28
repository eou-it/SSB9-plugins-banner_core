/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.configuration

/**
 * Utilities for application configuration.
 */
abstract
class ApplicationConfigurationUtils {

    
    // Loads a configuration file, using the following search order:
    // 1. Load the configuration file if its location was specified on the command line using -DmyEnvName=myConfigLocation
    // 2. Load the configuration file if it exists within the user's .grails directory (i.e., convenient for developers)
    // 3. Load the configuration file if its location was specified as a system environment variable
    public static void addLocation( List locations, String envName, String filePathName ) {
        if (System.getProperty( envName ) && new File( System.getProperty( envName ) ).exists()) {
            println "Including configuration file specified on command line: ${System.getProperty( envName )}."
            locations << "file:" + System.getProperty( envName )
        }
        else if (new File( "${filePathName}").exists()) {
            println "Including user-specific configuration file: ${filePathName}."
            locations << "file:${filePathName}"
        }
        else if (System.getenv( envName ) && new File( System.getenv( envName ) ).exists()) {
            println "Including System Environment specified configuration file: ${System.getenv( envName )}."
            locations << "file:" + System.getenv( envName )
        }
        else {
            println "*** WARNING *** --> Could not find configuration file using either environment name $envName or file name $filePathName"
        }
    }

}