package net.clonecomputers.lab.touchscreen2.streamutils;

import java.io.*;

public class PrintableAsciiTunnelInputStream extends FilterInputStream {

	protected PrintableAsciiTunnelInputStream(InputStream is) {
		super(is);
	}

}
