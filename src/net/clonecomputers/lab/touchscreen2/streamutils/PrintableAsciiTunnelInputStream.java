package net.clonecomputers.lab.touchscreen2.streamutils;

import java.io.*;

public class PrintableAsciiTunnelInputStream extends FilterInputStream {

	public PrintableAsciiTunnelInputStream(InputStream is) {
		super(is);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int numBytesRead = super.read(b, off, len);
		for(int i = off; i < off+numBytesRead && i < off+len; i++) {
			b[i] = transform(b[i]);
		}
		return numBytesRead;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int numBytesRead = super.read(b);
		for(int i = 0; i < numBytesRead; i++) {
			b[i] = transform(b[i]);
		}
		return numBytesRead;
	}
	
	@Override
	public int read() throws IOException {
		return transform((byte)super.read());
	}
	
	private byte transform(byte b) {
		if(b > (95+32) || b < 32) throw new IllegalArgumentException("This ascii tunnel only supports up to 32 to (95+32); it recieved "+b);
		return (byte)(b - 32);
	}

}
