/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.exceptions


/**
 * A runtime exception thrown when an entity cannot be found by it's id.  Note
 * this method should ONLY be thrown when it is considered an error -- that is, 
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