/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.ArrayList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

/**
 *
 * @author lukas_000
 */
public class RoadSegment {
	int index;
	WorldPoint p1;
	WorldPoint p2;
	float curve;
	float clip;
	
	Color colorGrass;
	Color colorRoad;
	Color colorRumble;
	Color colorLane;
	
	boolean looped;
	
	private ArrayList<RoadSprite> sprites = new ArrayList<>();
	private ArrayList<Car> cars = new ArrayList<>();
	
	public ArrayList<RoadSprite> getSprites() { return sprites; }
	public ArrayList<Car> getCars() { return cars; }
	
	public void project(float cameraX, float cameraY, float cameraZ, float cameraDepth, int width, int height, int roadWidth, float x, float dx) {
		project(p1, cameraX - x, cameraY, cameraZ, cameraDepth, width, height, roadWidth);
		project(p2, cameraX - x - dx, cameraY, cameraZ, cameraDepth, width, height, roadWidth);
	}
	
	void project(WorldPoint p, float cameraX, float cameraY, float cameraZ, float cameraDepth, float width, float height, int roadWidth) {
		p.cam_x = p.world_x - cameraX;
		p.cam_y = p.world_y - cameraY;
		p.cam_z = p.world_z - cameraZ;
		p.screen_scale = cameraDepth/p.cam_z;
		
		p.screen_x     = Math.round((width / 2) + (p.screen_scale * p.cam_x  * width/2));
		p.screen_y     = Math.round((height/2) - (p.screen_scale * p.cam_y  * height/2));
		p.screen_w     = Math.round(             (p.screen_scale * roadWidth   * width/2));
	}
}
