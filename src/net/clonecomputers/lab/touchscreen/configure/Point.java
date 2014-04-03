package net.clonecomputers.lab.touchscreen.configure;

import java.awt.geom.*;

public class Point {
	public double x;
	public double y;
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public Point(Point2D p) {
		this(p.getX(),p.getY());
	}
	public Point(double[] p) {
		this(p[0],p[1]);
	}
	public Point2D getPoint() {
		return new Point2D.Double(x, y);
	}
}
