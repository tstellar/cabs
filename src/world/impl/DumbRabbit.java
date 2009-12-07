package world.impl;

import java.util.Random;
import java.awt.Color;
import world.Agent;

public class DumbRabbit extends Agent {
	Random r = new Random();
	final private int repoAge = 5;
	public int age = 0;
	
	@Override
	public void go() {
		age++;
		move(r.nextInt(2), r.nextInt(2));
		
		if (age == 5) {
			reproduce();
		}
	}

	public Color getColor(){
		return Color.RED;
	}
}	
