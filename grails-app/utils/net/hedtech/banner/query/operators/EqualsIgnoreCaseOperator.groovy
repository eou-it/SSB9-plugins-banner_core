package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class EqualsIgnoreCaseOperator extends CriteriaOperator {

    public EqualsIgnoreCaseOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.equals";
        this.operator = "=";
        this.key = Operators.EQUALS_IGNORE_CASE
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);
            return "lower(${data.tableAlias}.${data.tableBindingAttribute}) = lower(:${data.paramKey})"
        }
        return ""
    }
}
