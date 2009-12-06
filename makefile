
.PHONY:
	cabs
	test

cabs:
	javac -cp src/ src/engine/LocalEngine.java

test:
	javac -cp src/ src/test/AgentWriteTest.java
