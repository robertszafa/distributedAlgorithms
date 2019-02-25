All code is in the /src directory

### Compile
javac -cp JavaPlot.jar *.java

### Run 
java -cp JavaPlot.jar:. Simulation n a -f
n - integer greater than 1
a - integer at least or greater than 1
f - flag for id assignment. 'r' for random, 'a' for clockwise ascending, 'd' for counterclockwise ascending


#### Example
java -cp JavaPlot.jar:. Simulation 1000 3 -a
Will run the simulation for a ring with 1000 processors with ids in clockwise order ranging from 1 to 1000 * 3
