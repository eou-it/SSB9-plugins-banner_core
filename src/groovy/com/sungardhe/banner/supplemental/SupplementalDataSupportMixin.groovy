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
import org.apache.log4j.Logger

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


    @Lazy // note: Lazy needed here to ensure 'this' refers to the service we're mixed into (if we're mixed in)
    def log = Logger.getLogger( this.class )

    /**
     * Model instance-specific values for the supplemental data properties.
     * The key is the name of the property and the value is a SupplementalPropertyValue,
     * which is in turn a statically typed map.  A property would thus look like:
     *     [ 'myProperty': SupplementalPropertyValue_instance ]
     * For a single-valued property, the SupplementalPropertyValue would look like:
     *     [ 1: SupplementalPropertyDiscriminatorContent_instance ]
     * and for a multi-valued property the SupplementalPropertyValue would look like:
     *     [ 1: SupplementalPropertyDiscriminatorContent_instance1,
     *       2: SupplementalPropertyDiscriminatorContent_instance2 ]
     * where the key is the discriminator value.
     * Only SDE-configured attributes are persisted.
     */
    @Transient
    Map<String,SupplementalPropertyValue> supplementalDataContent = new HashMap()


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
    public List getSupplementalPropertyNames() {
        return this.@supplementalDataContent?.keySet()?.asList()
    }


    @Deprecated // use myModel.getsupplementalPropertyNames() or direct access via myModel.supplementalPropertyNames
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
            nameValues << (v instanceof SupplementalPropertyValue ? [ (k): v.value ] : [ (k): v ])
        }
    }


    /**
     * Sets a Map of supplemental property name-value pairs, including those that may have null values.
     * This will replace any previous supplemental data held within the model. It should be used
     * primarily by the framework, not by application code.
     * @param supplementalData the map of supplemental data property names to property values
     */
    public void setSupplementalProperties( Map<String, SupplementalPropertyValue> supplementalData, boolean setAsDirty = true ) {
        this.@supplementalDataContent = supplementalData
        if (setAsDirty) {
            this.@supplementalDataContent.values()?.each { supplementalPropertyValue ->
                supplementalPropertyValue.values()?.each { discriminatorValue -> discriminatorValue.isDirty = true }
            }
        }
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
        // We'll only support setting of pre-configured supplemental data properties for an existing
        // model instance that was loaded from the database.  If the model is newly instantiated
        // (and not loaded from the database), the setter for any supplemental property will throw a
        // MissingPropertyException.
        if (!this.@id || !this.@supplementalDataContent.containsKey( name )) {
            throw new MissingPropertyException( name, this.class )
        }

        switch (value) {
            case SupplementalPropertyValue: // may have many discriminated values; will replace the property value entirely
                this.@supplementalDataContent."$name" = value
                this.@supplementalDataContent."$name"?.values()?.each { it.isDirty = true }
                break
            case SupplementalPropertyDiscriminatorContent: // will add or replace only the specific discriminated value
                value.isDirty = true
                this.@supplementalDataContent."$name"["${value.disc}"] = value
                break
            default: throw IllegalArgumentException( "Supplemental data must be of type SupplementalPropertyValue or SupplementalPropertyDiscriminatorContent" )
        }
    }


    // 'getter' for supplemental data properties.
    // For any property that isn't defined on the model, check to see if it has been configured as a
    // supplemental data property. If it hasn't, then throw a MissingMethodException.
    def propertyMissing( name ) {
        if (this.@supplementalDataContent.containsKey( name ))  {
            this.@supplementalDataContent."$name"
        } else {
            throw new MissingPropertyException( name, this.class )
        }
    }

}
