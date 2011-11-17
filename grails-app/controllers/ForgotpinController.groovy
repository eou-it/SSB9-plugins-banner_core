import com.sungardhe.banner.forgotpin.ForgotPinService

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Created by IntelliJ IDEA.
 * User: Vijendra.Rao
 * Date: 31/10/11
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
class ForgotpinController {

/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

    com.sungardhe.banner.forgotpin.ForgotPinService forgotPinService

    def questans ={
        String id = request.getParameter("j_username")
        def config = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.securityConfig
        def cancelUrl = "${request.contextPath}/forgotpin/auth"
        if(id == null || id.trim().length() == 0){

            render view: "auth", model: [usernameRequired = true]
        }
        else if(forgotPinService.isPidmUser(id)){
            String postUrl = "${request.contextPath}/forgotpin/validateans"

            String view = 'questans'
            Map questionsInfoMap = forgotPinService.getQuestionInfoByLoginId(id)

            session.setAttribute("questions", questionsInfoMap.get(id))
            session.setAttribute("pidm", questionsInfoMap.get(id+"pidm"))

            render view: view, model: [questions: questionsInfoMap.get(id), userName: id, postUrl : postUrl, cancelUrl: cancelUrl]
        }
        else{
            String postUrl = "${request.contextPath}/forgotpin/recovery"
            String view = 'recovery'
            render view: view, model: [userName: id, postUrl : postUrl, cancelUrl: cancelUrl]
        }
    }

    def validateAnswer ={
       def config = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.securityConfig
       String postBackUrl = "${request.contextPath}/forgotpin/validateans"
       def cancelUrl = "${request.contextPath}/forgotpin/auth"
       String id = request.getParameter("username")
       String pidm = session.getAttribute("pidm")
       List questions = session.getAttribute("questions")
        flash.message = null
       questions.each {
           String answer = request.getParameter("answer"+it[0])
           if(answer == null || answer.trim().length() == 0){
               flash.message = "Please answer all the questions"
           }
       }
       if(flash.message){
           String view = 'questans'
           render view: view, model: [questions: questions, userName: id, postBackUrl : postBackUrl, cancelUrl: cancelUrl]
       }
        else{
           int questionCount = 0;
           String errorMessage = new String("")
           questions.each {
               questionCount ++;
               String userAnswer = request.getParameter("answer"+it[0])
               boolean answerMatch = forgotPinService.isAnswerMatched(userAnswer, pidm, it[0])
               if(!answerMatch){
                   errorMessage += "Answer for Question ${questionCount} doesn't match \n";
               }
           }
           if(errorMessage){
              flash.message = errorMessage
              String view = 'questans'
              render view: view, model: [questions: questions, userName: id, postBackUrl : postBackUrl, cancelUrl: cancelUrl]
           }
           else{
               String view = 'resetpin'
               postBackUrl = "${request.contextPath}/forgotpin/resetpin"
               render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
           }
       }
    }

    def resetPin ={
        String password = request.getParameter("password")
        String confirmPassword = request.getParameter("repassword")
        String pidm = session.getAttribute("pidm")
        String postBackUrl = "${request.contextPath}/forgotpin/resetpin"
        def cancelUrl = "${request.contextPath}/forgotpin/auth"
        if(password != confirmPassword){
           flash.message = "Both password did not match"
           String view = 'resetpin'
           render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
        }
        else{
            def rowsUpdated = forgotPinService.resetUserPassword(pidm, password)
            if(rowsUpdated > 0){
                flash.reloginMessage = "Password reset was successful, please relogin"
            }
            else{
                flash.message = "Error while reseting the password. try again"
            }

            redirect(controller: "login", action: "auth")
        }
    }
    def cancel ={
         redirect(controller: "login", action: "auth")
    }
}
