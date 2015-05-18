package net.clonecomputers.lab.touchscreen2.streamutils;

import java.io.*;

public class PrintableAsciiTunnelOutputStream extends FilterOutputStream {

	public PrintableAsciiTunnelOutputStream(OutputStream os) {
		super(os);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for(int i = off; i < (off+len); i++) {
			b[i] = transform(b[i]);
		}
		super.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		for(int i = 0; i < b.length; i++) {
			b[i] = transform(b[i]);
		}
		super.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		super.write(transform((byte)b));
	}
	
	private byte transform(byte b) {
		if(b > 95 || b < 0) throw new IllegalArgumentException(String.format("This ascii tunnel only supports 0x00 to 0x5e; you passed 0x%02x",b));
		return (byte)(b + 32);
	}
}
