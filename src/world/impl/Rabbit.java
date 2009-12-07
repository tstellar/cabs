package world.impl;

import java.awt.Color;

import world.Agent;

public class Rabbit extends Agent {
	// Random r = new Random();

	@Override
	public void go() {

		// move(r.nextInt(5),r.nextInt(5));
		move(1, 0);

	}
	public Color getColor(){
		return Color.RED;
	}
}
