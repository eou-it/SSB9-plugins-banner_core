package com.sungardhe.banner.general.utility

import com.sungardhe.banner.testing.BaseIntegrationTestCase

class MultiAppUserSessionIntegrationTests extends BaseIntegrationTestCase {

    def multiAppUserSessionService

    protected void setUp() {
		formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
	}

    protected void tearDown() {
        super.tearDown()
    }

    /**
     * Before switching the context, the app persists all the
     * information to be shared.
     */
    void testPersistMultiAppUserSession() {
        multiAppUserSessionService.save (
                "User1",
                [
                        "goto.currently.opened" : "basicCourseInformation, courseDetailInformation" ,
                        "goto.recently.opened" : "courseSearch",
                        "banner.globals.global.subject.label" : "ACCT"
                ])

        def userSessionInfo = multiAppUserSessionService.findByUserName ('User1')

        assertEquals 3L, userSessionInfo.size()

        assertEquals "User1", userSessionInfo[0].userName
        assertEquals "goto.currently.opened", userSessionInfo[0].infoType
        assertEquals "basicCourseInformation, courseDetailInformation", userSessionInfo[0].info

        assertEquals "User1", userSessionInfo[1].userName
        assertEquals "goto.recently.opened", userSessionInfo[1].infoType
        assertEquals "courseSearch", userSessionInfo[1].info

        assertEquals "User1", userSessionInfo[2].userName
        assertEquals "banner.globals.global.subject.label", userSessionInfo[2].infoType
        assertEquals "ACCT", userSessionInfo[2].info

        multiAppUserSessionService.delete ('User1')

        userSessionInfo = multiAppUserSessionService.findByUserName ('User1')
        assertEquals 0L, userSessionInfo.size()

    }


}
