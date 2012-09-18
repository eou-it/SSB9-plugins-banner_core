/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.representations;


/**
 * A resource representation handler is responsible for both 'params extraction' and representation formatting
 * ready for rendering.  
 */
public interface ResourceRepresentationHandler {
    
    public String getRepresentationName();

    public Class getModelClass();

    public ParamsExtractor paramsExtractor();

    public RepresentationBuilder singleBuilder();

    public RepresentationBuilder collectionBuilder();

}
