package world.impl;

import java.util.Collection;

import world.Agent;

public class EatingRabbit extends Agent {

	public static final int EAT_AMOUNT = 5;
	public static final int METABOLISM_PER_TURN = 2;
	public static final int REPRODUCE_ENERGY = 100;

	public int health = 50;

	@Override
	public void go() {
		Collection<? extends Agent> agents = look(0, 0);
		for (Agent a : agents) {
			if (a instanceof Grass) {
				Grass g = (Grass) a;
				if (g.height > Grass.MIN_EAT_HEIGHT) {
					health += EAT_AMOUNT;
				}
			}
		}
		if (health >= REPRODUCE_ENERGY) {
			this.cell.add(new EatingRabbit());
		}
	}
}
