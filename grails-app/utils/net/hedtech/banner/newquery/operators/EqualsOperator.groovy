package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.Query
import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class EqualsOperator extends CriteriaOperator {

    public EqualsOperator () {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.equals";
        this.operator = "=";
        this.key = Operators.EQUALS
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            CriteriaParam param = data.params.get(0);

            if(param.data instanceof Date) {
                return new DateEqualsOperator().getQueryString(data)
            }
        }
        return "${data.tableAlias}.${data.tableBindingAttribute} = :${data.paramKey} "
    }
}
