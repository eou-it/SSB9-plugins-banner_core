/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import grails.util.Holders  as CH
import net.hedtech.banner.apisupport.ApiUtils
import org.apache.commons.lang.ArrayUtils
import org.grails.datastore.mapping.engine.event.SaveOrUpdateEvent
import org.grails.web.converters.ConverterUtil
import org.hibernate.cfg.Configuration
import org.springframework.security.core.context.SecurityContextHolder

//import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder as SCH
import net.hedtech.banner.security.BannerUser
import java.security.Principal
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PreDeleteEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.springframework.context.ApplicationEvent

/**
 * A hibernate event listener responsible for setting audit trail fields. Performing this
 * via an event listener ensures audit trail properties set even if a service isn't implemented
 * to update these fields or if GORM is used directly to persist a model.
 */
@CompileStatic
@Slf4j
class AuditTrailPropertySupportHibernateListener extends AbstractPersistenceEventListener {

    static detailed = false

    public AuditTrailPropertySupportHibernateListener(final Datastore datastore) {
        super(datastore)
    }
    //public void initialize( final Configuration config ) { }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        switch (event.getEventType()) {
            case EventType.SaveOrUpdate:
                updateSystemFields(event)
                break;
        }
    }

    // Sets the three audit trail fields:
    // lastModified - the time with 'second' resolution    (created here)
    // lastModifiedBy - the person who modified the record (taken from the authentication token of the user)
    // dataOrigin - the system that modified the value     (taken from Config.groovy)
    boolean updateSystemFields( AbstractPersistenceEvent event ) {
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
        def  origin =  event.entity?.getPropertyByName('dataOrigin')
        def  lastModifiedBy

        if (event.entityObject.hasProperty('lastModifiedBy')) {
            def temp = event.entityObject.metaClass.getMetaProperty('lastModifiedBy')
            lastModifiedBy = getLastModifiedBy(temp.toString())
        }
        def  dataOrigin = ApiUtils.isApiRequest() ?
                origin ?: (CH.config?.dataOrigin ?: "Banner") :  CH.config?.dataOrigin ?: "Banner"

        if(lastModifiedBy) {
            setPropertyValue( event, "lastModifiedBy" ) { lastModifiedBy }
        }
        if (event.entityObject.hasProperty('dataOrigin')) {
            setPropertyValue(event, "dataOrigin") { dataOrigin }
        }
        if (event.entityObject.hasProperty('lastModified')) {
            setPropertyValue( event, "lastModified" ) { lastModified }
        }
    }

    // Note that in a Hibernate callback, we can't just change the model (it's too late for that) -- so, we have to set the value within the event.
    boolean setPropertyValue(AbstractPersistenceEvent  event, String name, Closure valueClosure ) {
        def d = event.entity
        def domainObject = event.entityObject
        domainObject[name] = valueClosure.call()
    }

    def getLastModifiedBy(String existingLastModifiedBy =  null) {
        String  lastModifiedBy
        try {

            if  (SecurityContextHolder.context?.authentication?.principal instanceof BannerUser )
                lastModifiedBy = ((BannerUser)(SecurityContextHolder.context?.authentication?.principal))?.username
            else {
                lastModifiedBy = SecurityContextHolder.context?.authentication?.principal
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
    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return  eventType.isAssignableFrom(SaveOrUpdateEvent)
    }

}
