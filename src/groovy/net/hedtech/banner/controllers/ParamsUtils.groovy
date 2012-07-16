/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package net.hedtech.banner.controllers

/**
 * Utility class for managing params
 */
class ParamsUtils {

    public static def keyblock = { params ->
        return ParamsUtils.namedParams( params, "keyblock." )
    }


    public static def namedParams = { params, prefix ->
        def parameterMap = [:]

        params.each { param ->
            if (param.key.length() > prefix.length() ) {
                if (param.key.substring( 0, prefix.length() ) == prefix ) {
                    parameterMap.put( param.key.substring( prefix.length() ), param.value )
                }
            }
        }

        return parameterMap
    }
    

    @Deprecated
    public static def marshallParams = { params, domainClass, prefix, props = null ->

        def list = []

        if (!props) {
            props = getValidPropertyNames( domainClass )
        }


        // Loop through the props supplied to get the index of the parameters.
        // If there is only 1 domain object sent to marshall, the params will not be index and flatten.
        def propertyBase
        def x = 0
        while (!propertyBase) {

            if (params["${prefix}.${props[x]}"] instanceof String[]) {
                propertyBase = params["${prefix}.${props[x]}"]
            } else {
                propertyBase = [params["${prefix}.${props[x]}"]]
            }

            x++
        }

        propertyBase.eachWithIndex { item, i ->

            def propertyMap = [:]

            props.each { property ->
                if (params["${prefix}.${property}"]) {
                    if (propertyBase.size() > 1) {
                        propertyMap.put( property, params["${prefix}.${property}"][i] )
                    } else {
                        propertyMap.put( property, params["${prefix}.${property}"] )    
                    }
                }
            }
            try {
                assert propertyMap.id
                def domain = domainClass.get( propertyMap.id )

                domain.properties = propertyMap
                if (propertyMap.version && domain.hasProperty( "version" )) {
// TODO:  This is absolutely wrong and needs to be resolved.  For some reason I'm unable to set the version in certain undetermined scenarios
//                    domain.version = propertyMap.version
                }

                list << domain
            } catch (e) {
                println "An error has occurred during marshalling.  ${e.message}"
                throw e
            }

        }

        return list
    }


    private static def getValidPropertyNames( clazz ) {
        def props = []

        clazz.metaClass.properties.unique().each {
            if (!it.getType().equals(java.lang.Object.class)
             && !it.getName().equals("metaClass")
             && !it.getName().equals("errors")
             && !it.getName().equals("class")) {
                props << it.name
            }
        }

        return props
    }

}
