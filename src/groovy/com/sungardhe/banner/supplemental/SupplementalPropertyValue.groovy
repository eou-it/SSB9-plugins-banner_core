/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

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
        return result
    }


    public String toString() {
        super.toString() + "disc=$disc, value=$value, id=$id, required=$required, dataType=$dataType, pkParentTab=$pkParentTab, prompt=$prompt"
    }
}
