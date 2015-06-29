package net.hedtech.banner.testing

class CommonMenuControllerFunctionalTests extends BaseFunctionalTestCase {


    protected void setUp() {
        formContext = [ 'GUAGMNU' ]
        super.setUp()
    }

    void testCommonMenuData_JSON() {
        login()
        get( "/commonMenu" ) {
            headers[ 'Content-Type' ] = 'application/json'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertEquals 3, data.data.size()
        assertEquals 'root', data.data.name
        assertEquals 'root', data.data.caption
        assertEquals 3, data.data.items.size()
        assertEquals 'Banner', data.data.items[0].name
        assertEquals 'My Banner', data.data.items[1].name
        assertEquals 'Banner-SelfService', data.data.items[2].name

    }

    void testCommonMenuBannerData_JSON() {
        login()
        get( "/commonMenu?type=Banner&menu=Banner&caption=Banner" ) {
            headers[ 'Content-Type' ] = 'application/json'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertEquals 3, data.data.size()
        assertEquals 'Banner', data.data.name
        assertEquals 'Banner', data.data.caption
    }

    void testCommonMenuSelfServiceData_JSON() {
        login()
        get( "/commonMenu?type=Banner-SelfService&menu=Banner-SelfService&caption=Banner-SelfService" ) {
            headers[ 'Content-Type' ] = 'application/json'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertEquals 3, data.data.size()
        assertEquals 'Banner-SelfService', data.data.name
        assertEquals 'Banner-SelfService', data.data.caption
    }

    void testCommonMenuSearchData_JSON() {
        login()
        get( "/commonMenu?q=Stu" ) {
            headers[ 'Content-Type' ] = 'application/json'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertEquals 3, data.data.size()
        assertEquals 'root', data.data.name
        assertEquals 'root', data.data.caption
    }
}