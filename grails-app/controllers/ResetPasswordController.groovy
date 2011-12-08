import com.sungardhe.banner.security.ResetPasswordService

/**
 * Created by IntelliJ IDEA.
 * User: Vijendra.Rao
 * Date: 31/10/11
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
class ResetPasswordController {

/**
	 * Dependency injection for the authenticationTrustResolver.
	 */
	def authenticationTrustResolver

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

    ResetPasswordService resetPasswordService

    def questans ={
        String id = request.getParameter("j_username")
        def cancelUrl = "${request.contextPath}/resetPassword/auth"
        if(id == null || id.trim().length() == 0){

            render view: "auth", model: [usernameRequired = true]
        }
        else if(resetPasswordService.isPidmUser(id)){
            String postUrl = "${request.contextPath}/resetPassword/validateans"

            String view = 'questans'
            Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(id)

            session.setAttribute("questions", questionsInfoMap.get(id))
            session.setAttribute("pidm", questionsInfoMap.get(id+"pidm"))

            render view: view, model: [questions: questionsInfoMap.get(id), userName: id, postUrl : postUrl, cancelUrl: cancelUrl]
        }
        else if(resetPasswordService.isNonPidmUser(id)){
            String postUrl = "${request.contextPath}/resetPassword/recovery"
            String view = 'recovery'
            render view: view, model: [userName: id, postUrl : postUrl, cancelUrl: cancelUrl]
        }
        else{
            flash.message = message( code: "com.sungardhe.banner.resetpassword.user.invalid")
            redirect(uri: "/login/auth")
        }
    }

    def validateAnswer ={
       def config = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.securityConfig
       String postBackUrl = "${request.contextPath}/resetPassword/validateans"
       def cancelUrl = "${request.contextPath}/resetPassword/auth"
       String id = request.getParameter("username")
       String pidm = session.getAttribute("pidm")
       List questions = session.getAttribute("questions")
       Map questionValidationMap = new HashMap();
        flash.message = ""
        def errorflag = false;
       questions.each {
           String answer = request.getParameter("answer"+it[0])
           if(answer == null || answer.trim().length() == 0){
               errorflag = true
               flash.message = "Answer for question ${it[0]} is required"
               questionValidationMap.put(it[0], [error:true, answer: "", message: flash.message])
           }
           else{
               questionValidationMap.put(it[0], [error: false, answer: answer, message:""])
           }
       }
       if(flash.message){
           String view = 'questans'
           render view: view, model: [questions: questions, questionValidationMap: questionValidationMap, userName: id, postBackUrl : postBackUrl, cancelUrl: cancelUrl]
       }
        else{
           int questionCount = 0;
           def errorMessage = new String("")
           questions.each {
               questionCount ++;
               String userAnswer = request.getParameter("answer"+it[0])
               boolean answerMatch = resetPasswordService.isAnswerMatched(userAnswer, pidm, it[0])
               if(!answerMatch){
                   errorMessage = "Authorization failure: One or more question did not match";
                   questionValidationMap.put(it[0], [error:true, answer:"", message: errorMessage])
               }
               else{
                   questionValidationMap.put(it[0], [error:false, answer: userAnswer, message: ""])
               }
           }
           if(errorMessage){
              flash.message = errorMessage
              String view = 'questans'
              render view: view, model: [questions: questions, userName: id, postBackUrl : postBackUrl, cancelUrl: cancelUrl, questionValidationMap: questionValidationMap]
           }
           else{
               String view = 'resetpin'
               postBackUrl = "${request.contextPath}/resetPassword/resetpin"
               render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
           }
       }
    }

    def resetPin ={
        String password = request.getParameter("password")
        String confirmPassword = request.getParameter("repassword")
        String pidm = session.getAttribute("pidm")
        String postBackUrl = "${request.contextPath}/resetPassword/resetpin"
        def cancelUrl = "${request.contextPath}/resetPassword/auth"
        if(password != confirmPassword){
           flash.message = message( code:"com.sungardhe.banner.resetpassword.password.match.error" )
           String view = 'resetpin'
           render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
        }
        else if(password.trim().length() == 0 || confirmPassword.trim().length() == 0){
           flash.message = message( code:"com.sungardhe.banner.resetpassword.password.required.error" )
           String view = 'resetpin'
           render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
        }
        else{
            def rowsUpdated = resetPasswordService.resetUserPassword(pidm, password)
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
