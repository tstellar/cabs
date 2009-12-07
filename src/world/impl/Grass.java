package world.impl;

import java.util.Collection;

import world.Agent;

public class Grass extends Agent {

	public static final int GROW_AMOUNT = 1;
	public static final int MIN_EAT_HEIGHT = 20;

	public int height = 100;

	@Override
	public void go() {
		height += GROW_AMOUNT;
		if (height >= MIN_EAT_HEIGHT) {
			Collection<? extends Agent> agents = look(0, 0);
			for (Agent a : agents) {
				if (a instanceof EatingRabbit) {
					height -= EatingRabbit.EAT_AMOUNT;
				}
			}
		}
	}

}
