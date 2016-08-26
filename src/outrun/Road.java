/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.ArrayList;
import java.util.Random;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;

/**
 *
 * @author lukas_000
 * 
 * How the rendering works:
 * The underlying math behind the geometry of the road works like this:
 * 
 * The road basically stores a long list of "segments", a segment consist
 * of two points in 3D space, p1 and p2. Point p2 of a segment seg_n in a
 * road shares its value with point p1 of the following segment seg_(n+1),
 * therefore all of the segments are connected to each other in a long line.
 * 
 * The segment holds all of the information needed to draw a "segment" of
 * the road, when running the program it is very clear where a segment starts
 * and ends, as the colors of the road change as the segments begin and end.
 * 
 * All of the segments in the road are displaced from each other along their
 * z coordinates, each subsequent segment increasing their z coordinate with
 * a segmentLength longer than the last segment (seg_(n).p1.z = seg_(n - 1).p1.z 
 * segmentLength).
 * 
 * Curvature:
 * The x coordinate of a segment signifies the difference between the position
 * of the segment and the middle of the screen, on the horizontal axis.
 * 
 * But in practice, only the y and the z coordinate of the road is stored in a segment
 * (See RoadSegment.java). Instead of storing the x coordinate, there is a 
 * curve field in the segment, which is the acceleration of the change in the x-coordinate
 * in that particular segment.
 * 
 * This might seem as an overly complicated way of doing it, but it actually
 * makes it easier to calculate the road curvature. The idea is that, because
 * it'll be far too difficult to simulate real 3D curvature of the road,
 * it'll be easier to just emulate curvature by just displacing the horizontal
 * slices of the road (the segments) on the x-axis. The displacement of the
 * segments are supposed to curve like a 2nd or 3rd degree function, which is why 
 * segment.curve stores an acceleration rather than an actual coordinate. If you'd
 * map out all of the curve values of all the segments on a graph you would effectively
 * get a graph of the second derivative of the x-displacement of all of the segments.
 * 
 * Projecting world coordinates to screen coordinates:
 * The RoadSegment class holds the coordinates of where the segment is in the world,
 * these fields are called world_x, world_y, world_z. Along with these fields there
 * are also corresponding coordinates for the camera and the screen. When rendering the
 * road the program projects it to the screen by first translating the world coordinates
 * to camera coordinates (gets the difference between the camera x and y position and
 * the world coordinates), and then it projects this camera coordinate on the screen
 * (using the law of similar triangles.).
 * 
 * Establishing perspective:
 * Note that, so far, the road has been solely described as a 2 dimensional line
 * with no width. No information about the width of the road is actually stored.
 * Instead of that, there is a screen_scale field which signifies the down-scaling
 * of the road as you go along it.
 * 
 * The width of the road is kept constant (see roadWidth)), when rendering the road,
 * roadWidth is multiplied by the scale of the segment being drawn, thereby achieving
 * the illusion of perspective.
 * 
 * Sprites:
 * Sprites and cars being drawn on the road work very similarly, and most of the
 * calculation needed for their rendering are made easier with the segments. Their
 * size depends on the screen_scale field of the segment that they are standing on,
 * and their displacement along the center of the road is measured by a float variable, where
 * -1 is at the left end of the road and 1 is at the right end.
 * 
 * As the road curves left to right, the player car is affect by a simulated centripetal force.
 * 
 * Ups and downs in the road is done in a very simple fashion. Each segment has a y
 * coordinate that signifies its altitude. Generating hills and valleys is simply
 * a matter of producing y values for the segments looking like the shapes desired.
 * 
 * Camera:
 * The player class has information necessary to calculate the position of the
 * camera in the world. The players position in the world is determined by a
 * position field (that signifies its Z-coordinate) and a floating point
 * X-offset field (where -1 is at the left end and 1 at the right end of the road.),
 * with that it is pretty easy to calculate what the CameraX and CameraZ should be.
 * CameraY is calculated as the sum of the current y coordinate of the segment the player
 * is standing on and a static cameraHeight field.
 */
