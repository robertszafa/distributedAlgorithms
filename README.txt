All code is in the /src directory

1. Compile
javac -cp JavaPlot.jar *.java

2. Run 
java -cp JavaPlot.jar:. Simulation n a -f
n - integer greater than 1
a - integer greater than 0
f - type of id assignment. 'r' for random, 'a' for clockwise ascending, 'd' for counter clockwise ascending.
Program reads only one letter for f. 


Example
java -cp JavaPlot.jar:. Simulation 1000 3 -a
Will run the simulation for a ring with 1000 processors with ids in clockwise order ranging from 1 to 1000 * 3.
After finishing, 2 plots will be displayed (time & communication complexity).
