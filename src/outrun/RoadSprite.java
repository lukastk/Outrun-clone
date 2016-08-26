/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import org.newdawn.slick.Image;

/**
 *
 * @author lukas_000
 */
public class RoadSprite {
	private OffsetImage sprite;
	private float offset;
	
	public OffsetImage getSprite() { return sprite; }
	public float getOffset() { return offset; }
	
	public RoadSprite(OffsetImage sprite, float offset) {
		this.sprite = sprite;
		this.offset = offset;
	}
}
