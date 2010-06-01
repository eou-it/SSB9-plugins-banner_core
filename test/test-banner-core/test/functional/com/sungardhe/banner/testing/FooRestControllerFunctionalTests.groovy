/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.testing.BaseFunctionalTestCase

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import grails.converters.JSON

import groovy.sql.Sql


/**
 * Functional tests verifying the RESTful controller for Foo.
 */
class FooRestControllerFunctionalTests extends BaseFunctionalTestCase {

 
   protected void setUp() {
       formContext = [ 'STVCOLL' ]
       super.setUp()
       
       login()        
   }
   
   
   void testRestJsonApiForList() {

       def pageSize = 5
       get( "/api/foo?max=$pageSize" ) {
           headers[ 'Content-Type' ] = 'application/json'
           headers[ 'Authorization' ] = authHeader()
       }

       assertStatus 200
       assertEquals 'application/json', page?.webResponse?.contentType
       def stringContent = page?.webResponse?.contentAsString
       def data = JSON.parse( stringContent )
       assertTrue 45 <= data.totalCount
       assertEquals 5, data.data.size()
   }
   
}
