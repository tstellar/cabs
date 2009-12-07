
.PHONY:
	cabs
	test
	agents

cabs: src/world/impl/DumbRabbit.class src/world/impl/DumbWolf.class
	javac -cp src/ src/engine/LocalEngine.java


%.class: %.java
	javac -cp src/ $^
	

test:
	javac -cp src/ src/test/AgentWriteTest.java
