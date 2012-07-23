package net.hedtech.banner.general.utility

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
    def save(seamlessToken, Map<String, Object> infoToPersist) {
        delete (seamlessToken)

        infoToPersist?.each { infoType, info ->
            if (isNull(info)) {
                //not required, because it starts with a fresh db, so the obsolete data of the fresh null key wont be existing
//                deleteInfoType(seamlessToken, infoType)
                log.info(infoType + ": NULL VALUE to share:- the obsolete value for the info-type would be removed from DB")
            } else {
                def multiAppUserSession = new MultiAppUserSession(
                        seamlessToken: seamlessToken,
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

    def deleteInfoType (seamlessToken, infoType) {
        this.findBySeamlessTokenAndInfoType(seamlessToken, infoType).each {
            it.delete( failOnError: true, flush: true )
        }
    }

    def delete (seamlessToken) {
        this.findAllBySeamlessToken(seamlessToken).each {
            it.delete( failOnError: true, flush: true )
        }
    }

    def findAllBySeamlessToken(seamlessToken) {
        MultiAppUserSession.findAllBySeamlessToken (seamlessToken)
    }

    def findBySeamlessTokenAndInfoType (seamlessToken, infoType) {
        MultiAppUserSession.findAllBySeamlessTokenAndInfoType (seamlessToken, infoType)
    }
}
