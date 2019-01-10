/* *****************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.utility

import grails.util.Holders

class BindingUtility {
    /**
     * A convenience method that copies a given map of properties to a domain object.
     * By default, the properties to be bound are limited to the persistent properties of the entity.
     * Optionally, you may specify a list of properties to be included and/or a list of properties to be excluded.
     *
     * @param domainObject the domain object that will be updated
     * @param propertyMap the data to be bound to the domain object
     * @param bindOptions an optional map containing an include list and/or exclude list of property names to be bound
     * @return the domain object with updated properties
     */
    public static bind(domainObject, Map propertyMap, Map bindOptions = [:]) {
        def entity = Holders.getGrailsApplication().getMappingContext().getPersistentEntity(domainObject.class.name)
        def propertyNames = bindOptions?.include ?: entity.getPersistentPropertyNames()
        def excludeList = bindOptions?.exclude ?: []

        if (excludeList) {
            propertyNames.removeAll(excludeList as Object[])
        }

        propertyNames.each { it ->
            if (propertyMap.containsKey(it)) {
                domainObject[it] = propertyMap[it]
            }
        }

        return domainObject
    }
}
