package com.virtuallypreinstalled.hene.rocket.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.vaadin.gwtgraphics.client.VectorObjectContainer;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Path;

public abstract class AbstractRocket {

	/**
	 * This class represents a coordinate point (x, y).
	 * 
	 */
	public class Point {

		public int x;

		public int y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	/**
	 * This class represents a thruster.
	 */
	public class Thruster {

		public int x;

		public int y;

		public int force;

		public boolean act;

		public int a;

		public Thruster(int x, int y, int force, boolean act, int a) {
			this.x = x;
			this.y = y;
			this.force = force;
			this.act = act;
			this.a = a;
		}
	}

	private double x;

	private double xV;

	private double y;

	private double yV;

	private double a;

	private double aV;

	protected double damping;

	protected double gravity;

	protected double cmx;

	protected double cmy;

	protected double mass;

	protected double invMass;

	protected double invMoment;

	private long lastUpdate;

	protected Point[] points;

	protected List<Thruster> thrusters = new ArrayList<Thruster>();

	public AbstractRocket() {
		lastUpdate = new Date().getTime();
	}

	public abstract void reset();

	protected void reset(int x, int xv, int y, int yv, int a, int av) {
		this.x = x;
		this.xV = xv;
		this.y = y;
		this.yV = yv;
		this.a = a;
		this.aV = av;
	}

	public void setThrusterOn(int i, boolean on) {
		thrusters.get(i).act = on;
	}

	/**
	 * Calculate how much the variables of this object change in the specified
	 * duration using the Runge-Kutta numerical evaluation method.
	 */
	public void update() {
		long time = new Date().getTime();
		double dur = Math.min(0.04, (time - lastUpdate) / 1000.0);
		lastUpdate = time;

		double[] comps = new double[] { x, xV, y, yV, a, aV };
		double[] initial = new double[] { 0, 0, 0, 0, 0, 0 };

		double[] k1 = evaluate(comps, true);
		for (int i = 0; i < k1.length; i++) {
			initial[i] = comps[i] + (k1[i] * dur) / 2.0;
		}

		double[] k2 = evaluate(initial, false);
		for (int i = 0; i < k2.length; i++) {
			initial[i] = comps[i] + (k2[i] * dur) / 2.0F;
		}

		double[] k3 = evaluate(initial, false);
		for (int i = 0; i < k3.length; i++) {
			initial[i] = comps[i] + k3[i] * dur;

		}

		double[] k4 = evaluate(initial, false);

		for (int i = 0; i < comps.length; i++) {
			comps[i] += ((k1[i] + 2.0 * k2[i] + 2.0 * k3[i] + k4[i]) * dur) / 6.0;
		}

		x = comps[0];
		xV = comps[1];
		y = comps[2];
		yV = comps[3];
		a = comps[4] % (Math.PI * 2);
		aV = comps[5];
	}

	/**
	 * Evaluate the coordinate components after one step.
	 */
	private double[] evaluate(double[] values, boolean doPrint) {
		double x = values[0];
		double xv = values[1];
		double y = values[2];
		double yv = values[3];
		double a = values[4];
		double av = values[5];

		// Return the changes as a vector that contains x pos, x veloity, y pos,
		// y velocity, angle, angular velocity (in that order)
		double[] deltas = new double[] { 0, 0, 0, 0, 0, 0 };

		// X position changes by y velocity
		deltas[0] = xv;

		// Calculate the effects of the thrusters
		ArrayList<double[]> thrusters = new ArrayList<double[]>();
		for (Thruster thruster : this.thrusters) {
			thrusters.add(calcThruster(x, y, a, thruster));
		}

		// X velocity changes by damping, mass, previous velocity and forces
		// affecting the object
		double xvel = -damping * xv * invMass;
		for (double[] d : thrusters) {
			xvel += d[4] * invMass;
		}

		deltas[1] = xvel;

		// Y position changes by y velocity
		deltas[2] = yv;

		// y velocity changes by damping, mass, gravity, previous velocity and
		// thrusters
		double yvel = (-damping * yv) * mass;
		if (invMass != 0.0) {
			yvel -= gravity;
		}

		for (double[] d : thrusters) {
			yvel += d[5] * invMass;
		}
		deltas[3] = yvel;

		deltas[4] = av;

		// Angular velocity changes by damping, mass, and thrusters
		double avel = -damping * av;
		for (double[] d : thrusters) {
			avel += (d[0] * d[5] - d[1] * d[4]) * invMoment;
		}
		deltas[5] = avel;

		return deltas;
	}

	/**
	 * Calculate the effects of a trust vector.
	 */
	private double[] calcThruster(double x, double y, double a,
			Thruster thruster) {
		if (!thruster.act) {
			return new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		}

		double sinAngle = Math.sin(a);
		double cosAngle = Math.cos(a);

		double ex = x + sinAngle * cmy;
		double ey = y - cosAngle * cmy;

		double ax = ex - cosAngle * cmx;
		double ay = ey - sinAngle * cmx;

		double tx = (ax + cosAngle * thruster.x) - sinAngle * thruster.y;
		double ty = ay + sinAngle * thruster.x + cosAngle * thruster.y;

		double tx2 = tx - Math.sin(a + thruster.a) * thruster.force;
		double ty2 = ty + Math.cos(a + thruster.a) * thruster.force;

		double rx = tx - x;
		double ry = ty - y;

		double rlen = Math.sqrt(rx * rx + ry * ry);

		// Return the effects of the force to the craft as x/y components
		double[] result = new double[] { 0, 0, 0, 0, 0, 0 };
		result[0] = rx;
		result[1] = ry;
		result[2] = rx / rlen;
		result[3] = ry / rlen;
		result[4] = tx2 - tx;
		result[5] = ty2 - ty;

		return result;
	}

	public void draw(VectorObjectContainer screen) {
		int xRel = 0;
		int yRel = 0;

		double sin = Math.sin(a);
		double cos = Math.cos(a);

		Path path = null;
		for (Point p : points) {
			int x = p.x;
			int y = p.y;
			double newx = x * cos - y * sin - xRel * cos + yRel * sin + xRel
					+ this.x;
			double newy = World.SCREEN_SIZE
					- (x * sin + y * cos - xRel * sin - yRel * cos + yRel + this.y);
			if (path == null) {
				path = new Path((int) newx, (int) newy);
			} else {
				path.lineTo((int) newx, (int) newy);
			}
		}
		path.setStrokeColor("white");
		path.setFillOpacity(0);
		path.close();
		screen.add(path);

		for (Thruster thruster : this.thrusters) {
			if (!thruster.act) {
				continue;
			}
			int x = thruster.x;
			int y = thruster.y;

			double newx = x * cos - y * sin - xRel * cos + yRel * sin + xRel
					+ this.x;
			double newy = World.SCREEN_SIZE
					- (x * sin + y * cos - xRel * sin - yRel * cos + yRel + this.y);

			Circle c = new Circle((int) newx, (int) newy, thruster.force / 23);
			c.setStrokeColor("yellow");
			c.setFillColor("yellow");
			screen.add(c);
		}
	}

}