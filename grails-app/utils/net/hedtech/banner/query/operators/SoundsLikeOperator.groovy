/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData

class SoundsLikeOperator extends CriteriaOperator {
    public SoundsLikeOperator () {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.soundslike";
        this.operator = "sounds like";
        this.key = Operators.SOUNDS_LIKE
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            return "((soundex(${data.tableAlias}.${data.tableBindingAttribute}) = soundex(:${data.paramKey})) or :${data.paramKey} is null)"
        }
        return ""
    }
}
