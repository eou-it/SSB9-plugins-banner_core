/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import net.hedtech.banner.security.ResetPasswordService
import org.apache.commons.codec.binary.Base64
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import javax.servlet.http.HttpSession
import java.sql.SQLException

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
            flash.message = message(code: "net.hedtech.banner.resetpassword.request.invalid.message")
            redirect (uri: "/resetPassword/auth")
        }
        else if(id == null || id.trim().length() == 0){

            render view: "auth", model: [usernameRequired = true]
        }
        else if(resetPasswordService.isPidmUser(id)){
            if(CH?.config.ssbPassword.reset.enabled == null || CH?.config.ssbPassword.reset.enabled == false){
                flash.message = message(code: "net.hedtech.banner.resetpassword.disabled.message")
                redirect (uri: "/resetPassword/auth")
            }
            else if(resetPasswordService.isAccountDisabled(id)){
                flash.message = message(code: "net.hedtech.banner.errors.login.disabled")
                redirect (uri: "/resetPassword/auth")
            }
            else{
                String postUrl = "${request.contextPath}/resetPassword/validateans"
                String view = 'questans'
                try{
                    Map questionsInfoMap = resetPasswordService.getQuestionInfoByLoginId(id)
                    if(((List)questionsInfoMap.get(id)).size() == 0 || ((List)questionsInfoMap.get(id)).size() < questionsInfoMap.get(id+"qstn_no")){
                        flash.message = message(code:"net.hedtech.banner.resetpasword.securityquestion.notfound.message")
                        redirect (uri: "/resetPassword/auth")
                    }
                    else{
                        session.setAttribute("questions", questionsInfoMap.get(id))
                        session.setAttribute("pidm", questionsInfoMap.get(id+"pidm"))
                        render view: view, model: [questions: questionsInfoMap.get(id), userName: id, postUrl : postUrl, cancelUrl: cancelUrl]
                    }
                }
                catch(SQLException sqle){
                    flash.message = message(code:sqle.getMessage())
                    redirect (uri: "/resetPassword/auth")
                }
            }
        }
        else if(resetPasswordService.isNonPidmUser(id)){
            if(CH?.config.ssbPassword.guest.reset.enabled == null || CH?.config.ssbPassword.guest.reset.enabled == false){
                flash.message = message(code: "net.hedtech.banner.resetpassword.disabled.message")
                redirect (uri: "/resetPassword/auth")
            }
            else{
                String baseUrl = "${CH?.config.banner.events.resetpassword.guest.url}${request.contextPath}/resetPassword/recovery"
                String postUrl = "${request.contextPath}/resetPassword/recovery"
                resetPasswordService.generateResetPasswordURL(id, baseUrl)
                String view = 'recovery'
                render view: view, model: [userName: id, postUrl : postUrl, cancelUrl: cancelUrl, infoPage:true]
            }
        }
        else{
            flash.message = message( code: "net.hedtech.banner.resetpassword.user.invalid")
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
            flash.message = message(code: "net.hedtech.banner.resetpassword.request.invalid.message")
            redirect (uri: "/resetPassword/auth")
        }
        else{
            questions.each {
                String answer = request.getParameter("answer"+it[0])
                if(answer == null || answer.trim().length() == 0){
                    errorflag = true
                    flash.message = message(code:"net.hedtech.banner.resetpassword.answer.required.error")
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
                            errorMessage = message(code: "net.hedtech.banner.resetpassword.answer.match.error")
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
                    if(resetPasswordService.isPidmAccountDisabled(pidm)){
                        session.invalidate()
                        flash.message = message(code: "net.hedtech.banner.errors.login.disabled")
                        redirect (uri: "/resetPassword/auth")
                    }
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
        def validateResult =  resetPasswordService.validatePassword(pidm, password)

        if(session.getAttribute("requestPage") != "resetpin"){
            session.invalidate()
            flash.message = message(code: "net.hedtech.banner.resetpassword.request.invalid.message")
            redirect (uri: "/resetPassword/auth")
        }
        else if(password != confirmPassword){
            flash.message = message( code:"net.hedtech.banner.resetpassword.password.match.error" )
            String view = 'resetpin'
            render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
        }
        else if(password.trim().length() == 0 || confirmPassword.trim().length() == 0){
            flash.message = message( code:"net.hedtech.banner.resetpassword.password.required.error" )
            String view = 'resetpin'
            render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
        }
        else if(validateResult.get("error") == true){
            flash.message = validateResult.get("errorMessage")
            String view = 'resetpin'
            render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
        }
        else{
            def result = resetPasswordService.validatePassword(pidm,password)
            if(result.get("error")){
                flash.message = message(code: result.get("error"))
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
                    flash.reloginMessage = message(code: "net.hedtech.banner.resetpassword.resetpin.success.message")
                    redirect(controller: "login", action: "auth")
                }
                catch(SQLException sqle){
                    if(20100 == sqle.getErrorCode()){
                        flash.message = message(code:"net.hedtech.banner.resetpassword.resetpin.password.length.error")
                    }
                    else{
                        flash.message = message(code:sqle.getMessage())
                    }
                    String view = 'resetpin'
                    render view: view, model: [postBackUrl : postBackUrl, cancelUrl: cancelUrl]
                }
            }
        }
    }
    def recovery ={
        String postUrl = "${request.contextPath}/ssb/resetPassword/validateCode"
        def cancelUrl = "${request.contextPath}/ssb/resetPassword/auth"
        def token= request.getParameter("token")

        if(token){
            byte[] decoded = Base64.decodeBase64(token.getBytes());
            def decodedToken = new String(decoded);
            def result = resetPasswordService.validateToken(decodedToken)
            if(result.get("nonPidmId")){
                HttpSession session = request.getSession(true)
                session.setAttribute("requestPage", "recovery")
                String view = 'recovery'
                render view: view, model: [postUrl : postUrl, cancelUrl: cancelUrl, nonPidmIdm:result.get("nonPidmId")]
            }
            else if(result.get("error")){
                flash.message =  message(code:result.get("error"))
                redirect (uri: "/resetPassword/auth")
            }
        }
    }

    def validateCode ={
        def recoveryCode = request.getParameter("recoverycode")
        def nonPidmIdm = request.getParameter("nonPidmId")
        if(session.getAttribute("requestPage") != "recovery"){
            session.invalidate()
            flash.message = message(code: "net.hedtech.banner.resetpassword.request.invalid.message")
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
                flash.message = message(code:result.get("error"))
                String view = 'recovery'
                render view: view, model: [postUrl : postUrl, cancelUrl: cancelUrl, nonPidmIdm:nonPidmIdm]
            }
        }
    }
}
