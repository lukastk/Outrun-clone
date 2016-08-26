/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import outrun.Player;
import outrun.Road;
import outrun.RoadSegment;
import outrun.State;

/**
 *
 * @author lukas_000
 * 
 * The main class that handles the actual game.
 */
public class OutRunGame extends State
{
	public static int screenWidth = 320;
	public static int screenHeight = 194;
	public static int scale = 5; //Scales the game screen to three times its size.
	public static int drawDistance = 300;  
	public static int speedMax = 12000;
	
	Music music;
	
	int fps = 60;  
	float step = 1/fps;			
	Image background = null; 
	int lanes = 3;  
	int fieldOfView = 100;  
	int cameraHeight = 1000;  
	float cameraDepth = 0;  
	
	float fogDensity = 5; 
	
	//Horizontal parallax speeds
	float skySpeed    = 0.00001f;
	float hillsSpeed   = 0.0003f; 
	float buildings1Speed   = 0.00001f; 
	float buildings2Speed   = 0.00007f; 
	float skyOffset   = 0;     
	float hillsOffset  = 0;    
	float buildings1Offset  = 0;    
	float buildings2Offset  = 0;     
	
	//Vertical parallax speeds
	float skySpeedY    = 0.00000002f; 
	float hillsSpeedY   = 0.000001f; 
	float buildings1SpeedY   = 0.0000002f;
	float buildings2SpeedY   = 0.0000008f; 
	float skyOffsetY   = 0;     
	float hillsOffsetY  = 0;    
	float buildings1OffsetY  = 0;     
	float buildings2OffsetY  = 0;    
	
	Image sky;
	Image hills;
	Image buildings1;
	Image buildings2;
	Image buildings3;
	
	Road road;
	Player player;
	
	long startTime;
	boolean endGame = false;
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		music = new Music("res/music/justice.wav");
		music.play();
		
		sky = new Image("res/background/sky.png");
		hills = new Image("res/background/hills.png");
		buildings1 = new Image("res/background/buildings1.png");
		buildings2 = new Image("res/background/buildings2.png");
		
		cameraDepth            = (float)(1 / Math.tan((fieldOfView/2) * Math.PI/180));
		
		road = new Road();
		road.createRoad();
		
		player = new Player(cameraHeight * cameraDepth);
		
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public void update(GameContainer gc, int delta, boolean focus) throws SlickException {
		super.update(gc, delta, focus);
		
		if (endGame) {
			return;
		}
		
		// If the player has crossed the finish line, start the FinishGameScreen state.
		if (player.hasWon) {
			long endTime = System.currentTimeMillis() - startTime;
			StateManager.push(new FinishGameScreen(endTime));
			music.stop();
			
			endGame = true;
			return;
		}
		
		if (gc.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
			suspend();
		}
		
		if (!focus)
			return;
		
		player.update(gc.getInput(), delta, road);
		road.update(delta, player);
					
		RoadSegment playerSegment = road.findSegment(player.getPosition() + player.getPosZ());
		float playerPercent = ((player.getPosition() + player.getPosZ()) % road.getSegmentLength()) / road.getSegmentLength();
		float speedPercent  = player.getSpeedPercent();
		
		skyOffset  = increase(skyOffset,  skySpeed  * playerSegment.curve * speedPercent, 1);
		hillsOffset = increase(hillsOffset, hillsSpeed * playerSegment.curve * speedPercent, 1);
		buildings1Offset = increase(buildings1Offset, buildings1Speed * playerSegment.curve * speedPercent, 1);
		buildings2Offset = increase(buildings2Offset, buildings2Speed * playerSegment.curve * speedPercent, 1);
		
		skyOffsetY  = skySpeedY  * playerSegment.p1.world_y * speedPercent;
		hillsOffsetY = hillsSpeedY  * playerSegment.p1.world_y * speedPercent;
		buildings1OffsetY = buildings1SpeedY  * playerSegment.p1.world_y * speedPercent;
		buildings2OffsetY = buildings2SpeedY  * playerSegment.p1.world_y * speedPercent;
	}
	
	float increase(float start, float increment, float max) {
		float res = start + increment;
		
		while (res > max) {
			res -= max;
		}
		while (res < 0) {
			res += max;
		}
		
		return res;
	}
	
	@Override
	public void render(GameContainer gc, Graphics g, boolean focus) throws SlickException {
		renderBg(g, sky, skyOffset, skyOffsetY,  skySpeed  * player.getPosY());
		renderBg(g, buildings1, buildings1Offset, buildings1OffsetY,  buildings1Offset  * player.getPosY());
		renderBg(g, buildings2, buildings2Offset, buildings2OffsetY,  buildings2Offset  * player.getPosY());
		renderBg(g, hills, hillsOffset, hillsOffsetY,  hillsOffset  * player.getPosY());
		
		road.renderRoad(g, screenWidth, screenHeight, drawDistance, player.getPosX(), cameraHeight, player.getPosition(), player.getPosY(), cameraDepth, lanes, player);
	}
	
	void renderBg(Graphics g, Image bg, float offset, float offsetY, float rot) {
		float destX = (float)Math.floor(bg.getWidth() * offset);
		float destY = (float)Math.floor(bg.getHeight() * offsetY);
		float destW = bg.getWidth();
		float destH = bg.getHeight();
		
		float srcX = 0;
		float srcY = 0;
		float srcW = bg.getWidth();
		float srcH = bg.getHeight();
		
		g.drawImage(bg, destX, destY, destX + destW, destY + destH, srcX, srcY, srcX + srcW, srcY + srcH);
		g.drawImage(bg, destX - bg.getWidth(), destY, destX + destW  - bg.getWidth(), destY + destH, srcX, srcY, srcX + srcW, srcY + srcH);
	}
	
	@Override
	public void closing() throws SlickException {
		sky.destroy();
		hills.destroy();
		buildings1.destroy();
		buildings2.destroy();
		
		player.getCurrentImage().destroy();
		
		road.dispose();
		
		music.stop();
	}
	
	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new outrun.Game());
		
		app.setDisplayMode(screenWidth * scale, screenHeight * scale, false);
		app.start();
	}
	
	public void recievedFocus() { 
		music.play();
	}
}