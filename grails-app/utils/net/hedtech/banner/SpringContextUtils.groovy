/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner

import grails.util.Holders  as CH

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
