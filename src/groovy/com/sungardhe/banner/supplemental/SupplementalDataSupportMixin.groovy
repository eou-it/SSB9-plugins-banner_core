/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import javax.persistence.Transient

/**
 * A mixin that provides supplemental data support to models.  Note that while this mixin
 * will store any property that isn't defined on the model, this does not mean this
 * property will be persisted. Persistence of 'supplemental data' is handled by the
 * services, and only those supplemental data properties that are configured within
 * Banner SDE will be persisted.
 * To find 'what' properties are actually supported by a particular model that has not yet
 * been persisted, please use the SupplementalDataService.  Once a model has been persisted,
 * it will ensure only SDE-defined attributes may be populated, but a 'new' model instance
 * will allow any attribute name-value pair (although any attributes not supported by SDE
 * will be 'dropped on the floor'.
 * This mixin provides 'hasSupplementalProperties' method that returns true if additional
 * properties are being held by this mixin (and for 'new' instances, even
 * if those properties are not configured via SDE).
 * Properties that are held in this mixin that are not supported will be dropped on the floor --
 * they will not prevent persistence of the model's supported properties.
 */
class SupplementalDataSupportMixin {

    /**
     * Model instance-specific values for the supplemental data properties.
     * The key is the name of the property and the value is a map, which in
     * turn has a key that is the index and a value that is an instance of
     * SupplementalProperty.  A 'normal' property would thus look like:
     *     [ 'myProperty': [ 1: SupplementalProperty_Instance ] ]
     * A multi-valued property would look like:
     *     [ 'myIndexedProperty': [ 1: SupplementalProperty_Instance1,
     *                              2: SupplementalProperty_Instance2, ] ]
     * If a model instance is created via 'new' and does not yet have
     * supplemental data information, extended attributes may still be populated
     * and this map will (temporarily) contain a map of attribute names to
     * attribute values. Once the model is persisted, the map will be
     * fully populated with SupplementalProperty instances as appropriate.
     * Once a model has been persisted, only those attributes 'supported by SDE'
     * can have their values set.  Any unsupported attributes that were
     * set on a 'new' model instance before it was saved are discarded.
     * Only SDE-configured attributes are persisted.
     */
    @Transient
    Map supplementalDataContent = [:]


    /**
     * Returns true if this model instance has supplemental data.
     * @return boolean true if this model instance has supplemental data
     */
    public boolean hasSupplementalProperties() {
        this.@supplementalDataContent?.size() > 0
    }


    /**
     * Returns true if this model instance has the identified supplemental data property.
     * Optionally, a 'true' may be provided as the second argument which will change behavior
     * such that this method will return true only if the supplemental data property is
     * defined AND if it currently has a value.
     * @param name the name of the supplemental data property
     * @param onlyIfPopulated optional, if 'true' is provided this method will return true only if the identified supplemental data property has a value
     * @return boolean true if this model instance has supplemental data
     */
    public boolean hasSupplementalProperty( String name ) {
        this.@supplementalDataContent.keySet().contains( name )
    }


    /**
     * Returns a List of supplemental property names, regardless of whether there are values for these properties.
     * @return List the list of supplemental data property names
     */
    public List supplementalPropertyNames() {
        return this.@supplementalDataContent?.keySet()?.asList()
    }


    /**
     * Returns a Map of supplemental property name-value pairs, including those that may have null values.
     * @return Map the map of supplemental data property names to property values
     */
    public Map getSupplementalProperties() {
        def nameValues = [:]
        this.@supplementalDataContent?.each { k, v ->
            nameValues << (v instanceof SupplementalProperty ? [ (k): v.value ] : [ (k): v ])
        }
    }


    /**
     * Sets a Map of supplemental property name-value pairs, including those that may have null values.
     * This will replace any previous supplemental data held within the model. It should be used
     * primarily by the framework, not by application code.
     * @param supplementalData the map of supplemental data property names to property values
     */
    public void setSupplementalProperties( Map<String, SupplementalProperty> supplementalData ) {
        this.@supplementalDataContent = supplementalData
    }


    /**
     * Returns true if all supplemental data properties are valid. This will add any validation errors to the
     * Errors object held within the model. If there are no supplemental data properties defined for this model,
     * this method will return true.
     */
    public boolean validateSupplementalDataProperties() {
        return true
    }


    // 'setter' for supplemental data properties.
    // For any property that isn't defined on the model, check to see if it has been configured as a
    // supplemental data property. If it hasn't, then throw a MissingMethodException.
    def propertyMissing( name, value ) {
println "ZZZZZOOOOOOOOOOOOOOOOOOOXXXXXXXXXXXXXX mixin setter will set property $name with value $value"
        // if the model already holds SDE-configured properties
        this.@supplementalDataContent.values().each { println "XXXXXXXXXXXXXXXXXXXXXXX value: $it" }
        if (this.@supplementalDataContent.values()?.any { it instanceof SupplementalProperty } ) {

printn "XXXXXXXXXXXXXXX model already HAS supp data"
            if (this.@supplementalDataContent.containsKey( name ))  {
                def currentValues = this.@supplementalDataContent."$name"

                if (currentValues instanceof Map && currentValue?.values() )

println "XXXXXXXXXXXXXX setting value to the predefined property"
                this.@supplementalDataContent."$name".value = value
            } else {
println "XXXXXXXXXXXXXX model does not have property $name but has ${this.@supplementalDataContent.entrySet()*.toString()}"
                throw new MissingPropertyException( name, String ) // we'll assume String when reporting missing exceptions
            }
        } else {
            // model does not hold SDE-configured properties, so we'll use the map to hold the new property value
            // temporarily until we try to save the model (at which time we'll know if this is a 'valid' SDE-configured property)
            this.@supplementalDataContent."$name" = value
        }
    }


    // 'getter' for supplemental data properties.
    // For any property that isn't defined on the model, check to see if it has been configured as a
    // supplemental data property. If it hasn't, then throw a MissingMethodException.
    def propertyMissing( name ) {
        if (this.@supplementalDataContent.containsKey( name ))  {
            this.@supplementalDataContent."$name"
        } else {
            throw new MissingPropertyException( name, String ) // we'll assume String when reporting missing exceptions
        }
    }

}
