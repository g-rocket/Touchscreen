package net.clonecomputers.lab.touchscreen2.streamutils;

import java.io.*;

public class PrintableAsciiTunnelOutputStream extends FilterOutputStream {

	public PrintableAsciiTunnelOutputStream(OutputStream os) {
		super(os);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		super.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		super.write(b);
	}

	@Override
	public void write(int b) throws IOException {
		super.write(transform((byte)b));
	}
	
	private byte transform(byte b) {
		if(b > 95 || b < 0) throw new IllegalArgumentException("This ascii tunnel only supports up to 95; you passed "+b);
		return (byte)(b + 32);
	}
}
