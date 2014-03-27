/*********************************************************************************
  Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.utility

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder as LCH

/**
 * This is a helper class that is used for retrieving Message from i18n messsage.properties
 */
class MessageResolver {

    public static String message(key, args = null, locale = null) {
        String value = "";
        if (key){
              if(!locale) locale = LCH.getLocale()
              MessageSource messageSource = ApplicationHolder.application.mainContext.getBean('messageSource')
              value = messageSource.getMessage(key,args,locale)
        }
        return value
    }
}
