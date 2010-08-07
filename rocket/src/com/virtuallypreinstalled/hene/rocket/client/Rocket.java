package com.virtuallypreinstalled.hene.rocket.client;


public class Rocket extends AbstractRocket {

	public Rocket() {

		points = new Point[] { new Point(0, -25), new Point(-25, -40),
				new Point(0, 40), new Point(25, -40) };

		reset();

		// Thrusters. t_f is the force of each thruster, and t_x, t:_y and t_a
		// are thruster position and angle components.
		// T_a tells if the thrusters are currently on Both thrusters always
		// point straight down (in the rocket's frame of reference)
		thrusters.add(new Thruster(-25, -40, 200, false, 0));
		thrusters.add(new Thruster(25, -40, 200, false, 0));

		damping = 0.1;
		gravity = 30;

		// Object's center of mass coordinates
		cmx = 0.0;
		cmy = 0.0;

		mass = 2;
		invMass = 1.0 / mass;

		// Inverse momentum of a rectangular object is 12 / (mass * (width^2 +
		// height^2)). The figure below is an approximate of this for the rocket
		invMoment = 12.0 / (mass * (20 * 20 + 75 * 75));
	}

	@Override
	public void reset() {
		reset(World.SCREEN_SIZE / 2, 0, World.SCREEN_SIZE / 2, 0, 0, 0);
	}

}
