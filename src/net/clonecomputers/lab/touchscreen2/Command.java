package net.clonecomputers.lab.touchscreen2;

import java.io.*;
import java.util.*;

public enum Command {
	KEYBOARD_NOOP(0, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	KEYBOARD_TOGGLE(1, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.keyboard.setVisible(!shl.keyboard.isVisible());
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	KEYBOARD_ON(2, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.keyboard.setVisible(true);
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	KEYBOARD_OFF(3, 0, 1) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			shl.keyboard.setVisible(false);
			return new int[]{shl.keyboard.isVisible()? 1: 0};
		}
	},
	CONFIGURE(4, 0, 48) {
		@Override
		public int[] runCommand(int[] args, InputStream input,
				OutputStream output, SerialHIDListener shl) {
			double[][] config;
			try {
				config = Configurator.configure(20, 200, 1024, 768, false, output, input);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			int numBytes = 0;
			for(double[] configRow: config) numBytes += configRow.length * 8;
			int[] retVal = new int[numBytes];
			int i = 0;
			for(double[] configRow: config) {
				for(double configItem: configRow) {
					long doubleBits = Double.doubleToLongBits(configItem);
					for(int shift = 0; shift < 64; shift += 8) {
						retVal[i++] = (int)((doubleBits >>> shift) & 0xffl);
					}
				}
			}
			return retVal;
		}
	},
	NOOP(5,0,0) {
		public int[] runCommand(int[] args, InputStream input, OutputStream output, SerialHIDListener shl) {
			return new int[0];
		}
	},
	QUIT(127,0,0) {
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
