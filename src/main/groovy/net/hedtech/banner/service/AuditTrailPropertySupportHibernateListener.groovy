/*******************************************************************************
 Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service

import net.hedtech.banner.apisupport.ApiUtils
import org.apache.commons.lang.ArrayUtils

import grails.util.Holders  as CH
import net.hedtech.banner.apisupport.ApiUtils
import net.hedtech.banner.security.BannerUser
import org.apache.commons.lang.ArrayUtils
import org.hibernate.cfg.Configuration
import org.hibernate.event.spi.PreInsertEvent
import org.hibernate.event.spi.PreInsertEventListener
import org.hibernate.event.spi.PreUpdateEvent
import org.hibernate.event.spi.PreUpdateEventListener
import org.springframework.security.core.context.SecurityContextHolder as SCH


import org.springframework.security.core.context.SecurityContextHolder as SCH
import net.hedtech.banner.security.BannerUser

/**
 * A hibernate event listener responsible for setting audit trail fields. Performing this
 * via an event listener ensures audit trail properties set even if a service isn't implemented
 * to update these fields or if GORM is used directly to persist a model.
 */
class AuditTrailPropertySupportHibernateListener implements PreInsertEventListener, PreUpdateEventListener {

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
        try {
            updateSystemFields( event )
        } catch (e) {
            e.printStackTrace()
            throw e
        }
        return false
    }


    // Sets the three audit trail fields:
    // lastModified - the time with 'second' resolution    (created here)
    // lastModifiedBy - the person who modified the record (taken from the authentication token of the user)
    // dataOrigin - the system that modified the value     (taken from Config.groovy)
    boolean updateSystemFields( event ) {
        // Unfortunately, 'lastModified' is mapped to SQL DATE type versus SQL TIMESTAMP, and thus does not retain fractional seconds.
        // More unfortunately, the Hibernate cache does not reflect this, but will hold the 'lastModified' instance with fractional seconds
        // as the 'persistentValue'. Usually this is ok, but if a 'refresh()' is subsequently performed on the model the model and
        // it's 'persistentValue' (cache) will be updated to reflect the 'actual' database value (without fractional seconds).
        //
        // To avoid issues (e.g., during testing), we'll just make sure the hibernate cache reflects the 'actual' persisted value,
        // by removing the fractional seconds.
        Calendar cal = Calendar.getInstance()
        cal.setTime( new Date() )
        cal.set( Calendar.MILLISECOND, 0 ) // truncate fractional seconds, so that we can compare dates to those retrieved from the database
        Date lastModified = new Date( cal.getTime().getTime() )
        def  lastModifiedBy
        if (event.entity.hasProperty('lastModifiedBy')) {
            lastModifiedBy = getLastModifiedBy(event.entity?.lastModifiedBy)
        }
        def  dataOrigin = ApiUtils.isApiRequest() ?
                event.entity?.dataOrigin ?: (CH.config?.dataOrigin ?: "Banner") :
                CH.config?.dataOrigin ?: "Banner"

        if(lastModifiedBy) {
            setPropertyValue( event, "lastModifiedBy" ) { lastModifiedBy }
        }
        if (event.entity.hasProperty('dataOrigin')) {
            setPropertyValue(event, "dataOrigin") { dataOrigin }
        }
        if (event.entity.hasProperty('lastModified')) {
            setPropertyValue( event, "lastModified" ) { lastModified }
        }
    }


    // Note that in a Hibernate callback, we can't just change the model (it's too late for that) -- so, we have to set the value within the event.
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
            log.error("Error in setPropertyValueAuditTrailPropertySupportHibernateListener.setPropertyValue caught $e")
            false
        }
    }

    def getLastModifiedBy(String existingLastModifiedBy =  null) {
        String  lastModifiedBy
        try {
            if  (SCH.context?.authentication?.principal instanceof BannerUser )
                lastModifiedBy = SCH.context?.authentication?.principal?.username
            else {
                lastModifiedBy = SCH.context?.authentication?.principal
            }

            if (lastModifiedBy == null) {
                lastModifiedBy = (existingLastModifiedBy?.equalsIgnoreCase('BANNER'))? existingLastModifiedBy : 'anonymous'
            }

            if (lastModifiedBy?.length() > 30) {
                lastModifiedBy = lastModifiedBy.substring(0,30)
            }
        } catch (e) {
            log.error("Error : Could not retrieve last modified by lastModifiedBy:$lastModifiedBy $e")
        }
        return lastModifiedBy?.toUpperCase()
    }
}
