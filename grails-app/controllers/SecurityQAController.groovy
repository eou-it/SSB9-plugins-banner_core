
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
        //securityQAService.saveSecurityQAResponse()
        completed()
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
