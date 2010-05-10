/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.service

import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.exceptions.NotFoundException

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.hibernate.StaleObjectStateException

import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException as OptimisticLockException
import org.springframework.security.context.SecurityContextHolder as SCH

import grails.validation.ValidationException


/**
 * Contains CRUD ('create', 'read', 'update', and 'delete') methods that may be injected into a service.
 **/
public class DomainManagementMethodsInjector {


    /**
     * This method will inject the basic CRUD methods into a service class.  If any of the CRUD
     * methods are implemented, this method will NOT inject or alter them.  Note that models may be used 
     * for read-only directly in controllers, but must never modify the models without using a 
     * transactional service.  The class injects get, list, and count methods for convenience (although 
     * the transaction attributes are read-write, so there is a little extra overhead using these methods). 
     *
     * The create, update, and delete method implementions can callback to the server (upon which these 
     * methods are injected) if the service responds to preCreate, postCreate, preUpdate, postUpdate, 
     * preDelete, or postDelete.
     * Services need not provide any of these callback handlers -- they are used only if exposed by the service.
     *
     * The create and update methods accept any of the following arguments:
     * --> a domain model instance to be created or updated
     * --> a 'params-like' map that contains entries representing the properties of the domain object to be created or updated
     * --> a map that contains a key named 'domainModel' that contains a domain model instance to be created or updated
     *
     * Additional entries provided in a 'params' map that do not correspond to domain model properties will be ignored.
     * Whether these additional entries are provided in either a 'params-like' map or map containing a domainModel field, 
     * they will be supplied to the preCreate or preUpdate callbacks. They are not provided to the postCreate and 
     * postUpdate callbacks, as these callbacks supply only the as-persisted domain model instance. 
     * 
     * The use of the abovementioned callbacks may allow use of these injected methods even when additional 
     * behavior is necessary.  If this isn't sufficient, the service can always provide it's own implementation
     * of the CRUD method (and the implementation in this class won't be injected).
     * 
     * The create, read, update, delete (CRUD) methods implemented here may throw ApplicationException runtime exceptions.
     * An ApplicationException wraps underlying checked and unchecked exceptions (e.g., from Hibernate, Spring, etc.)
     * and provides a consistent interface for controllers. See ApplicationException.
     *   
     * Controllers should also catch, after ApplicationException, any exception (in case services provide 
     * CRUD implementations that fail to wrap all exceptions).
     */
    public static void injectDataManagement( serviceClassOrInstance, domainClass ) {
        def serviceClass = (serviceClassOrInstance instanceof Class) ? serviceClassOrInstance : serviceClassOrInstance.class

        def log = Logger.getLogger( serviceClass.name )        
        String domainSimpleName = domainClass.simpleName

        if (!serviceClass.metaClass.respondsTo( serviceClass, "create" )) {

            serviceClass.metaClass.create = { domainModelOrMap ->
                log.trace "${domainSimpleName}Service.create invoked with $domainModelOrMap"
                try {
                    if (delegate.respondsTo( 'preCreate' )) delegate.preCreate( domainModelOrMap )
                    
                    def domainObject = assignOrInstantiate( domainClass, domainModelOrMap )
                    assert domainObject.id == null                    
                    updateSystemFields( domainObject )
                    log.trace "${domainSimpleName}Service.create will save $domainObject"
                    def createdModel = domainObject.save( failOnError: true, flush: true )
                    
                    if (delegate.respondsTo( 'postCreate' )) delegate.postCreate( [ before: domainModelOrMap, after: createdModel ] )
                    
                    createdModel
                } catch (ApplicationException ae) {
                    log.debug "Could not save a new ${domainSimpleName} due to exception: $ae", ae
                    throw ae
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Could not save a new ${domainSimpleName} due to exception: $ae", e
                    throw ae
                }
            }
        }

        if (!serviceClass.metaClass.respondsTo( serviceClass, "update" )) {
            serviceClass.metaClass.update = { domainModelOrMap ->
                log.trace "${domainSimpleName}Service.update invoked with $domainModelOrMap"
                def domainObject
                try {
                    if (delegate.respondsTo( 'preUpdate' )) delegate.preUpdate( domainModelOrMap )
                    
                    def content = extractParams( domainClass, domainModelOrMap )
                    domainObject = fetch( domainClass, content?.id, log )
                    domainObject.properties = content
                    domainObject.version = content.version // needed as version is not included in bulk assignment                    
                    updateSystemFields( domainObject )
                                      
                    log.trace "${domainSimpleName}Service.update applied updates and will save $domainObject"
                    def updatedModel = domainObject.save( failOnError:true, flush: true )

                    if (delegate.respondsTo( 'postUpdate' )) delegate.postUpdate( [ before: domainModelOrMap, after: updatedModel ] )

                    updatedModel
                } catch (ApplicationException ae) {
                    log.debug "Could not update an existing ${domainSimpleName} with id = ${domainModelOrMap?.id} due to exception: ${ae.message}", ae
                    throw ae
                } catch (ValidationException e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Could not update an existing ${domainSimpleName} with id = ${domainObject?.id} due to exception: $ae", e
                    checkOptimisticLockIfInvalid( domainObject, content, log ) // optimistic lock trumps validation errors
                    throw ae
                } catch (e) {
                    log.debug "Could not update an existing ${domainSimpleName} with id = ${domainModelOrMap?.id} due to exception: ${e.message}", e
                    throw new ApplicationException( domainClass, e, false )
                }
            }
        }

        if (!(serviceClass.metaClass.respondsTo( serviceClass, "delete" ) || serviceClass.metaClass.respondsTo( serviceClass, "remove" ))) {
            serviceClass.metaClass.delete = { domainModelOrMapOrId ->
                log.trace "${domainSimpleName}Service.delete invoked with $domainModelOrMapOrId"
                def domainObject
                try {
                    if (delegate.respondsTo( 'preDelete' )) delegate.preDelete( domainModelOrMapOrId )
                    
                    def id = extractId( domainClass, domainModelOrMapOrId )
                    domainObject = fetch( domainClass, id, log )
                    domainObject.delete( failOnError:true, flush: true )
                    
                    if (delegate.respondsTo( 'postDelete' )) delegate.postDelete( [ before: domainModelOrMapOrId, after: null ] )
                    true
                } catch (ApplicationException ae) {
                    log.debug "Could not delete ${domainSimpleName} with id = ${domainObject?.id} due to exception: $ae", ae
                    throw ae
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Could not delete ${domainSimpleName} with id = ${domainObject?.id} due to exception: $ae", e
                    throw ae
                }
            }
        } 
                
        if (!serviceClass.metaClass.respondsTo( serviceClass, "read" )) { // same as 'get' below, included as it's a 'cRud' method
            serviceClass.metaClass.read = { id ->
                try {
                    fetch( domainClass, id, log )
                } catch (ApplicationException ae) {
                    log.debug "Exception executing ${domainSimpleName}.read() with id = $id, due to exception: $ae", ae
                    throw ae
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Exception executing ${domainSimpleName}.read() with id = $id, due to exception: $ae", e
                    throw ae
                }                
            }  
        }
        
        if (!serviceClass.metaClass.respondsTo( serviceClass, "get" )) { // same as 'read' above, included as it is very common to have 'get'
            serviceClass.metaClass.get = { id ->
                try {
                    fetch( domainClass, id, log )
                } catch (ApplicationException ae) {
                    log.debug "Exception executing ${domainSimpleName}.get() with id = $id, due to exception: $ae", ae
                    throw ae
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Exception executing ${domainSimpleName}.get() with id = $id, due to exception: $ae", e
                    throw ae
                }                
            }  
        } 
        
        if (!serviceClass.metaClass.respondsTo( serviceClass, 'list' )) {
            serviceClass.metaClass.list = { args ->
                try {
                    domainClass.list( args )
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Exception executing ${domainSimpleName}.list() with args = $args, due to exception: $ae", e
                    throw ae
                }
            }
        }   
        
        if (!serviceClass.metaClass.respondsTo( serviceClass, 'count' )) {
            serviceClass.metaClass.count = { 
                try {
                    domainClass.count()
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e, false )
                    log.debug "Exception executing ${domainSimpleName}.count() due to exception: $ae", e
                    throw ae
                }
            }
        }   
        
    }    
        
    
    // ---------------------------- Helper Methods -----------------------------------
    // (public static methods, so they may be used within services that implement their own CRUD methods.)
    
    
    private static boolean isDomainModelInstance( domainClass, domainModelOrMap ) {
        (domainClass.isAssignableFrom( domainModelOrMap.getClass() ) && 
            !(Map.isAssignableFrom( domainModelOrMap.getClass() )))
    }


    /**
     * Returns a model instance based upon the supplied domainModelOrMap.
     * The domainModelOrMap may:
     * 1) already be the domain model instance that should be returned, 
     * 2) be a 'params' map that may be used to create a new model instance 
     * 3) be a map that contains a 'domainModel' key whose value is the domain model instance to return
     * This static method is public so that it may be used within any services that implement their own CRUD methods.
     **/
    public static def assignOrInstantiate( domainClass, domainModelOrMap ) {
        if (isDomainModelInstance( domainClass, domainModelOrMap )) {
            domainModelOrMap
        } else if (domainModelOrMap instanceof Map) {
            domainClass.newInstance( extractParams( domainClass, domainModelOrMap ) )
        } else {
            throw new ApplicationException( domainClass, "Cannot assign a $domainClass using ${domainModelOrMap}", false )
        }
    }


    /**
     * Returns a 'params map' based upon the supplied domainObjectOrMap.
     * The domainObjectOrMap may:
     * 1) be a domain model instance whose properties should be returned, 
     * 2) already be a 'params' map that may be returned
     * 3) be a map that contains a 'domainModel' key whose value is the domain model instance whose properties should be returned
     * This static method is public so that it may be used within any services that implement their own CRUD methods.
     **/
    public static def extractParams( domainClass, domainObjectOrMap ) {
        if (isDomainModelInstance( domainClass, domainObjectOrMap )) {
            def paramsMap = domainObjectOrMap.properties
            if (domainObjectOrMap.version) {
                paramsMap.version = domainObjectOrMap.version // version is not included in bulk asisgnments
            }
            paramsMap
        } else if (domainObjectOrMap instanceof Map) {
            if (domainObjectOrMap.domainModel) {
                extractParams( domainClass, domainObjectOrMap.domainModel )
            } else {
                domainObjectOrMap
            }
        } else {
            throw new ApplicationException( domainClass, "Cannot extract a params map supporting $domainClass from: ${domainObjectOrMap}", false )
        }
    }
    
    
    /**
     * Returns an 'id' extracted from the supplied domainObjectParamsIdOrMap.
     * The domainObjectParamsIdOrMap may:
     * 1) already be a Long 'id' that should be returned, 
     * 2) be a 'params' map that contains a key named 'id' 
     * 3) be a map that contains a 'domainModel' key whose value is a domain model instance from which the 'id' may be extracted
     * This static method is public so that it may be used within any services that implement their own CRUD methods.
     **/
    public static def extractId( domainClass, domainObjectParamsIdOrMap ) {
        if (domainObjectParamsIdOrMap.toString().isNumber()) {
            (Long) domainObjectParamsIdOrMap
        } else if (isDomainModelInstance( domainClass, domainObjectParamsIdOrMap )) {
            (Long) domainObjectParamsIdOrMap.id
        } else if (domainObjectParamsIdOrMap instanceof Map) {
            def paramsMap = extractParams( domainClass, domainObjectParamsIdOrMap )
            paramsMap?.id
        } else {
            throw new ApplicationException( domainClass, "Could not extract an 'id' from ${domainObjectParamsIdOrMap}", false )
        }
    }
    
    
    public static def fetch( domainClass, id, log ) {
        if (id == null) {
            throw new NotFoundException( id: id, entityClassName: domainClass.simpleName )
        }
        def persistentEntity = domainClass.get( id )
        if (!persistentEntity) {
            throw new ApplicationException( domainClass, new NotFoundException(  id: id, entityClassName: domainClass.simpleName ), false )
        }
        persistentEntity
    }
    
    
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
        

    /**
     * Checks the optimistic lock. If there are validation errors on other fields and the optimistic lock is violated,
     * the optimistic lock violation should take precedence.  Why?  Consider the 
     * following scenario:  A domain object has field1 = 3, field2 = 10, and is at version = 5. It also has 
     * a validation constraint that says field1 must be less than field2.  A user changes 
     * field2 = 5 and saves. This is fine, and passes both validation and optimistic lock checks. 
     * A second user was also working on version 5 of this domain object, and sets field1 = 7. 
     * This seems correct to the user, who still sees field2 = 10 (as he/she is now using a stale version
     * of the object.)  When the second user tries to save, he/she will fail. Without this initial
     * optimistic lock check, the user will fail validation (as field1 is no longer less than field2).
     * The feedback given to the user due to this validation failure will likely cause confusion. 
     * It would be less confusing to the user to simply inform the user that someone else has 
     * already changed the object. 
     *
     * Again, this pre-update check is not needed for data integrity -- it's only needed to 
     * ensure we throw an optimistic lock exception before throwing a validation failure exception,
     * when both optimistic lock and validation rules are violated.
     * @param domainObject the domainObject as represented in the database
     * @content a map contianing updated fields
     **/  
    public static def checkOptimisticLockIfInvalid( domainObject, content, log ) {
        if ((content.version != null) && (domainObject.hasProperty( 'version' ) != null)) { 
            domainObject.refresh() // query the database, as a domainObject.get(id) will just hit the cache...
            if (content.version != domainObject.version) {
                log.debug "Optimistic lock violation between params $content and the model's state in the database $domainObject that has version ${domainObject.version}"
                throw new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.name, domainObject.id ) ), false )
            }
        } else if (domainObject.hasProperty( 'version' )) {
            def ae = new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.simpleName, domainObject.id ) ), false )
            log.debug "Could not update an existing ${domainObject.class.simpleName} with id = ${domainObject?.id} and version = ${domainObject?.version} due to Optimistic Lock violation, when given version ${content.version}. Exception is $ae"
            throw ae
        } else {
            log.warn "An optimistic lock version was provided when updating ${domainObject?.class.name} with id = ${domainObject?.id}, \
                      but this object doesn't support optimistic locking!"
        }
    }
    
}