public class Road {
	private RoadSegment[] segments;  // array of road segments
	private int segmentLength = 200;  // length of a single segment
	private int rumbleLength = 3;  // number of segments per red/white rumble strip
	private int trackLength = 0;  // z length of entire track (computed)
	private int roadWidth = 2000;
	
	private static Color road1 = new Color(100, 100, 100);
	private static Color road2 = new Color(90, 90, 90);
	private static Color grass1 = new Color(0, 60, 0);
	private static Color grass2 = new Color(0, 70, 0);
	private static Color lane = new Color(210, 210, 210);
	private static Color rumble1 = new Color(170, 170, 170);
	private static Color rumble2 = new Color(60, 60, 60);
	
	private OffsetImage streetLight_r;
	private OffsetImage streetLight_l;
	private OffsetImage gosling;
	private OffsetImage billboard1;
	private OffsetImage billboard2;
	private OffsetImage boat_house;
	private OffsetImage boulder1;
	private OffsetImage boulder2;
	private OffsetImage boulder3;
	private OffsetImage stump;
	
	static int TRUCK = 0;
	static int CAR1 = 1;
	static int CAR2 = 2;
	static int CAR3 = 3;
	static int CAR4 = 4;
	private int totalCars = 1400;
	OffsetImage[] carSprites;
	ArrayList<Car> cars = new ArrayList<>();
	
	public Road() throws SlickException {
		streetLight_r =	new OffsetImage(new Image("res/sprites/streetlight.png"), -0.8f, -0.68f);
		streetLight_l =	new OffsetImage(streetLight_r.image, -0.8f, -0.68f).flip();
		gosling =		new OffsetImage(new Image("res/sprites/gosling.png"), -0.5f, -1f);
		billboard1 =	new OffsetImage(new Image("res/sprites/billboard1.png"), -0.5f, -1f);
		billboard2 =	new OffsetImage(new Image("res/sprites/billboard2.png"), -0.5f, -1f);
		boat_house =	new OffsetImage(new Image("res/sprites/boat_house.png"), -0.5f, -1f);
		boulder1 =		new OffsetImage(new Image("res/sprites/boulder1.png"), -0.5f, -1f);
		boulder2 =		new OffsetImage(new Image("res/sprites/boulder2.png"), -0.5f, -1f);
		boulder3 =		new OffsetImage(new Image("res/sprites/boulder3.png"), -0.5f, -1f);
		stump =			new OffsetImage(new Image("res/sprites/stump.png"), -0.5f, -1f);
		
		carSprites = new OffsetImage[5];
		carSprites[0] = new OffsetImage(new Image("res/cars/truck.png"), -0.5f, -1f);
		carSprites[1] = new OffsetImage(new Image("res/cars/car01.png"), -0.5f, -1f);
		carSprites[2] = new OffsetImage(new Image("res/cars/car02.png"), -0.5f, -1f);
		carSprites[3] = new OffsetImage(new Image("res/cars/car03.png"), -0.5f, -1f);
		carSprites[4] = new OffsetImage(new Image("res/cars/car04.png"), -0.5f, -1f);
	}
	
	public RoadSegment[] getSegments() { return segments; }
	
	public int getSegmentLength() { return segmentLength; }
	public int getRumbleLength() { return rumbleLength; }
	public int getTrackLength() { return trackLength; }
	public int getRoadWidth() { return roadWidth; }
	
