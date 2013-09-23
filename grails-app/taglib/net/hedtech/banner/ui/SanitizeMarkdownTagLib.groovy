/*********************************************************************************
 Copyright 2011-2012 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.ui

class SanitizeMarkdownTagLib {
    static namespace = "sanitizeMarkdown"

    def markdownService

    def renderHtml = { attrs, body ->
        def text
        if (attrs.template) {
            text = g.render(template: attrs.template)
        } else {
            text = attrs.text ?: body()
        }
        out << markdownService.markdown(markdownService.sanitize(text.toString()))
    }
}
