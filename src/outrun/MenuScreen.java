/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

/**
 *
 * @author lukas_000
 */
public class MenuScreen extends State {
	Image drive;
	Image scorpion;
	Image gosling;
	Image car;
	Image press_enter;
	Image copyright;
	
	Image[] menuImages;
	
	Music introMusic;
	
	float drive_a = 1f;
	float drive_offsetY = -0.6f;
	float scorpion_a = 0f;
	float gosling_a = 0f;
	float car_a = 0f;
	float press_enter_a = 0f;
	float copyright_a = 0f;
	float blackScreen_a = 0f;
	
	boolean done = false;
	
	boolean menuMode = false;
	int selectedMenu = 0;
	float selectedMenuScale = 1f;
	float selectedMenuScaleSwing = 0.05f;
	float selectedMenuScale_d = 0.35f;
	
	boolean disableMoveSelect = false;
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		super.init(gc);
		
		introMusic =	new Music("res/music/nightcall.wav");
		
		drive =			new Image("res/screens/main/drive.png");
		scorpion =		new Image("res/screens/main/scorpion.png");
		gosling =		new Image("res/screens/main/gosling.png");
		car =			new Image("res/screens/main/car.png");
		press_enter =	new Image("res/screens/main/press_enter.png");
		copyright =		new Image("res/screens/main/copyright.png");
		
		menuImages = new Image[] { new Image("res/screens/main/start_game.png"),
			new Image("res/screens/main/see_highscores.png"),
			new Image("res/screens/main/credits.png"),
			new Image("res/screens/main/exit_game.png")};
		
		final Ticker showPressEnter = new Ticker() {
			float d_alfa = 0.8f;

			@Override
			boolean update(float dt) {
				press_enter_a += d_alfa * dt;
				
				if (menuMode) {
					press_enter_a = 0;
					return true;
				}
				
				if (press_enter_a >= 1f || (done && press_enter_a <= 0.4f)) {
					done = true;
					d_alfa = -d_alfa;
				}

				return false;
			}
		};
		
		final Ticker showCar = new Ticker() {
			@Override
			boolean update(float dt) {
				car_a += 0.5f * dt;
				
				if (car_a >= 1f) {
					car_a = 1f;
					
					addTicker(showPressEnter);
					return true;
				}
				
				return false;
			}
		};
		
		final Ticker showGosling = new Ticker() {
			@Override
			boolean update(float dt) {
				gosling_a += 0.8f * dt;
				
				if (gosling_a >= 1f) {
					gosling_a = 1f;
					
					addTicker(showCar);
					return true;
				}
				
				return false;
			}
		};
		
		final Ticker showScorpion = new Ticker() {
			@Override
			boolean update(float dt) {
				scorpion_a += 0.5f * dt;
				
				if (scorpion_a >= 1f) {
					scorpion_a = 1f;
					
					addTicker(getWaiter(1f, showGosling));
					
					return true;
				}
				
				return false;
			}
		};
		
		final Ticker playMusic = new Ticker() {
			@Override
			void start() {
				introMusic.play(1f, 0.05f);
			}
			
			@Override
			boolean update(float dt) {
				introMusic.setVolume(introMusic.getVolume() + 0.2f * dt);
				
				if (introMusic.getVolume() > 0.7f) {
					introMusic.setVolume(0.7f);
					return true;
				}
				
				return false;
			}
		};
		
		final Ticker scrollDriveLogo = new Ticker() {
			@Override
			boolean update(float dt) {
				drive_offsetY += 0.1 * dt;
				
				if (drive_offsetY >= 0) {
					drive_offsetY = 0;
					
					addTicker(showScorpion);
					
					return true;
				}
				
				return false;
			}
		};
		
		final Ticker showCopyright = new Ticker() {
			@Override
			boolean update(float dt) {
				copyright_a += 0.5f * dt;
				
				if (copyright_a >= 1f) {
					copyright_a = 1f;
					
					addTicker(playMusic);
					addTicker(scrollDriveLogo);
					
					return true;
				}
				
				return false;
			}
		};
		
