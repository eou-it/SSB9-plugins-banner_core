/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner

/**
 * Created by IntelliJ IDEA.
 * User: rajanandppk
 * Date: 2/4/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
class ListManipulator {

    /**
     * Returns the map from string represented map.
     *
     * @param stringRepresentation eg: "a:b:c:d"
     * @param delimiter eg: ":"
     * @return   [a:b, c:d]
     */

    public static def stringRepresentationToMap (String stringRepresentation, String delimiter) {
        List list = stringRepresentation?.split(delimiter)
        return listToMap (list)
    }

    /**
     * Returns the map from the list-representation of map.
     *
     * @param list [a,b,c,d]
     * @return  [a:b, c:d]
     */
    public static def listToMap(list) {
        def map = [:]
        list?.eachWithIndex { elem, index ->
            if (index % 2 != 0) {
                 map[list[index-1]] = elem
            }
        }
        return map
    }


}
