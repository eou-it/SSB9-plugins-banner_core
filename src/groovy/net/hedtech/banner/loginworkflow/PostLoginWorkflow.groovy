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
    private static Map<String,PostLoginWorkflow> flowEntries = new TreeMap();
    public static final String URI_ACCESSED = "SS_POST_lOGIN_WORKFLOW_URI_ACCESSED";
    public static final String URI_REDIRECTED = "SS_POST_lOGIN_WORKFLOW_URI_REDIRECTED";
    public static final String FLOW_COMPLETE = "SS_POST_lOGIN_WORKFLOW_COMPLETE";

    public abstract boolean isShowPage(request);
    public abstract String getControllerUri();
    public abstract String getControllerName();

    public setRegisterFlowClass(Map<Integer, String> entryMap) {
        Set <Object>keySet = entryMap.keySet();
        for(Object key : keySet)
        {
            flowEntries.put(key, this);
        }
    }

    public static List<PostLoginWorkflow> getListOfFlows() {
        return new ArrayList(flowEntries.values());
    }

}
