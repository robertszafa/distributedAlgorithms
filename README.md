1. How to run
Compile from the /src directory: javac -cp JavaPlot.jar *.java
Then run: 
java -cp JavaPlot.jar:. Simulation (number of processors) (alpha, multiplyier for id assigmnet) (type of id assignment)
number of processors - integer greater than 1
alpha - integer at least or greater than 1
type of id assignment - 'r' for random, 'a' for clockwise ascending, 'd' for counterclockwise ascending

Example: java -cp JavaPlot.jar:. Simulation 1000 3 -a
Will run the simulation for a ring with 1000 processors with ids in clockwise order ranging from 1 to 1000 * 3