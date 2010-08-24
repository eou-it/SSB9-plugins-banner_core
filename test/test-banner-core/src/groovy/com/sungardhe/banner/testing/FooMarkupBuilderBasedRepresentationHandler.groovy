/** *****************************************************************************
 Copyright 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.representations.ResourceRepresentationHandler
import com.sungardhe.banner.representations.ParamsExtractor
import com.sungardhe.banner.representations.RepresentationBuilder

import groovy.xml.StreamingMarkupBuilder

import javax.servlet.http.HttpServletRequest
import groovy.xml.XmlUtil

/**
 * An implementation of a custom resource representation handler.  Classes like this

 * may be implemented by a customer, placed into the classpath, and registered within
 * the CustomRepresentationConfig.groovy configuration file.
 */
class FooMarkupBuilderBasedRepresentationHandler implements ResourceRepresentationHandler {


    String getRepresentationName() {
        return "application/vnd.whatsamattau.student.v0.03+xml"
    }


    Class getModelClass() {
        return Foo
    }


    ParamsExtractor paramsExtractor() {
        new FooParamsExtractor()
    }


    RepresentationBuilder singleBuilder() {
        new FooSingleRepresentationBuilder()
    }


    RepresentationBuilder collectionBuilder() {
        new FooCollectionRepresentationBuilder()
    }


    // ---------------------- Helper Methods and Inner Classes ---------------------------


    def buildFoo( builder, Foo foo ) {
        builder.Foo( id: foo?.id, apiVersion: '1.0', systemRequiredIndicator: (foo?.systemRequiredIndicator ? true : false),
                     lastModified: foo?.lastModified, lastModifiedBy: foo?.lastModifiedBy, dataOrigin: foo?.dataOrigin,
                     optimisticLockVersion: foo?.version ) {
            Code( foo?.code )
            Description( foo?.description )
            buildAddress( builder, foo )
            VoiceResponseMessageNumber( foo?.voiceResponseMessageNumber )
            StatisticsCanadianInstitution( foo?.statisticsCanadianInstitution )
            DistrictDivision( foo?.districtDivision )
        }
    }


    def buildAddress( builder, Foo foo ) {
        builder.address {
            addressStreetLine1 foo?.addressStreetLine1
            addressStreetLine2 foo?.addressStreetLine2
            addressStreetLine3 foo?.addressStreetLine3
            addressStreetLine4 foo?.addressStreetLine4
            houseNumber        foo?.houseNumber
            addressCity        foo?.addressCity
            addressState       foo?.addressState
            addressCountry     foo?.addressCountry
            addressZipCode     foo?.addressZipCode
        }
    }
    

    // Note: You may construct a single RepresentationBuilder that handles both representing a single instance
    // and a collection, or separate builders (as is done here, within this handler).
    class FooSingleRepresentationBuilder implements RepresentationBuilder {

        Object buildRepresentation( Map content ) {
            def markupBuilder = new StreamingMarkupBuilder()
            markupBuilder.encoding = 'UTF-8'
            def xml = markupBuilder.bind { 
//                mkp.xmlDeclaration()
                FooInstance {
                    buildFoo it, content.data
                    Ref "${content.refBase}/${content.data?.id}"
                }
            }
            XmlUtil.serialize( xml )
        }
    }


    class FooCollectionRepresentationBuilder implements RepresentationBuilder {

        Object buildRepresentation( Map content ) {
            List foos = content.data
            def markupBuilder = new StreamingMarkupBuilder()
            markupBuilder.encoding = 'UTF-8'
            def xml = markupBuilder.bind { builder ->
//                mkp.xmlDeclaration()
                FooList( totalCount: content.totalCount, page: "${content.pageOffset}",
                         pageSize: "${content.pageMaxSize}", apiVersion: "1.0") {
//                    mkp.declareNamespace( '': "urn:com:sungardhe:Student" )
                    foos.each { foo ->
                        buildFoo builder, foo

                        Ref( "${content.refBase}/${foo.id}"  )
                    }
                }
                Ref( "${content.refBase}"  )
            }
            XmlUtil.serialize( xml )
        }
    }


    class FooParamsExtractor implements ParamsExtractor {

        Map extractParams( HttpServletRequest request ) {
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
        }
    }

}