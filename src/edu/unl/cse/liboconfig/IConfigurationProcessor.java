package edu.unl.cse.liboconfig;

import com.sun.star.uno.XInterface;

// Java callback interface
// Interface to process information when browsing the configuration tree
public interface IConfigurationProcessor {
    // process a value item
    public abstract void processValueElement(String sPath_, String sName_, Object aValue_);
    // process a structural item
    public abstract void processStructuralElement(String sPath_, XInterface xElement_);
};