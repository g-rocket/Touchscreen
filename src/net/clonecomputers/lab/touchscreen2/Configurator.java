package net.clonecomputers.lab.touchscreen2;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import net.clonecomputers.lab.touchscreen.configure.*;

/**
 * Will display a configuration image and generate configuration parameters
 * @author g-rocket
 */
public class Configurator {
	public static void main(String[] args) throws IOException {
		System.out.println(Arrays.toString(configure(2, 100,
			new OutputStream() {
				@Override
				public void write(int b) throws IOException {}
			}, new InputStream() {
				@Override
				public int read() throws IOException {
					return 0;
				}
			}
		)));
	}
	
	/**
	 * 
	 * @param sparsity is how many pixels apart we sample position
	 * @param velocity is how many pixels we move per second
	 * (if sparsity is too low and velocity is too high, we can fall behind)
	 * @param tsControl is the OutputStream connected to the touchscreen
	 * @param tsData is the InputStream connected to the touchscreen
	 * @return the configuration data in the form:
	 * 
	 * @throws IOException
	 */
	public static double[][] configure(double sparsity, double velocity,
			OutputStream tsControl, InputStream tsData) throws IOException {
		System.out.println("configuring");
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		JFrame pathWindow = new JFrame();
		pathWindow.setUndecorated(true);
		gd.setFullScreenWindow(pathWindow);
		pathWindow.pack();
		pathWindow.setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getWidth());
		PathPanel pathPanel = new PathPanel(sparsity);
		pathWindow.setContentPane(pathPanel);
		pathWindow.setVisible(true);
		//tsControl.write(0x08); // put in "configure" mode
		// 0x08 could have been anything but 0x00 or 0x0f
		tsData.skip(tsData.available()); // clear buffer
		List<int[]> tsPoints = new ArrayList<int[]>();
		List<int[]> screenPoints = new ArrayList<int[]>();
		long startTime;
		for(int[] screenPoint: pathPanel) {
			startTime = System.currentTimeMillis();
			screenPoints.add(screenPoint);
			pathPanel.repaint();
			tsControl.write(0x00); // ask for next point
			tsPoints.add(readXY(tsData));
			System.out.println(Arrays.toString(screenPoint));
			try {
				long sleepTime = (long)((sparsity/velocity) * 1000) - (System.currentTimeMillis() - startTime);
				if(sleepTime > 0) {
					Thread.sleep(sleepTime);
				} else {
					System.out.println("Can't keep up!");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pathWindow.setVisible(false);
		pathWindow.dispose();
		tsData.skip(tsData.available()); // clear buffer
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
		
		double[][] configuration = new double[2][];
		
		reg.newSampleData(outputRegressionParamaters[0], inputRegressionParamaters);
		configuration[0] = reg.estimateRegressionParameters();
		
		reg.newSampleData(outputRegressionParamaters[1], inputRegressionParamaters);
		configuration[1] = reg.estimateRegressionParameters();
		
		System.out.println("xp: "+Arrays.toString(configuration[0]));
		System.out.println("yp: "+Arrays.toString(configuration[1]));
		
		return configuration;
	}

	private static int[] readXY(InputStream tsData){
		byte[] args = new byte[3];
		try {
			tsData.read(args, 0, 3);
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
	}
}
