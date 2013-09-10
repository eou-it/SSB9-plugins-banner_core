package net.hedtech.banner.loginworkflow
/** *****************************************************************************
 © 2013 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
abstract class PostLoginWorkflow {
    static Map flowEntries = new TreeMap();
    public abstract boolean showPage(request);
    public abstract String getControllerUri();

    public setRegisterFlowClass(Map entryMap) {
        Set <Object>keySet = entryMap.keySet();
        for(Object key : keySet)
        {
            flowEntries.put(key, entryMap.get(key));
        }
    }

    public static List getListOfFlows() {
        return new ArrayList(flowEntries.values());
    }
}
