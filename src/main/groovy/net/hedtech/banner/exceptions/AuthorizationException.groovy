/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.exceptions;

import org.springframework.security.authentication.AccountStatusException;

/**
 * Class for authorization exceptions which are caused by a particular
 * user account not having any authorities.
 */
public class AuthorizationException extends AccountStatusException {


    //~ Constructors ===================================================================================================

    public AuthorizationException(String msg) {
        super(msg);
    }

    public AuthorizationException(String msg, Object extraInformation) {
        super(msg, extraInformation);
    }

    public AuthorizationException(String msg, Throwable t) {
        super(msg, t);
    }
}

