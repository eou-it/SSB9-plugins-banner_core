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
package com.sungardhe.banner.menu

/**
 * This class represents menu item.
 * 
 * @author Rajesh Kumar
 * 
 *  This class contains representation of a banner menu item. 
 *    
 */
public class Menu implements Serializable {
    int seq
    def menu
    def pageName
    def formName
    def url
    def caption
    def level
    def module
    def parent
    def type
    def path
    def captionProperty
    def pageCaption
}

