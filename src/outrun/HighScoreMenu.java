/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Scanner;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

/**
 *
 * @author lukas_000
 */
public class HighScoreMenu extends State{
	public static String[] names;
	public static String[] scores;
	
	UnicodeFont font;
	
	Music music;
	
	float title_scale = 1f;
	float title_scale_swing = 0.2f;
	float press_enter_a = 0.4f;
	
	float colorOffsetY = (float)Math.PI;
	float colorOffsetMax = (float)Math.PI * 2;
	float colorMin = 0.2f;
	float colorMax = 0.6f;
	
	float blackScreen_a = 1f;
	
	boolean newHighScoreMode = false;
	int newScoreIndex;
	long endTime;
	
	int letterIndex_x = 0;
	int[] name = new int[3];
	static String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	static void loadScore() {
		ArrayList<String> nameList = new ArrayList<>();
		ArrayList<String> scoreList = new ArrayList<>();
		
		File file = new File("highscores.dat"); //for ex foo.txt
		try {
			Scanner sc = new Scanner(file);
			
			while (sc.hasNextLine()) {
				String[] line = sc.nextLine().split("\\s+");
			   
				nameList.add(line[0]);
				scoreList.add(line[1]);
			}
		   
			sc.close();
		} catch (IOException e) {
			nameList.clear();
			scoreList.clear();
			
			for (int i = 0; i < 10; i++) {
				nameList.add("AAA");
				scoreList.add("99:99:9999");
			}
			
			names = new String[nameList.size()];
			nameList.toArray(names);
			scores = new String[scoreList.size()];
			scoreList.toArray(scores);
		   
			writeScore();
		}
		
		names = new String[nameList.size()];
		nameList.toArray(names);
		scores = new String[scoreList.size()];
		scoreList.toArray(scores);
	}
	static void writeScore() {
		try {
			File file = new File("highscores.dat");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (int i = 0; i < names.length; i++) {
				bw.append(names[i]);
				bw.append("\t");
				bw.append(scores[i]);
				bw.newLine();
			}
			
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HighScoreMenu() {}
	
	public HighScoreMenu(long endTime) {
		this.endTime = endTime;
		newHighScoreMode = true;
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		super.init(gc);
		
		music = new Music("res/music/highscore.wav");
		
		loadScore();
		
		font = new UnicodeFont("res/font.ttf", 15, false, false);
		font.addAsciiGlyphs();
		font.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
		font.loadGlyphs();
		
		addTicker(new Ticker() {
			float d_alfa = 0.8f;

			@Override
			boolean update(float dt) {
				press_enter_a += d_alfa * dt;
				
				if (press_enter_a >= 1f || (press_enter_a <= 0.4f)) {
					d_alfa = -d_alfa;
				}
				
				if (press_enter_a <= 0.4f)
					press_enter_a = 0.4f;
				else if (press_enter_a >= 1f)
					press_enter_a = 1f;

				return false;
			}
		});
		
		addTicker(new Ticker() {
			@Override
			boolean update(float dt) {
				colorOffsetY -= colorOffsetMax * dt;
				
				while (colorOffsetY < 0)
					colorOffsetY += colorOffsetMax;

				return false;
			}
		});
		
		wakeUp(null);
		
		if (newHighScoreMode) {
			newHighScoreMode = false;
			
			for (int i = 0; i < scores.length; i++) {
				String[] time = scores[i].split(":");

				int miliseconds = Integer.parseInt(time[0]) * 60 * 1000 + Integer.parseInt(time[1]) * 1000 + Integer.parseInt(time[2]);
				
				if (endTime < miliseconds) {
					newHighScoreMode = true;
					newScoreIndex = i;
					
					int minutes = (int) ((endTime / (1000*60)) % 60);
					int seconds = (int) ((endTime / 1000) % 60);
					int mils = (int)(endTime - seconds * 1000 - minutes * 1000 * 60);
					
					String min_str = String.valueOf(minutes);
					while (min_str.length() < 2) min_str = "0" + min_str;
					
					String sec_str = String.valueOf(seconds);
					while (sec_str.length() < 2) sec_str = "0" + sec_str;
					
					String mil_str = String.valueOf(mils);
					while (mil_str.length() < 4) mil_str = "0" + mil_str;
					
					//Move down scores
					for (int n = names.length - 1; n > i; n--) {
						if (n - 1 < 0)
							break;
							
						names[n] = names[n - 1];
						scores[n] = scores[n - 1];
					}
					
					scores[newScoreIndex] = min_str + ":" + sec_str + ":" + mil_str;
					
					break;
				}
			}
		}
	}
	
	@Override
	public void update(GameContainer gc, int delta, boolean focus) throws SlickException  {
		super.update(gc, delta, focus);
		
		if (gc.getInput().isKeyPressed(Input.KEY_ENTER) && blackScreen_a == 0f) {
			if (newHighScoreMode)
				writeScore();
			
			fallAsleep(new Delegate() {
				@Override
				public void run() throws SlickException {
					close();
				}
			});
		}
		
		if (newHighScoreMode && blackScreen_a == 0f) {
			if (gc.getInput().isKeyPressed(Input.KEY_UP)) {
				name[letterIndex_x] = (name[letterIndex_x] - 1) % validChars.length();
			} else if (gc.getInput().isKeyPressed(Input.KEY_DOWN)) {
				name[letterIndex_x] = (name[letterIndex_x] + 1) % validChars.length();
			}
			
			while (name[letterIndex_x] < 0) name[letterIndex_x] += validChars.length();
			
			if (gc.getInput().isKeyPressed(Input.KEY_LEFT)) {
				letterIndex_x = (letterIndex_x - 1) % 3;
			} else if (gc.getInput().isKeyPressed(Input.KEY_RIGHT)) {
				letterIndex_x = (letterIndex_x + 1) % 3;
			}
			
			while (letterIndex_x < 0) letterIndex_x += 3;
			
			names[newScoreIndex] = validChars.substring(name[0], name[0] + 1) +
					validChars.substring(name[1], name[1] + 1) +
					validChars.substring(name[2], name[2] + 1);
		}
	}

	@Override
	public void render(GameContainer gc, Graphics g, boolean focus) throws SlickException  {
		String line;
		
		line = newHighScoreMode ? "NEW HIGH SCORE!" : "HIGH-SCORES:";
		float width = font.getWidth(line);
		font.drawString(OutRunGame.screenWidth / 2 - width / 2, 7, line);

		float numsX = OutRunGame.screenWidth / 2 - 80f;
		float namesX = OutRunGame.screenWidth / 2 - 60f;
		float timesX = OutRunGame.screenWidth / 2f - 10f;
		
		float startY = font.getHeight(line) + 15;
		float endY = OutRunGame.screenHeight - 20;
		float stepY = (endY - startY) / names.length;
		
		float colorVal = colorOffsetY;
		
		for (int i = 0; i < names.length; i++) {
			Color c = new Color((float)(0.5f + Math.cos(colorVal) / 2) * colorMax + colorMin,
					(float)(0.5f + Math.cos(colorVal - Math.PI / 2) / 2) * colorMax + colorMin,
					(float)(0.5f + Math.cos(colorVal - Math.PI) / 2) * colorMax + colorMin);
			
			if (newHighScoreMode && i == newScoreIndex) {
				c = new Color(1f, 1f, 1f);
			}
			
			line = names[i];
			font.drawString(namesX, startY + stepY * i, line, c);
			
			if (newHighScoreMode && i == newScoreIndex) {
				width = font.getWidth(line);
				font.drawString(namesX + (width / 3) * letterIndex_x, startY + stepY * i + 3, "_", c);
			}
			
			line = (i + 1) + ". ";
			font.drawString(numsX, startY + stepY * i, line, c);
			
			line = scores[i];
			font.drawString(timesX, startY + stepY * i, line, c);
			
			colorVal += (float)Math.PI / 10;
			
			while (colorVal >= colorOffsetMax)
				colorVal -= colorOffsetMax;
		}
		
		line = "PRESS ENTER TO RETURN";
		width = font.getWidth(line);
		Color c = new Color(0.5f, 1f, 1f, press_enter_a);
		font.drawString(OutRunGame.screenWidth / 2 - width / 2, endY + 2, line, c);
		
		g.setColor(new Color(0f, 0f, 0f, blackScreen_a));
		g.fillRect(0, 0, OutRunGame.screenWidth, OutRunGame.screenHeight);
	}

	void fallAsleep(final Delegate followingAction) {
		addTicker(new Ticker() {
				@Override
				boolean update(float dt) throws SlickException {
					blackScreen_a += 1f * dt;
					music.setVolume(music.getVolume() - 0.35f * dt);

					if (blackScreen_a >= 1f) {
						blackScreen_a = 1f;
						music.stop();
						
						if (followingAction != null)
							followingAction.run();
						
						return true;
					}

					return false;
				}
			});
	}
	
	void wakeUp(final Delegate followingAction) {
		addTicker(new Ticker() {
			@Override
			void start() {
				music.play();
				music.setVolume(0);
			}
			
			@Override
			boolean update(float dt) throws SlickException {
					blackScreen_a -= 1f * dt;
					music.setVolume(music.getVolume() + 0.35f * dt);
					
					if (blackScreen_a <= 0f) {
						blackScreen_a = 0f;
						music.setVolume(0.7f);
						
						if (followingAction != null)
							followingAction.run();
						
						return true;
					}

					return false;
				}
			});
	}
}
