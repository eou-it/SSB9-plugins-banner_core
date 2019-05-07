/* ****************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.exceptions


/**
 * A runtime exception thrown when an entity cannot be found by it's id.
 * This method should ONLY be thrown when it is considered an error -- that is,
 * it should never be thrown when 'just trying to find' an entity given an id.
 **/
class NotFoundException extends RuntimeException {

    def    entityClassName
    def    id

    public String getMessage() {
        "NotFoundException:[id=$id, entityClassName=$entityClassName]"
    }

    String toString() {
        getMessage()
    }

}
