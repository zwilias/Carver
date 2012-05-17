/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package khl.dip.assignment;

import ij.process.ImageProcessor;

/**
 *
 * @author ilias
 */
public abstract class LineChanger {
    public ImageProcessor changeLine(int[] toChange, ImageProcessor imageProcessor, boolean addLines) {
        if (addLines) {
            return addLine(toChange, imageProcessor);
        } else {
            return removeLine(toChange, imageProcessor);
        }
    }
    
    public abstract ImageProcessor addLine(int[] toAdd, ImageProcessor imageProcessor);
    public abstract ImageProcessor removeLine(int[] toRemove, ImageProcessor imageProcessor);
}
