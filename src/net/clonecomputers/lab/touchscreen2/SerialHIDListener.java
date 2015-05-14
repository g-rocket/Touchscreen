package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.net.*;

import net.clonecomputers.lab.touchscreen2.streamutils.*;

import org.apache.commons.io.*;

/**
 * Will listen for messages from the emulated serial port (using teensy_gateway) and
 * open the Configurator or KeyboardDisplayer if needed
 * @author g-rocket
 */
public class SerialHIDListener implements Closeable {
	private Process teensyGatewayProcess;
	private Socket teensyGatewayConnection;
	public boolean shouldQuit;
	public final KeyboardDisplayer keyboard;
	
	public SerialHIDListener() throws IOException {
		keyboard = new KeyboardDisplayer();
		File teensyGatewayPath = new File(getClass().getResource("teensy_gateway").getPath());
		try {
			teensyGatewayProcess = Runtime.getRuntime().exec(teensyGatewayPath.getAbsolutePath());
		} catch(IOException e) {
			teensyGatewayPath = File.createTempFile("teensy_gateway", "");
			FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("teensy_gateway"), teensyGatewayPath);
			Runtime.getRuntime().exec("chmod u+x "+teensyGatewayPath.getAbsolutePath());
			teensyGatewayPath.deleteOnExit();
			teensyGatewayProcess = Runtime.getRuntime().exec(teensyGatewayPath.getAbsolutePath());
		}
		teensyGatewayConnection = new Socket();
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getByAddress(new byte[]{127,0,0,1}), 28541);
        teensyGatewayConnection.connect(addr, 50);
		//teensyGatewayConnection = new Socket(InetAddress.getByAddress(new byte[]{127,0,0,1}), 28541);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					SerialHIDListener.this.close();
				} catch (IOException e) {
					// Don't care
				}
			}
		}));
		
		// clear the input
		String expected = "teensy_gateway";
		InputStream serialInput = teensyGatewayConnection.getInputStream();
		for(int i = 0; i < expected.length(); i++) {
			if((char)serialInput.read() != expected.charAt(i)) throw new IOException("Error starting up teensy_gateway");
		}
		System.out.println();
	}
	
	public void listenForCommands() throws IOException {
		InputStream serialInput = new LoggingInputStream(new PrintableAsciiTunnelInputStream(teensyGatewayConnection.getInputStream()));
		OutputStream serialOutput = new LoggingOutputStream(new PrintableAsciiTunnelOutputStream(teensyGatewayConnection.getOutputStream()));
		System.out.println("Listening");
		while(!shouldQuit) {
			System.out.println("waiting for command");
			Command.runCommand(serialInput, serialOutput, this);
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
			// that should have thrown an exception
			System.err.println(this+" was garbage collected without being closed!");
		} catch (IOException e) {
			// expected
		}
	}
}
