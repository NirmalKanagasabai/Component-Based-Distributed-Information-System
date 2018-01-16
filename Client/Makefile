policy:
	@echo "grant codeBase \"file:$(CURDIR)\" {" > java.policy
	@echo "    permission java.security.AllPermission;" >> java.policy
	@echo "};" >> java.policy

compileclient: policy
	javac -cp .:ResInterface.jar client.java

runclient: compileclient
	java -Djava.security.policy=./java.policy -Djava.rmi.server.codebase=file:$(CURDIR) -cp .:ResInterface.jar client localhost

clean:
	rm -f client.class
