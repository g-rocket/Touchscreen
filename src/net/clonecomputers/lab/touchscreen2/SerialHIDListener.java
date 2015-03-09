package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.net.*;

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
		if(!teensyGatewayPath.isFile()) {
			teensyGatewayPath = File.createTempFile("teensy_gateway", "");
			FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("teensy_gateway"), teensyGatewayPath);
			Runtime.getRuntime().exec("chmod u+x "+teensyGatewayPath.getAbsolutePath());
			teensyGatewayPath.deleteOnExit();
		}
		teensyGatewayProcess = Runtime.getRuntime().exec(teensyGatewayPath.getAbsolutePath());
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
		InputStream serialInput = teensyGatewayConnection.getInputStream();
		while(serialInput.available() > 0) {
			System.out.print((char)serialInput.read());
		}
		System.out.println();
	}
	
	private class LoggingInputStream extends FilterInputStream {
		public LoggingInputStream(InputStream arg0) {
			super(arg0);
		}
		
		@Override
		public int read() throws IOException {
			int val = super.read();
			System.out.println("read "+Integer.toHexString(val));
			return val;
		}
	}
	
	private class LoggingOutputStream extends FilterOutputStream {
		public LoggingOutputStream(OutputStream arg0) {
			super(arg0);
		}
		
		@Override
		public void write(int val) throws IOException {
			System.out.println("wrote "+Integer.toHexString(val));
			super.write(val);
		}
	}
	
	public void listenForCommands() throws IOException {
		InputStream serialInput = new LoggingInputStream(new BufferedInputStream(teensyGatewayConnection.getInputStream()));
		OutputStream serialOutput = new LoggingOutputStream(new BufferedOutputStream(teensyGatewayConnection.getOutputStream()));
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
