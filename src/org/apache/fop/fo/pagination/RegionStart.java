/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.RegionArea;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.apps.FOPException;

public class RegionStart extends Region {

    public static final String REGION_CLASS = "start";


    public RegionStart(FONode parent) {
        super(parent);
    }

    RegionArea makeRegionArea(int allocationRectangleXPosition,
                              int allocationRectangleYPosition,
                              int allocationRectangleWidth,
                              int allocationRectangleHeight,
                              boolean beforePrecedence,
                              boolean afterPrecedence, int beforeHeight,
                              int afterHeight) {
        int extent = this.properties.get("extent").getLength().mvalue();

        int startY = allocationRectangleYPosition;
        int startH = allocationRectangleHeight;
        if (beforePrecedence) {
            startY -= beforeHeight;
            startH -= beforeHeight;
        }
        if (afterPrecedence)
            startH -= afterHeight;
        return new RegionArea(allocationRectangleXPosition, startY, extent,
                              startH);
    }

    RegionArea makeRegionArea(int allocationRectangleXPosition,
                              int allocationRectangleYPosition,
                              int allocationRectangleWidth,
                              int allocationRectangleHeight) {

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // this.properties.get("clip");
        // this.properties.get("display-align");
        int extent = this.properties.get("extent").getLength().mvalue();
        // this.properties.get("overflow");
        // this.properties.get("region-name");
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        return makeRegionArea(allocationRectangleXPosition,
                              allocationRectangleYPosition,
                              allocationRectangleWidth, extent, false, false,
                              0, 0);
    }

    protected String getDefaultRegionName() {
        return "xsl-region-start";
    }

    protected String getElementName() {
        return "fo:region-start";
    }

    public String getRegionClass() {
        return REGION_CLASS;
    }

}
