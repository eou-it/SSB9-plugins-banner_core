/* *****************************************************************************
 Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service


import grails.validation.ValidationException
import grails.util.GrailsNameUtils
import groovy.util.logging.Slf4j
import org.grails.core.DefaultGrailsDomainClass
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.web.converters.ConverterUtil
import org.hibernate.StaleObjectStateException

import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.exceptions.MepCodeNotFoundException
import net.hedtech.banner.security.FormContext

import grails.util.Holders
import grails.util.GrailsClassUtils
import org.hibernate.StaleObjectStateException

import org.springframework.context.ApplicationContext
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException as OptimisticLockException
import grails.gorm.transactions.Transactional
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.transaction.support.DefaultTransactionStatus
import java.beans.BeanInfo
import java.beans.Introspector
import java.beans.PropertyDescriptor
import java.lang.reflect.Method

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
@Slf4j
@Transactional(readOnly = false, propagation = Propagation.REQUIRED )
class ServiceBase {


    Class domainClass // if not explicitly set by a subclass, this will be determined when needed

    def sessionFactory // injected by Spring
    def dataSource     // injected by Spring


    /**
     * Creates model instances provided within the supplied domainModelsOrMaps list.
     **/
    public def create( List domainModelsOrMaps, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.create(List domainModelsOrMaps) invoked with a list of size ${domainModelsOrMaps.size()} -- will iterate list and invoke 'create' on each..."

        List results = []
        domainModelsOrMaps.each { modelOrMap ->
            results << this.create( modelOrMap, flushImmediately )
        }
        results
    }


    /**
     * Creates a model instance within the persistent store, based on the supplied domainModelOrMap.
     * The supplied domainModelOrMap may be:
     * 1) a 'params' map containing model properties for the new model instance
     * 2) a map containing a key named as the property-style simple class name of the model, whose value is a new model instance
     * 3) a map containing a key named 'domainModel', whose value is a new model instance
     * 4) a new domain model instance
     **/
    public def create( domainModelOrMap, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.create invoked with domainModelOrMap = $domainModelOrMap and flushImmediately = $flushImmediately"
        log.trace "${this.class.simpleName}.create transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        setDbmsApplicationInfo "${this.class.simpleName}.create()"

        try {
            def domainObject = assignOrInstantiate( getDomainClass(), domainModelOrMap )

            domainObject = invokeServicePreCreate( domainModelOrMap, domainObject )

            log.trace "${this.class.simpleName}.create will now save the ${getDomainClass()}"
            def createdModel = domainObject.save( failOnError: true, flush: flushImmediately )

            refreshIfNeeded( createdModel )
            log.trace "${this.class.simpleName}.create will now invoke the postCreate callback if it exists"
            if (this.respondsTo( 'postCreate' )) this.postCreate( [  before: domainModelOrMap, after: createdModel ] )

            createdModel
        }
        catch (ApplicationException ae) {
            log.debug "Could not save a new ${this.class.simpleName} due to exception: $ae", ae
            throw ae
        }
        catch (e) {
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Could not save a new ${this.class.simpleName} due to exception: $ae", e
            throw ae
        } finally {
            clearDbmsApplicationInfo()
        }
    }


    /**
     * Updates model instances provided within the supplied domainModelsOrMaps list.
     **/
    public def update( List domainModelsOrMaps, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.update(List domainModelsOrMaps) invoked with a list of size ${domainModelsOrMaps.size()} -- will iterate list and invoke 'update' on each..."

        List results = []
        domainModelsOrMaps.each { modelOrMap ->
            results << this.update( modelOrMap, flushImmediately )
        }
        results
    }


    /**
     * Updates a model instance provided within the supplied domainModelOrMap.
     * The supplied domainModelOrMap may be:
     * 1) a 'params' map containing model properties for an existing model instance
     * 2) a map containing a key named as the property-style simple class name of the model, whose value is an existing model instance
     * 3) a map containing a key named 'domainModel', whose value is an existing model instance
     * 4) an existing domain model instance
     **/
    public def update( domainModelOrMap, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.update invoked with domainModelOrMap = $domainModelOrMap and flushImmediately = $flushImmediately"
        log.trace "${this.class.simpleName}.update transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"
        setDbmsApplicationInfo "${this.class.simpleName}.update()"

        def content      // we'll extract the domainModelOrMap into a params map of the properties
        def domainObject // we'll fetch the model instance into this, and bulk assign the 'content'
        try {

            content = extractParams( getDomainClass(), domainModelOrMap, log )
            domainObject = fetch( getDomainClass(), content?.id, log )

            // Now we'll set the provided properties (content) onto our pristine domainObject instance -- this may make the model dirty
            updateDomainProperties(domainObject, content)

            def updatedModel
            if (isDirty( domainObject )) {
                log.trace "${this.class.simpleName}.update will update model with dirty properties ${domainObject.getDirtyPropertyNames()?.join(", ")}"

                // Next we'll explicitly check the optimistic lock.  Even though GORM will include the version within the 'where' clause
                // when issuing an update, the 'version' property used with be that from the Hibernate cache (reflecting the persistent state)
                // versus the one set on our domainObject. (This is true even when explicitly assigning the version property.)
                //
                checkOptimisticLock( domainObject, content, log )

                // throw a RuntimeException if any properties identified as 'readonly' within the model are dirty
                validateReadOnlyPropertiesNotDirty( domainObject )

                domainObject = invokeServicePreUpdate( domainModelOrMap, domainObject )

                log.trace "${this.class.simpleName}.update applied updates and will save $domainObject"
                updatedModel = domainObject.save( failOnError: true, flush: flushImmediately )
            }
            else {
                log.trace "${this.class.simpleName}.update found the model to not be dirty and will not update it"
                updatedModel = domainObject
            }

            refreshIfNeeded( updatedModel ) // after we persist everything, including supplemental data...

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
            checkOptimisticLock( domainObject, content, log ) // optimistic lock trumps validation errors
            throw ae
        }
        catch (e) {
            log.debug "Could not update an existing ${this.class.simpleName} with id = ${domainModelOrMap?.id} due to exception: ${e.message}", e
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            throw new ApplicationException( getDomainClass(), e )
        } finally {
            clearDbmsApplicationInfo()
        }
    }

    private void updateDomainProperties(domainObject, content) {

        def d = Holders.getGrailsApplication().getMappingContext().getPersistentEntity( ConverterUtil.trimProxySuffix(getDomainClass().getName()))
        d.getPersistentProperties().each { it ->
            if(content.containsKey(it.name))   {
                domainObject[it.name] = content[it.name]
            }
        }
    }

    /**
     * Creates or updates model instances provided within the supplied domainModelsOrMaps list.
     **/
    public def createOrUpdate( List domainModelsOrMaps, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.createOrUpdate invoked with a list of size ${domainModelsOrMaps.size()} -- will iterate list and invoke 'createOrUpdate' on each..."

        List results = []
        domainModelsOrMaps.each { modelOrMap ->
            results << this.createOrUpdate( modelOrMap, flushImmediately )
        }
        results
    }


    /**
     * Creates or updates a model instance provided within the supplied domainModelOrMap.
     * The supplied domainModelOrMap may be:
     * 1) a 'params' map containing model properties for a new or existing model instance
     * 2) a map containing a key named as the property-style simple class name of the model, whose value is a new or existing model instance
     * 3) a map containing a key named 'domainModel', whose value is a new or existing model instance
     * 4) a new or existing domain model instance
     **/
    public def createOrUpdate( domainModelOrMap, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.createOrUpdate invoked with domainModelOrMap = $domainModelOrMap and flushImmediately = $flushImmediately"
        log.trace "${this.class.simpleName}.createOrUpdate transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"

        def content = extractParams( getDomainClass(), domainModelOrMap, log )
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


    /**
     * Deletes the model instances identified within the supplied domainModelsOrMapsOrIds list.
     **/
    public def delete( List domainModelsOrMapsOrIds, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.delete invoked with a list of size ${domainModelsOrMapsOrIds.size()} -- will iterate list and invoke 'delete' on each..."

        // Since we can't delete while iterating the list containing things being deleted (i.e., concurrent access exception) we'll make them values in a map
        Map toBeDeletedMap = [:]
        domainModelsOrMapsOrIds.eachWithIndex { modelOrMap, i -> toBeDeletedMap << ["$i": modelOrMap] }

        List results = []
        toBeDeletedMap.each { k, v ->
            results << this.delete( v, flushImmediately )
        }
        results
    }


    /**
     * Deletes a model instance from the persistent store, as identified within the supplied domainModelOrMapOrId.
     * The supplied domainModelOrMap may be:
     * 1) a 'params' map containing model properties for a an existing model instance
     * 2) a map containing a key named as the property-style simple class name of the model, whose value is an existing model instance
     * 3) a map containing a key named 'domainModel', whose value is an existing model instance
     * 4) an existing domain model instance
     * 5) a Long representing the id
     * 6) a String representing the id
     **/
    public def delete( domainModelOrMapOrId, flushImmediately = true ) {

        log.debug "${this.class.simpleName}.delete invoked with domainModelOrMapOrId = $domainModelOrMapOrId and flushImmediately = $flushImmediately"
        log.trace "${this.class.simpleName}.delete transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"
        setDbmsApplicationInfo "${this.class.simpleName}.delete()"

        def domainObject
        try {
            log.trace "${this.class.simpleName}.delete will now invoke the preDelete callback if it exists"
            if (this.respondsTo( 'preDelete' )) this.preDelete( domainModelOrMapOrId )

            def id = extractId( getDomainClass(), domainModelOrMapOrId )
            domainObject = fetch( getDomainClass(), id, log )
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
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Could not delete ${getDomainClass().simpleName} with id = ${domainObject?.id} due to exception: $ae", e
            throw ae
        } finally {
            clearDbmsApplicationInfo()
        }
    }


    /**
     * Returns the model instance having the supplied id.
     * Note that the 'get' method is equivalent.
     **/
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def read( id ) {

        log.trace "${this.class.simpleName}.read, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"
        setDbmsApplicationInfo "${this.class.simpleName}.read()"

        try {
            fetch( getDomainClass(), id, log )
        }
        catch (ApplicationException ae) {
            log.debug "Exception executing ${this.class.simpleName}.read() with id = $id, due to exception: $ae", ae
            throw ae
        }
        catch (e) {
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.read() with id = $id, due to exception: $ae", e
            throw ae
        } finally {
            clearDbmsApplicationInfo()
        }
    }


    /**
     * Returns the model instance having the supplied id.
     * Note that the 'read' method is equivalent.
     **/
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def get( id ) {

        log.trace "${this.class.simpleName}.get, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"
        setDbmsApplicationInfo "${this.class.simpleName}.get()"

        try {
            fetch( getDomainClass(), id, log )
        }
        catch (ApplicationException ae) {
            log.debug "Exception executing ${this.class.simpleName}.get() with id = $id, due to exception: $ae", ae
            throw ae
        }
        catch (e) {
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.get() with id = $id, due to exception: $ae", e
            throw ae
        }
        finally {
            clearDbmsApplicationInfo()
        }
    }


    /**
     * Returns a list of the model instances, passing the supplied args to the GORM list() method.
     * It is important to set the 'max' parameter used for paging, particularly if the
     * totalCount is needed (as including 'max' will result in a PageResultList being return,
     * which includes the totalCount).
     **/
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def list( args ) {

        log.trace "${this.class.simpleName}.list, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"
        setDbmsApplicationInfo "${this.class.simpleName}.list()"

        try {
            getDomainClass().list( args )
        }
        catch (e) {
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.list() with args = $args, due to exception: $ae", e
            throw ae
        } finally {
            clearDbmsApplicationInfo()
        }
    }


    /**
     * Returns a count of the domain class.
     * Note: Depending on the transaction demarcation, this may execute
     * in a separate transaction from a preceeding 'list' invocation.
     *
     * When a PagedResultList is returned from 'list' (which occurs when a
     * named parameter of 'max' is included), the totalCount should
     * be retrieved from that versus calling this separate service method.
     *
     * This method should be overriden if the list is filtered and cannot
     * return a PagedResultList, as the default implementation is based on
     * GORM's 'count' method which does not support filtering.
     **/
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS )
    public def count( args = null ) {

        log.trace "${this.class.simpleName}.count, transaction attributes: ${TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()}"
        setDbmsApplicationInfo "${this.class.simpleName}.count()"

        try {
            getDomainClass().count()
        }
        catch (e) {
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Exception executing ${this.class.simpleName}.count() due to exception: $ae", e
            throw ae
        } finally {
            clearDbmsApplicationInfo()
        }
    }


    /**
     * Flushes the hibernate session.
     **/
    public def flush() {

        log.trace "${this.class.simpleName}.flush invoked"
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
            def notFound = extractNestedNotFoundException(e)
            e = notFound ?: e
            def ae = new ApplicationException( getDomainClass(), e )
            log.debug "Could not save a new ${this.getDomainClass().simpleName} due to exception: $ae", e
            throw ae
        }
    }


    // ------------------------------------ Public Helper Methods --------------------------------------


    static public String currentTransactionIdentifier() {
        def attr = TransactionAspectSupport?.currentTransactionInfo()?.getTransactionAttribute()
        DefaultTransactionStatus dts = TransactionAspectSupport?.currentTransactionStatus()
        [ "Transaction<id=${dts?.transaction.connectionHolder.hashCode()}",
          "Attributes=${attr}",
          "isNew?=${dts?.isNewTransaction()}>"
        ].join(", ")
    }


    /**
     * Sets a context variable for use by Banner database APIs.
     * Specifically, this calls the gb_common.p_set_context(?,?,?) procedure.
     **/
    public void setApiContext( String packageName, String contextName, String contextVal ) {

        def sessionFactory = Holders.grailsApplication.getMainContext().sessionFactory
        def sql = new Sql( sessionFactory?.currentSession?.connection() )
        sql.call( "{call gb_common.p_set_context( ?, ?, ?)}", [ packageName, contextName, contextVal ] )
        // note: the connection is managed by hibernate, hence we won't close it here
    }


    /**
     * Determines if the supplied model is dirty. This method provides additional behavior than the normal
     * GORM implementation. Specifically, this method provides a workaround for Hibernate exposing a Timestamp
     * from it's cache...
     * Unfortunately, Hibernate returns a Timestamp versus just a Date. Although there are
     * blogs and Jiras (HB-6810), Hibernate will not address this (as it isn't 'really' a Hibernate problem).
     *
     * Unfortunately, Java violates the 'equals' contract for Date, by using an implemention
     * that is not symmetrical (i.e., 'Timestamp == Date' and 'Date == Timestamp' may return different results).
     *
     * While this should be fixed in either Hibernate, Java, or GORM (TODO: Submit Jira to GORM),
     * this method provides ServiceBase with a 'band-aid' -- go ahead, call this a hack.  It is...
     *
     * This method simply ensures that if only 'Date' properties are identified as being 'Dirty', that
     * we test them in the correct order (specifically, 'persistent value' == 'property value').
     *
     * This method also 'ignores' the 'lastModified' for models where the lastModified is set within the
     * database, as indicated by a 'List requirePostOperationRefreshing = [ ModelClass ]' property in the concrete service.
     * This property identifies the models that are modified inside the database (i.e., by either a trigger or an API),
     * which must therefore be 'refreshed' to attain the modified database values.
     **/
    public boolean isDirty( model ) {

        if (!(model?.isDirty())) return false

        log.trace "Model ${model?.class} with id=${model?.id} has GORM-reported dirty properties:  ${model?.getDirtyPropertyNames()}"
        if (model?.getDirtyPropertyNames()?.size() > 0) {
            // check the list, as some test models always return 'isDirty = true' even when there are no dirty fields
            if (model?.getDirtyPropertyNames()?.size() == model?.getDirtyPropertyNames()?.findAll { model."$it" instanceof Date }?.size()) {
                // All of the dirty property values are of type 'Date'
                log.trace "However, ALL of the dirty properties have instances that are instanceof 'Date' which may have resulted in a 'false positive'"
                def returnValue = false
                model?.getDirtyPropertyNames()?.each {
                    log.trace "Going to check property ${it}"
                    def propValue = model?."$it"
                    def persistentValue = model?.getPersistentValue( "$it" )
                    if ('lastModified' == it && databaseMayAlterPropertiesOf( model )) {
                        // We'll ignore the 'lastModified' property if the database is known to make changes to models (e.g., via triggers).
                        // That is, even though the 'refreshIfNeeded' method called whenever ServiceBase saves a model, we'll protect ourselves
                        // here just in case the model was saved outside of a ServiceBase method.
                        log.trace "Property $it is managed within the database, and will be ignored for the purposes of 'isDirty()"
                    } else {
                        log.trace "Property $it : persistentValue = $persistentValue and propertyValue = $propValue, so isDirty = ${persistentValue == propValue}"
                        if (persistentValue != propValue) {
                            returnValue = true
                            return true // we found one that is really dirty, so can stop now...
                        }
                    }
                }
                if (returnValue) {
                    log.trace "Model ${model?.class} with id=${model?.id} was determined to really be dirty"
                } else {
                    log.trace "All 'Date' properties that were reported as 'dirty' for model ${model?.class} with id=${model?.id} are not really dirty..."
                    model.discard() // so we need to prevent GORM from proceeding with the update
                }
                return returnValue
            }
        }
        true
    }


    /**
     * Returns true if the supplied model is annotated to indicate that the database may alter state.
     **/
    public boolean databaseMayAlterPropertiesOf( model ) {
        model.class.getAnnotation( DatabaseModifiesState.class ) ? true : false
    }


    /**
     * Refreshes the supplied model if that model has the '@DatabaseModifiesState' annatation.
     * Models that are backed by APIs (often indirectly, via a database view with 'instead of' triggers) usually (always?) have their 'activity date'
     * (i.e., lastModified property) modified within the database, and may modify other fields.  This method will refresh the model
     * if it is identified as one that may be modified in the database (specifically, if it is annotated with the 'DatabaseModifiesState' annotation).
     **/
    public refreshIfNeeded( model ) {
        if (databaseMayAlterPropertiesOf( model )) {
            log.debug "Model ${model.class} is identified as a model that may be modified within the database, and will therefore be refreshed"
            model.refresh()
        }
    }


    /**
     * Returns true if the supplied object is found to be assignable from the supplied class.
     * This method is static to facilitate use from services that do not extend or mixin ServiceBase.
     **/
    public static boolean isDomainModelInstance( Class domainClass, object ) {
        (domainClass.isAssignableFrom( object?.getClass() ) && !(Map.isAssignableFrom( object?.getClass() )))
    }


    /**
     * Returns a model instance based upon the supplied domainObjectOrProperties map.
     * The domainObjectOrProperties argument is expected to:
     *   1) be a 'params' map that may be used to create a new model instance
     *   2) be a map that contains a key using the property-style simple class name, whose value is the domain model instance to return
     *   3) be a map that contains a 'domainModel' key whose value is the domain model instance to return
     **/
    public def assignOrInstantiate( domainClass, Map domainObjectOrProperties ) {
        Map content = extractParams(domainClass, domainObjectOrProperties, log)
        def entity = Holders.getGrailsApplication().getMappingContext().getPersistentEntity(ConverterUtil.trimProxySuffix(getDomainClass().getName()))
        def propertyNames = entity.getPersistentProperties().collect{ it.name }
        def properties = content.subMap(propertyNames)
        def domainObject = domainClass.newInstance(properties)
        return domainObject
    }


    /**
     * Returns a model instance based upon the supplied domainModel instance.
     **/
    public def assignOrInstantiate( domainClass, domainModel ) {

        if (isDomainModelInstance( domainClass, domainModel )) {
            domainModel
        } else {
            log.error "${this.class.simpleName}.assignOrInstantiate(domainModel) cannot recognize the supplied ${getDomainClass()} as a domain model: ${domainModel}"
            throw new ApplicationException( getDomainClass(), "@@r1:default.unknown.banner.api.exception@@" )
        }
    }


    /**
     * Returns a 'params map' based upon the supplied Map that contains a model instance or model properties.
     * The domainObjectOrProperties map may contain:
     *   1) a 'params' map alreay, that may be returned with no other action required
     *   2) a model instance as the value for a key whose name is the property-style simple class name (e.g., 'college')
     *   3) a model instance as the value for a key named 'domainModel'
     * This method is static to facilitate use from services that do not extend or mixin ServiceBase.
     **/
    public static def extractParams( domainClass, Map domainObjectOrProperties, log = null ) {
        def model = domainObjectOrProperties."${GrailsNameUtils.getPropertyName( domainClass.simpleName )}" ?: domainObjectOrProperties.domainModel
        model ? extractParams( domainClass, model, log ) : domainObjectOrProperties
    }


    /**
     * Returns a 'params map' based upon the supplied object, that is expected to be a domain model instance.
     * This method is static to facilitate use from services that do not extend or mixin ServiceBase.
     **/
    public static def extractParams( domainClass, domainObject, log = null ) {

        if (isDomainModelInstance( domainClass, domainObject )) {
            /*def paramsMap = domainObject.properties*/
            /*** Replaced above line due to performance issue ***/

            /*** propMap Performance fix start ***/
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            BeanInfo info = Introspector.getBeanInfo(domainObject.getClass());
            for (PropertyDescriptor propertyDescriptor : info.getPropertyDescriptors()) {
                Method reader = propertyDescriptor.getReadMethod();
                if (reader != null)
                    paramsMap.put(propertyDescriptor.getName(),reader.invoke(domainObject));
            }
            /*** propMap performance fix end ***/
            if (domainObject.version) paramsMap.version = domainObject.version // version is not included in bulk asisgnments
            paramsMap
        }
        else {
            log?.error "${this.class.simpleName}.extractParams(domainModel) cannot recognize the supplied $domainClass as a domain model: $domainObject"
            throw new ApplicationException( domainClass, "@@r1:default.unknown.banner.api.exception@@" )
        }
    }


    /**
     * Returns an 'id' based on the supplied input. Since this is a Long, it will simply be returned.
     **/
    public def extractId( domainClass, Long id ) {
        id
    }


    /**
     * Returns an 'id' based on the supplied idString.  The returned id will be converted to a Long if possible,
     * otherwise the supplied string will be returned.
     **/
    public def extractId( domainClass, String idString ) {

        if (idString.isNumber()) {
            // note: we'll 'assume' we can coerce a number into a long -- given our use of long for IDs, this should not be too risky...
            //       but this note is included here in case there are issues -- note the use of isNumber and toLong
            (Long) idString.toLong()
        }
        else {
            return idString // return as a string
        }
    }


    /**
     * Returns an 'id' extracted from the supplied inputMap.
     * The inputMap may contain:
     * 1) a key named 'id', whose value will simply be returned
     * 2) a key named with the 'property name' form of the model's simple class name (e.g., campusParty), whose value is a domain model instance from which the 'id' may be extracted
     * 3) a key named 'domainModel' whose value is a domain model instance from which the 'id' may be extracted
     **/
    public def extractId( domainClass, Map inputMap ) {
        def paramsMap = extractParams( domainClass, inputMap, log )
        extractId( domainClass, paramsMap?.id )
    }


    /**
     * Returns an 'id' based on the supplied inputObject, which is expected to be a domain model instance.
     **/
    public def extractId( domainClass, inputObject ) {

        if (isDomainModelInstance( domainClass, inputObject )) {
            extractId( domainClass, inputObject.id )
        }
        else {
            // now we'll try to see if we can use an intermediate coercion (to a string) to extract the id
            if (inputObject.toString().isNumber()) {
                extractId( domainClass, inputObject.toString() )
            }
            else {
                throw new ApplicationException( domainClass, "Could not extract an 'id' from ${inputObject}" )
            }
        }
    }


    /**
     * Returns a model of the identified domain class that has the supplied 'id'.
     **/
    public def fetch( domainClass, id, log ) {

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
    public def checkOptimisticLock( domainObject, content, log ) {

        if (domainObject.hasProperty( 'version' )) {
            if (content.version != null) {
                int ver = content.version instanceof String ? content.version.toInteger() : content.version
                if (ver != domainObject.version) {
                    throw exceptionForOptimisticLock( domainObject, content, log )
                }
            }
            else {
                log.debug "The params content did not provide a 'version', but the ${domainObject?.class.simpleName} model requires a version to support optimistic locking"
                throw exceptionForOptimisticLock( domainObject, content, log )
            }
        }
        else if (content.version != null) {
            log.warn "An optimistic lock version was provided when updating ${domainObject?.class.simpleName} with id = ${domainObject?.id}, \
                      but this object doesn't support optimistic locking!"
        }
    }


    protected def getBannerConnection() {
        if (!sessionFactory) {
            ApplicationContext ctx = (ApplicationContext) Holders.grailsApplication.getMainContext()
            sessionFactory = ctx.getBean( 'sessionFactory' )
        }
        sessionFactory.getCurrentSession().connection()
    }



    /**
     * Validates the supplied model, and throws a ValidationException if the model is invalid.
     **/
    protected validate( model ) {
        if (model && !model.validate()) {
            throw new ValidationException( "${model.class.simpleName}", model.errors )
        }
    }


    /**
     * Sets the 'domainClass' property if not already populated, using naming conventions.
     * Specifically, this will derive the model class name from this service's class name.
     **/
    public Class getDomainClass() {
        if (!domainClass) {
            String serviceClassName = this.class.name
            String domainClassName = serviceClassName.substring( 0, serviceClassName.indexOf( "Service" ) )
            domainClass = Class.forName( domainClassName, true, Thread.currentThread().getContextClassLoader() )
        }
        domainClass
    }


    private def invokeServicePreCreate( domainModelOrMap, domainObject ) {

        log.trace "${this.class.simpleName}.create will now invoke the preCreate callback if it exists"
        if (this.respondsTo( 'preCreate' )) {
            def preCreateParam
            if (domainModelOrMap instanceof Map && !domainModelOrMap.domainModel) {
                preCreateParam = domainModelOrMap << [ domainModel: domainObject ]
            } else {
                preCreateParam = domainModelOrMap
            }
            this.preCreate( preCreateParam )
            domainObject?.discard() // we'll re-create since the domainModelOrMap may have been modified
            domainObject = assignOrInstantiate( getDomainClass(), domainModelOrMap )
            domainObject
        }
        else {
            domainObject
        }
    }


    private def invokeServicePreUpdate( domainModelOrMap, domainObject ) {

        log.trace "${this.class.simpleName}.update will now invoke the 'preUpdate' callback if it exists"
        if (this.respondsTo( 'preUpdate' )) {
            def preUpdateParam
            if (domainModelOrMap instanceof Map && !domainModelOrMap.domainModel) {
                preUpdateParam = domainModelOrMap << [ domainModel: domainObject ]
            } else {
                preUpdateParam = domainModelOrMap
            }
            this.preUpdate( preUpdateParam )
            updateDomainProperties(domainObject, extractParams( getDomainClass(), domainModelOrMap, log ))
        }
        domainObject
    }


    /**
     * Ensures that dirty properties are not identified as 'readonlyProperties' within the model. If any read only
     * properties are found to be dirty, a runtime exception is thrown.
     **/
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


    private ApplicationException exceptionForOptimisticLock( domainObject, content, log ) {
        log.debug "Optimistic lock violation between params $content and the model's state in the database $domainObject"
        new ApplicationException( domainObject?.class, new OptimisticLockException( new StaleObjectStateException( domainObject.class.simpleName, domainObject.id ) ) )
    }


    /*
     * Extracts a NotFoundException if one is nested, otherwise returns null.
     **/
    public static Throwable extractNestedNotFoundException( Throwable e ) {

        if (e instanceof NotFoundException || e instanceof MepCodeNotFoundException) {
            return e
        }
        else if (e.getCause() != null) {
            return extractNestedNotFoundException( e.getCause() )
        }
        else {
            return null
        }
    }

    private void setDbmsApplicationInfo( action ) {
        if (log.debugEnabled) {
            dataSource?.setDbmsApplicationInfo( getBannerConnection(), FormContext.get() ? FormContext.get()[0] : null, action as String )
        }
    }


    private void clearDbmsApplicationInfo() {
        if (log.debugEnabled) {
            dataSource?.clearDbmsApplicationInfo( getBannerConnection() )
        }
    }

}
