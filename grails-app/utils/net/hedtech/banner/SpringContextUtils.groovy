/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner

import grails.util.Holders
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.ApplicationContext

/**
 * @deprecated Use grails.util.Holders instead.
 * Common configuration utilities.
 */
abstract class SpringContextUtils {

    /**
     * @deprecated Use grails.util.Holders instead.
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return Holders.applicationContext;
    }

    /**
     * @deprecated Use grails.util.Holders instead.
     * @return
     */
    public static GrailsApplication getGrailsApplication () {
        applicationContext.getBean( "grailsApplication" )
    }

    /**
     * @deprecated Use grails.util.Holders instead.
     * @return
     */
    public static java.lang.ClassLoader getGrailsApplicationClassLoader () {
        getGrailsApplication().getClassLoader()
    }
}
