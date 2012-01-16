import com.sungardhe.banner.security.ResetPasswordService
import java.sql.SQLException
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.codec.binary.Base64
import javax.servlet.http.HttpSession
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

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
        def config = SpringSecurityUtils.securityConfig
        response.setHeader("Cache-Control", "no-cache")
        response.setHeader("Cache-Control", "no-store")
        response.setDateHeader("Expires", 0)
        response.setHeader("Pragma", "no-cache")
        String id = request.getParameter("j_username")
        def cancelUrl = "${request.contextPath}/resetPassword/auth"
        if(session.getAttribute("requestPage") != "questans"){
            session.invalidate()
            flash.message = "Invalid Request. Try Again!"
            redirect (uri: "/resetPassword/auth")
        }
        else if(id == null || id.trim().length() == 0){

            render view: "auth", model: [usernameRequired = true]
        }
        else if(resetPasswordService.isPidmUser(id)){
            if(resetPasswordService.isPidmAccountDisabled(id)){
                flash.message = message(code: "com.sungardhe.banner.resetpassword.user.disabled.message")
                redirect (uri: "/resetPassword/auth")
            }
            else{
                String postUrl = "${request.contextPath}/resetPassword/validateans"
                String view = 'questans'
                try{
                    Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(id)
                    if(((List)questionsInfoMap.get(id)).size() == 0 || ((List)questionsInfoMap.get(id)).size() < questionsInfoMap.get(id+"qstn_no")){
                        flash.message = "Security question/answers need to be defined"
                        redirect (uri: "/resetPassword/auth")
                    }
                    else{
                    session.setAttribute("questions", questionsInfoMap.get(id))
                    session.setAttribute("pidm", questionsInfoMap.get(id+"pidm"))
                    render view: view, model: [questions: questionsInfoMap.get(id), userName: id, postUrl : postUrl, cancelUrl: cancelUrl]
                    }
                }
                catch(SQLException sqle){
                    flash.message = sqle.getMessage()
                    redirect (uri: "/resetPassword/auth")
                }
            }
        }
        else if(resetPasswordService.isNonPidmUser(id)){
            String baseUrl = "${CH?.config.banner.events.resetpassword.guest.url}${request.contextPath}/resetPassword/recovery"
            String postUrl = "${request.contextPath}/resetPassword/recovery"
            resetPasswordService.generateResetPasswordURL(id, baseUrl)
            String view = 'recovery'
            render view: view, model: [userName: id, postUrl : postUrl, cancelUrl: cancelUrl, infoPage:true]
        }
        else{
            flash.message = message( code: "com.sungardhe.banner.resetpassword.user.invalid")
            redirect(controller: "login", action: "auth")
        }
    }

    def validateAnswer ={
        response.setHeader("Cache-Control", "no-cache")
        response.setHeader("Cache-Control", "no-store")
        response.setDateHeader("Expires", 0)
        response.setHeader("Pragma", "no-cache")
       def config = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.securityConfig
       String postBackUrl = "${request.contextPath}/resetPassword/validateans"
       def cancelUrl = "${request.contextPath}/resetPassword/auth"
       String id = request.getParameter("username")
       String pidm = session.getAttribute("pidm")
       List questions = session.getAttribute("questions")
       Map questionValidationMap = new HashMap();
        flash.message = ""
        def errorflag = false;

        if(session.getAttribute("requestPage") != "questans"){
            session.invalidate()
            flash.message = "Invalid Request. Try Again!"
            redirect (uri: "/resetPassword/auth")
        }
       else{
           questions.each {
               String answer = request.getParameter("answer"+it[0])
               if(answer == null || answer.trim().length() == 0){
                   errorflag = true
                   flash.message = "Answer is required"
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
                   try{
                       boolean answerMatch = resetPasswordService.isAnswerMatched(userAnswer, pidm, it[0])
                       if(!answerMatch){
                           errorMessage = message(code: "com.sungardhe.banner.resetpassword.answer.match.error")
                           questionValidationMap.put(it[0], [error:true, answer:"", message: errorMessage])
                       }
                       else{
                           questionValidationMap.put(it[0], [error:true, answer: "", message: errorMessage])
                       }
                   }
                   catch(SQLException sqle){
                       errorMessage = sqle.getMessage()
                   }
               }
               if(errorMessage){
                  flash.message = errorMessage
                  resetPasswordService.loginAttempt(pidm)
                  String view = 'questans'
                  render view: view, model: [questions: questions, userName: id, postBackUrl : postBackUrl, cancelUrl: cancelUrl, questionValidationMap: questionValidationMap]
               }
               else{
                   session.setAttribute("requestPage", "resetpin")
                   String view = 'resetpin'
                   postBackUrl = "${request.contextPath}/resetPassword/resetpin"
                   render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
               }
           }
       }
    }

    def resetPin ={
        response.setHeader("Cache-Control", "no-cache")
        response.setHeader("Cache-Control", "no-store")
        response.setDateHeader("Expires", 0)
        response.setHeader("Pragma", "no-cache")
        String password = request.getParameter("password")
        String confirmPassword = request.getParameter("repassword")
        String pidm = session.getAttribute("pidm")
        String nonPidm = session.getAttribute("nonPidmId")
        String postBackUrl = "${request.contextPath}/resetPassword/resetpin"
        def cancelUrl = "${request.contextPath}/resetPassword/auth"
        if(session.getAttribute("requestPage") != "resetpin"){
            session.invalidate()
            flash.message = "Invalid Request. Try Again!"
            redirect (uri: "/resetPassword/auth")
        }
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
            try{
                if(pidm){
                resetPasswordService.resetUserPassword(pidm, password)
                }
                else if(nonPidm){
                    resetPasswordService.resetNonPidmPassword(nonPidm, password)
                }
                session.invalidate()
                flash.reloginMessage = "Password reset was successful, please relogin"
                redirect(controller: "login", action: "auth")
            }
            catch(SQLException sqle){
                if(20100 == sqle.getErrorCode()){
                   flash.message = "Password must be only 6 characters long"
                }
                else{
                    flash.message = sqle.getMessage()
                }
                String view = 'resetpin'
                render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
            }

        }
    }
    def recovery ={
        String postUrl = "${request.contextPath}/ssb/resetPassword/validateCode"
        def cancelUrl = "${request.contextPath}/ssb/resetPassword/auth"
        def token= request.getParameter("token")
        def username = request.getParameter("j_username")
        if(username){
            def infoMessage = "A web page link has been sent to your e-mail address. Use the link to reset your password."
        }
        else if(token){
            def decodedToken = new String(new Base64().decode(token))
            def result = resetPasswordService.validateToken(decodedToken)
            if(result.get("nonPidmId")){
                HttpSession session = request.getSession(true)
                session.setAttribute("requestPage", "recovery")
                String view = 'recovery'
                render view: view, model: [postUrl : postUrl, cancelUrl: cancelUrl, nonPidmIdm:result.get("nonPidmId")]
            }
            else if(result.get("error")){
                flash.message =  result.get("error")
                redirect (uri: "/resetPassword/auth")
            }
        }
    }

    def validateCode ={
        def recoveryCode = request.getParameter("recoverycode")
        def nonPidmIdm = request.getParameter("nonPidmId")
        if(session.getAttribute("requestPage") != "recovery"){
            session.invalidate()
            flash.message = "Invalid Request. Try Again!"
            redirect (uri: "/resetPassword/auth")
        }
        else{
            Map result = resetPasswordService.validateRecoveryCode(recoveryCode, nonPidmIdm)
            HttpSession session = request.getSession(true)
            if(result.get("validate")){
                   session.setAttribute("requestPage", "resetpin")
                   session.setAttribute("nonPidmId", nonPidmIdm)
                   String view = 'resetpin'
                   def postUrl = "${request.contextPath}/resetPassword/resetpin"
                   def cancelUrl = "${request.contextPath}/resetPassword/auth"
                   render view: view, model: [postBackUrl : postUrl, cancelUrl: cancelUrl]
            }
            else if(result.get("error")){
                String postUrl = "${request.contextPath}/ssb/resetPassword/validateCode"
                def cancelUrl = "${request.contextPath}/ssb/resetPassword/auth"
                flash.message = result.get("error")
                String view = 'recovery'
                render view: view, model: [postUrl : postUrl, cancelUrl: cancelUrl, nonPidmIdm:nonPidmIdm]
            }
        }
    }
}