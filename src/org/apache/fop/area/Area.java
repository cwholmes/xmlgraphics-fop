/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.io.Serializable;

// If the area appears more than once in the output
// or if the area has external data it is cached
// to keep track of it and to minimize rendered output
// renderers can render the output once and display it
// for every occurence
// this should also extend to all outputs (including PDFGraphics2D)
// and all types of renderers

/**
 * Base object for all areas.
 */
public class Area implements Serializable {
    // stacking directions
    public static final int LR = 0;
    public static final int RL = 1;
    public static final int TB = 2;
    public static final int BT = 3;

    // orientations for reference areas
    public static final int ORIENT_0 = 0;
    public static final int ORIENT_90 = 1;
    public static final int ORIENT_180 = 2;
    public static final int ORIENT_270 = 3;

}
