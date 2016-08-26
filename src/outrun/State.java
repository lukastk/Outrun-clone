/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

/**
 *
 * @author lukas_000
 * 
 * Transitions between different screens in the game is handled
 * by this class.
 */
public class State {
	private boolean suspended = false;
	private boolean closing = false;
	private ArrayList<Ticker> tickers = new ArrayList<>();
	private Stack<Ticker> addStack = new Stack<>();
	
	protected void addTicker(Ticker ticker) {
		addStack.add(ticker);
	}
	protected void clearTickers() {
		addStack.clear();
	}
	
	public final boolean getClosing() { return closing; }
	
	public void update(GameContainer gc, int delta, boolean focus) throws SlickException  {
		float dt = ((float)delta) / 1000;
		
		for (Iterator<Ticker> it = tickers.iterator(); it.hasNext();) {
			Ticker ticker = it.next();
			
			if (ticker.update(dt)) {
				it.remove();
			}
		}
		
		while (!addStack.empty()) {
			Ticker ticker = addStack.pop();
			ticker.start();
			
			tickers.add(ticker);
		}
	}

	public void render(GameContainer gc, Graphics g, boolean focus) throws SlickException  {
		if (focus) {
			for (Iterator<Ticker> it = tickers.iterator(); it.hasNext();) {
				Ticker ticker = it.next();
				ticker.render(g);
			}
		}
	}

	public void init(GameContainer gc) throws SlickException {}
	
	public void recievedFocus() { }
	
	public void closing() throws SlickException  {}
	
	public final void close() {
		closing = true;
	}
	
	public boolean isSuspended() { return suspended; }
	public void suspend() {
		suspended = true;
	}
	public void unsuspend() {
		suspended = false;
	}
}
