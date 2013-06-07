package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class SoundsLikeOperator extends CriteriaOperator {
    public SoundsLikeOperator () {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.soundslike";
        this.operator = "sounds like";
        this.key = Operators.SOUNDS_LIKE
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);
            return "((soundex(${data.tableAlias}.${data.tableBindingAttribute} = soundex(:${data.paramKey})) or :${data.paramKey} is null)"
        }
        return ""
    }
}
