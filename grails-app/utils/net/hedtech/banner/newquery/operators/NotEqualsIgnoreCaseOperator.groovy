package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class NotEqualsIgnoreCaseOperator extends CriteriaOperator {

    public NotEqualsIgnoreCaseOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.notequals";
        this.operator = "!=";
        this.key = Operators.NOT_EQUALS_IGNORE_CASE
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);

            return "lower(${data.tableAlias}.${data.tableBindingAttribute}) != lower(:${data.paramKey})"
        }

    }
}
