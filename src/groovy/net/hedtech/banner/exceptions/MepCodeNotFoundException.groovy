/* ****************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.exceptions

import net.hedtech.banner.i18n.LocalizeUtil

/**
 * A runtime exception indicating an entity or resource was not found.
 **/
class MepCodeNotFoundException extends RuntimeException {

    public static final String NO_MEP_CODE_PROVIDED = "NO_MEP_CODE_PROVIDED"
    public static final String MESSAGE_KEY_MEPCODE_NOT_FOUND = "mepcode.not.found.message"
    public static final String MESSAGE_KEY_MEPCODE_INVALID = "mepcode.invalid.message"
    def mepCode

    public String getMessage() {
        if (!mepCode || mepCode == NO_MEP_CODE_PROVIDED) {
            LocalizeUtil.message(MESSAGE_KEY_MEPCODE_NOT_FOUND)
        } else {
            LocalizeUtil.message(MESSAGE_KEY_MEPCODE_INVALID, [mepCode]?.toArray() )
        }
    }

    String toString() {
        getMessage()
    }

}

