/*
 * Copyright (c) 2003 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */

package com.jgoodies.forms.util;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

/**
 * This is the default implementation of the {@link UnitConverter} interface.
 * It converts horizontal and vertical dialog base units to pixels.
 * <p> 
 * The horizontal base unit is equal to the average width, in pixels,
 * of the characters in the system font; the vertical base unit is equal
 * to the height, in pixels, of the font.
 * <p>
 * Each horizontal base unit is equal to 4 horizontal dialog units;
 * each vertical base unit is equal to 8 vertical dialog units.
 * 
 * @author  Karsten Lentzsch
 * @see     UnitConverter
 * @see     com.jgoodies.forms.layout.Size
 * @see     com.jgoodies.forms.layout.Sizes
 */
public final class DefaultUnitConverter extends AbstractUnitConverter {
    
//    public static final String UPPERCASE_ALPHABET =
//        "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
//
//    public static final String LOWERCASE_ALPHABET =
//        "abcdefghijklmnopqrstuvwxyz";

    /**
     * Holds the sole instance that will be lazily instantiated.
     */
    private static DefaultUnitConverter instance;

    /**
     * Holds the string that is used to compute the average character width.
     * By default this is just &quot;X&quot;.
     */
    private String averageCharWidthTestString = "X";    


    // Cached *****************************************************************
    
    /**
     * Holds the cached global dialog base units that are used if 
     * a component is not (yet) available - for example in a Border.
     */    
    private DialogBaseUnits cachedGlobalDialogBaseUnits = 
        computeGlobalDialogBaseUnits();
        
    /**
     * Maps <code>FontMetrics</code> to horizontal dialog base units.
     * This is a second-level cache, that stores dialog base units
     * for a <code>FontMetrics</code> object.
     */ 
    private Map cachedDialogBaseUnits = new HashMap();


    // Instance Creation and Access *******************************************
    
    /**
     * Constructs a <code>DefaultFontUnitConverter</code> and registers
     * a listener that handles changes in the look&amp;feel.
     */
    private DefaultUnitConverter () {
        UIManager.addPropertyChangeListener(new LAFChangeHandler());
    }
    
    /**
     * Lazily instantiates and returns the sole instance.
     */
    public static DefaultUnitConverter getInstance() {
        if (instance == null) {
            instance = new DefaultUnitConverter();
        }
        return instance;
    }
    
    
    // Setting the Average Character Width Test String ************************

    /**
     * Sets a string that will be used to compute the average character width.
     * By default it is just the &quot;X&quot; character. You can provide
     * other test strings, for example: 
     * <ul>
     *  <li>&quot;Xximeee&quot;</li>
     *  <li>&quot;ABCEDEFHIJKLMNOPQRSTUVWXYZ&quot;</li> 
     *  <li>&quot;abcdefghijklmnopqrstuvwxyz&quot;</li>
     * 
     * @param newTestString   the test string to be set
     */
    public void setAverageCharacterWidthTestString(String newTestString) {
        averageCharWidthTestString = newTestString;
    }
    

    // Implementing Abstract Superclass Behavior ******************************
    
    /**
     * Answers the cached or computed horizontal dialog base units. 
     * 
     * @param component     a Component that provides the font and graphics
     * @return the horizontal dialog base units
     */
    protected double getDialogBaseUnitsX(Component component) {
        return getDialogBaseUnits(component).x;
    }
    
    /**
     * Answers the cached or computed vertical dialog base units
     * for the given component.  
     * 
     * @param component     a Component that provides the font and graphics
     * @return the vertical dialog base units
     */
    protected double getDialogBaseUnitsY(Component component) {
        return getDialogBaseUnits(component).y;
    }


    // Compute and Cache Global and Components Dialog Base Units **************
    
    /**
     * Lazily computes and answer the global dialog base units.
     * Should be re-computed if the l&amp;f, platform, or screen changes. 
     */
    private DialogBaseUnits getGlobalDialogBaseUnits() {
        if (cachedGlobalDialogBaseUnits == null) {
            cachedGlobalDialogBaseUnits = computeGlobalDialogBaseUnits();
        }
        return cachedGlobalDialogBaseUnits;
    }
    
