package net.clonecomputers.lab.touchscreen.configure;

// this was all written in C#3.0 originally

/** 
 *  Bezier Spline methods
 *  
 *  @author Oleg V. Polikarpotchkin, Peter Lee
 */
public class BezierSpline {
	/**
	 * Get open-ended Bezier Spline Control Points.
	 * 
	 * @param knots Input Knot Bezier spline points.
	 * @param firstControlPoints Output First Control points
	 * array of knots.length - 1 length.
	 * @param secondControlPoints Output Second Control points
	 * array of knots.length - 1 length.
	 * @exception NullPointerException knots parameter must be not null.
	 * @exception IllegalArgumentException knots array must contain at least two points.
	 */
	public static void getCurveControlPoints(Point[] knots,
			Point[] firstControlPoints, Point[] secondControlPoints) {
		if (knots == null)
			throw new NullPointerException("knots");
		int n = knots.length - 1;
		if (n < 1)
			throw new IllegalArgumentException
			("At least two knot points required");
		if (n == 1) { // Special case: Bezier curve should be a straight line.
			firstControlPoints = new Point[1];
			// 3P1 = 2P0 + P3
			firstControlPoints[0].x = (2 * knots[0].x + knots[1].x) / 3;
			firstControlPoints[0].y = (2 * knots[0].y + knots[1].y) / 3;

			secondControlPoints = new Point[1];
			// P2 = 2P1 – P0
			secondControlPoints[0].x = 2 *
					firstControlPoints[0].x - knots[0].x;
			secondControlPoints[0].y = 2 *
					firstControlPoints[0].y - knots[0].y;
			return;
		}

		// Calculate first Bezier control points
		// Right hand side vector
		double[] rhs = new double[n];

		// Set right hand side x values
		for (int i = 1; i < n - 1; ++i)
			rhs[i] = 4 * knots[i].x + 2 * knots[i + 1].x;
		rhs[0] = knots[0].x + 2 * knots[1].x;
		rhs[n - 1] = (8 * knots[n - 1].x + knots[n].x) / 2.0;
		// Get first control points x-values
		double[] x = getFirstControlPoints(rhs);

		// Set right hand side y values
		for (int i = 1; i < n - 1; ++i)
			rhs[i] = 4 * knots[i].y + 2 * knots[i + 1].y;
		rhs[0] = knots[0].y + 2 * knots[1].y;
		rhs[n - 1] = (8 * knots[n - 1].y + knots[n].y) / 2.0;
		// Get first control points y-values
		double[] y = getFirstControlPoints(rhs);

		// Fill output arrays.
		//firstControlPoints = new Point[n];	// breaks the java
		//secondControlPoints = new Point[n];	// breaks the java
		for (int i = 0; i < n; ++i) {
			// First control point
			firstControlPoints[i] = new Point(x[i], y[i]);
			// Second control point
			if (i < n - 1) {
				secondControlPoints[i] = new Point(2 * knots
						[i + 1].x - x[i + 1], 2 *
						knots[i + 1].y - y[i + 1]);
			} else {
				secondControlPoints[i] = new Point((knots
						[n].x + x[n - 1]) / 2,
						(knots[n].y + y[n - 1]) / 2);
			}
		}
	}

	/**
	 * Solves a tridiagonal system for one of coordinates (x or y)
	 * of first Bezier control points.
	 * @param rhs Right hand side vector.
	 * @return Solution vector.
	 */
	private static double[] getFirstControlPoints(double[] rhs) {
		int n = rhs.length;
		double[] x = new double[n]; // Solution vector.
		double[] tmp = new double[n]; // Temp workspace.

		double b = 2.0;
		x[0] = rhs[0] / b;
		for (int i = 1; i < n; i++) { // Decomposition and forward substitution.
			tmp[i] = 1 / b;
			b = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
			x[i] = (rhs[i] - x[i - 1]) / b;
		}
		for (int i = 1; i < n; i++) {
			x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.
		}

		return x;
	}
}