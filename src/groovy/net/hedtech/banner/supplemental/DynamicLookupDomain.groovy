/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.supplemental

/**
 * Generic class to hold LOV data for the validation
 * tables that does not have a domain created for.
 */
class DynamicLookupDomain {

    def storage = [:]

    String codeTitle = "Code"
    String descTitle = "Description"

    def propertyMissing(String name, value) {
        storage[name] = value
    }

    def propertyMissing(String name) {
        storage[name]
    }

}
