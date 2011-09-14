/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
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