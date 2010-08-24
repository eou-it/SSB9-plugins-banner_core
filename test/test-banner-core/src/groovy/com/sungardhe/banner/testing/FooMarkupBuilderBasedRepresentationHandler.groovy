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
        new FooSingleRepresentationBuilder( this )
    }


    RepresentationBuilder collectionBuilder() {
        new FooCollectionRepresentationBuilder( this )
    }


    // ----------------------------- Helper Methods  -------------------------------


    def buildFoo( builder, Foo foo, Map content ) {
        builder.Foo( id: foo?.id, apiVersion: '1.0', systemRequiredIndicator: (foo?.systemRequiredIndicator ? true : false),
                     lastModified: foo?.lastModified, lastModifiedBy: foo?.lastModifiedBy, dataOrigin: foo?.dataOrigin,
                     optimisticLockVersion: foo?.version ?: 0) {
            Code( foo?.code )
            Description( foo?.description )
            // buildAddress( builder, foo ) // note: a separate method is not used here, as address is flattened within the schema
            if (foo?.addressStreetLine1) AddressStreetLine1( foo?.addressStreetLine1 )
            if (foo?.addressStreetLine2) AddressStreetLine2( foo?.addressStreetLine2 )
            if (foo?.addressStreetLine3) AddressStreetLine3( foo?.addressStreetLine3 )
            if (foo?.addressStreetLine4) AddressStreetLine4( foo?.addressStreetLine4 )
            if (foo?.houseNumber)        HouseNumber(        foo?.houseNumber )
            if (foo?.addressCity)        AddressCity(        foo?.addressCity )
            if (foo?.addressState)       AddressState(       foo?.addressState )
            if (foo?.addressCountry)     AddressCountry(     foo?.addressCountry )
            if (foo?.addressZipCode)     AddressZipCode(     foo?.addressZipCode )

            if (foo?.voiceResponseMessageNumber)    VoiceResponseMessageNumber( foo?.voiceResponseMessageNumber )
            if (foo?.statisticsCanadianInstitution) StatisticsCanadianInstitution( foo?.statisticsCanadianInstitution )
            if (foo?.districtDivision)              DistrictDivision( foo?.districtDivision )
            Ref( "${content.refBase}/${foo.id}"  )
        }
    }

      // Often, dependent objects will need to be rendered. Placing this into a separate
      // method like buildAddress is recommended.  This approach is not used here, as the
      // target XML Schema has this 'flattened'. The following would result in an <Address> element
      // with the address elements within it.
//    def buildAddress( builder, content ) {
//        def foo = content.data
//        builder.Address {
//            AddressStreetLine1 foo?.addressStreetLine1
//            AaddressStreetLine2 foo?.addressStreetLine2
//            AddressStreetLine3 foo?.addressStreetLine3
//            AddressStreetLine4 foo?.addressStreetLine4
//            HouseNumber        foo?.houseNumber
//            AddressCity        foo?.addressCity
//            AddressState       foo?.addressState
//            AddressCountry     foo?.addressCountry
//            AddressZipCode     foo?.addressZipCode
//        }
//    }
    
}

// NOTE: These cannot be inner classes, as StreamingMarkupBuilder is not functional within an inner class
//       (it's dynamic methods/properties are not available). Consequently, the following classes are
//       packaged in this file for convenience, but are not inner classes. To facilitate reuse, the handler
//       is passed to these classes in its constructor. 


// Note: You may construct a single RepresentationBuilder that handles both representing a single instance
// and a collection, or separate builders (as is done here, within this handler).
class FooSingleRepresentationBuilder implements RepresentationBuilder {

    def handler

    FooSingleRepresentationBuilder( FooMarkupBuilderBasedRepresentationHandler handler ) {
        this.handler = handler
    }

    Object buildRepresentation( Map content ) {
        def markupBuilder = new StreamingMarkupBuilder()
        markupBuilder.encoding = 'UTF-8'
        def xml = markupBuilder.bind { builder ->
            mkp.xmlDeclaration()
            FooInstance {
                namespaces << [ '': "urn:com:sungardhe:Student" ]
//                mkp.declareNamespace( '': "urn:com:sungardhe:Student" )
                handler.buildFoo builder, content.data, content
            }
        }
        XmlUtil.serialize( xml )
    }
}


class FooCollectionRepresentationBuilder implements RepresentationBuilder {

    def handler

    FooCollectionRepresentationBuilder( FooMarkupBuilderBasedRepresentationHandler handler ) {
        this.handler = handler
    }

    Object buildRepresentation( Map content ) {
        List foos = content.data
        def markupBuilder = new StreamingMarkupBuilder()
        markupBuilder.encoding = 'UTF-8'
        def xml = markupBuilder.bind { builder ->
            mkp.xmlDeclaration()
            namespaces << [ '': "urn:com:sungardhe:Student" ]
            FooList( totalCount: content.totalCount, page: "${content.pageOffset}",
                     pageSize: "${content.pageMaxSize}", apiVersion: "1.0") {
//                mkp.declareNamespace( '': "urn:com:sungardhe:Student" )
                foos.each { foo ->
                    handler.buildFoo builder, foo, content
                }
                Ref( "${content.refBase}"  )
            }
        }
        XmlUtil.serialize( xml )
    }
}

// This could be an inner class (it has no StreamingMarkupBuilder requirements), but is left as a normal
// class to be consistent with the builders above.
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
