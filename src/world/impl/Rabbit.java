package world.impl;

import java.util.Collection;

import world.Agent;

public class Rabbit extends Agent {
	// Random r = new Random();

	@Override
	public void go() {

		// move(r.nextInt(5),r.nextInt(5));
		Collection<? extends Agent> agents = look(0, 1);
		System.out.println(">>>>>>> I can see " + agents.size() + " agents below me");
		move(1, 0);

	}
}
