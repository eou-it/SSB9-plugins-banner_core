package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class NotEqualsOperator extends CriteriaOperator {

    public NotEqualsOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.notequals";
        this.operator = "!=";
        this.key = Operators.NOT_EQUALS
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);
            return "${data.tableAlias}.${data.tableBindingAttribute} != :${data.paramKey}"
        }

    }
}
