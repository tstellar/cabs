package world.impl;

import java.util.Collection;
import java.util.Random;

import world.Agent;

public class EatingRabbit extends Agent {

	public static final int EAT_AMOUNT = 5;
	public static final int METABOLISM_PER_TURN = 2;
	public static final int REPRODUCE_ENERGY = 100;
	public static final int DEATH_ENERGY = 10;

	private Random random = new Random();

	public int health = 50;

	@Override
	public void go() {
		System.out.println("Rabbit at " + cell.x + ", " + cell.y + " has energy " + health);

		if (health <= DEATH_ENERGY) {
			System.out.println("Rabbit at " + cell.x + ", " + cell.y + " is dying");
			this.die();
			return;
		}
		Collection<? extends Agent> agents = look(0, 0);
		for (Agent a : agents) {
			if (a instanceof Grass) {
				Grass g = (Grass) a;
				System.out.println("Rabbit at " + cell.x + ", " + cell.y + " sees grass of height "
						+ g.height);
				if (g.height > Grass.MIN_EAT_HEIGHT) {
					System.out.println("Rabbit at " + cell.x + ", " + cell.y + " eats grass");
					health += EAT_AMOUNT;
				}
			}
		}
		if (health >= REPRODUCE_ENERGY) {
			this.cell.add(new EatingRabbit());
			System.out.println("Rabbit at " + cell.x + ", " + cell.y + " reproduced");
		}

		int x = random.nextInt(2);
		int y = random.nextInt(2);

		System.out.println("Rabbit at " + cell.x + ", " + cell.y + " moving by " + x + ", " + y);

		move(x, y);
	}
}
