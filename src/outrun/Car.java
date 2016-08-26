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
 * This class controls the NPC cars.
 */
public class Car {
	OffsetImage sprite;
	
	float percent; //How far along the current segment the car has gotten.
	float offset; //It's x-position offset along the road.
	
	float z; //It's Z position along the road.
	float speed;
	float speedMax;
	float carW; //Car width;
	
	public OffsetImage getSprite() { return sprite; }
	public float getPercent() { return percent; }
	public float getOffset() { return offset; }
	
	/*
	 * Handles the moving of the car, and basic collision.
	 */
	public void update(float dt, RoadSegment playerSegment, Road road, float playerW, Player player) {
		RoadSegment oldSegment = road.findSegment(z);
		offset = offset + updateOffset(oldSegment, playerSegment, playerW, road, player);
		z += dt * speed;
		while (z > road.getTrackLength()) { z -= road.getTrackLength(); }
		while (z < 0) { z += road.getTrackLength(); }
		
		percent = (z % road.getSegmentLength()) / road.getSegmentLength();
		RoadSegment newSegment = road.findSegment(z);
		
		//The car is stored in segments in the road, so it's necessary to constantly 
		// keep in track of which segment it should exist in.
		if (oldSegment != newSegment) {
			int index = oldSegment.getCars().indexOf(this);
			oldSegment.getCars().remove(this);
			newSegment.getCars().add(this);
		}
	}
	
	/*
	 * Updates the offset of the car.
	 */
	public float updateOffset(RoadSegment carSegment, RoadSegment playerSegment, float playerW, Road road, Player player) {
		int lookahead = 20;
		
		if ((carSegment.index - playerSegment.index) > OutRunGame.drawDistance) {
			return 0;
		}
		
		for (int i = 1; i < lookahead; i++) {
			RoadSegment segment = road.findSegment((carSegment.index+i) % road.getSegments().length);
			
			float dir;
			
			if ((segment == playerSegment) && (player.getSpeed() > speed) &&
					(overlap(player.getPosX(), player.getWidth(), offset, carW, 1.2f))) {
				if (player.getPosX() > 0.5)
					dir = -1;
				else if (player.getPosX() < -0.5)
					dir = 1;
				else
					dir = (offset > player.getPosX()) ? 1 : -1;
				return dir * 1/i * (speed-player.getSpeed())/OutRunGame.speedMax;
			}
			
			for (int j = 0; j < segment.getCars().size(); j++) {
				Car otherCar = segment.getCars().get(j);
				if ((speed > otherCar.speed) && overlap(offset * road.getRoadWidth(), carW, otherCar.offset * road.getRoadWidth(), otherCar.carW, 1.2f)) {
					if (otherCar.offset > 0.5)
						dir = -1;
					else if (otherCar.offset < -0.5)
						dir = 1;
					else
						dir = (offset > otherCar.offset) ? 1 : -1;
					return dir * 1/i * (speed-otherCar.speed)/OutRunGame.speedMax;
				}
			}
		}
		
		if (offset < -0.9)
			return 0.1f;
		else if (offset > 0.9)
			return -0.1f;
		else
			return 0;
	}
	
	/*
	 * Basic collision.
	 */
	boolean overlap(float x1, float w1, float x2, float w2, float percent) {
		float half = percent / 2;
		
		float min1 = x1 - (w1*half);
		float max1 = x1 + (w1*half);
		float min2 = x2 - (w2*half);
		float max2 = x2 + (w2*half);
		return ! ((max1 < min2) || (min1 > max2));
	}
}
