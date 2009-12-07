package world.impl;

import java.awt.Color;
import java.util.Collection;

import world.Agent;

public class Grass extends Agent {

	public static final int GROW_AMOUNT = 1;
	public static final int MIN_EAT_HEIGHT = 20;

	public int height = 50;

	@Override
	public void go() {
		height += GROW_AMOUNT;
		System.out.println("Grass at " + cell.x + ", " + cell.y + " grew to " + height);

		if (height >= MIN_EAT_HEIGHT) {
			Collection<? extends Agent> agents = look(0, 0);
			for (Agent a : agents) {
				if (a instanceof EatingRabbit) {
					height -= EatingRabbit.EAT_AMOUNT;
					System.out.println("Grass at " + cell.x + ", " + cell.y
							+ " eaten by rabbit; height is now " + height);
				}
			}
		}

		if (height <= 0) {
			System.out.println("Grass at " + cell.x + ", " + cell.y + " dying due to overeating");

			die();
		}
	}

	public Color getColor(){
		return Color.GREEN;
	}

}
