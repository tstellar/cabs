import java.util.Random;


public class ComplexRabbit extends Agent{
	final int deathFromAge = 20;
	Random r = new Random();
	int energy = 75;
	int age = 0;
	
	public void go(){
		
//		move(r.nextInt(5),r.nextInt(5));
		move(1,0);
		energy = energy - 10;
		age++;
		
		if(energy >= 100){
			reproduce();
		}
		
		if(age == 20){
			die();
		}
	}
	
	public void reproduce(){
		energy-=25;
	}
	
	public void die(){
		
	}
}