		addTicker(showCopyright);
	}
	
	@Override
	public void update(GameContainer gc, int delta, boolean focus) throws SlickException  {
		super.update(gc, delta, focus);
		
		if (!focus)
			return;
		
		float dt = ((float)delta) / 1000;
		
		if (!menuMode)
			updateIntro(gc, dt);
		else
			updateMenu(gc, dt);
	}
	
	void updateIntro(GameContainer gc, float dt) throws SlickException {
		if (!done && (gc.getInput().isKeyDown(Input.KEY_SPACE) ||
				gc.getInput().isKeyDown(Input.KEY_UP) ||
				gc.getInput().isKeyDown(Input.KEY_LEFT) ||
				gc.getInput().isKeyDown(Input.KEY_RIGHT))) {
			done = true;
			clearTickers();
			
			if (!introMusic.playing())
				introMusic.play(1f, 0.7f);
			
			drive_a = 1f;
			drive_offsetY = 0f;
			scorpion_a = 1f;
			gosling_a = 1f;
			car_a = 1f;
			press_enter_a = 1f;
			copyright_a = 1f;
			
			addTicker(new Ticker() {
				float d_alfa = 0.8f;
				boolean ready = false;

				@Override
				boolean update(float dt) {
					press_enter_a += d_alfa * dt;
					
					if (menuMode) {
						press_enter_a = 0f;
						return true;
					}
					
					if (press_enter_a >= 1f || (ready && press_enter_a <= 0.4f)) {
						ready = true;
						d_alfa = -d_alfa;
					}

					return false;
				}
			});
		} else if (done && gc.getInput().isKeyPressed(Input.KEY_ENTER)) {
			menuMode = true;
		}
	}
	
	void updateMenu(GameContainer gc, float dt) throws SlickException {
		updateSelectMenu(dt);
		
		if (disableMoveSelect)
			return;
			
		if (gc.getInput().isKeyPressed(Input.KEY_UP)) {
			selectedMenu -= 1;
		} else if (gc.getInput().isKeyPressed(Input.KEY_DOWN)) {
			selectedMenu += 1;
		}
		
		while (selectedMenu >= menuImages.length) {
			selectedMenu -= menuImages.length;
		}
		while (selectedMenu < 0) {
			selectedMenu += menuImages.length;
		}
		
		if (gc.getInput().isKeyPressed(Input.KEY_ENTER)) {
			transition();
		} 
	}
	
	void updateSelectMenu(float dt) {
		selectedMenuScale += selectedMenuScale_d * dt;
		
		if (selectedMenuScale >= 1 + selectedMenuScaleSwing) {
			selectedMenuScale = 1 + selectedMenuScaleSwing;
			selectedMenuScale_d = -selectedMenuScale_d;
		} else if (selectedMenuScale <= 1 - selectedMenuScaleSwing) {
			selectedMenuScale = 1 - selectedMenuScaleSwing;
			selectedMenuScale_d = -selectedMenuScale_d;
		}
	}
	
	void transition() throws SlickException {
		fallAsleep(new Delegate() {
			@Override
			public void run() throws SlickException {
				switch (selectedMenu) {
					case 0: //Start Game
						if (!StateManager.exists(OutRunGame.class)) {
							StateManager.push(new OutRunGame());
						} else {
							StateManager.get(OutRunGame.class).unsuspend();
						}
						break;
					case 1: //Look at high scores
						StateManager.push(new HighScoreMenu());
						break;
					case 2: //Credits
						StateManager.push(new CreditsScreen());
						break;
					case 3: //Exit game
						StateManager.close();
						break;
				}
			}
		});
	}
	void fallAsleep(final Delegate followingAction) {
		disableMoveSelect = true;
		
		addTicker(new Ticker() {
				@Override
				boolean update(float dt) throws SlickException {
					blackScreen_a += 1f * dt;
					introMusic.setVolume(introMusic.getVolume() - 0.35f * dt);

					if (blackScreen_a >= 1f) {
						blackScreen_a = 1f;
						
						if (followingAction != null)
							followingAction.run();
						
						introMusic.stop();
						return true;
					}

					return false;
				}
			});
	}
	void wakeUp(final Delegate followingAction) {
		disableMoveSelect = true;
		
		addTicker(new Ticker() {
			@Override
			void start() {
				introMusic.play();
				introMusic.setVolume(0f);
			}
			
			@Override
			boolean update(float dt) throws SlickException {
					blackScreen_a -= 1f * dt;
					introMusic.setVolume(introMusic.getVolume() + 0.35f * dt);
					
					if (blackScreen_a <= 0f) {
						blackScreen_a = 0f;
						disableMoveSelect = false;
						if (followingAction != null)
							followingAction.run();
						
						return true;
					}

					return false;
				}
			});
	}
	
	@Override
	public void recievedFocus() {
		wakeUp(null);
	}
	
	@Override
	public void render(GameContainer gc, Graphics g, boolean focus) throws SlickException  {
		if (!focus)
			return;
		
		scorpion.setAlpha(scorpion_a);
		drive.setAlpha(drive_a);
		car.setAlpha(car_a);
		gosling.setAlpha(gosling_a);
		press_enter.setAlpha(press_enter_a);
		copyright.setAlpha(copyright_a);
		
		g.drawImage(scorpion, 0f, 0f);
		g.drawImage(drive, 0f, drive.getHeight() * drive_offsetY);
		g.drawImage(car, 0f, 0f);
		g.drawImage(gosling, 0f, 0f);
		g.drawImage(press_enter, 0f, 0f);
		g.drawImage(copyright, 0f, 0f);
		
		if (menuMode) {
			int beginY = 98;
			int endY = 176;
			int stepY = (endY - beginY) / (menuImages.length - 1);
			
			for (int i = 0; i < menuImages.length; i++) {
				float destX = OutRunGame.screenWidth/ 2 - menuImages[i].getWidth() / 2;
				float destY = beginY + stepY * i;
				float destW = menuImages[i].getWidth();
				float destH = menuImages[i].getHeight();
				
				if (i == selectedMenu) {
					destW *= selectedMenuScale;
					destH *= selectedMenuScale;
					destX = OutRunGame.screenWidth/ 2 - destW / 2;
					destY = beginY + stepY * i - (destH - menuImages[i].getHeight());
				}
				
				g.drawImage(menuImages[i], destX, destY, destX + destW, destY + destH, 0, 0, menuImages[i].getWidth(), menuImages[i].getHeight());
			}
		}
		
		g.setColor(new Color(0f, 0f, 0f, blackScreen_a));
		g.fillRect(0, 0, OutRunGame.screenWidth, OutRunGame.screenHeight);
	}
	
	Ticker getWaiter(final float seconds, final Ticker newTicker) {
		Ticker waiter = new Ticker() {
				long end = System.currentTimeMillis() + (long)(seconds * 1000);
				
				@Override
				boolean update(float dt) {
					if (System.currentTimeMillis() >= end) {
						addTicker(newTicker);
						return true;
					}
						
					return false;
				}
			};
		
		return waiter;
	}
	
	@Override
	public void closing() throws SlickException {
		press_enter.destroy();
	}
}
