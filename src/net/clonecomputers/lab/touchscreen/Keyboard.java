package net.clonecomputers.lab.touchscreen;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;


public class Keyboard{
	public static void main(String[] args) {
		final MouseMover myMover = new MouseMover();
		myMover.init();
		final JFrame window = new JFrame("keyboard");
		window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		window.getContentPane().add(new KeyboardWindow());
		//window.setFocusableWindowState(false);
		window.setUndecorated(true);
		window.setAlwaysOnTop(true);
		window.setLocation(0, 768-window.getHeight());
		
		//window.setAutoRequestFocus(false);
		//window.setOpacity(0.5f);
		//window.setFocusable(false);
		//window.setType(Window.Type.UTILITY);
		window.addWindowFocusListener(new WindowFocusListener(){

			//@Override
			public void windowGainedFocus(WindowEvent e) {

				Process p;
				try {
					p = Runtime.getRuntime().exec("/Users/gavinsyancey/Desktop/touchscreen/swap.sh");
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
				try {
					p.waitFor();Thread.sleep(50);
				} catch (InterruptedException ie) {
					throw new RuntimeException(ie);
				}
			}

			//@Override
			public void windowLostFocus(WindowEvent e) {
				
			}
			
		});

		window.pack();
		//java.net.URL iconLoc = window.getClass().getResource("images/icon.png");
		String iconLoc = "/Users/gavinsyancey/Documents/workspace/Touchscreen/src/images/icon.png";
		System.out.println(iconLoc);
		ImageIcon icon = new ImageIcon(iconLoc);
		TrayIcon tray = new TrayIcon(icon.getImage(), "Open keyboard");
		tray.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				//System.out.println("click");
				if(e.getButton() == MouseEvent.BUTTON3 || e.isControlDown()){
					myMover.requestConfigure();
				}else{
					window.setVisible(!window.isVisible());
				}
			}
		});
		try {
			SystemTray.getSystemTray().add(tray);
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
		System.out.println(tray.getSize());
		//window.setVisible(true);
	}
	
	
	
}
