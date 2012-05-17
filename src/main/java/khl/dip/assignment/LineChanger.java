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
public interface LineChanger {
    ImageProcessor changeLine(int[] toRemove, ImageProcessor imageProcessor);
}
