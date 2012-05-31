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

package org.apache.fop.fonts;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.io.URIResolverWrapper;
import org.apache.fop.fonts.truetype.TTFFontLoader;
import org.apache.fop.fonts.type1.Type1FontLoader;

/**
 * Base class for font loaders.
 */
public abstract class FontLoader {

    /** logging instance */
    protected static final Log log = LogFactory.getLog(FontLoader.class);

    /** URI representing the font file */
    protected final URI fontFileURI;
    /** the FontResolver to use for font URI resolution */
    protected final URIResolverWrapper resolver;
    /** the loaded font */
    protected CustomFont returnFont;

    /** true if the font has been loaded */
    protected boolean loaded;
    /** true if the font will be embedded, false if it will be referenced only. */
    protected boolean embedded;
    /** true if kerning information false be loaded if available. */
    protected boolean useKerning;
    /** true if advanced typographic information shall be loaded if available. */
    protected boolean useAdvanced;

    /**
     * Default constructor.
     * @param fontFileURI the URI to the PFB file of a Type 1 font
     * @param embedded indicates whether the font is embedded or referenced
     * @param useKerning indicates whether kerning information shall be loaded if available
     * @param useAdvanced indicates whether advanced typographic information shall be loaded if
     * available
     * @param resolver the font resolver used to resolve URIs
     */
    public FontLoader(URI fontFileURI, boolean embedded, boolean useKerning,
            boolean useAdvanced, URIResolverWrapper resolver) {
        this.fontFileURI = fontFileURI;
        this.embedded = embedded;
        this.useKerning = useKerning;
        this.useAdvanced = useAdvanced;
        this.resolver = resolver;
    }

    private static boolean isType1(URI fontURI) {
        return fontURI.toASCIIString().toLowerCase().endsWith(".pfb");
    }

    /**
     * Loads a custom font from a URI. In the case of Type 1 fonts, the PFB file must be specified.
     * @param fontFileURI the URI to the font
     * @param subFontName the sub-fontname of a font (for TrueType Collections, null otherwise)
     * @param embedded indicates whether the font is embedded or referenced
     * @param encodingMode the requested encoding mode
     * @param useKerning indicates whether kerning information should be loaded if available
     * @param useAdvanced indicates whether advanced typographic information shall be loaded if
     * available
     * @param resolver the font resolver to use when resolving URIs
     * @return the newly loaded font
     * @throws IOException In case of an I/O error
     */
    public static CustomFont loadFont(URI fontFileURI, String subFontName,
            boolean embedded, EncodingMode encodingMode, boolean useKerning,
            boolean useAdvanced, URIResolverWrapper resolver) throws IOException {
        boolean type1 = isType1(fontFileURI);
        FontLoader loader;
        if (type1) {
            if (encodingMode == EncodingMode.CID) {
                throw new IllegalArgumentException(
                        "CID encoding mode not supported for Type 1 fonts");
            }
            loader = new Type1FontLoader(fontFileURI, embedded, useKerning, resolver);
        } else {
            loader = new TTFFontLoader(fontFileURI, subFontName,
                    embedded, encodingMode, useKerning, useAdvanced, resolver);
        }
        return loader.getFont();
    }

    /**
     * Reads/parses the font data.
     * @throws IOException In case of an I/O error
     */
    protected abstract void read() throws IOException;

    /**
     * Returns the custom font that was read using this instance of FontLoader.
     * @return the newly loaded font
     * @throws IOException if an I/O error occurs
     */
    public CustomFont getFont() throws IOException {
        if (!loaded) {
            read();
        }
        return this.returnFont;
    }
}