	public void createRoad() {
		ArrayList<RoadSegment> segmentList = new ArrayList<>();
		
		addRoad(segmentList, 100, 100, 100, 0, 0);
		addRoad(segmentList, 0, 150, 150, -2, 0);
		addRoad(segmentList, 0, 150, 150, 2, 0);
		addRoad(segmentList, 100, 100, 100, 0, 0);
		
		addBumpyHills(segmentList, 20, 1400, 5);
		addSnakeRoad(segmentList, 25, 1.2f, 7);
		
		addRoad(segmentList, 200, 200, 100, -4, 10000);
		addRoad(segmentList, 200, 200, 100, -4, -10000);
		
		addHill(segmentList, 25, 25, 25, -2, -10000);
		addHill(segmentList, 25, 25, 25, 2, 7000);
		addHill(segmentList, 25, 25, 25, -1, -2000);
		addHill(segmentList, 25, 100, 25, 4, -7000);
		addHill(segmentList, 10, 10, 10, -3, 5000);
		addHill(segmentList, 10, 20, 10, 4, -4000);
		addHill(segmentList, 10, 20, 10, -4, -4000);
		addHill(segmentList, 10, 20, 10, 4, 5000);
		
		addHill(segmentList, 50, 50, 50, 1, -10000);
		addHill(segmentList, 50, 700, 50, 1, -100000);
		addHill(segmentList, 50, 500, 50, 1, 80000);
		addHill(segmentList, 10, 500, 10, -1, 10000);
		
		addHill(segmentList, 10, 500, 10, -1, 1000);
		addHill(segmentList, 10, 500, 10, -2, 2000);
		addHill(segmentList, 10, 500, 10, -3, 4000);
		addHill(segmentList, 10, 500, 10, -4, 5000);
		
		addHill(segmentList, 10, 100, 10, -1, 1000);
		addHill(segmentList, 10, 100, 10, -2, -2000);
		addHill(segmentList, 10, 100, 10, -3, 4000);
		addHill(segmentList, 10, 100, 10, -4, -5000);
		addHill(segmentList, 10, 100, 10, -6, 8000);
		addHill(segmentList, 10, 100, 10, -7, -9000);
		addHill(segmentList, 20, 20, 20, 7, 4000);
		addHill(segmentList, 20, 20, 20, -7, -4000);
		addHill(segmentList, 20, 20, 20, 7, 2000);
		addHill(segmentList, 20, 20, 20, -7, -2000);
		addHill(segmentList, 20, 20, 20, 7, 0);
		addHill(segmentList, 20, 20, 20, -7, 0);
		addHill(segmentList, 20, 20, 20, 7, 0);
		addHill(segmentList, 20, 20, 20, -7, 0);
		addHill(segmentList, 20, 20, 20, 7, 0);
		addHill(segmentList, 20, 20, 20, -3, 0);
		addHill(segmentList, 20, 20, 20, 7, 0);
		
		roadToYPoint(segmentList, 10, 300, 10, 2, 0);
		
		segments = new RoadSegment[segmentList.size()];
		segmentList.toArray(segments);
		
		trackLength = segments.length * segmentLength;
		
		resetSprites();
		resetCars();
	}
	
	public void resetSprites() {
		addSprite(250, gosling, 1.5f);
		
		for (int i = 0; i < 15; i++) {
			addSprite(20 * i, streetLight_r, 1.5f);
			addSprite(20 * i, streetLight_l, -1.5f);
		}
		
		for (int i = 15; i < 30; i++) {
			addSprite(20 * i, streetLight_r, 1.5f);
		}
		addSprite(300, billboard1, -1.8f);
		addSprite(330, billboard2, -1.8f);
		addSprite(380, boat_house, -1.9f);
		
		for (int i = 30; i < 45; i++) {
			addSprite(20 * i, streetLight_l, -1.5f);
		}
		
		Random rand = new Random();
		OffsetImage[] sprites = new OffsetImage[] { billboard1, billboard2, boat_house, gosling, boulder1, boulder2, boulder3, stump};
		
		for (int i = 0; i < 150; i++) {
			float offset = 1.5f;
			if (rand.nextBoolean())
				offset = -offset;
				
			addSprite(900 + rand.nextInt(trackLength / segmentLength  - 900), sprites[rand.nextInt(sprites.length)], offset);
		}
	}
	
