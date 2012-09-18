/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.service

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.springframework.security.core.context.SecurityContextHolder as SCH


/**
 * Provides a static updateSystemFields method (which is no longer needed).
 **/
@Deprecated // Please use ServiceBase instead
public class DomainManagementMethodsInjector {


    // TODO: Remove this class -- audit trail fields are updated by a hibernate event listener.

    public static def updateSystemFields( entity ) {
        if (entity.hasProperty( 'dataOrigin' )) {
            def dataOrigin = CH.config?.dataOrigin ?: "Banner" // protect from missing configuration
            entity.dataOrigin = dataOrigin
        }
        if (entity.hasProperty( 'lastModifiedBy' )) {
            entity.lastModifiedBy = SCH.context?.authentication?.principal?.username
        }
        if (entity.hasProperty( 'lastModified' )) {
            entity.lastModified = new Date()
        }
    }

}
