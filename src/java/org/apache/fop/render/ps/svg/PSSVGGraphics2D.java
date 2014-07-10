/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.ps.svg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.render.shading.Function;
import org.apache.fop.render.shading.GradientFactory;
import org.apache.fop.render.shading.GradientRegistrar;
import org.apache.fop.render.shading.PSGradientFactory;
import org.apache.fop.render.shading.Pattern;
import org.apache.fop.render.shading.Shading;


public class PSSVGGraphics2D extends PSGraphics2D implements GradientRegistrar {

    private static final Log LOG = LogFactory.getLog(PSSVGGraphics2D.class);

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSSVGGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    /**
     * Create a new Graphics2D that generates PostScript code.
     * @param textAsShapes True if text should be rendered as graphics
     * @param gen PostScript generator to use for output
     * @see org.apache.xmlgraphics.java2d.AbstractGraphics2D#AbstractGraphics2D(boolean)
     */
    public PSSVGGraphics2D(boolean textAsShapes, PSGenerator gen) {
        super(textAsShapes, gen);
    }

    /**
     * Constructor for creating copies
     * @param g parent PostScript Graphics2D
     */
    public PSSVGGraphics2D(PSGraphics2D g) {
        super(g);
    }

    protected void applyPaint(Paint paint, boolean fill) {
        super.applyPaint(paint, fill);
        if (paint instanceof RadialGradientPaint) {
            RadialGradientPaint rgp = (RadialGradientPaint)paint;
            try {
                handleRadialGradient(rgp, gen);
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        } else if (paint instanceof LinearGradientPaint) {
            LinearGradientPaint lgp = (LinearGradientPaint)paint;
            try {
                handleLinearGradient(lgp, gen);
            } catch (IOException ioe) {
                handleIOException(ioe);
            }
        }
    }

    private void handleLinearGradient(LinearGradientPaint lgp, PSGenerator gen) throws IOException {
        MultipleGradientPaint.CycleMethodEnum cycle = lgp.getCycleMethod();
        if (cycle != MultipleGradientPaint.NO_CYCLE) {
            return;
        }
        float[] fractions = lgp.getFractions();
        Color[] cols = lgp.getColors();

        AffineTransform transform = new AffineTransform(getBaseTransform());
        transform.concatenate(getTransform());
        transform.concatenate(lgp.getTransform());

        List theMatrix = new ArrayList();
        double [] mat = new double[6];
        transform.getMatrix(mat);
        for (int idx = 0; idx < mat.length; idx++) {
            theMatrix.add(Double.valueOf(mat[idx]));
        }


        List<Double> theCoords = new java.util.ArrayList<Double>();
        theCoords.add(lgp.getStartPoint().getX());
        theCoords.add(lgp.getStartPoint().getX());
        theCoords.add(lgp.getEndPoint().getX());
        theCoords.add(lgp.getEndPoint().getY());


        List<Color> someColors = new java.util.ArrayList<Color>();
        if (fractions[0] > 0f) {
            someColors.add(cols[0]);
        }
        for (int count = 0; count < cols.length; count++) {
            Color c1 = cols[count];
            if (c1.getAlpha() != 255) {
                LOG.warn("Opacity is not currently supported for Postscript output");
            }
            someColors.add(c1);
        }
        if (fractions[fractions.length - 1] < 1f) {
            someColors.add(cols[cols.length - 1]);
        }
        List<Double> theBounds = new java.util.ArrayList<Double>();
        for (int count = 0; count < fractions.length; count++) {
            float offset = fractions[count];
            if (0f < offset && offset < 1f) {
                theBounds.add(new Double(offset));
            }
        }
        PDFDeviceColorSpace colSpace;
        colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

        PSGradientFactory gradientFactory = (PSGradientFactory)GradientFactory.newInstance(this);
        PSPattern myPattern = gradientFactory.createGradient(false, colSpace,
                someColors, theBounds, theCoords, theMatrix);

        gen.write(myPattern.toString());

    }



    private void handleRadialGradient(RadialGradientPaint rgp, PSGenerator gen) throws IOException {
        MultipleGradientPaint.CycleMethodEnum cycle = rgp.getCycleMethod();
        if (cycle != MultipleGradientPaint.NO_CYCLE) {
            return;
        }

        AffineTransform transform;
        transform = new AffineTransform(getBaseTransform());
        transform.concatenate(getTransform());
        transform.concatenate(rgp.getTransform());

        AffineTransform resultCentre = applyTransform(rgp.getTransform(),
                rgp.getCenterPoint().getX(), rgp.getCenterPoint().getY());
        AffineTransform resultFocus = applyTransform(rgp.getTransform(),
                rgp.getFocusPoint().getX(), rgp.getFocusPoint().getY());

        List<Double> theMatrix = new java.util.ArrayList<Double>();
        double [] mat = new double[6];
        transform.getMatrix(mat);
        for (int idx = 0; idx < mat.length; idx++) {
            theMatrix.add(Double.valueOf(mat[idx]));
        }

        float[] fractions = rgp.getFractions();

        double ar = rgp.getRadius();
        Point2D ac = rgp.getCenterPoint();
        Point2D af = rgp.getFocusPoint();
        List<Double> theCoords = new java.util.ArrayList<Double>();
        double dx = af.getX() - ac.getX();
        double dy = af.getY() - ac.getY();
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d > ar) {
            // the center point af must be within the circle with
            // radius ar centered at ac so limit it to that.
            double scale = (ar * .9999) / d;
            dx = dx * scale;
            dy = dy * scale;
        }

        theCoords.add(new Double(ac.getX() + dx)); // Fx
        theCoords.add(new Double(ac.getY() + dy)); // Fy
        theCoords.add(new Double(0));
        theCoords.add(new Double(ac.getX()));
        theCoords.add(new Double(ac.getY()));
        theCoords.add(new Double(ar));

        Color[] cols = rgp.getColors();
        List<Color> someColors = new java.util.ArrayList<Color>();
        if (fractions[0] > 0f) {
            someColors.add(cols[0]);
        }
        for (int count = 0; count < cols.length; count++) {
            Color cc = cols[count];
            if (cc.getAlpha() != 255) {
                /* This should never happen because radial gradients with opacity should now
                 * be rasterized in the PSImageHandlerSVG class. Please see the shouldRaster()
                 * method for more information. */
                LOG.warn("Opacity is not currently supported for Postscript output");
            }

            someColors.add(cc);
        }
        if (fractions[fractions.length - 1] < 1f) {
            someColors.add(cols[cols.length - 1]);
        }

        List<Double> theBounds = new java.util.ArrayList<Double>();
        for (int count = 0; count < fractions.length; count++) {
            float offset = fractions[count];
            if (0f < offset && offset < 1f) {
                theBounds.add(new Double(offset));
            }
        }
        PDFDeviceColorSpace colSpace;
        colSpace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);

