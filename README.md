# Running the program

Simply run 

java -jar .\target\railnetcut-jar-with-dependencies.jar dataVertices.txt dataEdges.txt

Add the extra -quiet argument to skip the interactive part.

Have fun.

# Architecture choices

Our program is very simple in term of architecture.

We decided not to set the Edges generic in the AdjacencyNetwork class for the simple reason that maxFlow/minCut is rarely used on any other thing than Integers. But instead of using integers directly as edges, we created a class Edge that has a capacity and a unique id. 

We decided to store the residual graph in the AdjacencyNetwork to keep the whole program simple. 
Thus, it allows us to build it easily while we build the original graph, which saves the complexity of a copy.

Of course, the drawback of this is that we can run the algorithm only once, which is not such a bad thing in the end since the result is always the same for a given set of data.

# Complexity of max-flow

We used Edmond Karp algorithm, which is a special of Ford-Fulkerson algorithm in which the search for an s-t path is made by a breadth-first search.

Edmond Karp's algorithm has a complexity of O(VE^2).
The proof of that is long but luckily the Internet can do it for me : https://brilliant.org/wiki/edmonds-karp-algorithm/

Our particular implementation is a bit less optimal in term of complexity if we consider the initialization of the flow as part of the algorithm. 
Indeed, we reach O(VE^2 + E) since initializing the flow has a complexity of O(E).

# Complexity of min-cut

Here is the algorithm to find the set of edges to cut :

1) In residual graph, find all vertices reachable from the source (here we use BFS)
2) For every of these vertices, find outgoing edges that goes to a non reachable vertex.
3) Return such edges.

BFS has a complexity of O(V+E)
Looping through outgoing edges of a Vertex has O(Ea) complexity, where Ea is the average number of edges per vertex.
So the complexity of this algorithm is O(Ea(V+E))

## Complexity of min-cut

Note: this program is made for directed flow networks. It will work as an undirected flow network only if, in the data files, eache edge has a copy of itself with reverted vertices. However, the GUI will not be adapted for such cases.