	public void addSegment(ArrayList<RoadSegment> segmentList, float curve, float y) {
		int n = segmentList.size();
		
		float last_y;
		if (segmentList.size() != 0) {
			last_y = segmentList.get(segmentList.size() - 1).p2.world_y;
		} else {
			last_y = 0;
		}
		
		RoadSegment segment = new RoadSegment();

		segment.index = n;
		
		segment.p1 = new WorldPoint();
		segment.p1.world_z = n * segmentLength;
		segment.p1.world_y = last_y;
		
		segment.p2 = new WorldPoint();
		segment.p2.world_z = (n + 1) * segmentLength;
		segment.p2.world_y = y;
		
		segment.colorRoad = Math.floor(n / rumbleLength) % 2 == 0 ? road1 : road2;
		segment.colorGrass = Math.floor(n / rumbleLength) % 2 == 0 ? grass1 : grass2;
		segment.colorLane = Math.floor(n / rumbleLength) % 2 == 0 ? lane : null;
		segment.colorRumble = Math.floor(n / rumbleLength) % 2 == 0 ? rumble1: rumble2;
		
		segment.curve = curve;
		
		segmentList.add(segment);
	}
	
	public void addRoad(ArrayList<RoadSegment> segmentList, int enter, int hold, int leave, float curve, float endY) {
		float startY;
		if (!segmentList.isEmpty()) {
			startY = segmentList.get(segmentList.size() - 1).p2.world_y;
		} else {
			startY = 0;
		}
		
		float y;
		
		endY = startY + endY;
		
		for (float n = 0; n < enter; n++) {
			y = easeIn(startY, endY, n/enter);
			addSegment(segmentList, easeIn(0, curve, n/enter), y);
		}
		for (float n = 0; n < hold; n++) {
			addSegment(segmentList, curve, endY);
		}
		for (float n = 0; n < leave; n++) {
			y = easeOut(endY, startY, n/leave);
			addSegment(segmentList, easeOut(curve, 0, n/leave), y);
		}
	}
	public void addHill(ArrayList<RoadSegment> segmentList, int enter, int hold, int leave, float curve, float endY) {
		float startY;
		if (!segmentList.isEmpty()) {
			startY = segmentList.get(segmentList.size() - 1).p2.world_y;
		} else {
			startY = 0;
		}
		
		endY = startY + endY;
		
		float y;
		
		int total = enter + hold + leave;
		
		for (float n = 0; n < enter; n++) {
			y = easeInOut(startY, endY, n/total);
			addSegment(segmentList, easeIn(0, curve, n/enter), y);
		}
		for (float n = 0; n < hold; n++) {
			y = easeInOut(startY, endY, (n + enter) /total);
			addSegment(segmentList, curve, y);
		}
		for (float n = 0; n < leave; n++) {
			y = easeInOut(startY, endY, (n + enter + hold)/total);
			addSegment(segmentList, easeOut(curve, 0, n/leave), y);
		}
	}
	public void roadToYPoint(ArrayList<RoadSegment> segmentList, int enter, int hold, int leave, float curve, float endY) {
		float startY;
		if (!segmentList.isEmpty()) {
			startY = segmentList.get(segmentList.size() - 1).p2.world_y;
		} else {
			startY = 0;
		}
		
		float y;
		
		int total = enter + hold + leave;
		
		for (float n = 0; n < enter; n++) {
			y = easeInOut(startY, endY, n/total);
			addSegment(segmentList, easeIn(0, curve, n/enter), y);
		}
		for (float n = 0; n < hold; n++) {
			y = easeInOut(startY, endY, (n + enter) /total);
			addSegment(segmentList, curve, y);
		}
		for (float n = 0; n < leave; n++) {
			y = easeInOut(startY, endY, (n + enter + hold)/total);
			addSegment(segmentList, easeOut(curve, 0, n/leave), y);
		}
	}
	
	public void addSnakeRoad(ArrayList<RoadSegment> segmentList, int roadLength, float factor, int turns) {
		for (int i = 1; i < turns; i++) {
			addRoad(segmentList, roadLength, roadLength, roadLength, i * factor * (i % 2 == 0 ? 1 : -1), 0);
		}
	}
	public void addBumpyHills(ArrayList<RoadSegment> segmentList, int roadLength, float factor, int hills) {
		for (int i = 1; i < hills; i++) {
			addHill(segmentList, roadLength, roadLength, roadLength, 0, i * factor * (i % 2 == 0 ? 1 : -1));
		}
	}
	
	
	
