package net.clonecomputers.lab.touchscreen;
import gnu.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.border.*;

import net.clonecomputers.lab.touchscreen.configure.*;

import org.apache.commons.math3.analysis.interpolation.*;
import org.apache.commons.math3.analysis.polynomials.*;
import org.apache.commons.math3.stat.regression.*;

public class MouseMover {
	double[] xp;
	double[] yp;
	Robot r;
	String[] acceptablePortNames = {
			"/dev/tty.usbmodem12341",
			"/dev/tty.usbmodem12345",
			"/dev/tty.usbmodem123451",
	};
	SerialPort port = null;
	BufferedInputStream input;
	boolean recievingCommand = false;
	//private int numDataNeeded = 0;
	//byte currentCommand = 0x00;
	//byte[] cmdArgs;
	private boolean needsToConfigure = false;

	/*public void recieveData(byte data) {
		if ((data & 0x80) != 1) {
			if (numDataNeeded == 0)
				return;
			else{
				cmdArgs[cmdArgs.length-numDataNeeded] = data;
				numDataNeeded--;
			}
		}
	}*/

	public static void main(String[] args){
		MouseMover myMover = new MouseMover();
		if(args.length > 0 && (args[0].equals("-c") || args[0].equals("--configure"))){
			myMover.requestConfigure();
			myMover.init();
		}else if(args.length > 0 && (args[0].equals("-t"))){
			try {
				myMover.configure(true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}else{
			myMover.init();
		}
	}

	@SuppressWarnings("unchecked")
	public void init() {
		CommPortIdentifier portId = null;
		while(portId == null){
			for (CommPortIdentifier cpi : new IterableEnumeration<CommPortIdentifier>(
					CommPortIdentifier.getPortIdentifiers())) {
				System.out.println(cpi.getName());
				if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL
						&& cpi.getName().contains("/dev/tty.usbmodem")) {
					portId = cpi;
					break;
				}
			}
		}
		try {
			port = (SerialPort) portId.open("MouseMover", 
					10000 // Wait max. 10 sec. to acquire port
					);
		} catch (PortInUseException e) {
			throw new RuntimeException(e);
		}
		try {
			port.setSerialPortParams(9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			throw new RuntimeException(e);
		}
		/*try {
			port.addEventListener(new SerialPortEventListener() {

				@Override
				public void serialEvent(SerialPortEvent e) {
					System.out.println(e);
					if(e.getEventType() == SerialPortEvent.DATA_AVAILABLE){
						synchronized(MouseMover.this){
							if(recievingCommand) return;
							recievingCommand = true;
						}
						recieveCmd();
						recievingCommand = false;
					}
				}

			});
		} catch (TooManyListenersException e) {
			throw new RuntimeException(e);
		}*/
		try {
			input = new BufferedInputStream(port.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		/*try {
			configure();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}*/
		getPrefrences();
		new Thread(new Runnable(){
			//@Override
			public void run(){
				if(needsToConfigure){
					try {
						configure(false);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					needsToConfigure = false;
				}
				while(true) recieveCmd();
			}
		}).start();
	}

	private void putPreferences() {
		Preferences prefs = Preferences.userRoot().node("net.clonecomputers.lab.Touchscreen");
		prefs.putInt("n", xp.length);
		for(int i = 0; i < xp.length; i++) {
			prefs.putDouble("xp["+i+"]", xp[i]);
			prefs.putDouble("yp["+i+"]", yp[i]);
		}
	}

	public void getPrefrences() {
		Preferences prefs = Preferences.userRoot().node("net.clonecomputers.lab.Touchscreen");
		int n = prefs.getInt("n", -1);
		if(n <= 0) {
			try {
				configure(false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			xp = new double[n];
			yp = new double[n];
			for(int i = 0; i < n; i++) {
				xp[i] = prefs.getDouble("xp["+i+"]", Double.NaN);
				yp[i] = prefs.getDouble("yp["+i+"]", Double.NaN);
			}
		}
		/*xM = prefs.getDouble("xM",Double.NaN);
		xB = prefs.getDouble("xB",Double.NaN);
		yM = prefs.getDouble("yM",Double.NaN);
		yB = prefs.getDouble("yB",Double.NaN);
		System.out.println("xM = " + xM);
		System.out.println("xB = " + xB);
		System.out.println("yM = " + yM);
		System.out.println("yB = " + yB);
		if(Double.isNaN(xM) || Double.isNaN(xB) || Double.isNaN(yM) || Double.isNaN(yB)){
			try {
				configure(false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}*/
	}

	@Deprecated
	private class CrosshairsPanel extends JPanel{
		private int x, y;
		boolean showCrosshairs = false;
		@Override
		protected void paintComponent(Graphics g){
			if(showCrosshairs){
				g.setColor(Color.RED);
				g.fillRect(0, 0, x, y);
				g.setColor(Color.YELLOW);
				g.fillRect(0, y, x, getHeight());
				g.setColor(Color.BLUE);
				g.fillRect(x, 0, getWidth(), y);
				g.setColor(Color.GREEN);
				g.fillRect(x, y, getWidth(), getHeight());
				g.setColor(Color.BLACK);
				g.drawLine(x, 0, x, getHeight());
				g.drawLine(0, y, getWidth(), y);
			}
		}
		public void setLoc(int x, int y){
			this.x = x;
			this.y = y;
		}
		public void hide(){
			showCrosshairs = false;
		}
		public void show(){
			showCrosshairs = true;
		}
	}
	
	public void configure(boolean isTest) throws IOException {
		System.out.println("configuring");
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		JFrame pathWindow = new JFrame();
		if(isTest) pathWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pathWindow.setUndecorated(true);
		gd.setFullScreenWindow(pathWindow);
		pathWindow.pack();
		pathWindow.setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getWidth());
		PathPanel pathPanel = new PathPanel();
		pathWindow.setContentPane(pathPanel);
		pathWindow.setVisible(true);
		OutputStream tsControl;
		if(!isTest) {
			tsControl = port.getOutputStream();
		} else {
			tsControl = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// do nothing
				}
			};
		}
		tsControl.write(0x08); // put in "configure" mode
		// 0x08 could have been anything but 0x00 or 0x0f
		clearBuffer(isTest);
		List<int[]> tsPoints = new ArrayList<int[]>();
		List<int[]> screenPoints = new ArrayList<int[]>();
		int millisPerUpdate = 50;
		for(int[] screenPoint: pathPanel) {
			long millis = System.currentTimeMillis();
			screenPoints.add(screenPoint);
			tsPoints.add(readXY(isTest));
			System.out.println(Arrays.toString(screenPoint));
			while(System.currentTimeMillis()-millis < millisPerUpdate);
		}
		pathWindow.setVisible(false);
		pathWindow.dispose();
		clearBuffer(isTest);
		System.out.println("done");
		
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		
		double[][] inputRegressionParamaters = new double[tsPoints.size()][3];
		double[][] outputRegressionParamaters = new double[2][screenPoints.size()];
		for(int i = 0; i < tsPoints.size(); i++) {
			inputRegressionParamaters[i][0] = tsPoints.get(i)[0];
			inputRegressionParamaters[i][1] = tsPoints.get(i)[1];
			inputRegressionParamaters[i][2] = tsPoints.get(i)[0]*tsPoints.get(i)[1];
			outputRegressionParamaters[0][i] = screenPoints.get(i)[0];
			outputRegressionParamaters[1][i] = screenPoints.get(i)[1];
		}
		
		reg.newSampleData(outputRegressionParamaters[0], inputRegressionParamaters);
		xp = reg.estimateRegressionParameters();
		
		reg.newSampleData(outputRegressionParamaters[1], inputRegressionParamaters);
		yp = reg.estimateRegressionParameters();
		
		System.out.println("xp: "+Arrays.toString(xp));
		System.out.println("yp: "+Arrays.toString(yp));
		
		if(!isTest) putPreferences();
		//if(!isTest) getPrefrences();
	}
	
	/*public void configure(boolean isTest) throws IOException {
		System.out.println("configuring");
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		JFrame crosshairsWindow = new JFrame();
		CrosshairsPanel crosshairsPanel = new CrosshairsPanel();
		crosshairsWindow.setUndecorated(true);
		gd.setFullScreenWindow(crosshairsWindow);
		crosshairsWindow.pack();
		crosshairsWindow.setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getWidth());
		crosshairsWindow.setContentPane(crosshairsPanel);
		crosshairsWindow.setVisible(true);
		
		int numberOfPoints = 40;
		Random r = new Random();
		//double[] xi = new double[numberOfPoints];
		//double[] yi = new double[numberOfPoints];
		//double[] xa = new double[numberOfPoints];
		//double[] ya = new double[numberOfPoints];
		int[][] screenPoints = new int[numberOfPoints][2];
		int[][] touchscreenPoints = new int[numberOfPoints][2];
		for(int i = 0; i < numberOfPoints; i++) {
			screenPoints[i][0] = r.nextInt(1024);
			screenPoints[i][1] = r.nextInt(768);
		}
		for(int i = 0; i < numberOfPoints; i++){
			crosshairsPanel.setLoc(screenPoints[i][0],screenPoints[i][1]);
			crosshairsPanel.repaint();
			clearBuffer(isTest);
			waitForClick(isTest);
			touchscreenPoints[i] = readXY(isTest);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		crosshairsWindow.dispose();
		clearBuffer(isTest);

		OLSMultipleLinearRegression xr = new OLSMultipleLinearRegression();
		OLSMultipleLinearRegression yr = new OLSMultipleLinearRegression();
		
		double[][] inputRegressionParamaters = new double[numberOfPoints][3];
		double[][] outputRegressionParamaters = new double[numberOfPoints][2];
		for(int i = 0; i < numberOfPoints; i++) {
			inputRegressionParamaters[i][0] = touchscreenPoints[i][0];
			inputRegressionParamaters[i][1] = touchscreenPoints[i][1];
			inputRegressionParamaters[i][2] = touchscreenPoints[i][0]*touchscreenPoints[i][1];
			outputRegressionParamaters[0][i] = screenPoints[i][0];
			outputRegressionParamaters[1][i] = screenPoints[i][1];
		}
		
		xr.newSampleData(outputRegressionParamaters[0], inputRegressionParamaters);
		yr.newSampleData(outputRegressionParamaters[1], inputRegressionParamaters);
		
		xp = xr.estimateRegressionParameters();
		yp = yr.estimateRegressionParameters();
		
		System.out.println("xp: "+Arrays.toString(xp));
		System.out.println("yp: "+Arrays.toString(yp));
		
		if(!isTest) putPreferences();
		//if(!isTest) getPrefrences();
	}*/

	private int[] mapXY(int[] xy) {
		int x = xy[0], y = xy[1];
		xy[0] = (int)(xp[0] + x*xp[1] + y*xp[2] + x*y*xp[3]);
		xy[1] = (int)(yp[0] + x*yp[1] + y*yp[2] + x*y*yp[3]);
		return xy;
	}

	private void waitForClick(boolean testingConfigure) throws IOException {
		if(!testingConfigure){
			while(input.read() != 0x80);
		}else{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void clearBuffer(boolean testConfigure) throws IOException {
		if(!testConfigure){
			input.skip(input.available());
		}
	}

	/* Fits a line to the data
	 * @return {slope, intercept}
	 */
	private static double[] fitLine(double[][] pts) {
		double sumX = 0, sumY = 0, sumX2 = 0, sumXY = 0, numPts = pts.length;
		for(double[] p: pts){
			sumX += p[0];
			sumY += p[1];
			sumX2 += p[0]*p[0];
			sumXY += p[0]*p[1];
		}
		double slope = ((numPts * sumXY) - (sumX * sumY)) / ((numPts * sumX2) - (sumX * sumX));
		double intercept = ((sumX2 * sumY) - (sumX * sumXY)) / ((numPts * sumX2) - (sumX * sumX));
		return new double[]{slope, intercept};
	}

	public void recieveCmd(){
		int cmd;
		try {
			cmd = input.read();
		} catch (IOException ioe) {
			System.out.println("failed to read from serial");
			ioe.printStackTrace();
			return;
		}
		System.out.printf("cmd = %x: ", cmd);
		if((cmd & 0x80) == 0) return;
		switch(cmd){
		case 0xC0: // start left click
			System.out.println("left click");
			r.mousePress(InputEvent.BUTTON1_MASK);
			break;
		case 0xC1: // end left click
			System.out.println("left release");
			r.mouseRelease(InputEvent.BUTTON1_MASK);
			break;
		case 0xC2: // start right click
			System.out.println("right click");
			r.mousePress(InputEvent.BUTTON3_MASK);
			break;
		case 0xC3: // end right click
			System.out.println("right release");
			r.mouseRelease(InputEvent.BUTTON3_MASK);
			break;
		case 0x80: // move mouse
			int[] xy = readXY(false);
			xy = mapXY(xy);
			r.mouseMove(xy[0], xy[1]);
			System.out.printf("move to (%d, %d)\n", xy[0], xy[1]);
			break;
		case 0xF0: // set keyboard modifiers
			setModifiersWithRobot();
			break;
		default:
			System.out.printf("undefined: %x\n");
		}
	}

	@SuppressWarnings("unused") @Deprecated
	private void setModifiersWithApplescript(){
		String cmd = "osascript -e 'tell application \"System Events\"' -e 'key %s command' -e 'key %s option' -e 'key %s control' -e 'key %s shift' -e 'end tell'";
		int data = 0;
		try {
			data = input.read();
		} catch (IOException ioe) {
			System.out.println("failed to read from serial");
			ioe.printStackTrace();
			return;
		}
		cmd = String.format(cmd,((data & 0x01) != 0)?"down":"up",
				((data & 0x02) != 0)?"down":"up",
						((data & 0x04) != 0)?"down":"up",
								((data & 0x08) != 0)?"down":"up");
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void setModifiersWithRobot() {
		int data = 0;
		try {
			data = input.read();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		if((data & 0x01) != 0) r.keyPress(KeyEvent.VK_META);
		else r.keyRelease(KeyEvent.VK_META);
		if((data & 0x02) != 0) r.keyPress(KeyEvent.VK_CONTROL);
		else r.keyRelease(KeyEvent.VK_CONTROL);
		if((data & 0x04) != 0) r.keyPress(KeyEvent.VK_ALT);
		else r.keyRelease(KeyEvent.VK_ALT);
		if((data & 0x08) != 0) r.keyPress(KeyEvent.VK_SHIFT);
		else r.keyRelease(KeyEvent.VK_SHIFT);
	}

	private int[] readXY(boolean testingConfigure){
		if(!testingConfigure){
			byte[] args = new byte[3];
			try {
				input.read(args, 0, 3);
			} catch (IOException ioe) {
				System.out.println("failed to read from serial");
				ioe.printStackTrace();
				return new int[]{0, 0};
			}
			int x = ((args[0] & 0x7F) << 3) + ((args[1] & 0x70) >> 4);
			int y = ((args[1] & 0x0F) << 6) + ((args[2] & 0x3F) >> 0);
			//x = map(x, 805, 218, 0, 1024);
			//y = map(y, 232, 770, 0, 768);
			return new int[]{x, y};
		}else{
			return new int[]{0, 0};
		}
	}

	int map(int value, int fromLow, int fromHigh, int toLow, int toHigh){
		return (int)(((value-fromLow)*((toHigh-toLow)/(double)(fromHigh-fromLow)))+toLow);
	}

	public void close() {
		if (port != null)
			port.close();
	}

	public MouseMover() {
		try {
			r = new Robot();
		} catch (AWTException e) {
			throw new RuntimeException(e);
		}
	}

	public void requestConfigure() {
		needsToConfigure  = true;
	}

}
