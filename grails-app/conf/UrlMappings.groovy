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
class UrlMappings {
    static mappings = {
      "/$controller/$action?/$id?"{
	      constraints {
			 // apply constraints here
		  }
	  }
      "/"(view:"/index")
	  "500"(view:'/error')

        "/login/resetPassword" {
             controller = "login"
             action = "forgotpassword"
        }
        "/resetPassword/validateans" {
            controller = "resetPassword"
            action = "validateAnswer"
        }
        "/resetPassword/resetpin" {
            controller = "resetPassword"
            action = "resetPin"
        }
        "/resetPassword/auth" {
            controller = "login"
            action = "auth"
        }
        "/resetPassword/recovery" {
             controller = "resetPassword"
             action = "recovery"
        }
        "/resetPassword/validateCode" {
             controller = "resetPassword"
             action = "validateCode"
        }
        "/resetPassword/login/auth" {
            controller = "login"
            action = "auth"
        }

        "/resetPassword/logout/timeout" {
            controller = "logout"
            action = "timeout"
        }

	}

}
