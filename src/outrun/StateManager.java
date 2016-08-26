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
 */
public final class StateManager {
	private static ArrayList<State> states = new ArrayList<>();
	private static ArrayList<State> states_reverse = new ArrayList<>();
	
	private static Stack<State> addStates = new Stack<>();
	private static Stack<State> removeStates = new Stack<>();
	private static GameContainer gc;
	
	private static State lastStateWithFocus;
	
	public static void init(GameContainer gc) {
		StateManager.gc = gc;
	}
	
	public static void push(State state) throws SlickException {
		state.init(gc);
		addStates.add(state);
	}
	
	public static void pop() throws SlickException {
		for (State state : states) {
			if (!removeStates.contains(state)) {
				removeStates.add(state);
				return;
			}
		}
	}
	
	public static void update(int delta) throws SlickException {
		boolean focus = true;
		
		while (!addStates.empty()) {
			State state = addStates.pop();
			states.add(0, state);
			states_reverse.add(state);
		}
		
		while (!removeStates.empty()) {
			State state = removeStates.pop();
			state.closing();
			states.remove(state);
			states_reverse.remove(state);
		}
		
		for (Iterator<State> it = states.iterator(); it.hasNext();) {			
			State state = it.next();
			
			if (state.isSuspended())
				continue;
			
			if (lastStateWithFocus != state && focus) {
				state.recievedFocus();
			}
			
			state.update(gc, delta, focus);
			
			if (focus)
				lastStateWithFocus = state;
			
			focus = false;
			
			if (state.getClosing()) {
				it.remove();
				states_reverse.remove(state);
				state.closing();
			}
		}
	}
	
	public static void render(Graphics g) throws SlickException {
		//Drawing has to be done in the reverse order
		for (State state : states_reverse) {
			if (!state.isSuspended())
				state.render(gc, g, lastStateWithFocus == state);
		}
	}
	
	public static void close() throws SlickException {
		for (Iterator<State> it = states.iterator(); it.hasNext();) {
			State state = it.next();
			it.remove();
			state.closing();
		}
		
		gc.exit();
	}
	
	public static boolean exists(Class type) {
		for (State state : states) {
			return state.getClass().equals(type);
		}
		
		for (State state : addStates) {
			return state.getClass().equals(type);
		}
		
		return false;
	}
	
	public static State get(Class type) {
		for (State state : states) {
			return state;
		}
		
		for (State state : addStates) {
			return state;
		}
		
		return null;
	}
}
