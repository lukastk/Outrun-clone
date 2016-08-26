/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.Random;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.PackedSpriteSheet;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.XMLPackedSheet;

/**
 *
 * @author lukas_000
 */
public class Player {
	Random rand;
	
	Image player_l;
	Image player_l_uphill;
	Image player_r;
	Image player_r_uphill;
	Image player_f;
	Image player_f_uphill;
	
	float playerX;
	float playerZ;
	float playerY;
	int position;  // the z position of the player along the road.
	float speed;
	float speedMax;
	float accel;  
	float breaking; 
	float decel;  // the friction from the road that counteracts the forward velocity.
	float offRoadDecel;  // the friction from the grass that counteracts the forward velocity.
	float offRoadLimit; 
	Image currenSprite;
	
	DriveMode driveMode;
	
	public boolean hasWon;
	
	public enum DriveMode {
		FORWARD, LEFT, RIGHT
	}
	
	public int getPosition() { return position; }
	public float getPosX() { return playerX; }
	public float getPosY() { return playerY; }
	public float getPosZ() { return playerZ; }
	public float getWidth() { return getCurrentImage().getWidth(); }
	
	public Image getCurrentImage() { return currenSprite; }
			
	public float getSpeed() { return speed; }
	public float getSpeedMax() { return speedMax; }
	public float getSpeedPercent() { return speed/speedMax; }
	
	public Player(float playerZ) throws SlickException {
		speedMax = OutRunGame.speedMax;  // top speed (ensure we can't move more than 1 segment in a single frame to make collision detection easier)
		accel =  speedMax/5;  // acceleration rate - tuned until it 'felt' right
		breaking = -speedMax;  // deceleration rate when braking
		decel = -speedMax/5;  // 'natural' deceleration rate when neither accelerating, nor braking
		offRoadDecel = -speedMax/2;  // off road deceleration is somewhere in between
		offRoadLimit =  speedMax/4;  // limit when off road deceleration no longer applies (e.g. you can always go at least this speed even when off road)
		
		this.playerZ = playerZ;
		
		rand = new Random();
		
		player_l = new Image("res/player/player_l.png");
		player_l_uphill = new Image("res/player/player_l_uphill.png");
		player_r = new Image("res/player/player_r.png");
		player_r_uphill = new Image("res/player/player_r_uphill.png");
		player_f = new Image("res/player/player_f.png");
		player_f_uphill = new Image("res/player/player_f_uphill.png");
		
		driveMode = DriveMode.FORWARD;
		currenSprite = player_f;
	}
	
	public void render(Graphics g, int screenWidth, int screenHeight, float res,
			int roadWidth, float cameraDepth, int destX, int destY, float deltaY) {
		
		float speedPercentage = speed / speedMax;
		float bounce = (float)(rand.nextInt(2) - 1);
		
		if (rand.nextBoolean())
			bounce = -bounce;
		
		int tilt_max = 10;
		
		currenSprite = null;
		if (driveMode == DriveMode.LEFT)
			currenSprite = (deltaY > tilt_max) ? player_l_uphill : player_l;
		else if (driveMode == DriveMode.RIGHT)
			currenSprite = (deltaY > tilt_max) ? player_r_uphill : player_r;
		else
			currenSprite = (deltaY > tilt_max) ? player_f_uphill : player_f;
		
		//Adjust the destination
		destX -= currenSprite.getWidth() / 2;
		destY -= currenSprite.getHeight() + bounce;
		
		g.drawImage(currenSprite, destX, destY);
	}

	public void update(Input input, int dt, Road road) {
		
		RoadSegment playerSegment = road.findSegment(position+playerZ);
		float speedPercent  = getSpeedPercent();
		float centrifugal = 0.2f;
		float dt_seconds = (float)dt / 1000;
		float dspeed = dt_seconds * 2 * speedPercent;
		
		if (input.isKeyDown(Input.KEY_LEFT)) {
			playerX -= dspeed;
			driveMode = DriveMode.LEFT;
		} else if (input.isKeyDown(Input.KEY_RIGHT)) {
			playerX += dspeed;
			driveMode = DriveMode.RIGHT;
		} else {
			driveMode = DriveMode.FORWARD;
		}
		
		playerX -= (dspeed * speedPercent * playerSegment.curve * centrifugal);
		
		if (input.isKeyDown(Input.KEY_UP)) { 
			speed = speed + accel * dt_seconds;
		} else if (input.isKeyDown(Input.KEY_DOWN)) {
			speed = speed + breaking * dt_seconds;
		} else {
			speed = speed + decel * dt_seconds;
		}
		
		if (((playerX < -1) || (playerX > 1)) && (speed > offRoadLimit))
			speed = speed + offRoadDecel * dt_seconds;
		
		if (playerX > 2) playerX = 2;
		else if (playerX < -2) playerX = -2;
		
		if (speed < 0) speed = 0;
		else if (speed > speedMax) speed = speedMax;
		
		position += dt_seconds * speed;
		
		playerY = playerSegment.p1.world_y + (playerSegment.p2.world_y - playerSegment.p1.world_y) * 1;
		
		//Check collision
		for (Car car : playerSegment.getCars()) {
			if (speed > car.speed &&
					overlap(playerX  * road.getRoadWidth(), 100, car.offset * road.getRoadWidth(), 100, 2f)) {
				speed = car.speed * (car.speed / speed);
				position = (int)Math.round(car.z -playerZ);
			}
		}
		
		//Check if the player has won, and adjust the position.
		while (position > road.getTrackLength()) {
			position -= road.getTrackLength();
			
			hasWon = true;
		}
		while (position < 0) {
			position += road.getTrackLength();
		}
	}
	
	boolean overlap(float x1, float w1, float x2, float w2, float percent) {
		float half = percent / 2;
		float min1 = x1 - (w1*half);
		float max1 = x1 + (w1*half);
		float min2 = x2 - (w2*half);
		float max2 = x2 + (w2*half);
		return ! ((max1 < min2) || (min1 > max2));
	}
}