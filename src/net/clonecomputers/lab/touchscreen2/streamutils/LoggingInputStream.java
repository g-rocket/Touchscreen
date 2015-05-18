package net.clonecomputers.lab.touchscreen2.streamutils;

import java.io.*;

import static net.clonecomputers.lab.touchscreen2.streamutils.LoggingOutputStream.arrayHexToString;

public class LoggingInputStream extends FilterInputStream {
	private final String prefix;
	
	private static int globalNextId = 0;
	public LoggingInputStream(InputStream is) {
		this(is, "LOS #"+(globalNextId++));
	}
	
	public LoggingInputStream(InputStream is, String name) {
		super(is);
		prefix = name + " read";
	}
	
	@Override
	public int read(byte[] ba, int off, int len) throws IOException {
		int bytesRead = super.read(ba, off, len);
		System.out.printf("%s %s from %d to %d (%d bytes)\n", prefix, arrayHexToString(ba), off, len, bytesRead);
		return bytesRead;
	}
	
	@Override
	public int read(byte[] ba) throws IOException {
		int bytesRead = super.read(ba);
		System.out.printf("%s %s (%d bytes)\n", prefix, arrayHexToString(ba), bytesRead);
		return bytesRead;
	}
	
	@Override
	public int read() throws IOException {
		int b = super.read();
		System.out.printf("%s 0x%02x\n", prefix, b);
		return b;
	}
}