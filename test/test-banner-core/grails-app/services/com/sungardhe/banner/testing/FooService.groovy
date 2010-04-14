/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

// NOTE:
// This service is injected with create, update, and delete methods.
// These injected CRUD methods may throw a runtime ApplicationException that should
// be caught (e.g., by a controller using this service). In addition to handling
// ApplicationException, controllers should also catch Exception to ensure a desirable
// response is still provided to the user. Exceptions other than ApplicationException 
// are likely programming errors (as they were not wrapped in an ApplicationException).

// Please review ApplicationException and it's integration test for details on handling
// exceptions. The FooController in this project illustrates use of ApplicationException 
// functionality.   

/**
 * A transactional service supporting persistence of the College model. 
 **/
class FooService {

    boolean transactional = true

    static defaultCrudMethods = true
}
