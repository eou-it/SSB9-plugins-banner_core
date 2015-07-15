/*********************************************************************************
  Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.utility

import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder as LCH

/**
 * This is a helper class that is used for retrieving Message from i18n messsage.properties
 */

class MessageResolver {

    /**
     *@deprecated use MessageHelper.message Instead
     * @param key
     * @param args
     * @param locale
     * @return
     */
    @Deprecated
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
