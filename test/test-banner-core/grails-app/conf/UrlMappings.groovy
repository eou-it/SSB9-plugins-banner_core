class UrlMappings {
    
    
    static mappings = {
        
  	  
        // ----------------------- API End Points -----------------------
      
        "/api/foo"( controller: "fooRestful" ) {
            action = [ GET: "list", POST: "save" ]
        }
        
        "/api/foo/$id"( controller: "fooRestful" ) {
            action = [ GET: "show", PUT: "update", DELETE: "delete" ]
        }
        
        // The following is 'normal' configuration, but since we are testing the framework we'll be more explicit
        // and map to specific controllers
/*        
        "/api/$controller" { // note we don't use ( parseRequest:true ) as we'll parse manually via 'request.XML'
            action = [ GET: "list", POST: "create" ]
        }
        
        "/api/$controller/$id" { // note we don't use ( parseRequest:true ) as we'll parse manually via 'request.XML'
            action = [ GET: "show", PUT: "update", DELETE: "remove" ]
        }
        
        // -------------------- Default and Errors ---------------------
        
        "/$controller/$action?/$id?" {
            constraints {
  			   // apply constraints here
  		    }
        }
        
        "/"( view: "/index" )
*/
        "500"( view: '/error' )
	}
}
