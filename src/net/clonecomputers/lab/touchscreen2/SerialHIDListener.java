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
		mainLoop:
		while(true) {
			System.out.println("waiting for command");
			Command command = Command.readCommand(serialInput);
			serialOutput.write(0); // recieved command
			serialOutput.flush();
			@SuppressWarnings("unused")
			int[] args = command.readArgs(serialInput);
			int[] retVal = new int[command.numReturns];
			System.out.println("recieved "+command);
			switch(command) {
			case CONFIGURE:
				double[][] config = Configurator.configure(20, 200, 1024, 768, false, serialOutput, serialInput);
				for(double[] configRow: config) {
					for(double configItem: configRow) {
						int floatBits = Float.floatToIntBits((float)configItem);
						for(int shift = 0; shift < 32; shift += 8) {
							serialOutput.write((floatBits & (0xff << shift)) >> shift);
						}
					}
				}
				break;
			case QUIT:
				break mainLoop;
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
			// Don't care
		}
	}
}
