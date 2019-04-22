/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service

import grails.util.Holders as CH
import groovy.util.logging.Slf4j
import net.hedtech.banner.apisupport.ApiUtils
import net.hedtech.banner.security.BannerUser
import org.apache.commons.lang.ArrayUtils
import org.hibernate.event.spi.PreInsertEvent
import org.hibernate.event.spi.PreInsertEventListener
import org.hibernate.event.spi.PreUpdateEvent
import org.hibernate.event.spi.PreUpdateEventListener
import org.springframework.security.core.context.SecurityContextHolder as SCH

/**
 * A hibernate event listener responsible for setting audit trail fields. Performing this
 * via an event listener ensures audit trail properties set even if a service isn't implemented
 * to update these fields or if GORM is used directly to persist a model.
 */
@Slf4j
class AuditTrailPropertySupportHibernateListener implements PreInsertEventListener, PreUpdateEventListener {

    public boolean onPreInsert(final PreInsertEvent event) {
        try {
            updateSystemFields(event)
            log.debug "After onPreInsert event - ${event.entity}"
        } catch (e) {
            e.printStackTrace()
            throw e
        }
        return false
    }


    public boolean onPreUpdate(final PreUpdateEvent event) {
        try {
            updateSystemFields(event)
            log.debug "After onPreUpdate event - ${event.entity}"
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
    boolean updateSystemFields(event) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(new Date())
        cal.set(Calendar.MILLISECOND, 0)
        // truncate fractional seconds, so that we can compare dates to those retrieved from the database
        Date lastModified = new Date(cal.getTime().getTime())
        def lastModifiedBy
        if (event.entity.hasProperty('lastModifiedBy')) {
            lastModifiedBy = getLastModifiedUser(event.entity.lastModifiedBy)
        }
        def dataOrigin = ApiUtils.isApiRequest() ?
                event.entity?.dataOrigin ?: (CH.config?.dataOrigin ?: "Banner") :
                CH.config?.dataOrigin ?: "Banner"

        if (lastModifiedBy) {
            setPropertyValue(event, "lastModifiedBy") { lastModifiedBy }
        }
        if (event.entity.hasProperty('dataOrigin')) {
            setPropertyValue(event, "dataOrigin") { dataOrigin }
        }
        if (event.entity.hasProperty('lastModified')) {
            setPropertyValue(event, "lastModified") { lastModified }
        }
    }

    // Note that in a Hibernate callback, we can't just change the model (it's too late for that) -- so, we have to set the value within the event.
    void setPropertyValue(event, String name, Closure valueClosure) {
        try {
            int index = ArrayUtils.indexOf(event.getPersister().getPropertyNames(), name)
            if (index != ArrayUtils.INDEX_NOT_FOUND) {
                def value = valueClosure.call()
                event.getState()[index] = value // this is the state that will be written to the database
                event.entity."$name" = value
                // and updating the corresponding model properties to ensure these fields are not identified as 'dirty'
            }
        } catch (e) {
            log.error("Error in setPropertyValueAuditTrailPropertySupportHibernateListener.setPropertyValue caught $e")
        }
    }


    String getLastModifiedUser(String existingLastModifiedBy = null) {
        String lastModifiedBy
        try {
            if (SCH.context?.authentication?.principal instanceof BannerUser)
                lastModifiedBy = SCH.context?.authentication?.principal?.username
            else if(!((SCH.context?.authentication?.principal?.username)?.equalsIgnoreCase("__grails.anonymous.user__"))) {
                lastModifiedBy = SCH.context?.authentication?.principal
            }

            if (lastModifiedBy == null) {
                lastModifiedBy = (existingLastModifiedBy) ? existingLastModifiedBy : 'anonymous'
            }

            if (lastModifiedBy?.length() > 30) {
                lastModifiedBy = lastModifiedBy.substring(0, 30)
            }
        } catch (e) {
            log.error("Error : Could not retrieve last modified by lastModifiedBy:$lastModifiedBy $e")
        }
        log.debug "LastModifiedBy user is {}",lastModifiedBy
        return lastModifiedBy?.toUpperCase()
    }
}
