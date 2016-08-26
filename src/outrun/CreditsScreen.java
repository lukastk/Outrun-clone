/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.ArrayList;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

/**
 *
 * @author lukas_000
 */
public class CreditsScreen extends State {
	float offsetY = 0f;
	UnicodeFont font;
	Music music;
	
	ArrayList<String> creditStrings = new ArrayList<String>();
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		font = new UnicodeFont("res/font.ttf", 12, false, false);
		font.addAsciiGlyphs();
		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		font.loadGlyphs();
		
		music = new Music("res/music/someday.wav");
		music.play(1f, 1f);
		
		creditStrings.add("Programming - Lukas Kikuchi");
		creditStrings.add(" ");
		creditStrings.add("Art:");
		creditStrings.add("Main menu art - Lukas Kikuchi");
		creditStrings.add("In-game sprites - Sega");
		creditStrings.add(" ");
		creditStrings.add("Music:");
		creditStrings.add("Nightcall 8 bit theme - Scattle");
		creditStrings.add("Audio Video Disco 8 bit theme - Scattle");
		creditStrings.add("Barbra Streisand 8 bit theme - Rundegroot");
		creditStrings.add("Someday 8 bit theme - Coy Pendleton");
		creditStrings.add(" ");
		creditStrings.add("Thanks to:");
		creditStrings.add(" ");
		creditStrings.add("The great info from ");
		creditStrings.add("Code inComplete and Lou's Pseudo 3d Page");
	}
	
	@Override
	public void update(GameContainer gc, int delta, boolean focus) throws SlickException {
		float dt = ((float) delta) / 1000;
		
		offsetY -= dt * 50f;
		
		if (offsetY < -400f)
			offsetY = 0;
		
		if (gc.getInput().isKeyPressed(Input.KEY_ENTER)) {
			close();
		}
	}
	
	@Override
	public void render(GameContainer gc, Graphics g, boolean focus) throws SlickException {
		float destY = OutRunGame.screenHeight + offsetY;
		
		for (String str : creditStrings) {
			int width = font.getWidth(str);
			font.drawString(OutRunGame.screenWidth / 2 - width / 2, destY, str);
			
			destY += font.getHeight(str) + 2f;
		}
	}
	
	public void closing() throws SlickException  {
		music.stop();
	}
}
