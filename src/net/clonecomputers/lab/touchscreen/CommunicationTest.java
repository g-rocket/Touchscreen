package net.clonecomputers.lab.touchscreen;
import java.io.*;


public class CommunicationTest {
	/**
	 * @param args
	 */
	public static void main(String[] wertiop) {
		while(true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String input;
			try {
				input = in.readLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			String[] vars = input.split(",");
			int x = new Integer(vars[0]);
			int y = new Integer(vars[1]);
			byte[] args = new byte[3];
			args[0] = (byte)((x & 0x3F8) >> 3); // high 7 bits of x
			args[1] = (byte)(((x & 0x007) << 4) | ((y & 0x3C0) >> 6)); // low 3 bits of x, then high 4 bits of y
			args[2] = (byte)(y & 0x03F); // low 6 bits of y
			
			int newX = ((args[0] & 0x7F) << 3) + ((args[1] & 0x70) >> 4);
			int newY = ((args[1] & 0x0F) << 6) + ((args[2] & 0x3F) >> 0);
			System.out.println(newX + "," + newY);
		}
	}

}
