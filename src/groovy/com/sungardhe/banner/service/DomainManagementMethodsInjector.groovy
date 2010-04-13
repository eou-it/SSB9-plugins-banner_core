/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

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
    public static void injectDataManagement( serviceInstance, domainClass ) {

        def log = Logger.getLogger( serviceInstance.class.name )
        
        String domainSimpleName = domainClass.simpleName

        if (!serviceInstance.metaClass.respondsTo( serviceInstance, "create" )) {
            serviceInstance.metaClass.create = { domainObjectOrParams ->
                log.trace "${domainSimpleName}Service.create invoked with $domainObjectOrParams"
                try {
                    def domainObject = assignOrInstantiate( domainClass, domainObjectOrParams )
                    assert domainObject.id == null                    
                    updateSystemFields( domainObject )
                    
                    if (delegate.respondsTo( 'preCreate' )) delegate.preCreate( domainObjectOrParams )
                    
                    log.trace "${domainSimpleName}Service.create will save $domainObject"
                    def createdModel = domainObject.save( failOnError: true, flush: true )
                    
                    if (delegate.respondsTo( 'postCreate' )) delegate.postCreate( createdModel )
                    
                    createdModel
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Could not save a new ${domainSimpleName} due to exception: $ae", e
                    throw ae
                }
            }
        }

        if (!serviceInstance.metaClass.respondsTo( serviceInstance, "update" )) {
            serviceInstance.metaClass.update = { domainObjectOrParams ->
                log.trace "${domainSimpleName}Service.update invoked with $domainObjectOrParams"
                def content
                if (domainObjectOrParams instanceof Map) {
                    content = domainObjectOrParams
                } else {
                    content = domainObjectOrParams.properties
                    content.version = domainObjectOrParams.version // version is not included in bulk asisgnments
                }
                def domainObject
                try {
                    domainObject = fetch( domainClass, content.id, log )
                    domainObject.properties = content
                    domainObject.version = content.version // needed as version is not included in bulk assignment                    
                    updateSystemFields( domainObject )
                  
                    if (delegate.respondsTo( 'preUpdate' )) delegate.preUpdate( domainObject )
                    
                    log.trace "${domainSimpleName}Service.update applied updates and will save $domainObject"
                    def updatedModel = domainObject.save( failOnError:true, flush: true )

                    if (delegate.respondsTo( 'postUpdate' )) delegate.postUpdate( updatedModel )

                    updatedModel
                } catch (ValidationException e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Could not update an existing ${domainSimpleName} with id = ${domainObject?.id} due to exception: $ae", e
                    checkOptimisticLockIfInvalid( domainObject, content, log ) // optimistic lock trumps validation errors
                    throw ae
                } catch (e) {
                    log.debug "Could not update an existing ${domainSimpleName} with id = ${domainObjectOrParams?.id} due to exception: ${e.message}", e
                    throw new ApplicationException( domainClass, e )
                }
            }
        }

        if (!(serviceInstance.metaClass.respondsTo( serviceInstance, "delete" ) || serviceInstance.metaClass.respondsTo( serviceInstance, "remove" ))) {
            serviceInstance.metaClass.delete = { id ->
                log.trace "${domainSimpleName}Service.delete invoked with $id"
                def domainObject
                try {
                    domainObject = fetch( domainClass, id, log )
                    if (delegate.respondsTo( 'preDelete' )) delegate.preDelete( domainObject )
                    domainObject.delete( failOnError:true, flush: true )
                    if (delegate.respondsTo( 'postDelete' )) delegate.postDelete( domainObject )
                    true
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Could not delete ${domainSimpleName} with id = ${domainObject?.id} due to exception: $ae", e
                    throw ae
                }
            }
        } 
                
        if (!serviceInstance.metaClass.respondsTo( serviceInstance, "read" )) {
            serviceInstance.metaClass.read = { id ->
                try {
                    fetch( domainClass, id, log )
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Exception executing ${domainSimpleName}.read() with id = $id, due to exception: $ae", e
                    throw ae
                }                
            }  
        }
        
        if (!serviceInstance.metaClass.respondsTo( serviceInstance, "get" )) { // TODO: remove this (refactor controllers that use it to instead use 'read')
            serviceInstance.metaClass.get = { id ->
                try {
                    fetch( domainClass, id, log )
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Exception executing ${domainSimpleName}.get() with id = $id, due to exception: $ae", e
                    throw ae
                }                
            }  
        } 
        
        if (!serviceInstance.metaClass.respondsTo( serviceInstance, 'list' )) {
            serviceInstance.metaClass.list = { args ->
                try {
                    domainClass.list( args )
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Exception executing ${domainSimpleName}.list() with args = $args, due to exception: $ae", e
                    throw ae
                }
            }
        }   
        
        if (!serviceInstance.metaClass.respondsTo( serviceInstance, 'count' )) {
            serviceInstance.metaClass.count = { 
                try {
                    domainClass.count()
                } catch (e) {
                    def ae = new ApplicationException( domainClass, e )
                    log.debug "Exception executing ${domainSimpleName}.count() due to exception: $ae", e
                    throw ae
                }
            }
        }   
        
    }    
        
    
    // ---------------------------- Private Methods -----------------------------------
    
    
    private static def assignOrInstantiate( domainClass, domainObjectOrParams ) {
        if (domainObjectOrParams.class == domainClass) {
            domainObjectOrParams
        } else if (domainObjectOrParams instanceof Map) {
            domainClass.newInstance( domainObjectOrParams )
        } else {
            throw new IllegalArgumentException( "${domainObjectOrParams.class} is not an instance of $domainClass}" )
        }
    }
    
    
    private static def fetch( domainClass, id, log ) {
        if (id == null) {
            throw new NotFoundException( id: id, entityClassName: domainClass.simpleName )
        }
        def persistentEntity = domainClass.get( id )
        if (!persistentEntity) {
            log.debug "Could not find an existing ${domainClass.simpleName} with id = $id"
            throw new NotFoundException(  id: id, entityClassName: domainClass.simpleName )
        }
        persistentEntity
    }
    
    
    private static def updateSystemFields( entity ) {
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
    private static def checkOptimisticLockIfInvalid( domainObject, content, log ) {
        if ((content.version != null) && (domainObject.hasProperty( 'version' ) != null)) { 
            domainObject.refresh() // query the database, as a domainObject.get(id) will just hit the cache...
            if (content.version != domainObject.version) {
                log.debug "Optimistic lock violation between params $content and the model's state in the database $domainObject that has version ${domainObject.version}"
                throw new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.name, domainObject.id ) ) )
            }
        } else if (domainObject.hasProperty( 'version' )) {
            def ae = new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.simpleName, domainObject.id ) ) )
            log.debug "Could not update an existing ${domainObject.class.simpleName} with id = ${domainObject?.id} and version = ${domainObject?.version} due to Optimistic Lock violation, when given version ${content.version}. Exception is $ae"
            throw ae
        } else {
            log.warn "An optimistic lock version was provided when updating ${domainObject?.class.name} with id = ${domainObject?.id}, \
                      but this object doesn't support optimistic locking!"
        }
    }
    
}