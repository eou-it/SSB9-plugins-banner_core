/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.query

class CriteriaOperatorFactory {

    static final operators = [equals:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.equals",
                                            operator:"=",
                                            key:"equals",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} = :${parammap.key} "},
                                            formatvalue : {value -> value } ],
                                equalsorisnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.equalsorisnull",
                                            operator:"=",
                                            key:"equalsorisnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and ($tableIdentifier.${parammap.binding} = :${parammap.key} OR $tableIdentifier.${parammap.binding} IS NULL) "},
                                            formatvalue : {value -> value } ],
                                contains:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.contains",
                                            operator:"like",
                                            key:"contains",
                                            dynamicQuery: {tableIdentifier, parammap -> " and lower($tableIdentifier.${parammap.binding}) like lower(:${parammap.key}) " },
                                            formatvalue : {value -> "%${value}%" } ],
                                startswith:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.startswith",
                                            operator:"like",
                                            key:"startswith",
                                            dynamicQuery: {tableIdentifier, parammap -> " and lower($tableIdentifier.${parammap.binding}) like lower(:${parammap.key}) " },
                                            formatvalue : {value -> "${value}%" }],
                                endswith:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.endswith",
                                            operator:"like",
                                            key:"endswith",
                                            dynamicQuery: {tableIdentifier, parammap -> " and lower($tableIdentifier.${parammap.binding}) like lower(:${parammap.key}) " },
                                            formatvalue : {value -> "%${value}" }],
                                between:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.between",
                                            operator:"like",
                                            key:"between",
                                            dynamicQuery: {tableIdentifier, parammap -> " and ($tableIdentifier.${parammap.binding} between :${parammap.key} and :${parammap.key}_and) " },
                                            formatvalue : {value -> value }],
                                numericbetween:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.between",
                                            operator:"like",
                                            key:"numericbetween",
                                            dynamicQuery: {tableIdentifier, parammap -> " and ($tableIdentifier.${parammap.binding} >= :${parammap.key} and $tableIdentifier.${parammap.binding} <= :${parammap.key}_and) " },
                                            formatvalue : {value -> value }],
                                notequals:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.notequals",
                                            operator:"!=",
                                            key:"notequals",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} != :${parammap.key} "},
                                            formatvalue : {value -> value }],
                                notequalsorisnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.notequalsorisnull",
                                            operator:"!=",
                                            key:"notequalsorisnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and ($tableIdentifier.${parammap.binding} != :${parammap.key} OR $tableIdentifier.${parammap.binding} IS NULL) "},
                                            formatvalue : {value -> value }],
                                greaterthan:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.greaterthan",
                                            operator:">",
                                            key:"greaterthan",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} > :${parammap.key}  "},
                                            formatvalue : {value -> value }],
                                greaterthanorisnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.greaterthanorisnull",
                                            operator:">",
                                            key:"greaterthanorisnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and ($tableIdentifier.${parammap.binding} > :${parammap.key} or $tableIdentifier.${parammap.binding} IS NULL) "},
                                            formatvalue : {value -> value }],
                                lessthan:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.lessthan",
                                            operator:"<",
                                            key:"lessthan",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} < :${parammap.key} "},
                                            formatvalue : {value -> value }],
                                lessthanorisnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.lessthanorisnull",
                                            operator:"<",
                                            key:"lessthanorisnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and ($tableIdentifier.${parammap.binding} < :${parammap.key} or $tableIdentifier.${parammap.binding} IS NULL) "},
                                            formatvalue : {value -> value }],
                                greaterthanequals:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.greaterthanequals",
                                            operator:">=",
                                            key:"greaterthanequals",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} >= :${parammap.key} "},
                                            formatvalue : {value -> value }],
                                greaterthanequalsorisnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.greaterthanequalsorisnull",
                                            operator:">=",
                                            key:"greaterthanequalsorisnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and ($tableIdentifier.${parammap.binding} >= :${parammap.key} or $tableIdentifier.${parammap.binding} IS NULL) "},
                                            formatvalue : {value -> value }],
                                lessthanequals:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.lessthanequals",
                                            operator:"<=",
                                            key:"lessthanequals",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} <= :${parammap.key}  "},
                                            formatvalue : {value -> value }],
                                lessthanequalsorisnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.lessthanequalsorisnull",
                                            operator:"<=",
                                            key:"lessthanequalsorisnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and ($tableIdentifier.${parammap.binding} <= :${parammap.key} or $tableIdentifier.${parammap.binding} IS NULL) "},
                                            formatvalue : {value -> value }],
                                isnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.isnull",
                                            operator:"is null",
                                            key:"isnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} IS NULL "},
                                            formatvalue : {value -> value }],
                                isnotnull:[label:"com.sungardhe.banner.ui.zk.search.advancedSearch.operator.isnotnull",
                                            operator:"is not null",
                                            key:"isnotnull",
                                            dynamicQuery: {tableIdentifier, parammap-> " and $tableIdentifier.${parammap.binding} IS NOT NULL "},
                                            formatvalue : {value -> value }]
                              ]

        static final operatorGroups = [alphanumeric:[
                                            [operator:operators.contains, default:"true"],
                                            [operator:operators.startswith],
                                            [operator:operators.endswith],
                                            [operator:operators.equals],
                                            [operator:operators.notequals],
                                            [operator:operators.notequalsorisnull],
                                            [operator:operators.isnull],
                                            [operator:operators.isnotnull]
                                        ],
                                        numeric:[
                                            [operator:operators.equals, default:"true" ],
                                            [operator:operators.notequals],
                                            [operator:operators.notequalsorisnull],
                                            [operator:operators.numericbetween],
                                            [operator:operators.greaterthan],
                                            [operator:operators.greaterthanequals],
                                            [operator:operators.lessthan],
                                            [operator:operators.lessthanorisnull],
                                            [operator:operators.lessthanequals],
                                            [operator:operators.lessthanequalsorisnull],
                                            [operator:operators.isnull],
                                            [operator:operators.isnotnull]
                                        ],
                                        date:[
                                            [operator:operators.equals, default:"true" ],
                                            [operator:operators.between],
                                            [operator:operators.greaterthan],
                                            [operator:operators.greaterthanequals],
                                            [operator:operators.lessthan],
                                            [operator:operators.lessthanorisnull],
                                            [operator:operators.lessthanequals],
                                            [operator:operators.lessthanequalsorisnull],
                                            [operator:operators.isnull],
                                            [operator:operators.isnotnull]
                                        ],
                                        boolean:[
                                            [operator:operators.equals, default:"true"]
                                        ],
                                        checkbox:[
                                            [operator:operators.equalsorisnull, default:"true"]
                                        ],
                                        radio:[
                                            [operator:operators.equalsorisnull, default:"true"]
                                        ]

        ]
}
