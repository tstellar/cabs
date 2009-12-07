package world.impl;

import java.util.Random;

import world.Agent;

public class DumbWolf extends Agent {
	Random r = new Random();
	final private int rabbitEnergy = 20;
	final private int turnEnergy = 5;
	final private int repoEnergy = 75;
	public int energy = 50;
	
	@Override
	public void go() {
		if (energy >= repoEnergy) {
			reproduce();
		}
		else{
			Agent a = look(0,0,"Rabbit");
			if(a!=null){
				a.die();
				this.energy += rabbitEnergy;
			}
			else{
				move(r.nextInt(2), r.nextInt(2));
			}
		}
		
	}
}	
