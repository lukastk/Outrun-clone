/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package outrun;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

/**
 *
 * @author lukas_000
 * 
 * A process, that finishes itself when its done.
 */
public abstract class Ticker {
	void start() throws SlickException {}
	abstract boolean update(float dt) throws SlickException;
	void render(Graphics g) throws SlickException {}
}