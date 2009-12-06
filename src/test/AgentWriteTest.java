package test;

import java.io.*;
import world.impl.ComplexRabbit;
import world.Agent;

public class AgentWriteTest{
	
	public static void main(String[] args){
	try{		
		ComplexRabbit rabbit = new ComplexRabbit();
		byte[] bytes = rabbit.toBytes();
		System.out.write(bytes);
		System.out.println();
		ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
		DataInputStream di = new DataInputStream(bi);
		ComplexRabbit sameRabbit = (ComplexRabbit)Agent.read(di);
		System.out.print("Energy = " + sameRabbit.energy + " Age= " + sameRabbit.age);
	}catch(Exception e){
		e.printStackTrace();
	}
	}
}
