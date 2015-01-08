package net.clonecomputers.lab.touchscreen.configure;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

public class PathPanel extends JPanel implements Iterable<int[]>, Iterator<int[]> {
	private PathIterator pathI;
	private double[] nextPoint = new double[2];
	private double[] point = new double[2];
	private double velocity = 1/50.; // pixels per millis
	private GeneralPath path;
	private long lastStepTime = Long.MIN_VALUE;
	
	@Override public void paintComponent(Graphics g1d) {
		Graphics2D g = (Graphics2D)g1d;
		g.setColor(Color.WHITE);
		g.fill(this.getBounds());
		g.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
		        RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.getHSBColor(.3f, .4f, .8f));
		g.draw(path);
		g.setStroke(new BasicStroke());
		g.setColor(Color.BLACK);
		g.drawLine(0, 768, 1024, 768);
		g.drawLine(1024, 0, 1024, 768);
		drawLine(g, point[0], 0, point[0], yMax);
		drawLine(g, 0, point[1], xMax, point[1]);
	}
	
	private static void plot(final Graphics2D g, final int x, final int y, double c) {
		c = Math.max(0, Math.min(c, 1));
		g.setColor(new Color(0,0,0,(float)c));
		g.drawLine(x,y,x,y);
		//image.setRGB(x, y, new Color((float) c, (float) c, (float) c).getRGB());
	}
	
	private static int round(final double x) {
		return (int) (x + 0.5);
	}

	private static double floatPart(final double x) {
		return x - (int) x;
	}
	
	public void drawLine(Graphics2D g, double x1, double y1, double x2, double y2) {
		//g.draw(new Line2D.Double(x1,y1,x2,y2));
		//g.draw(new Rectangle2D.Double(x1, y1, x2-x1+.5, y2-y1+.5));
		/*GeneralPath lineOutline = new GeneralPath.Double();
		lineOutline.moveTo(x1,y1);
		lineOutline.lineTo(x2, y2);
		lineOutline.closePath();
		g.draw(lineOutline);*/
		double dx = x2 - x1;
		double dy = y2 - y1;
		boolean steep = Math.abs(dx) < Math.abs(dy);
		if (steep) {
			double t = x1;
			x1 = y1;
			y1 = t;
			t = x2;
			x2 = y2;
			y2 = t;
			t = dx;
			dx = dy;
			dy = t;
		}
		if (x2 < x1) {
			double t = x1;
			x1 = x2;
			x2 = t;
			t = y1;
			y1 = y2;
			y2 = t;
		}
		double gradient = dy / dx;
		// handle first endpoint
		int xend = round(x1);
		double yend = y1 + gradient * (xend - x1);
		double xgap = 1 - floatPart(x1 + 0.5);
		int xpxl1 = xend; // this will be used in the main loop
		int ypxl1 = (int) yend;
		plot(g, xpxl1, ypxl1, (1 - floatPart(yend)) * xgap);
		plot(g, xpxl1, ypxl1 + 1, floatPart(yend) * xgap);
		double intery = yend + gradient; // first y-intersection for the main loop
		// handle second endpoint
		xend = round(x2);
		yend = y2 + gradient * (xend - x2);
		xgap = floatPart(x2 + 0.5);
		int xpxl2 = xend; // this will be used in the main loop
		int ypxl2 = (int) yend;
		plot(g, xpxl2, ypxl2, (1 - floatPart(yend)) * xgap);
		plot(g, xpxl2, ypxl2 + 1, floatPart(yend) * xgap);
		// main loop
		for (int x = xpxl1 + 1; x <= xpxl2 - 1; x++) {
			if(steep) {
				plot(g, (int) intery, x, 1 - floatPart(intery));
				plot(g, (int) intery + 1, x, floatPart(intery));
			} else {
				plot(g, x, (int) intery, 1 - floatPart(intery));
				plot(g, x, (int) intery + 1, floatPart(intery));
			}
			intery = intery + gradient;
		}
	}
	
	private static final int cornerOffset = 20;
	private static final int sideOffset = 50;
	private static final int centerOffset = 100;
	private static final int xMax = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
	private static final int yMax = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
	public PathPanel() {
		this(new double[][]{
				{cornerOffset, cornerOffset},
				{xMax/2, yMax/2 + centerOffset},
				{xMax - cornerOffset, cornerOffset},
				{xMax/2 + centerOffset, yMax/2},
				{xMax - cornerOffset, yMax - cornerOffset},
				{xMax/2, yMax/2 - centerOffset},
				{cornerOffset-10, yMax-cornerOffset-100},
				{xMax/2, yMax - sideOffset},
				{xMax - sideOffset, yMax/2},
				{xMax/2, sideOffset},
				{sideOffset, yMax/2},
				{350,yMax-125},
		});
	}
	
	public static GeneralPath getPathFromDoubleArray(double[][] points) {
		/*for(int i = 1; i < points.length; i++) {
			path.lineTo(points[i][0], points[i][1]);
		}*/
		Point[] knots = new Point[points.length];
		for(int i = 0; i < knots.length; i++) {
			knots[i] = new Point(points[i]);
		}
		Point[] firstControlPoints = new Point[knots.length-1];
		Point[] secondControlPoints = new Point[knots.length-1];
		BezierSpline.getCurveControlPoints(knots, firstControlPoints, secondControlPoints);

		GeneralPath path = new GeneralPath();
		path.moveTo((float)points[0][0],(float)points[0][1]);
		for(int i = 1; i < points.length; i++) {
			path.curveTo((float)firstControlPoints[i-1].x, (float)firstControlPoints[i-1].y, 
					(float)secondControlPoints[i-1].x, (float)secondControlPoints[i-1].y,
					(float)points[i][0], (float)points[i][1]);
		}
		/*path.curveTo(points[1][0], points[1][1],
				0, 0,
				secondControlPoints[0].x, secondControlPoints[0].y);
		for(int i = 2; i < points.length-1; i++) {
			path.curveTo(points[i][0], points[i][1], 
					firstControlPoints[i-2].x, firstControlPoints[i-2].y, 
					secondControlPoints[i-1].x, secondControlPoints[i-1].y);
		}
		path.curveTo(points[points.length-1][0], points[points.length-1][1],
				firstControlPoints[points.length-2].x, firstControlPoints[points.length-2].y,
				0, 0);*/
		return path;
	}
	
	public PathPanel(double[][] points) {
		this(getPathFromDoubleArray(points));
	}
	
	public PathPanel(GeneralPath path) {
		this.path = path;
		pathI = path.getPathIterator(new AffineTransform());
		pathI = new FlatteningPathIterator(pathI,2);
		pathI.currentSegment(nextPoint);
		System.arraycopy(nextPoint,0,point,0,2);
	}
	
	private void advance() {
		if(lastStepTime < 0) lastStepTime = System.currentTimeMillis() - 25;
		double distanceLeft = (System.currentTimeMillis() - lastStepTime) * velocity;
		lastStepTime = System.currentTimeMillis();
		double distanceToNextPoint = Math.hypot(nextPoint[0]-point[0], nextPoint[1]-point[1]);
		if(distanceLeft >= distanceToNextPoint) {
			distanceLeft -= distanceToNextPoint;
			System.arraycopy(nextPoint,0,point,0,2);
			if(pathI.isDone()) return;
			pathI.next();
			if(pathI.isDone()) return;
			pathI.currentSegment(nextPoint);
			distanceToNextPoint = Math.hypot(nextPoint[0]-point[0], nextPoint[1]-point[1]);
		}
		point[0] += (nextPoint[0]-point[0]) * (distanceLeft/distanceToNextPoint);
		point[1] += (nextPoint[1]-point[1]) * (distanceLeft/distanceToNextPoint);
		this.repaint();
	}
	
	public Iterator<int[]> iterator() {
		return this;
	}

	public boolean hasNext() {
		return !pathI.isDone() || !Arrays.equals(point, nextPoint);
	}

	public int[] next() {
		advance();
		return new int[]{
			(int) point[0],
			(int) point[1]
		};
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
