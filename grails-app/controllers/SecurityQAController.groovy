import net.hedtech.banner.security.BannerUser
import org.springframework.security.core.context.SecurityContextHolder

class SecurityQAController {

    static defaultAction = "index"
    //def securityQAService

    def index() {
        def model = [infoText:""]
        log.info("rendering view")
        render view: "securityQA", model: model
    }

    def save() {
        log.info("save")
        String pidm = getPidm()
        //securityQAService.saveSecurityQAResponse(pidm)
        completed()
    }
    public static String getPidm() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        if (user instanceof BannerUser) {
            return user.pidm
        }
        return null
    }

    def completed() {
        request.getSession().setAttribute("securityqadone", "true")
        done()
    }

    def done() {
        String path = request.getSession().getAttribute("URI_ACCESSED")
        if (path == null) {
            path = "/"
        }
        redirect uri: path
    }
}
