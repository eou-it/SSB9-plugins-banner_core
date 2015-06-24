/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package banner.security

import net.hedtech.banner.security.UrlBasedFormsIdentifier
import spock.lang.Specification
import spock.lang.Unroll

class UrlBasedFormsIdentifierSpec extends Specification {


    @Unroll
    def "Test tokenization of URLs"( String tokens, String url, List<String> prefixes ) {

        when:"a URL is tokenized"
        List<String> urlTokens = UrlBasedFormsIdentifier.getTokensFor( url )

        then:
        tokens == urlTokens.join(" ") // exact comparison of Strings

        where:
        tokens                | url                            | prefixes
        "mymepcode api foos"  | "/myMepCode/api/foos"          | ["api","qapi"]
        "mymepcode qapi foos" | "/myMepCode/qapi/foos"         | ["api","qapi"]
        "api foos"            | "/api/foos"                    | ["api","qapi"]
        "menu"                | "/menu?pageName=mainPage"      | ["api","qapi"]
        "banner.zul"          | "/banner.zul?page=mainPage"    | ["api","qapi"]
        "adv ad adv-srch"     | "/adv/AD/adv-srch?t=2&s=sapi1" | ["api","qapi"]
    }

    @Unroll
    def "Test form determination for URLs"( String forms, String url,
                                            List<String> prefixes, String pageName ) {

        setup:
        Map formControllerMap = [
          'first': ['FIRST'],
          'second': ['SECOND'],
          'thirdandfourth': ['THIRD', 'FOURTH'],
        ]

        when:"forms are being determined based on a URL"
        List<String> determinedForms =
            UrlBasedFormsIdentifier.getFormsFor( url, prefixes,
                                                 formControllerMap, pageName )
        then:
        forms == determinedForms.join(" ") // exact comparison of Strings

        where:
        forms          | url                      | prefixes       | pageName
        "FIRST"        | "/myMepCode/api/first"   | ["api","qapi"] | null
        "SECOND"       | "/myMepCode/qapi/second" | ["api","qapi"] | null
        "THIRD FOURTH" | "/api/thirdandfourth"    | ["api","qapi"] | null
        "FIRST"        | "/menu?pageName=first"   | ["api","qapi"] | "first"
        "FIRST"        | "/banner.zul?page=first" | ["api","qapi"] | "first"
        "SECOND"       | "/second.gsp"            | ["api","qapi"] | null
        "SECOND"       | "/apijunk/api/second"    | ["api","qapi"] | null
    }

}

