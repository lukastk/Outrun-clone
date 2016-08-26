/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.Iterator;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

/**
 *
 * @author lukas_000
 */
public class FinishGameScreen extends State {
	long endTime;
	Music endMusic;
	float blackScreen_a = 0f;
	UnicodeFont font;
	
	public FinishGameScreen(long endTime) {
		this.endTime = endTime;
	}
	
	public void update(GameContainer gc, int delta, boolean focus) throws SlickException  {
		super.update(gc, delta, focus);
	}

	public void render(GameContainer gc, Graphics g, boolean focus) throws SlickException  {
		g.setColor(new Color(0f, 0f, 0f, blackScreen_a * 0.9f));
		g.fillRect(0, 0, OutRunGame.screenWidth, OutRunGame.screenHeight);
	
		super.render(gc, g, focus);
	}

	public void init(GameContainer gc) throws SlickException {
		endMusic = new Music("res/music/nightcall.wav");
		
		font = new UnicodeFont("res/font.ttf", 15, false, false);
		font.addAsciiGlyphs();
		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		font.loadGlyphs();
		
		addTicker(new Ticker() {
			boolean draw = false;
			
			@Override
			void start() {
				endMusic.play(1f, 0f);
			}
			
			@Override
			boolean update(float dt) throws SlickException {
				if (draw)
					return false;
				
				blackScreen_a += 0.5 * dt;
				endMusic.setVolume(endMusic.getVolume() + 0.5f * dt);
				
				if (blackScreen_a >= 1) {
					blackScreen_a = 1;
					endMusic.setVolume(1f);
					
					draw = true;
					
					addTicker(new Ticker() {
						float wait = 0f;

						@Override
						boolean update(float dt) throws SlickException {
							wait += dt;
							
							if (wait >= 3) {
								endMusic.stop();
								StateManager.pop();
								StateManager.pop();
								
								StateManager.push(new HighScoreMenu(endTime));
								
								return true;
							}

							return false;
						}
					});
				}

				return false;
			}

			@Override
			void render(Graphics g) throws SlickException {
				if (!draw)
					return;

				String line = "Your time:";
				font.drawString(OutRunGame.screenWidth / 2 - font.getWidth(line) / 2, OutRunGame.screenHeight / 2 - 10, line, Color.white);
					
				int minutes = (int)Math.floor(endTime / (1000 * 60));
				int seconds = (int)Math.floor((endTime - minutes * 60 * 1000) / 1000);
				int mils = (int)Math.floor(endTime - (minutes * 60 + seconds) * 1000);

				String min_str = String.valueOf(minutes);
				while (min_str.length() < 2) min_str = "0" + min_str;

				String sec_str = String.valueOf(seconds);
				while (sec_str.length() < 2) sec_str = "0" + sec_str;

				String mil_str = String.valueOf(mils);
				while (mil_str.length() < 4) mil_str = "0" + mil_str;
				
				line = min_str + ":" + sec_str + ":" + mil_str;
				font.drawString(OutRunGame.screenWidth / 2 - font.getWidth(line) / 2, OutRunGame.screenHeight / 2 + 10, line, Color.white);
			}
		});
	}
	
	public void recievedFocus() { }
	
	public void closing() throws SlickException  {}
}
