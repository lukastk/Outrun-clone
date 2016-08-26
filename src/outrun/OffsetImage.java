/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import org.newdawn.slick.Image;

/**
 *
 * @author lukas_000
 * 
 * Contains an image and some information on the offset in use when drawing it.
 */
public class OffsetImage {
	public Image image;
	public float offsetX;
	public float offsetY;
	public boolean flip;
	
	public OffsetImage(Image image, float offsetX, float offsetY) {
		this.image = image;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public OffsetImage flip() {
		flip = !flip;
		return this;
	}
}
