import net.hedtech.banner.security.BannerUser
import org.springframework.security.core.context.SecurityContextHolder

class UserAgreementController {

    def userAgreementService

    static defaultAction = "index"
    def index() {
        def infoText = userAgreementService.getTermsOfUseInfoText()
        def model = [infoText:infoText]
        render view: "policy", model: model
    }

    def agreement() {
        request.getSession().setAttribute("useraggrementdone", "true")
        String pidm = getPidm()
        //WebTailorUtility.updateUsageIndicator(pidm,"Y")
        userAgreementService.updateUsageIndicator(pidm,"Y");
        done();
    }

    public static String getPidm() {
            def user = SecurityContextHolder?.context?.authentication?.principal
            if (user instanceof BannerUser) {
                return user.pidm
            }
            return null
        }

    def done () {
        String path = request.getSession().getAttribute("URI_ACCESSED")
        if(path == null) {
            path = "/"
        }
        redirect uri: path
    }
}
