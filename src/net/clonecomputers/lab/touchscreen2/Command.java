package net.clonecomputers.lab.touchscreen2;

import java.io.*;

public enum Command {
	KEYBOARD_NOOP(0, 0, 1),
	KEYBOARD_TOGGLE(1, 0, 1),
	KEYBOARD_ON(2, 0, 1),
	KEYBOARD_OFF(3, 0, 1),
	CONFIGURE(4, 0, 16);
	
	private static final Command[] commandsById = new Command[128];
	public final int id;
	public final int numArgs;
	public final int numReturns;
	
	private Command(int id, int numArgs, int numReturns) {
		this.id = id;
		this.numArgs = numArgs;
		this.numReturns = numReturns;
		setId();
	}
	
	private void setId() {
		commandsById[id] = this;
	}
	
	public static Command forId(int id) {
		return commandsById[id];
	}
	
	public static Command readCommand(BufferedInputStream input) throws IOException {
		int cmdId;
		while((cmdId = input.read()) < 0x80) Thread.yield(); // either no input or not a command
		Command cmd = forId(cmdId);
		if(cmd == null) throw new IOException("Invalid command: "+cmdId);
		return cmd;
	}
	
	public int[] readArgs(BufferedInputStream input) throws IOException {
		int[] args = new int[numArgs];
		int i = 0;
		while(input.available() == 0 || ((args[i++] = input.read()) < 0x80 && args[i] >= 0)) Thread.yield();
		if(i < args.length) throw new IOException("not enough args passed");
		return args;
	}

	public void sendReturn(BufferedOutputStream output, int[] retVal) throws IOException {
		if(retVal.length != numReturns) {
			throw new IllegalArgumentException("Wrong number of returns: passed "+retVal.length+", should be "+numReturns);
		}
		for(int i: retVal) output.write(i);
		output.flush();
	}
}
