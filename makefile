AP=src/world/impl/


.PHONY:
	cabs
	test
	agents

cabs: $(AP)/DumbRabbit.class $(AP)/DumbWolf.class $(AP)/Grass.class $(AP)/EatingRabbit.class
	javac -cp src/ src/engine/LocalEngine.java


%.class: %.java
	javac -cp src/ $^
	

test:
	javac -cp src/ src/test/AgentWriteTest.java
