package com.sungardhe.banner

import com.sungardhe.banner.general.utility.MultiAppUserSession

/**
 * Cross-app Shared Info Service.
 */
class MultiAppUserSessionService {

    public static final String MULTI_APP_USER_SESSION = "multi.app.user.session."


    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring

    /**
     * Before saving, it needs to delete
     * the existing data for this app belong
     * this user.
     *
     * @param domains
     * @return
     */
    def save(userName, Map<String, Object> infoToPersist) {
        //TODO revisit. instead of delete, should find&update?
        delete (userName)

        //TODO is there GORM batch save ? or flush=false would do batch operation automatically?
        infoToPersist?.each { infoType, info ->
            def crossAppSharedInfo = new MultiAppUserSession(userName: userName,infoType: MULTI_APP_USER_SESSION+infoType, info: info)
            crossAppSharedInfo.save( failOnError: true)
        }
    }

    def delete (userName) {
        MultiAppUserSession.executeUpdate("delete MultiAppUserSession c where c.userName = :userName", [userName:userName])
    }

    def findByUserName (userName) {
        MultiAppUserSession.findAllByUserName (userName)?.collect {
            it.infoType = it.infoType?.replaceFirst(MULTI_APP_USER_SESSION, "")
            return it
        }
    }
}
