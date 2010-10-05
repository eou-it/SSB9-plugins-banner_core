/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.service

import org.apache.commons.lang.ArrayUtils

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.hibernate.cfg.Configuration
import org.hibernate.event.Initializable
import org.hibernate.event.PreInsertEvent
import org.hibernate.event.PreUpdateEvent
import org.hibernate.event.PreInsertEventListener
import org.hibernate.event.PreUpdateEventListener

import org.springframework.security.core.context.SecurityContextHolder as SCH


/**
 * A hibernate event listener responsible for setting audit trail fields. Performing this
 * via an event listener ensures audit trail properties set even if a service isn't implemented
 * to update these fields or if GORM is used directly to persist a model.
 */
class AuditTrailPropertySupportHibernateListener implements PreInsertEventListener, PreUpdateEventListener, Initializable {

    static detailed = false


    public void initialize( final Configuration config ) { }


    public boolean onPreInsert( final PreInsertEvent event ) {
        try {
            updateSystemFields( event )
        } catch (e) {
            e.printStackTrace()
            throw e
        }
        return false
    }


    public boolean onPreUpdate( final PreUpdateEvent event ) {
println "XXXXXXXXXXXXXXXXX___________________________ audit trail listener onPreUpdate called"
        try {
            updateSystemFields( event )
        } catch (e) {
            e.printStackTrace()
            throw e
        }
        return false
    }


    boolean updateSystemFields( event ) {
        (setPropertyValue( event, "dataOrigin" ) { CH.config?.dataOrigin ?: "Banner" }
            && setPropertyValue( event, "lastModifiedBy" ) { SCH.context?.authentication?.principal?.username }
            && setPropertyValue( event, "lastModified" ) { new Date() })
    }


    boolean setPropertyValue( event, String name, Closure valueClosure ) {
        try {
            int index = ArrayUtils.indexOf( event.getPersister().getPropertyNames(), name )
            if (index != ArrayUtils.INDEX_NOT_FOUND) {
                def value = valueClosure.call()
                event.getState()[ index ] = value // this is the state that will be written to the database
                event.entity."$name" = value      // and updating the corresponding model properties to ensure these fields are not identified as 'dirty'
            }
            true
        } catch (e) {
            println "AuditTrailPropertySupportHibernateListener.setPropertyValue caught $e"
            false
        }
    }

}
