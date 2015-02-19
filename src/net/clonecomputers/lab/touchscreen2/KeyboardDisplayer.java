package net.clonecomputers.lab.touchscreen2;

import java.awt.*;
import java.io.*;

import javax.swing.*;
import javax.script.*;

/**
 * Will display a translucent keyboard overlay
 * @author g-rocket
 */
public class KeyboardDisplayer {
	private boolean isShowing = false;
	private JFrame keyboard;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		KeyboardDisplayer kbd = new KeyboardDisplayer();
		kbd.setVisible(true);
		Thread.sleep(500);
		kbd.setVisible(false);
		Thread.sleep(1000);
		kbd.setVisible(true);
	}
	
	public KeyboardDisplayer() {
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		keyboard = new JFrame();
		keyboard.setAlwaysOnTop(true);
		keyboard.setContentPane(new JPanel(){
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Color[] colors = {
						Color.BLACK,
						Color.BLUE,
						Color.CYAN,
						Color.DARK_GRAY,
						Color.GRAY,
						Color.GREEN,
						Color.LIGHT_GRAY,
						Color.MAGENTA,
						Color.ORANGE,
						Color.PINK,
						Color.RED,
						Color.WHITE,
						Color.YELLOW,
				};
				for(int i = 0; i < g.getClipBounds().height/5; i++) {
					g.setColor(colors[i % colors.length]);
					g.fillRect(0, i*5, g.getClipBounds().width, 5);
				}
			}
		});
		keyboard.getRootPane().putClientProperty("Window.alpha", new Float(.7));
		keyboard.setUndecorated(true);
		keyboard.setPreferredSize(new Dimension(dm.getWidth(), dm.getHeight()));
		keyboard.setBounds(0, 0, dm.getWidth(), dm.getHeight());
		keyboard.pack();
	}
	
	public void setVisible(boolean show) {
		if(isShowing == show) return;
		if(show) {
			keyboard.setVisible(true);
			try {
				Process p = Runtime.getRuntime().exec(new String[] {"osascript","-e","tell application \"net.clonecomputers.lab.touchscreen2.KeyboardDisplayer\" to activate"});
				//TODO: pipe streams
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			keyboard.setVisible(false);
			System.out.println("hiding");
			try {
				Runtime.getRuntime().exec(new String[] {"osascript","-e","tell application \"System Events\" to key code 48 using {command down}"}).waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("backgrounded?");
		}
		isShowing = show;
	}
	
	public boolean getVisible() {
		return isShowing;
	}
	
}