        PSGradientFactory gradientFactory = (PSGradientFactory)GradientFactory.newInstance(this);
        PSPattern myPattern = gradientFactory.createGradient(true, colSpace,
                someColors, theBounds, theCoords, theMatrix);

        gen.write(myPattern.toString());
    }

    private AffineTransform applyTransform(AffineTransform base, double posX, double posY) {
        AffineTransform result = AffineTransform.getTranslateInstance(posX, posY);
        AffineTransform orig = base;
        orig.concatenate(result);
        return orig;
    }

    protected AffineTransform getBaseTransform() {
        AffineTransform at = new AffineTransform(this.getTransform());
        return at;
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    @Override
    public Graphics create() {
        preparePainting();
        return new PSSVGGraphics2D(this);
    }

    /**
     * Registers a function object against the output format document
     * @param function The function object to register
     * @return Returns either the function which has already been registered
     * or the current new registered object.
     */
    public Function registerFunction(Function function) {
        //Objects aren't needed to be registered in Postscript
        return function;
    }

    /**
     * Registers a shading object against the otuput format document
     * @param shading The shading object to register
     * @return Returs either the shading which has already been registered
     * or the current new registered object
     */
    public Shading registerShading(Shading shading) {
        //Objects aren't needed to be registered in Postscript
        return shading;
    }

    /**
     * Registers a pattern object against the output format document
     * @param pattern The pattern object to register
     * @return Returns either the pattern which has already been registered
     * or the current new registered object
     */
    public Pattern registerPattern(Pattern pattern) {
        // TODO Auto-generated method stub
        return pattern;
    }
}
