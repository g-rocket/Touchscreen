package net.clonecomputers.lab.touchscreen2.streamutils;

import java.io.*;

public class LoggingOutputStream extends FilterOutputStream {
	private final String prefix;
	
	private static int globalNextId = 0;
	public LoggingOutputStream(OutputStream os) {
		this(os, "LOS #"+(globalNextId++));
	}
	
	public LoggingOutputStream(OutputStream os, String name) {
		super(os);
		prefix = name + " wrote";
	}
	
	@Override
	public void write(byte[] ba, int off, int len) throws IOException {
		System.out.printf("%s %s from %d to %d\n", prefix, arrayHexToString(ba), off, len);
		super.write(ba, off, len);
	}
	
	@Override
	public void write(byte[] ba) throws IOException {
		System.out.printf("%s %s\n", prefix, arrayHexToString(ba));
		super.write(ba);
	}
	
	static String arrayHexToString(byte[] ba) {
		StringBuilder sb = new StringBuilder("{");
		for(byte b: ba) {
			sb.append(String.format("%02x, ",b));
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		return sb.toString();
	}
	
	@Override
	public void write(int b) throws IOException {
		System.out.printf("%s %02x\n", prefix, b);
		super.write(b);
	}
}