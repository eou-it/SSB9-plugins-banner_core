package net.hedtech.banner.security

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AccessControlInterceptorSpec extends Specification implements InterceptorUnitTest<AccessControlInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test accessControl interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"accessControl")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
