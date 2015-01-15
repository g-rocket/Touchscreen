package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.net.*;

/**
 * Will listen for messages from the emulated serial port (using teensy_gateway) and
 * open the Configurator or KeyboardDisplayer if needed
 * @author g-rocket
 */
public class SerialHIDListener implements Closeable {
	private Process teensyGatewayProcess;
	private Socket teensyGatewayConnection;
	
	public SerialHIDListener() throws IOException {
		File teensyGatewayPath = new File(getClass().getResource("teensy_gateway").getPath());
		teensyGatewayProcess = Runtime.getRuntime().exec(teensyGatewayPath.getAbsolutePath());
		teensyGatewayConnection = new Socket("localhost", 28541);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					SerialHIDListener.this.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
	}
	
	public void listenForCommands() throws IOException {
		BufferedInputStream serialInput = new BufferedInputStream(teensyGatewayConnection.getInputStream());
		BufferedOutputStream serialOutput = new BufferedOutputStream(teensyGatewayConnection.getOutputStream());
		while(true) {
			Command command = Command.readCommand(serialInput);
			serialOutput.write(0); // recieved command
			@SuppressWarnings("unused")
			int[] args = command.readArgs(serialInput);
			int[] retVal = new int[command.numReturns];
			switch(command) {
			case CONFIGURE:
				double[][] config = Configurator.configure(2, 100, 1024, 768, serialOutput, serialInput);
				for(double[] configRow: config) {
					for(double configItem: configRow) {
						int floatBits = Float.floatToIntBits((float)configItem);
						for(int shift = 0; shift < 32; shift += 8) {
							serialOutput.write((floatBits & (0xff << shift)) >> shift);
						}
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("Command "+command+" is not implemented yet");
			}
			command.sendReturn(serialOutput, retVal);
		}
	}
	
	public static void main(String[] args) throws IOException {
		SerialHIDListener shl = new SerialHIDListener();
		shl.listenForCommands();
		shl.close();
	}

	public void close() throws IOException {
		teensyGatewayConnection.close();
		teensyGatewayProcess.destroy();
	}
	
	@Override
	protected void finalize() {
		try {
			this.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
