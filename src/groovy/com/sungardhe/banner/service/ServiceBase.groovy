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

import grails.validation.ValidationException
import grails.util.GrailsNameUtils

import org.apache.log4j.Logger

import org.hibernate.StaleObjectStateException

import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException as OptimisticLockException

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.codehaus.groovy.grails.commons.GrailsClassUtils

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.sungardhe.banner.supplemental.SupplementalDataService

/**
 * Base class for services that provides generic support for CRUD.
 *
 * While this class is best used as a
 * base class, it can also be 'mixed in' to a service. When mixing in this class, however, the
 * @Transactional annotations are not effective. Consequently, services that mix-in this class must
 * ensure they include 'static transactional = true' to ensure they are transactional.
 *
 * This service base can protect against modification of read-only properties when they are identified
 * within the model via a static list, such as this example:
 *     public static readonlyProperties = [ 'addressCountry', 'addressZipCode', 'districtDivision' ]
 *
 * This service base can invoke callback handlers if defined in the concrete service, before and after
 * create, update, and delete.  These callback handlers should be implemented within the concrete service
 * as follows:
 *     void preCreate( domainModelOrMap )     // argument is: whatever was passed into the create method
 *     void postCreate( map )                 // argument is: [  before: domainModelOrMap, after: createdModel ]
 *     void preUpdate( domainModelOrMap )     // argument is: whatever was passed into the update method; only invoked if the model is 'dirty'
 *     void postUpdate( map )                 // argument is: [ before: domainModelOrMap, after: updatedModel ]; only invoked if the model was 'dirty'
 *     void preDelete( domainModelOrMapOrId ) // argument is: whatever was passed into the delete method
 *     void postDelete( map )                 // argument is: [  before: domainModelOrMapOrId, after: null ]
 *
 * For additional details regarding how to use this class (by extending from it or by mixing it in), please
 * see 'FooService' developer comments.
 * The FooService, and Foo model, are 'test' artifacts used to test the framework.
 */
