/* ****************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.exceptions

/**
 * A runtime exception used to hold validation errors on business logic.
 * The errors are held within a list, where each entry is a simple map
 **/
class BusinessLogicValidationException extends RuntimeException {

    String messageCode
    List messageArgs = new ArrayList()
    List errorProperties = []


    public BusinessLogicValidationException(message, List messageArgs) {
        this.messageCode = message
        this.messageArgs = messageArgs
    }


    public BusinessLogicValidationException(String message, List messageArgs, List errorProperties) {
        this.messageCode = message
        this.messageArgs = messageArgs
        this.errorProperties = errorProperties
    }


    public String getMessage() {
        messageCode
    }

}
