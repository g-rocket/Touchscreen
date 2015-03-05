package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.util.*;

public enum Command {
	KEYBOARD_NOOP(0, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	KEYBOARD_TOGGLE(1, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	KEYBOARD_ON(2, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	KEYBOARD_OFF(3, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			// TODO Auto-generated method stub
			return null;
		}
	},
	CONFIGURE(4, 0, 16) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			int[] retVal = new int[16];
			int i = 0;
			double[][] config;
			try {
				config = Configurator.configure(20, 200, 1024, 768, false, output, input);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			for(double[] configRow: config) {
				for(double configItem: configRow) {
					int floatBits = Float.floatToIntBits((float)configItem);
					for(int shift = 0; shift < 32; shift += 8) {
						retVal[i++] = (floatBits & (0xff << shift)) >> shift;
					}
				}
			}
			return retVal;
		}
	},
	QUIT(127,0,0) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	private static final Command[] commandsById;
	public final int id;
	public final int numArgs;
	public final int numReturns;
	
	static {
		commandsById = new Command[128];
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
		output.write(0); // recieved command
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
		while((cmdId = input.read()) < 0x80) Thread.yield(); // either no input or not a command
		Command cmd = forId(cmdId & 0x7f);
		if(cmd == null) throw new IOException("Invalid command: "+cmdId);
		return cmd;
	}
	
	public int[] readArgs(InputStream input) throws IOException {
		int[] args = new int[numArgs];
		int i = 0;
		while(i < args.length && (input.available() == 0 || ((args[i++] = input.read()) < 0x80 && args[i] >= 0))) Thread.yield();
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
