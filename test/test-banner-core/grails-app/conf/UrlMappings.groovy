class UrlMappings {


    static mappings = {


        // ----------------------- API End Points -----------------------
        // note we don't use ( parseRequest:true ) for APIs as we'll parse manually via 'request.XML' and 'request.JSON'


        // The following entries are needed since foobar does not correspond to a controller

        "/api/foobar"( controller: "foo" ) {
            action = [ GET: "list", POST: "create" ]
        }

        "/api/foobar/$id"( controller: "foo" ) {
            action = [ GET: "show", PUT: "update", DELETE: "destroy" ]
        }

        // The following are 'normal' mappings, for when the controller can be determined by the URI

        "/api/$controller" {
            action = [ GET: "list", POST: "create" ]
        }

        "/api/$controller/$id" {
            action = [ GET: "show", PUT: "update", DELETE: "destroy" ]
        }


        // The following tests accessing a resource while already authenticated in a session.  I.e. doesn't require basic auth.
        "/resource/$controller" {
            action = [ GET: "list", POST: "create" ]
        }

        "/resource/$controller/batch" {
            action = [ POST: "processBatch" ]
        }

        "/resource/$controller/$id?" {
            action = [ GET: "show", PUT: "update", DELETE: "destroy" ]
            constraints {
                id(matches:/[0-9]+/)
            }
        }
        
        // --------------------- Self Service End Points --------------------        

        "/ssb/foobar/$action?/$id?"( controller: "foo" ) { }        

        
        "/ssb/$controller/$action?/$id?" { }        


        // --------------------- Non-API End Points --------------------

        // This entry only needed because foobar does not correspond to a controller
        "/foobar/$action?/$id?"( controller: "foo" ) {
         }

        // The following is normal configuration, where the controller can be determined from the URI
        "/$controller/$action?/$id?" {
        }


        // -------------------- Default and Errors ---------------------

        "/"( view: "/index" )
        "500"( view: '/error' )
	}
}