    /**
     * Looks up and answers the dialog base units for the given component.
     * In case the component is <code>null</code> the global dialog base units
     * are answered.
     * <p>
     * Before we compute the dialog base units, we check wether they
     * have been computed and cached before - for the same component
     * <code>FontMetrics</code>.
     * 
     * @param c  the component that provides the graphics object
     * @return
     */
    private DialogBaseUnits getDialogBaseUnits(Component c) {
        if (c == null) { // || (font = c.getFont()) == null) {
            logInfo("Missing font metrics: " + c);
            return getGlobalDialogBaseUnits();
        }
        FontMetrics fm = c.getFontMetrics(getDefaultDialogFont());
        DialogBaseUnits dialogBaseUnits = (DialogBaseUnits) cachedDialogBaseUnits.get(fm);
        if (dialogBaseUnits == null) {
            dialogBaseUnits = computeDialogBaseUnits(fm);
            cachedDialogBaseUnits.put(fm, dialogBaseUnits);
        }
        return dialogBaseUnits;
    }
    
    /**
     * Computes and answers the horizontal dialog base units. 
     * Honors the font, font size and resolution.
     * <p>
     * Implementation Note: 14dluY map to 22 pixel for 8pt Tahoma on 96 dpi.
     * I could not yet manage to compute the Microsoft compliant font height.
     * Therefore this method adds a correction value that seems to work
     * well with the vast majority of desktops.
     * Anyway, I plan to revise this, as soon as I have more information
     * about the original computation for vertical dialog base units.
     * 
     * @return the horizontal and vertical dialog base units
     */
    private DialogBaseUnits computeDialogBaseUnits(FontMetrics metrics) {
        double averageCharWidth =
            computeAverageCharWidth(metrics, averageCharWidthTestString);
        int    ascent = metrics.getAscent();
        double height = ascent > 14 ? ascent : ascent + (15 - ascent) / 3;
        DialogBaseUnits dialogBaseUnits =
            new DialogBaseUnits(averageCharWidth, height);
        logInfo(
            "Computed dialog base units "
                + dialogBaseUnits
                + " for: "
                + metrics.getFont());
        return dialogBaseUnits;
    }

    /**
     * Computes the global dialog base units. The current implementation
     * assumes a fixed 8pt font and on 96 or 120 dpi. A better implementation
     * should ask for the main dialog font and should honor the current
     * screen resolution.
     * <p>
     * Should be re-computed if the l&amp;f, platform, or screen changes. 
     */
    private DialogBaseUnits computeGlobalDialogBaseUnits() {
        logInfo("Computing global dialog base units...");
        Font dialogFont = getDefaultDialogFont();
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(dialogFont);
        DialogBaseUnits globalDialogBaseUnits = computeDialogBaseUnits(metrics);
        return globalDialogBaseUnits;
//        boolean isLowResolution =
//            Toolkit.getDefaultToolkit().getScreenResolution() < 112;
//        return isLowResolution
//            ? new DialogBaseUnits(6, 11)
//            : new DialogBaseUnits(8, 14);
    }
    
    private Font getDefaultDialogFont() {
        return UIManager.getFont("Button.font");
    }
    
    /**
     * Invalidates the caches. Resets the global dialog base units
     * and clears the Map from <code>FontMetrics</code> to dialog base units.
     * This is invoked after a change of the look&amp;feel.
     */
    private void invalidateCaches() {
        cachedGlobalDialogBaseUnits = null;
        cachedDialogBaseUnits.clear();
    }
    
    
    // Helper Code ************************************************************
    
    /**
     * Logs an info message to the console.
     * @param message    the message to log
     */
    private void logInfo(String message) {
//        System.out.println("INFO (DefaultUnitConverter) " + message);
    }
    
    
    // Describes horizontal and vertical dialog base units.
    private static class DialogBaseUnits {
        
        final double x;
        final double y;
        
        DialogBaseUnits(double dialogBaseUnitsX, double dialogBaseUnitsY) {
            this.x = dialogBaseUnitsX;
            this.y = dialogBaseUnitsY;
        }
        
        public String toString() {
            return "DBU(x=" + x + "; y=" + y + ")"; 
        }
    }
    
    // Listens to changes of the Look and Feel and invalidates a cache
    private class LAFChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            invalidateCaches();
        }
    }
    
}