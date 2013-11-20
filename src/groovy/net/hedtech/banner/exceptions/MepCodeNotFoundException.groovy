/* ****************************************************************************
Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.exceptions


/**
 * A runtime exception indicating an entity or resource was not found.
 **/
class MepCodeNotFoundException extends RuntimeException {

   def mepCode

    public String getMessage() {
        "MepCodeNotFoundException:[mepCode=$mepCode]"
    }

    String toString() {
        getMessage()
    }

}

