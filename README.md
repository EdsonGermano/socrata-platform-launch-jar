# launch-jar #
A compatiblity shim to allow loading Zip64 assembly jars under Java 7.

NOTE: You will want to compile this for Java 7 if you plan to use it
with the Java 7 JRE.

## Compilation ##
`sbt assembly`

## Usage ##
java -jar launch-jar-assembly-1.0.0.jar my-large-jar.jar