@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class ServiceBase {

    @Lazy
    // note: Lazy annotation is needed here to ensure 'this' refers to the service we're mixed into (if we're mixed in)
    def log = Logger.getLogger( this.getClass() )

    Class domainClass // if not explicitly set by a subclass, this will be determined when needed

    // since this class may be mixed-into a service versus being extended, we won't rely on injection of the
    // supplemental data service, but will fetch it if required.
    SupplementalDataService supplementalDataService


    public def create( domainModelOrMap, flushImmediately = true ) {

        log.debug "In ServiceBase.create, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        if (domainModelOrMap instanceof List) {
            log.trace "${this.class}.create invoked with a list of size ${domainModelOrMap.size()} -- will iterate list and invoke 'create' on each..."
            List results = []
            domainModelOrMap.each { modelOrMap ->
                results << this.create( modelOrMap, flushImmediately )
            }
            results
        }
        else {
            log.trace "${this.class.simpleName}.create invoked with domainModelOrMap = $domainModelOrMap and flushImmediately = $flushImmediately"
            try {
                log.trace "${this.class.simpleName}.create will now invoke the preCreate callback if it exists"
                if (this.respondsTo( 'preCreate' )) this.preCreate( domainModelOrMap )

                def domainObject = assignOrInstantiate( getDomainClass(), domainModelOrMap )
                
                // updateSystemFields( domainObject ) // Note: No longer needed as audit trail is set via AuditTrailPropertySupportHibernateListener
                log.trace "${this.class.simpleName}.create will save $domainObject"

                def createdModel = domainObject.save( failOnError: true, flush: flushImmediately )
                createdModel = persistSupplementalDataFor( createdModel )

                log.trace "${this.class.simpleName}.create will now invoke the postCreate callback if it exists"
                if (this.respondsTo( 'postCreate' )) this.postCreate( [  before: domainModelOrMap, after: createdModel ] )

                createdModel
            }
            catch (ApplicationException ae) {
                log.debug "Could not save a new ${this.class.simpleName} due to exception: $ae", ae
                throw ae
            }
            catch (e) {
                def ae = new ApplicationException( getDomainClass(), e )
                log.debug "Could not save a new ${this.class.simpleName} due to exception: $ae", e
                throw ae
            }
        }
    }


    public def update( domainModelOrMap, flushImmediately = true ) {

        log.debug "In ServiceBase.update, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        if (domainModelOrMap instanceof List) {
            log.trace "${this.class.simpleName}.update invoked with a list of size ${domainModelOrMap.size()} -- will iterate list and invoke 'update' on each..."
            List results = []
            domainModelOrMap.each { modelOrMap ->
                results << this.update( modelOrMap, flushImmediately )
            }
            results
        }
        else {
            log.trace "${this.class.simpleName}.update invoked with domainModelOrMap = $domainModelOrMap and flushImmediately = $flushImmediately"
            def content      // we'll extract the domainModelOrMap into a params map of the properties
            def domainObject // we'll fetch the model instance into this, and bulk assign the 'content'
            try {

                content = extractParams( getDomainClass(), domainModelOrMap )
                domainObject = fetch( getDomainClass(), content?.id, log )
                domainObject.properties = content
                domainObject.version = content.version // needed as version is not included in bulk assignment

                def updatedModel
                if (domainObject.isDirty()) {

                    validateReadOnlyPropertiesNotDirty( domainObject ) // throws RuntimeException if readonly properties are dirty

                    log.trace "${this.class.simpleName}.update will update model with dirty properties ${domainObject.getDirtyPropertyNames()?.join(", ")}"

                    log.trace "${this.class.simpleName}.update will now invoke the preUpdate callback if it exists"
                    if (this.respondsTo( 'preUpdate' )) this.preUpdate( domainModelOrMap )
                    // updateSystemFields( domainObject ) // Note: No longer needed as audit trail is set via AuditTrailPropertySupportHibernateListener

                    log.trace "${this.class.simpleName}.update applied updates and will save $domainObject"
                    updatedModel = domainObject.save( failOnError: true, flush: flushImmediately )
                    updatedModel = persistSupplementalDataFor( updatedModel )
                }
                else {
                    log.trace "${this.class.simpleName}.update found the model to not be dirty and will not update it"
                    updatedModel = domainObject
                }

                log.trace "${this.class.simpleName}.update will now invoke the postUpdate callback if it exists"
                if (this.respondsTo( 'postUpdate' )) this.postUpdate( [ before: domainModelOrMap, after: updatedModel ] )
                updatedModel

            }
            catch (ApplicationException ae) {
                log.debug "Could not update an existing ${this.class.simpleName} with id = ${domainModelOrMap?.id} due to exception: ${ae.message}", ae
                throw ae
            }
            catch (ValidationException e) {
                def ae = new ApplicationException( getDomainClass(), e )
                log.debug "Could not update an existing ${this.class.simpleName} with id = ${domainObject?.id} due to exception: $ae", e
                checkOptimisticLockIfInvalid( domainObject, content, log ) // optimistic lock trumps validation errors
                throw ae
            }
            catch (e) {
                log.debug "Could not update an existing ${this.class.simpleName} with id = ${domainModelOrMap?.id} due to exception: ${e.message}", e
                throw new ApplicationException(getDomainClass(), e)
            }
        }
    }


    public def createOrUpdate( domainModelOrMap, flushImmediately = true ) {

        log.debug "In ServiceBase.createOrUpdate, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        if (domainModelOrMap instanceof List) {
            log.trace "${this.class.simpleName}.createOrUpdate invoked with a list of size ${domainModelOrMap.size()} -- will iterate list and invoke 'createOrUpdate' on each..."
            List results = []
            domainModelOrMap.each { modelOrMap ->
                results << this.createOrUpdate( modelOrMap, flushImmediately )
            }
            results
        }
        else {
            log.trace "${this.class.simpleName}.createOrUpdate invoked with domainModelOrMap = $domainModelOrMap and flushImmediately = $flushImmediately"
            def content = extractParams( getDomainClass(), domainModelOrMap )
            if (content.id) {
                log.trace "${this.class.simpleName}.createOrUpdate will delegate to 'update'"
                // note: even though we extracted a params map, we'll pass the original so that other information (e.g., a keyBlock)
                // will remain available to any callbacks. If redundant processing incurs too much performance penalty, this
                // will require changes to prevent the redundant processing.
                this.update( domainModelOrMap, flushImmediately )
            }
            else {
                log.trace "${this.class.simpleName}.createOrUpdate will delegate to 'create'"
                // see note above
                this.create( domainModelOrMap, flushImmediately )
            }
        }
    }


    public def delete( domainModelOrMapOrId, flushImmediately = true ) {

        log.debug "In ServiceBase.delete, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        if (domainModelOrMapOrId instanceof List) {
            log.trace "${this.class.simpleName}.delete invoked with a list of size ${domainModelOrMapOrId.size()} -- will iterate list and invoke 'delete' on each..."

            // Since we can't delete while iterating the list containing things being deleted (i.e., concurrent access exception) we'll make them values in a map
            Map toBeDeletedMap = [:]
            domainModelOrMapOrId.eachWithIndex { modelOrMap, i -> toBeDeletedMap << ["$i": modelOrMap] }

            List results = []
            toBeDeletedMap.each { k, v ->
                results << this.delete( v, flushImmediately )
            }
            results
        }
        else {
            log.trace "${this.class.simpleName}.delete invoked with domainModelOrMapOrId = $domainModelOrMapOrId and flushImmediately = $flushImmediately"
            def domainObject
            try {
                log.trace "${this.class.simpleName}.delete will now invoke the preDelete callback if it exists"
                if (this.respondsTo( 'preDelete' )) this.preDelete( domainModelOrMapOrId )

                def id = extractId( getDomainClass(), domainModelOrMapOrId )
                domainObject = fetch( getDomainClass(), id, log )

                removeSupplementalDataFor( domainObject )
                domainObject.delete( failOnError: true, flush: flushImmediately )

                log.trace "${this.class.simpleName}.delete will now invoke the postDelete callback if it exists"
                if (this.respondsTo( 'postDelete' )) this.postDelete( [ before: domainObject, after: null ] )
                true
            }
            catch (ApplicationException ae) {
                log.debug "Could not delete ${this.class.simpleName} with id = ${domainObject?.id} due to exception: $ae", ae
                throw ae
            }
            catch (e) {
                def ae = new ApplicationException( getDomainClass(), e )
                log.debug "Could not delete ${getDomainClass().simpleName} with id = ${domainObject?.id} due to exception: $ae", e
                throw ae
            }
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def read( id ) {

        log.debug "In ServiceBase.read, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        try {
            fetch( getDomainClass(), id, log )
        }
        catch (ApplicationException ae) {
            log.debug "Exception executing ${this.class.simpleName}.read() with id = $id, due to exception: $ae", ae
            throw ae
        }
        catch (e) {
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.read() with id = $id, due to exception: $ae", e
            throw ae
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def get( id ) {

        log.debug "In ServiceBase.get, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        try {
            fetch( getDomainClass(), id, log )
        }
        catch (ApplicationException ae) {
            log.debug "Exception executing ${this.class.simpleName}.get() with id = $id, due to exception: $ae", ae
            throw ae
        }
        catch (e) {
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.get() with id = $id, due to exception: $ae", e
            throw ae
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def list( args ) {

        log.debug "In ServiceBase.list, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        try {
            getDomainClass().list( args )
        }
        catch (e) {
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.list() with args = $args, due to exception: $ae", e
            throw ae
        }
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def count( args = null ) {  // args are ignored -- TODO: Remove from signature

        log.debug "In ServiceBase.count, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        try {
            getDomainClass().count()
        }
        catch (e) {
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.count() due to exception: $ae", e
            throw ae
        }
    }


    public def flush() {
        log.trace "${this.class.simpleName}Service.flush invoked"
        try {
            getDomainClass().withSession { session ->
                session.flush()
            }
        }
        catch (ApplicationException ae) {
            log.debug "Could not save a new ${this.getDomainClass().simpleName} due to exception: $ae", ae
            throw ae
        }
        catch (e) {
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Could not save a new ${this.getDomainClass().simpleName} due to exception: $ae", e
            throw ae
        }
    }


    // ---------------------------- Public Static Helper Methods -------------------------------
    // (public static methods to facilitate use within services that implement their own CRUD methods.)


    public static boolean isDomainModelInstance( domainClass, domainModelOrMap ) {
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
        }
        else if (domainModelOrMap instanceof Map) {
            domainClass.newInstance( extractParams( domainClass, domainModelOrMap ) )
        }
        else {
            throw new ApplicationException( domainClass, "Cannot assign a $domainClass using ${domainModelOrMap}" )
        }
    }


    /**
     * Returns a 'params map' based upon the supplied domainObjectOrMap.
     * The domainObjectOrMap may:
     * 1) be a domain model instance whose properties should be returned,
     * 2) already be a 'params' map that may be returned
     * 3) be a map that contains a model instance as the value for a key whose name is the simple class name but with
     *    lower case first letter (i.e., in 'property name' form)
     * 4) be a map that contains a model instance as the value for a key named 'domainModel'
     * This static method is public so that it may be used within any services that implement their own CRUD methods.
     **/
    public static def extractParams( domainClass, domainObjectOrMap ) {
        if (isDomainModelInstance( domainClass, domainObjectOrMap )) {
            def paramsMap = domainObjectOrMap.properties
            if (domainObjectOrMap.version) {
                paramsMap.version = domainObjectOrMap.version // version is not included in bulk asisgnments
            }
            paramsMap
        }
        else if (domainObjectOrMap instanceof Map) {
            String specificModelKeyName = GrailsNameUtils.getPropertyName( domainClass.simpleName )
            if (domainObjectOrMap."${specificModelKeyName}") {
                extractParams( domainClass, domainObjectOrMap."${specificModelKeyName}" )
            }
            else if (domainObjectOrMap.domainModel) {
                extractParams( domainClass, domainObjectOrMap.domainModel )
            }
            else {
                domainObjectOrMap
            }
        }
        else {
            throw new ApplicationException( domainClass, "Cannot extract a params map supporting $domainClass from: ${domainObjectOrMap}" )
        }
    }


    /**
     * Returns an 'id' extracted from the supplied domainObjectParamsIdOrMap.
     * The domainObjectParamsIdOrMap may:
     * 1) already be a Long 'id' that should be returned,
     * 2) be a 'params' map that contains a key named 'id'
     * 3) be a map that contains a key named with the 'property name' form of the model's simple class
     *    name (e.g., campusParty), whose value is a domain model instance from which the 'id' may be extracted
     * 4) be a map that contains a 'domainModel' key whose value is a domain model instance from which the 'id' may be extracted
     * This static method is public so that it may be used within any services that implement their own CRUD methods.
     **/
    public static def extractId( domainClass, domainObjectParamsIdOrMap ) {
        if (domainObjectParamsIdOrMap instanceof Long) {
            (Long) domainObjectParamsIdOrMap
        }
        else if (domainObjectParamsIdOrMap instanceof String) {
            if (domainObjectParamsIdOrMap.isNumber()) {
                // note: we'll 'assume' we can coerce a number into a long -- given our use of long for IDs, this should not be too risky...
                //       but this note is included here in case there are issues -- note the use of isNumber and toLong
                (Long) domainObjectParamsIdOrMap.toLong()
            }
            else {
                return domainObjectParamsIdOrMap // return as a string
            }
        }
        else if (isDomainModelInstance( domainClass, domainObjectParamsIdOrMap )) {
            extractId( domainClass, domainObjectParamsIdOrMap.id )
        }
        else if (domainObjectParamsIdOrMap instanceof Map) {
            def paramsMap = extractParams( domainClass, domainObjectParamsIdOrMap )
            extractId( domainClass, paramsMap?.id )
        }
        else {
            if (domainObjectParamsIdOrMap.toString().isNumber()) {
                // now we'll try to see if we can use an intermediate coercion (to a string) to extract the id
                extractId( domainClass, domainObjectParamsIdOrMap.toString() )
            }
            else {
                throw new ApplicationException( domainClass, "Could not extract an 'id' from ${domainObjectParamsIdOrMap}" )
            }
        }
    }


    public static def fetch( domainClass, id, log ) {
        log.debug "Going to fetch a $domainClass using id $id"
        if (id == null) {
            throw new NotFoundException( id: id, entityClassName: domainClass.simpleName )
        }

        if (id instanceof String && id.isNumber()) {
            log.debug "Have a String id of '$id', and will convert it to a long"
            id = id.toLong()
        }

        def persistentEntity = domainClass.get( id )
        if (!persistentEntity) {
            throw new ApplicationException( domainClass, new NotFoundException( id: id, entityClassName: domainClass.simpleName ) )
        }
        persistentEntity
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
        if ((content.version != null) && (domainObject.hasProperty( 'version') != null )) {
            domainObject.refresh() // query the database, as a domainObject.get(id) will just hit the cache...
            if (content.version != domainObject.version) {
                log.debug "Optimistic lock violation between params $content and the model's state in the database $domainObject that has version ${domainObject.version}"
                throw new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.name, domainObject.id ) ) )
            }
        }
        else if (domainObject.hasProperty('version')) {
            def ae = new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.simpleName, domainObject.id ) ) )
            log.debug "Could not update an existing ${domainObject.class.simpleName} with id = ${domainObject?.id} and version = ${domainObject?.version} due to Optimistic Lock violation, when given version ${content.version}. Exception is $ae"
            throw ae
        }
        else {
            log.warn "An optimistic lock version was provided when updating ${domainObject?.class.name} with id = ${domainObject?.id}, \
                  but this object doesn't support optimistic locking!"
        }
    }


    // ---------------------------- Helper Methods -----------------------------------


    protected Class getDomainClass() {
        if (!domainClass) {
            String serviceClassName = this.class.name
            String domainClassName = serviceClassName.substring( 0, serviceClassName.indexOf( "Service" ) )
            domainClass = Class.forName( domainClassName, true, Thread.currentThread().getContextClassLoader() )
        }
        domainClass
    }


    protected def getSupplementalDataService() {
        if (!supplementalDataService) {
            // fyi - it's ok if another thread also sneaks in...
            ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
            supplementalDataService = (SupplementalDataService) ctx.getBean( GrailsNameUtils.getPropertyNameRepresentation( SupplementalDataService.class ) )
        }
        supplementalDataService
    }


    protected def persistSupplementalDataFor( modelInstance ) {
        if (getSupplementalDataService().supportsSupplementalProperties( modelInstance.class )) {
            return getSupplementalDataService().persistSupplementalDataFor( modelInstance )
        } else {
            modelInstance
        }
    }


    protected def removeSupplementalDataFor( modelInstance ) {
        if (getSupplementalDataService().supportsSupplementalProperties( modelInstance.class )) {
            getSupplementalDataService().persistSupplementalDataFor( modelInstance )
        }
    }


    private def validateReadOnlyPropertiesNotDirty( domainObject ) {
        def readonlyProperties = GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue( domainObject, 'readonlyProperties' )
        if (readonlyProperties) {
            def dirtyProperties = domainObject.getDirtyPropertyNames()
            def modifiedReadOnlyProperties = dirtyProperties?.findAll { it in readonlyProperties}
            if (modifiedReadOnlyProperties.size() > 0) {
                log.warn "Attempt to modify ${domainObject.class} read-only properties ${modifiedReadOnlyProperties}"
                def cleanNames = modifiedReadOnlyProperties.collect { GrailsNameUtils.getNaturalName( it as String ) }
                throw new RuntimeException( "@@r1:readonlyFieldsCannotBeModified:${cleanNames.join(', ')}@@" )
            }
        }

    }
}
