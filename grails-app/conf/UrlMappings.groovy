/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
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
        "/login/error" {
            controller = "login"
            action = "error"
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

        "/logout/customLogout" {
            controller = "logout"
            action = "customLogout"
        }
	}

}
