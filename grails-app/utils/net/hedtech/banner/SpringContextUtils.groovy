/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package net.hedtech.banner

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext

/**
 * Common configuration utilities.
 */
abstract
class SpringContextUtils {

    public static GrailsApplication getGrailsApplication () {
        applicationContext.getBean( "grailsApplication" )
    }

    public static java.lang.ClassLoader getGrailsApplicationClassLoader () {
        getGrailsApplication().getClassLoader()
    }

    public static ApplicationContext getApplicationContext() {
        return (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
    }

        
}
