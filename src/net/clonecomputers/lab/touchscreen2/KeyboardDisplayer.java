package net.clonecomputers.lab.touchscreen2;

import java.awt.*;

import javax.swing.*;

/**
 * Will display a translucent keyboard overlay
 * @author g-rocket
 */
public class KeyboardDisplayer {
	private boolean isShowing = false;
	private JFrame keyboard;
	
	public static void main(String[] args) throws InterruptedException {
		KeyboardDisplayer kbd = new KeyboardDisplayer();
		kbd.setVisible(true);
		Thread.sleep(5000);
		kbd.setVisible(false);
		Thread.sleep(5000);
		kbd.setVisible(true);
	}
	
	public KeyboardDisplayer() {
		keyboard = new JFrame(){
			@Override
			public boolean contains(int x, int y) {
				return false;
			}
			@Override
			public boolean contains(Point p) {
				return false;
			}
			@Override
			public Rectangle bounds() {
				return new Rectangle(0,0);
			}
			@Override
			public Rectangle getBounds() {
				return new Rectangle(0, 0);
			}
			@Override
			public Rectangle getBounds(Rectangle rv) {
				rv.width = 0;
				rv.height = 0;
				return rv;
			}
			
			
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(500, 500);
			}
		};
		keyboard.setAlwaysOnTop(true);
		keyboard.getRootPane().putClientProperty("Window.alpha", new Float(.5));
		keyboard.pack();
	}
	
	public void setVisible(boolean show) {
		if(isShowing == show) return;
		if(show) {
			keyboard.setVisible(true);
		} else {
			keyboard.setVisible(false);
		}
		isShowing = show;
	}
	
	public boolean getVisible() {
		return isShowing;
	}
	
}