	public float easeIn(float a, float b, float percent) { return a + (float)((b-a) * Math.pow(percent, 2)); }
	public float easeOut(float a, float b, float percent) { return a + (float)((b-a) * (1 - Math.pow(1 - percent, 2))); }
	public float easeInOut(float a, float b, float percent) { return a + (float)((b-a) * ((-Math.cos(percent*Math.PI)/2) + 0.5)); }
	
	public void addSprite(int index, OffsetImage sprite, float offset) {
		segments[index].getSprites().add(new RoadSprite(sprite, offset));
	}
	
	public RoadSegment findSegment(float z) {
		return segments[(int)Math.floor(z/segmentLength) % segments.length];
	}
	
	float a = 0;
	float da = 0.1f;
	
	public void renderRoad(Graphics g, int width, int height, float drawDistance, float playerX,
			float camHeight, float position, float playerY, float cameraDepth, int lanes, Player player) {
		RoadSegment base = findSegment(position);
		float basePercent = (position%segmentLength)/segmentLength;
		float max_y = height;
		
		float x = 0;
		float dx = base.curve * basePercent;
		RoadSegment segment;
		
		for (int i = 0; i < drawDistance; i++) {
			segment = segments[(base.index + i) % segments.length];
			segment.looped = segment.index < base.index;
			segment.clip = max_y;
			
			//Project
			segment.project((playerX * roadWidth), playerY + camHeight, position - (segment.looped ? trackLength : 0), cameraDepth, width, height, roadWidth, x, dx);
			
			x  = x + dx;
			dx = dx + segment.curve;
			
			if ((segment.p1.cam_z <= cameraDepth) ||
					(segment.p2.screen_y >= segment.p1.screen_y) ||
					(segment.p2.screen_y >= max_y)) {
				continue;
			}
			
			renderSegment(g, width, lanes, segment, Math.min(0.90f, (i * 3f)  / drawDistance));
			max_y = segment.p2.screen_y;
		}	
			
		for (int n = (int)Math.floor(drawDistance - 1); n > 0; n--) {
			segment = segments[(base.index + n) % segments.length];
			
			float carScaleMult = 0.6f;
			
			for (Car car : segment.getCars()) {
				OffsetImage sprite = car.getSprite();
				float spriteScale = roadWidth * carScaleMult * (segment.p1.screen_scale + (segment.p2.screen_scale - segment.p1.screen_scale) * car.getPercent());
				float spriteX = segment.p1.screen_x + segment.p1.screen_w * car.offset;
				float spriteY = segment.p1.screen_y;
				renderSprite(g, roadWidth, sprite.image, spriteScale, spriteX, spriteY, -0.5f, -1, segment.clip, false);
				
			}
			
			for (RoadSprite roadSprite : segment.getSprites()) {
				OffsetImage spriteImg = roadSprite.getSprite();
				float spriteScale = segment.p1.screen_scale * roadWidth * 2;
				float spriteX = segment.p1.screen_x + segment.p1.screen_w * roadSprite.getOffset();
				float spriteY = segment.p1.screen_y;
				
				renderSprite(g, roadWidth, spriteImg.image, spriteScale, spriteX, spriteY, spriteImg.offsetX, spriteImg.offsetY, segment.clip, spriteImg.flip);
			}
			
			// Draw player
			RoadSegment playerSegment = findSegment(player.getPosition() + player.getPosZ());
			
			if (playerSegment.index == segment.index) {
				player.render(g, OutRunGame.screenWidth, OutRunGame.screenHeight, 0, roadWidth, cameraDepth,
						OutRunGame.screenWidth / 2, OutRunGame.screenHeight, playerSegment.p2.world_y - playerSegment.p1.world_y);
			}
		}
	}
	
