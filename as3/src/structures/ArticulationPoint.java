package structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import template.Node;
import template.Segment;

public class ArticulationPoint {
	// visited nodes
	private HashMap<Integer, Point> visited = new HashMap<>();
	// the number of articulation points in each part of a graph
	private ArrayList<Integer> graphSizes = new ArrayList<>();

	/**
	 * returns a list of articulation points.
	 */
	public ArrayList<Node> getPoints(ArrayList<Node> nodes) {
		HashSet<Point> AP = new HashSet<>();

		while (true) {
			Point rootPoint = null;

			// look for any unvisited nodes
			for (Node node : nodes) {
				if (visited.get(node.nodeID) == null) {
					// if there is a node that's unvisited, set it as the root node
					rootPoint = new Point(node, 0);
					visited.put(node.nodeID, rootPoint);
					break;
				}
			}

			// break the loop if the root node hasn't been initialized
			if (rootPoint == null) {
				break;
			}

			// otherwise look for articulation points that are connected to the root node
			HashSet<Point> currentGraphAPs = findAPs(rootPoint);
			graphSizes.add(currentGraphAPs.size());
			AP.addAll(currentGraphAPs);
		}

		return setsToLists(AP);
	}

	/**
	 * checks for articulation points starting from the root node.
	 */
	private HashSet<Point> findAPs(Point rootPoint) {
		HashSet<Point> AP = new HashSet<>();

		// get the root node's children
		Stack<Point> neighbours = rootPoint.getChildren();
		int numSubTree = 0;

		for (Point neighbour : neighbours) {
			// check if the neighour hasn't been visited before
			if (neighbour.depth == Double.POSITIVE_INFINITY) {
				// set the node's parent, and depth
				neighbour.parent = rootPoint;
				neighbour.depth = 1;
				// look for articulation points that can be reached from the neighbour node
				iterAP(neighbour, AP);
				numSubTree++;
			}

			// add the root node as an articulation point if it has more than one subtree
			if (numSubTree > 1) {
				AP.add(rootPoint);
			}
		}

		visited.put(rootPoint.n.nodeID, rootPoint);

		return AP;
	}

	/**
	 * iterative algorithm to find articulation points.
	 */
	private void iterAP(Point firstPoint, HashSet<Point> AP) {
		Stack<Point> fringe = new Stack<>();
		// add passed point as the first element in the stack
		fringe.push(firstPoint);
		// set the current depth of the DFS
		double depth = firstPoint.depth;

		while (!fringe.isEmpty()) {
			Point currentPoint = fringe.peek();

			// check if the current point hasn't been visited before
			if (currentPoint.depth == Double.POSITIVE_INFINITY) {
				// set its depth, and reach back to the current DFS depth
				currentPoint.depth = depth;
				currentPoint.reachBack = depth;
				// initialize the node's children
				currentPoint.getChildren();
			} else if (!currentPoint.getChildren().isEmpty()) {
				// check if the current node has any unvisited children
				// get the top child, and remove it from the stack
				Point child = currentPoint.children.pop();

				// check if the child has been visited before; ie, we found an alternative path
				if (child.depth < Double.POSITIVE_INFINITY) {
					// update the current node's reach back
					currentPoint.reachBack = Math.min(child.depth, currentPoint.reachBack);
				} else {
					// otherwise increase the depth, and set child's depth to the new depth
					depth++;
					child.depth = depth;
					child.reachBack = depth;
					child.parent = currentPoint;
					fringe.push(child);
				}
			} else {
				// otherwise check if the current node isn't the first node that was passed to
				// the method earlier
				if (currentPoint != firstPoint) {
					// update its parent's reach back
					currentPoint.parent.reachBack = Math.min(currentPoint.parent.reachBack, currentPoint.reachBack);
					// check if the current node's reach back is not smaller its parent's depth
					if (currentPoint.reachBack >= currentPoint.parent.depth) {
						AP.add(currentPoint.parent);
					}
				}

				// remove the current node from the stack
				fringe.pop();
			}

			// add the current node to the visited set to make it easier to retrieve it later
			visited.put(currentPoint.n.nodeID, currentPoint);
		}
	}

	/**
	 * reset the global variables
	 */
	public void reset() {
		visited.clear();
		graphSizes.clear();
	}

	/**
	 * converts a hashset of Point objects to an arraylist of Node objects.
	 */
	private ArrayList<Node> setsToLists(HashSet<Point> set) {
		ArrayList<Node> nodes = new ArrayList<>();

		Iterator<Point> it = set.iterator();

		while (it.hasNext()) {
			Point p = it.next();
			nodes.add(p.n);
		}

		return nodes;
	}

	/**
	 * returns the numbers of articulation points in each part of the graph
	 */
	public ArrayList<Integer> getGraphSizes() {
		return graphSizes;
	}

	private class Point {
		final Node n;
		double depth;
		double reachBack;
		Point parent;
		private Stack<Point> children;

		public Point(Node n, double depth) {
			this.n = n;
			this.depth = depth;
			reachBack = depth;
		}

		/**
		 * lazily initializes the node's children, and returns them.
		 */
		public Stack<Point> getChildren() {
			if (children != null) {
				return children;
			}

			children = new Stack<>();

			for (Segment segment : n.segments) {
				Node neighbourNode;

				if (segment.start != this.n) {
					neighbourNode = segment.start;
				} else {
					neighbourNode = segment.end;
				}

				Point n;

				if (visited.get(neighbourNode.nodeID) != null) {
					n = visited.get(neighbourNode.nodeID);
				} else {
					n = new Point(neighbourNode, Double.POSITIVE_INFINITY);
					visited.put(neighbourNode.nodeID, n);
				}

				if (n == parent) {
					continue;
				}

				children.add(n);
			}

			return children;
		}
	}
}
