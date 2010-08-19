/** *****************************************************************************
 Copyright 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package edu.whatsamattau.rest

import com.sungardhe.banner.representations.ResourceRepresentationHandler
import com.sungardhe.banner.representations.ParamsExtractor
import com.sungardhe.banner.representations.RepresentationBuilder
import com.sungardhe.banner.testing.Foo

/**
 * An implementation of a custom resource representation handler.  Classes like this
 * may be implemented by a customer, placed into the classpath, and registered within
 * the CustomRepresentationConfig.groovy configuration file.
 */
class FooTestCustomRepresentationHandler implements ResourceRepresentationHandler {


    String getRepresentationName() {
        return "application/vnd.whatsamattau.student.v0.02+xml"
    }


    Class getModelClass() {
        return Foo
    }


    ParamsExtractor paramsExtractor() {
        // We'll just cast a closure to a ParamsExtractor, for convenience
        { request ->
                def xml = request.XML.Foo[0]
                def props = [:]
                if (xml.@id?.text())                           props.id                 = xml.@id.toInteger()
                if (xml.@systemRequiredIndicator?.text())      props.systemRequiredIndicator = xml.@systemRequiredIndicator?.text()
                if (xml.@lastModifiedBy?.text())               props.lastModifiedBy     = "${xml.@lastModifiedBy.text()}"
                if (xml.@lastModified?.text())                 props.lastModified       = xml.@lastModified.text()
                if (xml.@dataOrigin?.text())                   props.dataOrigin         = "${xml.@dataOrigin.text()}"
                if (xml.@optimisticLockVersion?.text())        props.version            = xml.@optimisticLockVersion.toInteger()

                if (xml.Code?.text())                          props.code               = "${xml.Code.text()}"
                if (xml.Description?.text())                   props.description        = "${xml.Description.text()}"

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
            } as ParamsExtractor
    }


    RepresentationBuilder singleBuilder() {
        // we'll just cast a closure to a RepresentationBuilder
        { renderDataMap -> [ template: "/foo/single.v1.0.xml",
                             model: [ foo: renderDataMap.data, refBase: renderDataMap.refBase ] ] } as RepresentationBuilder
    }


    RepresentationBuilder collectionBuilder() {
        // we'll just cast a closure to a RepresentationBuilder
        { renderDataMap -> [ template: "/foo/list.v1.0.xml",
                             model: [ fooList: renderDataMap.data, totalCount: renderDataMap.totalCount,
                             refBase: renderDataMap.refBase ] ] } as RepresentationBuilder
    }
}