	public void renderSprite(Graphics g, int roadWidth, Image sprite,
			float scale, float destX, float destY, float offsetX, float offsetY, float clipY, boolean flipH) {
		float destW = (sprite.getWidth() * scale);
		float destH = (sprite.getHeight() * scale);
		
		if (flipH) {
			destX += destW;
			destW = -destW;
			offsetX = 1 + offsetX;
		}
		
		destX += (destW * offsetX);
		destY += (destH * offsetY);
		
		float clipH = (float)Math.max(0, destY+destH-clipY);
		
		destX = (float)Math.round(destX);
		destY = (float)Math.round(destY);
		destW = (float)Math.round(destW);
		destH = (float)Math.round(destH);
		
		if (clipH < destH)
			g.drawImage(sprite, destX, destY, destX + destW, destY + destH, 0, 0, sprite.getWidth(), sprite.getHeight());
	}
	
	void renderSegment(Graphics g, int width, int lanes, RoadSegment segment, float colorFactor) {
		float x1 = (float)Math.round(segment.p1.screen_x);
		float y1 = (float)Math.round(segment.p1.screen_y);
		float w1 = (float)Math.round(segment.p1.screen_w);
		float x2 = (float)Math.round(segment.p2.screen_x);
		float y2 = (float)Math.round(segment.p2.screen_y);
		float w2 = (float)Math.round(segment.p2.screen_w);
		
		float r1 = (float)Math.round(w1/Math.max(6, 2*lanes));
		float r2 = (float)Math.round(w2/Math.max(6, 2*lanes));
		float l1 = (float)Math.round(w1/Math.max(32, 8*lanes));
		float l2 = (float)Math.round(w2/Math.max(32, 8*lanes));
		
		g.setColor(segment.colorGrass.darker(colorFactor));
		g.fillRect(0, y2, width, y1 - y2);
		
		g.setColor(segment.colorRumble.darker(colorFactor));
		g.fill(new Polygon(new float[] {x1-w1-r1, y1, x1-w1, y1, x2-w2, y2, x2-w2-r2, y2}));
		g.fill(new Polygon(new float[] {x1+w1+r1, y1, x1+w1, y1, x2+w2, y2, x2+w2+r2, y2}));
		
		g.setColor(segment.colorRoad.darker(colorFactor));
		g.fill(new Polygon(new float[] {x1-w1,    y1, x1+w1, y1, x2+w2, y2, x2-w2,    y2}));
		
		if (segment.colorLane != null) {
			float lanew1 = w1*2/lanes;
			float lanew2 = w2*2/lanes;
			float lanex1 = x1 - w1 + lanew1;
			float lanex2 = x2 - w2 + lanew2;
			
			for(int lane = 1 ; lane < lanes ; lanex1 += lanew1, lanex2 += lanew2, lane++) {
				g.setColor(segment.colorLane.darker(colorFactor));
				g.fill(new Polygon(new float[] {lanex1 - l1/2, y1, lanex1 + l1/2, y1, lanex2 + l2/2, y2, lanex2 - l2/2, y2}));
			}
		}
	}
	
	public void resetCars() {
		Random rand = new Random();
		
		for (int n = 0; n < totalCars; n++) {
			float offset = (float)(rand.nextDouble() * 0.8);
			if (rand.nextBoolean())
				offset = -offset;
			
			float z = (float)Math.floor(rand.nextDouble() * segments.length) * segmentLength;
			
			int imgIndex = rand.nextInt(carSprites.length);
			OffsetImage sprite = carSprites[imgIndex];
			float speed = (float)(OutRunGame.speedMax/4 + Math.random() * OutRunGame.speedMax/2); //Game.speedMax/(imgIndex == SEMI ? 4 : 2));
			
			Car car = new Car();
			car.offset = offset;
			car.z = z;
			car.sprite = sprite;
			car.speed = speed;
			
			RoadSegment segment = findSegment(car.z);
			segment.getCars().add(car);
			cars.add(car);
		}
	}

	public void update(int dt, Player player) {
		float dt_seconds = (float)dt / 1000;
		RoadSegment playerSegment = findSegment(player.getPosition() + player.getPosZ());
		
		for (Car car : cars)
			car.update(dt_seconds, playerSegment, this, player.getWidth(), player);
	}
	
	public void dispose() throws SlickException {
		for (int i = 0; i < carSprites.length; i++)
			carSprites[i].image.destroy();
		
		streetLight_r.image.destroy();
		streetLight_l.image.destroy();
	}
}