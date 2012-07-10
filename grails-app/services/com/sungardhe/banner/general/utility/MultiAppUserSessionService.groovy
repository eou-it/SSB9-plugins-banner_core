package com.sungardhe.banner.general.utility

import com.sungardhe.banner.general.utility.MultiAppUserSession
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Cross-app Shared Info Service.
 */
class MultiAppUserSessionService {

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
            if (isNull(info)) {
                deleteInfoType(userName, infoType)
                log.info(infoType + ": NULL VALUE to share:- the obsolete value for the info-type would be removed from DB")
            } else {
                def multiAppUserSession = new MultiAppUserSession(
                        userName: userName,
                        infoType: infoType,
                        info: info
                )
                multiAppUserSession.save( failOnError: true)
            }
        }
    }

    private boolean isNull (info){
        (info == null || (info instanceof String && info == ""))
    }

    def deleteInfoType (userName, infoType) {
        this.findByUserNameAndInfoType(userName, infoType).each { MultiAppUserSession multiAppUserSession ->
            multiAppUserSession.delete( failOnError: true, flush: true )
        }
    }

    def delete (userName) {
        this.findByUserName(userName).each { MultiAppUserSession multiAppUserSession ->
            multiAppUserSession.delete( failOnError: true, flush: true )
        }
    }

    def findByUserName (userName) {
        MultiAppUserSession.findAllByUserName (userName)
    }

    def findByUserNameAndInfoType (userName, infoType) {
        MultiAppUserSession.findAllByUserNameAndInfoType (userName, infoType)
    }
}
