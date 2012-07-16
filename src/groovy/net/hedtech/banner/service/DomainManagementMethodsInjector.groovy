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
