import net.hedtech.banner.security.BannerUser
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.general.overall.PinQuestion
import net.hedtech.banner.securityQA.SecurityQAService

class SecurityQAController {

    static defaultAction = "index"
    def securityQAService
    static def noOfquestions
    static def questions = [:]

    def index() {
        def ques = PinQuestion.fetchQuestions()
        def userDefinedQuesFlag = securityQAService.getUserDefinedQuestionFlag()
        ques.each {
            questions.put(it.pinQuestionId, it.description)
        }
        def questionList = questions.values()
        noOfquestions = securityQAService.getNumberOfQuestions()
        log.info("rendering view")
        render view: "securityQA", model: [questions: questionList, userDefinedQuesFlag: userDefinedQuesFlag, noOfquestions: noOfquestions]
    }

    def save() {
        log.info("save")
        String pidm = getPidm()
        List selectedQA = []


        for (int index = 0; index < noOfquestions; index++) {
            def questionsAnswered = [question: params.question[index], questionNo: questions.find{it.value==params.question[index]}?.key, userDefinedQuestion: params.userDefinedQuestion[index], answer: params.answer[index]]
            selectedQA.add(questionsAnswered)
        }

        securityQAService.saveSecurityQAResponse(pidm, selectedQA, params.pin)
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
