package net.hedtech.banner.query.criteria

class DateQueryBuilder {
    StringBuilder stringBuilder
    public DateQueryBuilder() {
        stringBuilder = new StringBuilder()
    }
    public void append(String str) {
        stringBuilder.append(str);
    }

    public void appendDateSupport(String str) {
        stringBuilder.append("(TO_DATE(trunc(")
        stringBuilder.append(str)
        stringBuilder.append(")))")

        /*stringBuilder.append("(trunc(")
        stringBuilder.append(str)
        stringBuilder.append("))")*/
    }


    public void appendDateTimeSupport(String str) {
        stringBuilder.append("to_timestamp(TO_CHAR(")
        stringBuilder.append(str)
        stringBuilder.append(",'MM-DD-YYYY HH24:MI:SS'), 'MM-DD-YYYY HH24:MI:SS')")
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
