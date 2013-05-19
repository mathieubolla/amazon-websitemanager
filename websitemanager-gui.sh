#/bin/sh
mvn clean compile exec:java -Dexec.mainClass="com.mathieubolla.MassUpload" -Djava.net.preferIPv4Stack=true
