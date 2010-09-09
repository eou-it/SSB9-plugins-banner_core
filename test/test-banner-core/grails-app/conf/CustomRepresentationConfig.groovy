/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

/**
 * Configuration for registering custom representations.
 * @see com.sungardhe.banner.representations.ResourceRepresentationRegistry
 * @see com.sungardhe.banner.representations.ResourceRepresentationHandler
 */
customRepresentationHandlerMap =

    // This first example illustrates how a customer could implement the support 'in-line' within this file. Since
    // this appraoch is very verbose, it is recommended that separate classes be used (as illustrated in the second example below).
    //
    [ "application/vnd.whatsamattau.student.v0.01+xml":

        [ "Foo": // prefer to use fully qualified class names, but short names are also handled (if unambiguous)

            [ paramsExtractor:  { request ->
                                def xml = request.XML.Foo[0]
                                def props = [:]
                                if (xml.@id?.text())                           props.id                 = xml.@id.toInteger()
                                if (xml.@systemRequiredIndicator?.text())      props.systemRequiredIndicator = xml.@systemRequiredIndicator?.text()
                                if (xml.@lastModifiedBy?.text())               props.lastModifiedBy     = xml.@lastModifiedBy.text()
                                if (xml.@lastModified?.text())                 props.lastModified       = xml.@lastModified.text()
                                if (xml.@dataOrigin?.text())                   props.dataOrigin         = xml.@dataOrigin.text()
                                if (xml.@optimisticLockVersion?.text())        props.version            = xml.@optimisticLockVersion.toInteger()

                                if (xml.Code?.text())                          props.code               =  xml.Code.text()
                                if (xml.Description?.text())                   props.description        = xml.Description.text()

                                if (xml.AddressStreetLine1?.text())            props.addressStreetLine1 = xml.AddressStreetLine1?.text()
                                if (xml.AddressStreetLine2?.text())            props.addressStreetLine2 = xml.AddressStreetLine2?.text()
                                if (xml.AddressStreetLine3?.text())            props.addressStreetLine3 = xml.AddressStreetLine3?.text()
                                if (xml.AddressStreetLine4?.text())            props.addressStreetLine4 = xml.AddressStreetLine4?.text()
                                if (xml.HouseNumber?.text())                   props.houseNumber        = xml.HouseNumber?.text()

                                if (xml.AddressCity?.text())                   props.addressCity        = xml.AddressCity?.text()
                                if (xml.AddressState?.text())                  props.addressState       = xml.AddressState?.text()
                                if (xml.AddressCountry?.text())                props.addressCountry     = xml.AddressCountry?.text()
                                if (xml.AddressZipCode?.text())                props.addressZipCode     = xml.AddressZipCode?.text()

                                if (xml.VoiceResponseMessageNumber?.text())    props.voiceResponseMessageNumber    = xml.VoiceResponseMessageNumber?.text()
                                if (xml.StatisticsCanadianInstitution?.text()) props.statisticsCanadianInstitution = xml.StatisticsCanadianInstitution?.text()
                                if (xml.DistrictDivision?.text())              props.districtDivision   = xml.DistrictDivision?.text()
                                props
                            },

              singleBuilder:     { renderDataMap -> [ template: "/foo/single.v1.0.xml",
                                                      model: [ foo: renderDataMap.data, refBase: renderDataMap.refBase ] ] },

              collectionBuilder: { renderDataMap -> [ template: "/foo/list.v1.0.xml",
                                                      model: [ fooList: renderDataMap.data, totalCount: renderDataMap.totalCount,
                                                               refBase: renderDataMap.refBase ] ] }

            ], // end Foo support of "application/vnd.whatsamattau.student.v0.01+xml"

            // next model supported by this same MIME type would go here...
         ],


      // This second example is used to support a different MIME type, and uses an external groovy file that has been made
      // available on the classpath. This this appraoch to configuration requires only two lines, it is recommended over the
      // in-line approach shown above.  Note that the identified class MUST implement the ResourceRepresentationHandler interface.
      "application/vnd.whatsamattau.student.v0.02+xml":
          [ "Foo": "edu.whatsamattau.rest.FooTestCustomRepresentationHandler"
            // next model supported by this MIME type would go here
          ],

      // The next MIME type would go here
]
// End of configuration.
