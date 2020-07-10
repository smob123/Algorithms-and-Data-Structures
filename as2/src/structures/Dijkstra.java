package structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import template_code.Graph;
import template_code.Node;
import template_code.Segment;

public class Dijkstra {

	/**
	 * returns the shortest path between two nodes using Dijkstra's algorithm
	 */
	public ArrayList<Node> search(Graph graph, Node start, Node end) {
		PriorityQueue<Tuple> fringe = new PriorityQueue<>();
		HashSet<Tuple> visited = new HashSet<>();
		ArrayList<Node> path = new ArrayList<>();

		// add the start node to the fringe
		Tuple origin = new Tuple(start, null, 0);
		fringe.add(origin);

		while (!fringe.isEmpty()) {
			// remove the top node from the fringe
			Tuple currenTuple = fringe.poll();
			// mark it as visited
			visited.add(currenTuple);

			// check if the destination is reached
			if (currenTuple.n == end) {
				Tuple t = currenTuple;

				// add all the nodes in the path to a list, and return it
				while (t != null) {
					path.add(0, t.n);
					t = t.prev;
				}

				return path;
			}

			ArrayList<Tuple> neighbors = currenTuple.gerNeighbors();
			// look for the neighbour with minimum total cost
			for (Tuple neighbor : neighbors) {
				// check if neighbor visited
				if (contains(visited, neighbor)) {
					// check if the cost to get to the neighbour from the current path is larger
					// than its attached cost in the fringe
					if (geTotalCost(visited, neighbor) < neighbor.cost) {
						continue;
					}
				}

				// check if there is a directed edge from this node to the current neighbour
				if (!canGoToNode(graph, currenTuple.n, neighbor.n)) {
					continue;
				}

				// add the neighbour to the fringe
				fringe.add(neighbor);
			}
		}

		return new ArrayList<Node>();
	}

	/**
	 * returns whether there is a directed edge from one node to the another one, or
	 * not
	 */
	private boolean canGoToNode(Graph graph, Node start, Node end) {
		Collection<Segment> segments = graph.getSegments();

		for (Segment segment : segments) {
			if ((segment.start == start && segment.end == end) || (segment.start == end && segment.end == start)) {
				if (segment.road.oneway) {
					if (segment.start != start) {
						return false;
					}
				}

				return true;
			}
		}

		return true;
	}

	/**
	 * returns the total cost of a given tuple.
	 */
	private double geTotalCost(HashSet<Tuple> set, Tuple element) {
		Iterator<Tuple> it = set.iterator();

		while (it.hasNext()) {
			Tuple current = it.next();

			if (current.n == element.n) {
				return current.cost;
			}
		}

		return Double.POSITIVE_INFINITY;
	}

	/**
	 * checks whether a set contains a tuple with a given node.
	 */
	private boolean contains(HashSet<Tuple> set, Tuple element) {
		Iterator<Tuple> it = set.iterator();

		while (it.hasNext()) {
			Tuple current = it.next();

			if (current.n == element.n) {
				return true;
			}
		}

		return false;
	}

	/**
	 * contains information about a given node in a path, including: 
	 * - the node itself 
	 * - the previous node in the path 
	 * - the g value from the start node to the given node
	 */
	private class Tuple implements Comparable<Tuple> {
		Node n;
		Tuple prev;
		double cost;

		public Tuple(Node n, Tuple prev, double cost) {
			this.n = n;
			this.prev = prev;
			this.cost = cost;
		}

		/**
		 * returns the current node's neighbours as Tuple objects with a total cost of
		 * positive infinity.
		 */
		ArrayList<Tuple> gerNeighbors() {
			ArrayList<Segment> segments = new ArrayList<>(n.segments);
			ArrayList<Tuple> neighbors = new ArrayList<>();

			for (Segment segment : segments) {
				Node n = segment.start == this.n ? segment.end : segment.start;
				Tuple neighborTuple = new Tuple(n, this, this.cost + segment.length);
				neighbors.add(neighborTuple);
			}

			return neighbors;
		}

		// used for the priority queue.
		@Override
		public int compareTo(Tuple o) {
			if (this.cost > o.cost) {
				return 1;
			} else if (this.cost < o.cost) {
				return -1;
			}

			return 0;
		}
	}
}
