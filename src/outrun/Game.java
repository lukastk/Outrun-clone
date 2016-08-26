/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

/**
 *
 * @author lukas_000
 */
import org.newdawn.slick.*;

public class Game extends BasicGame
{
	public Game() {
		super("Drive - The Game");
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		StateManager.init(gc);
		StateManager.push(new MenuScreen());
		
		gc.setShowFPS(false);
	}
	
	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		StateManager.update(delta);
	}
	
	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException {
		StateManager.render(g);

		// Scale the image
		// To get a pixelated feel, the game scales the game up.
		Image im = new Image(OutRunGame.screenWidth, OutRunGame.screenHeight);
        gc.getGraphics().copyArea(im, 0, 0);
		im.setFilter(Image.FILTER_NEAREST);
		
		g.clear();
		
		g.scale(OutRunGame.scale, OutRunGame.scale);
		g.drawImage(im, 0, 0);
		im.destroy();
	}
	
	public static void main(String[] args) throws SlickException {
		AppGameContainer app = new AppGameContainer(new Game());
		
		app.setDisplayMode(OutRunGame.screenWidth * OutRunGame.scale, OutRunGame.screenHeight * OutRunGame.scale, false);
		app.start();
	}
}
