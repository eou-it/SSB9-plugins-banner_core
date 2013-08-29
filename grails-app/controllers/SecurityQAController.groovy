import grails.converters.JSON
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.security.BannerUser
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.general.overall.PinQuestion

class SecurityQAController {

    static defaultAction = "index"
    def securityQAService
    static def noOfQuestions
    static def questions = [:]
    static List dataToView = []
    static def questionMinimumLength
    static def answerMinimumLength
    static def userDefinedQuesFlag
    static def questionList = []

    def index() {
        def ques = PinQuestion.fetchQuestions()
        userDefinedQuesFlag = securityQAService.getUserDefinedQuestionFlag()
        ques.each {
            questions.put(it.pinQuestionId, it.description)
        }
        questionList = questions.values().collect()
        noOfQuestions = securityQAService.getNumberOfQuestions()
        questionMinimumLength = securityQAService.getQuestionMinimumLength()
        answerMinimumLength = securityQAService.getAnswerMinimumLength()
        render view: "securityQA", model: [questions: questionList, userDefinedQuesFlag: userDefinedQuesFlag, noOfquestions: noOfQuestions, questionMinimumLength: questionMinimumLength, answerMinimumLength: answerMinimumLength]
    }

    def save() {
        def model = [:]
        String messages = null
        String pidm = getPidm()
        List selectedQA = []

        log.info("save")

        for (int index = 0; index < noOfQuestions; index++) {
            def questionId = params.question[index].split("question")[1].toInteger()
            def question
            def questionNo
            if (questionId != 0) {
                question = questionList.get(questionId - 1)
                questionNo = questions.find {it.value == question}?.key
            }
            else {
                question = null
                questionNo = null
            }

            def questionsAnswered = [question: question, questionNo: questionNo, userDefinedQuestion: params.userDefinedQuestion[index], answer: params.answer[index]]
            selectedQA.add(questionsAnswered)
        }

        dataToView = selectedQA

        try {
            securityQAService.saveSecurityQAResponse(pidm, selectedQA, params.pin)
        } catch(ApplicationException ae) {

            messages = message(code:ae.wrappedException.message)


            if(messages.contains("{0}")) {
                if(ae.wrappedException.message.equals("securityQA.invalid.length.question")) {
                    messages.replace("{0}", questionMinimumLength)
                } else if(ae.wrappedException.message.equals("securityQA.invalid.length.answer")) {
                    messages.replace("{0}", answerMinimumLength)
                }
            }

            model.put("questions", questionList)
            model.put("userDefinedQuesFlag", userDefinedQuesFlag)
            model.put("noOfquestions", noOfQuestions)
            model.put("questionMinimumLength", questionMinimumLength)
            model.put("answerMinimumLength", answerMinimumLength)
            model.put("dataToView", dataToView)
            model.put("notification", messages)

            render view: "securityQA", model: model
        }
        if(messages == null) {
            completed()
        }

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
