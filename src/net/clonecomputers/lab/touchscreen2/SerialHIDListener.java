package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.net.*;

/**
 * Will listen for messages from the emulated serial port (using teensy_gateway) and
 * open the Configurator or KeyboardDisplayer if needed
 * @author g-rocket
 */
public class SerialHIDListener {
	private Process teensyGatewayProcess;
	private Socket teensyGatewayConnection;
	
	public SerialHIDListener() throws IOException {
		File teensyGatewayPath = new File(getClass().getResource("teensy_gateway").getPath());
		teensyGatewayProcess = Runtime.getRuntime().exec(teensyGatewayPath.getAbsolutePath());
		teensyGatewayConnection = new Socket("localhost", 28541);
	}
	
	public void listenForCommands() {
		
	}
	
	public static void main(String[] args) throws IOException {
		new SerialHIDListener().listenForCommands();
	}
}
