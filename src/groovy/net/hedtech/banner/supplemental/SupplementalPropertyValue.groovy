/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.supplemental

/**
 * A class that represents the value of a supplemental property within a model.
 */
class SupplementalPropertyValue extends HashMap<Integer, SupplementalPropertyDiscriminatorContent> {

    def isDirty = false


    public def getIsDirty() {
        // if the property itself hasn't been marked dirty, check to see if any of the
        // discriminator values is dirty
        this.@isDirty ? true : this.values().any { it.isDirty }
    }


    boolean equals( o ) {
        if (this.is( o )) return true
        if (!(o instanceof SupplementalPropertyValue)) return false

        SupplementalPropertyValue that = (SupplementalPropertyValue) o
        if (this.size() != that.size()) return false

        this.each { k, v ->
            if (!that.containsKey( k) ) return false
            if (that[k] != this[k])     return false
        }
        return true
    }


    int hashCode() {
        int result = super.hashCode()
        result = 31 * result + (isDirty != null ? isDirty.hashCode() : 0)
        return result
    }


    public String toString() {
        super.toString() + "\n${this.entrySet().flatten()}"
    }

}


class SupplementalPropertyDiscriminatorContent {

    def isDirty = false

    def required = false
    def value
    def disc = 1
    def pkParentTab
    def id
    def dataType = String
    def prompt = ""
    def discType
    def validation
    def dataLength
    def dataScale
    def attrInfo
    def attrOrder
    def discMethod


    boolean equals( o ) {
        if (this.is( o )) return true
        if (!(o instanceof SupplementalPropertyDiscriminatorContent)) return false

        SupplementalPropertyDiscriminatorContent that = (SupplementalPropertyDiscriminatorContent) o

        if (dataType != that.dataType) return false
        if (disc != that.disc) return false
        if (id != that.id) return false
        if (pkParentTab != that.pkParentTab) return false
        if (prompt != that.prompt) return false
        if (required != that.required) return false
        if (value != that.value) return false
        if (discType != that.discType) return false
        if (discMethod != that.discMethod) return false
        if (validation != that.validation) return false
        if (dataLength != that.dataLength) return false
        if (dataScale != that.dataScale) return false
        if (attrInfo != that.attrInfo) return false
        if (attrOrder != that.attrOrder) return false
        return true
    }


    int hashCode() {
        int result
        result = (required != null ? required.hashCode() : 0)
        result = 31 * result + (value != null ? value.hashCode() : 0)
        result = 31 * result + (disc != null ? disc.hashCode() : 0)
        result = 31 * result + (pkParentTab != null ? pkParentTab.hashCode() : 0)
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0)
        result = 31 * result + (prompt != null ? prompt.hashCode() : 0)
        result = 31 * result + (discType != null ? discType.hashCode() : 0)
        result = 31 * result + (discMethod != null ? discMethod.hashCode() : 0)
        result = 31 * result + (validation != null ? validation.hashCode() : 0)
        result = 31 * result + (dataLength != null ? dataLength.hashCode() : 0)
        result = 31 * result + (dataScale != null ? dataScale.hashCode() : 0)
        result = 31 * result + (attrInfo != null ? attrInfo.hashCode() : 0)
        result = 31 * result + (attrOrder != null ? attrOrder.hashCode() : 0)
        return result
    }


    public String toString() {
        super.toString() + "disc=$disc, value=$value, id=$id, required=$required, dataType=$dataType, pkParentTab=$pkParentTab, prompt=$prompt, discType=$discType, discMethod=$discMethod, validation=$validation, dataLength=$dataLength, dataScale=$dataScale, attrInfo=$attrInfo, attrOrder=$attrOrder"
    }
}
