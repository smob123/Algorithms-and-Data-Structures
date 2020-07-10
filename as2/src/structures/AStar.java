package structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import template_code.Graph;
import template_code.Node;
import template_code.Road;
import template_code.Segment;

public class AStar {

	// path restrictions that the algorithm should avoid
	public enum RESTRICTIONS {
		NO_CAR, NO_PEDESTRIAN, NO_BICYCLE
	}

	// the heuristic function that will be used to calculate the total travel cost a
	// node
	public enum HEURESTIC_FUNCTION {
		DISTANCE, TIME
	}

	/**
	 * returns the shortest path between two nodes using the A* algorithm
	 */
	public ArrayList<Node> search(Graph graph, Node start, Node end, HEURESTIC_FUNCTION filter,
			List<RESTRICTIONS> restrictionsList) {
		PriorityQueue<Tuple> fringe = new PriorityQueue<>();
		HashSet<Tuple> visited = new HashSet<>();

		// add the start node to the fringe
		double h = distance(start, end);
		Tuple origin = new Tuple(start, null, 0, h);
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
				ArrayList<Node> path = new ArrayList<>();
				while (t != null) {
					path.add(0, t.n);
					t = t.prev;
				}

				return path;
			}

			ArrayList<Tuple> neighbors = currenTuple.gerNeighbors();
			// look for the neighbour with minimum total cost
			for (Tuple neighbor : neighbors) {
				double totalCost;

				// calculate the total cost of the neighbour based on the heuristic function
				if (filter == HEURESTIC_FUNCTION.TIME) {
					Road road = getSegment(graph, currenTuple.n, neighbor.n).road;
					// calculate the total cost by dividing the neighbour's g value by the 
					// sum of the maximum speed in the graph and the road class to force
					// the algorithm to choose roads with higher speed
					totalCost = (neighbor.g + distance(neighbor.n, end)) / (110.0 + road.roadClass);
				} else {
					totalCost = neighbor.g + distance(neighbor.n, end);
				}

				// check if neighbor visited
				if (contains(visited, neighbor)) {
					// check if the cost to get to the neighbour from the current path is larger
					// than its attached cost in the fringe
					if (getTotalCost(visited, neighbor) < totalCost) {
						continue;
					}
				}

				// check if there is a directed edge from this node to the current neighbour
				if (!canGoToNode(graph, currenTuple.n, neighbor.n)) {
					continue;
				}

				// check if there are any restrictions that prevent us from visiting this
				// neighbour
				if (restrictionsList.size() > 0) {
					if (!canGoToNode(graph, currenTuple.n, neighbor.n, restrictionsList)) {
						continue;
					}
				}

				// update the neighbour's total cost, and add it to the fringe
				neighbor.totalCost = totalCost;
				fringe.add(neighbor);
			}
		}

		return new ArrayList<Node>();
	}

	/**
	 * returns the segment that connects two nodes.
	 */
	private Segment getSegment(Graph graph, Node start, Node end) {
		Collection<Segment> segments = graph.getSegments();

		for (Segment segment : segments) {
			if ((segment.start == start && segment.end == end) || (segment.start == end && segment.end == start)) {
				return segment;
			}
		}

		return null;
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
	 * returns whether there is a restriction that prevents one node from going to
	 * another one
	 */
	private boolean canGoToNode(Graph graph, Node start, Node end, List<RESTRICTIONS> restrictionsList) {
		Collection<Segment> segments = graph.getSegments();

		for (Segment segment : segments) {
			if ((segment.start == start && segment.end == end) || (segment.start == end && segment.end == start)) {
				if (restrictionsList.contains(RESTRICTIONS.NO_CAR)) {
					return !segment.road.notForCar;
				}

				if (restrictionsList.contains(RESTRICTIONS.NO_PEDESTRIAN)) {
					return !segment.road.notForPede;
				}

				if (restrictionsList.contains(RESTRICTIONS.NO_BICYCLE)) {
					return !segment.road.notForBicy;
				}

				return true;
			}
		}

		return true;
	}

	/**
	 * returns the total cost of a given tuple.
	 */
	private double getTotalCost(HashSet<Tuple> set, Tuple element) {
		Iterator<Tuple> it = set.iterator();

		while (it.hasNext()) {
			Tuple current = it.next();

			if (current.n == element.n) {
				return current.totalCost;
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
	 * returns the euclidean distance between two nodes
	 */
	private double distance(Node origin, Node dest) {
		return origin.location.distance(dest.location);
	}

	/**
	 * contains information about a given node in a path, including: 
	 * - the node itself
	 * - the previous node in the path
	 * - the g value from the start node to the given node
	 * - the total cost; ie, the f value of the node
	 */
	private class Tuple implements Comparable<Tuple> {
		Node n;
		Tuple prev;
		double g;
		double totalCost;

		public Tuple(Node n, Tuple prev, double g, double h) {
			this.n = n;
			this.prev = prev;
			this.g = g;
			this.totalCost = h;
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
				Tuple neighborTuple = new Tuple(n, this, this.g + segment.length, Double.POSITIVE_INFINITY);
				neighbors.add(neighborTuple);
			}

			return neighbors;
		}

		// used for the priority queue.
		@Override
		public int compareTo(Tuple o) {
			if (this.totalCost > o.totalCost) {
				return 1;
			} else if (this.totalCost < o.totalCost) {
				return -1;
			}

			return 0;
		}
	}
}
