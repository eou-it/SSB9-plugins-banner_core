/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/
package net.hedtech.banner.restfulapi

import net.hedtech.restfulapi.RestfulServiceAdapter


/**
 * An service adapter implementation for use with the 'restful-api' plugin.
 * The RESTful API Grails Plugin delegates to transactional services,
 * however uses a slightly different contract with services than that
 * used within Banner XE (and specifically exposed by ServiceBase).
 * If this adapter is registered in Spring IoC (e.g., in resources.groovy)
 * it will be used by the RestfulApiController when delegating to services.
 **/
class RestfulApiServiceBaseAdapter implements RestfulServiceAdapter {


    /**
     * Returns a list of domain object instances satisfying the params.
     * It is particularly important to include a 'max' params property
     * for paging, as this will result in a PagedResultList being returned.
     * The PagedResultList will include a totalCount, which will be used
     * by the RestfulApiController versus invoking the 'count' method below.
     **/
    def list(def service, Map params) {
        service.list(params)
    }

    /**
     * Returns a count of the domain class.
     * Note that, depending on your transaction demarcation, this may
     * execute within a separate transaction from the 'list' invocation.
     * This method is exposed to handle cases where the 'list' results
     * do not provide the total count. Note the some implementations
     * of 'list' (e.g., using Criteria) will return a PagedResultList.
     * When a PagedResultList is returned, the RestfulApiController will
     * use that (which contains the total count) versus calling this method.
     **/
    def count(def service, Map params) {
        if (service.metaClass.respondsTo(service, "count", Map)) {
            service.count(params)
        } else {
            service.count()
        }
    }

    /**
     * Returns the domain object instance identified by 'params.id'.
     * Note: This 'show(params)' method will delegate to the service's
     * 'get(id) method.
     **/
    def show(def service, Map params) {
        service.get(params.id)
    }

    /**
     * Creates a new instance of the domain object.
     **/
    def create(def service, Map content, Map params) {
        service.create(content)
    }

    /**
     * Updates an existing domain object instance.
     * Note: When this method delegates to ServiceBase's 'update' method,
     * it only passes the 'content' map.
     **/
    def update(def service, def id, Map content, Map params) {
        if (!content.id) content.id = id
        service.update(content)
    }

    /**
     * Deletes an existing domain object instance.
     * Note: When this method delegates to ServiceBase's 'delete' method,
     * it only passes the 'content' map.
     **/
    void delete(def service, def id, Map content, Map params) {
        if (!content.id) content.id = id
        service.delete(content)
    }

}
