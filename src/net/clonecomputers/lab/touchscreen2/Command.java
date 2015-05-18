package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.util.*;

public enum Command {
	KEYBOARD_NOOP(0x00, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	KEYBOARD_TOGGLE(0x01, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.keyboard.setVisible(!shl.keyboard.isVisible());
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	KEYBOARD_ON(0x02, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.keyboard.setVisible(true);
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	KEYBOARD_OFF(0x03, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.keyboard.setVisible(false);
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	CONFIGURE(0x04, 0, 66) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			double[][] config;
			try {
				config = Configurator.configure(20, 200, 1024, 768, false, output, input);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return shiftOutDoubleAsBits(config);
		}
		
		private int[] shiftOutDoubleAsBits(double[][] config) {
			int numBytes = 0;
			for(double[] configRow: config) numBytes += configRow.length * 8;
			int[] retVal = new int[numBytes];
			int i = 0;
			for(double[] configRow: config) {
				for(double configItem: configRow) {
					long doubleBits = Double.doubleToLongBits(configItem);
					for(int shift = 0; shift < 64; shift += 6) {
						retVal[i++] = (int)((doubleBits >>> shift) & 0x3fl);
					}
				}
			}
			return retVal;
		}
	},
	NOOP(0x05,0,0) {
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			return new int[0];
		}
	},
	KEYBOARD_KEYDOWN(0x06, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			shl.keyboard.keyDown(args[0]);
			return new int[0];
		}
	},
	KEYBOARD_KEYUP(0x07, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			shl.keyboard.keyUp(args[0]);
			return new int[0];
		}
	},
	KEYBOARD_KEYPRESS(0x08, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			shl.keyboard.keyPress(args[0]);
			return new int[0];
		}
	},
	PRINT_POINT(0x10,0,0) {
		@Override
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			try {
				System.out.println(Arrays.toString(Configurator.readXY(input, output)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new int[0];
		}
	},
	RETURN_RANDOM_CONFIG(0x11,0,66) {
		@Override
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			double[][] config = new double[2][3];
			for(double[] configLine: config) {
				configLine[0] = 3;
				configLine[1] = -7;
				configLine[2] = 12;
			}
			System.out.println(Arrays.deepToString(config));
			int numBytes = 0;
			for(double[] configRow: config) numBytes += configRow.length * 11;
			int[] retVal = new int[numBytes];
			int i = 0;
			for(double[] configRow: config) {
				for(double configItem: configRow) {
					long doubleBits = Double.doubleToLongBits(configItem);
					for(int shift = 0; shift < 64; shift += 6) {
						retVal[i++] = (int)((doubleBits >> shift) & 0x3fl);
					}
				}
			}
			return retVal;
		}
	},
	SEND_LOTS_OF_BYTES(0x12,0,256) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			int[] retval = new int[256];
			for(int i = 0; i < retval.length; i++) {
				retval[i] = i;
			}
			return retval;
		}
	},
	QUIT(0x1e,0,0) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.shouldQuit = true;
			return new int[0];
		}
	};
	
	private static final Command[] commandsById;
	public final int id;
	public final int numArgs;
	public final int numReturns;
	
	static {
		commandsById = new Command[0x1f];
		for(Command c: Command.values()) {
			commandsById[c.id] = c;
		}
	}
	
	private Command(int id, int numArgs, int numReturns) {
		this.id = id;
		this.numArgs = numArgs;
		this.numReturns = numReturns;
	}
	
	public static void runCommand(InputStream input, OutputStream output, SerialHIDListener shl) throws IOException {
		Command cmd = readCommand(input);
		output.write(0x06); // recieved command
		output.flush();
		System.out.print("Recieved "+cmd);
		int[] args = cmd.readArgs(input);
		System.out.print(Arrays.toString(args));
		int[] retVal = cmd.runCommand(args, input, output, shl);
		System.out.println(": "+Arrays.toString(retVal));
		cmd.sendReturn(output, retVal);
	}
	
	public abstract int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl);
	
	public static Command forId(int id) {
		return commandsById[id];
	}
	
	public static Command readCommand(InputStream input) throws IOException {
		int cmdId;
		while((cmdId = input.read()) < 0x40) Thread.yield(); // either no input or not a command
		Command cmd = forId(cmdId & 0x1f);
		if(cmd == null) throw new IOException("Invalid command: "+cmdId);
		return cmd;
	}
	
	public int[] readArgs(InputStream input) throws IOException {
		int[] args = new int[numArgs];
		int i = 0;
		while(i < args.length && (input.available() == 0 || ((args[i++] = input.read()) < 0x40 && args[i] >= 0))) Thread.yield();
		if(i < args.length) throw new IOException("not enough args passed");
		return args;
	}

	public void sendReturn(OutputStream output, int[] retVal) throws IOException {
		if(retVal.length != numReturns) {
			throw new IllegalArgumentException("Wrong number of returns: passed "+retVal.length+", should be "+numReturns);
		}
		for(int i: retVal) output.write(i);
		output.flush();
	}
}
