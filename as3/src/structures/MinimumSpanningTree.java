package structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import template.Graph;
import template.Node;
import template.Segment;

public class MinimumSpanningTree {
	// list of root nodes
	HashSet<Point> forest = new HashSet<>();
	// list of all nodes in the graph
	HashMap<Integer, Point> allNodes = new HashMap<>();

	public ArrayList<Segment> kruskalAlgorithm(Graph graph) {
		// initialize edges, and nodes
		PriorityQueue<Edge> fringe = new PriorityQueue<>();
		initForest(graph.nodes.values());
		initEdges(fringe, graph.segments);
		// list of edges that make the MST
		HashSet<Segment> tree = new HashSet<>();

		while (forest.size() > 1 && !fringe.isEmpty()) {
			Edge currentEdge = fringe.poll();

			// get the root of the first, and second nodes in the segment
			Point startNodeRoot = getRoot(currentEdge.start);
			Point endNodeRoot = getRoot(currentEdge.end);

			// check if the nodes are not in the same disjoint set
			if (startNodeRoot != endNodeRoot) {
				// merge them into one set
				mergeSets(currentEdge.start, startNodeRoot, currentEdge.end, endNodeRoot);
				// add the current segment to the tree
				tree.add(currentEdge.seg);
			}
		}

		return new ArrayList<Segment>(tree);
	}

	/**
	 * converts Node objects to Point objects.
	 */
	private void initForest(Collection<Node> nodes) {
		for (Node n : nodes) {
			Point node = new Point(n);
			node.parent = node;
			forest.add(node);
			allNodes.put(n.nodeID, node);
		}
	}

	/**
	 * converts Segment objects to Edge objects.
	 */
	private void initEdges(PriorityQueue<Edge> fringe, Collection<Segment> segments) {
		for (Segment seg : segments) {
			Edge edge = new Edge(seg);
			edge.start = allNodes.get(seg.start.nodeID);
			edge.end = allNodes.get(seg.end.nodeID);
			fringe.add(edge);
		}
	}

	/**
	 * returns a root of a given node.
	 */
	private Point getRoot(Point node) {
		if (node.parent == node) {
			return node;
		}

		return getRoot(node.parent);
	}

	/**
	 * merges two disjoint sets by added the smaller set to the larger one.
	 */
	private void mergeSets(Point node1, Point xRoot, Point node2, Point yRoot) {
		if (xRoot.depth < yRoot.depth) {
			xRoot.parent = yRoot;
			forest.remove(xRoot);
		} else {
			yRoot.parent = xRoot;
			forest.remove(yRoot);
			if (xRoot.depth == yRoot.depth) {
				xRoot.depth++;
			}
		}
	}

	/**
	 * returns the depth of each disjoint set.
	 */
	public ArrayList<Integer> getDepths() {
		ArrayList<Integer> depths = new ArrayList<>();
		Iterator<Point> it = forest.iterator();

		while (it.hasNext()) {
			Point point = it.next();
			depths.add(point.depth);
		}

		return depths;
	}

	private class Edge implements Comparable<Edge> {
		Segment seg;
		Point start;
		Point end;
		double length;

		public Edge(Segment seg) {
			this.seg = seg;
			this.length = seg.length;
		}

		@Override
		public int compareTo(Edge other) {
			if (this.length > other.length) {
				return 1;
			} else if (this.length < other.length) {
				return -1;
			}

			return 0;
		}
	}

	private class Point {
		Node n;
		Point parent;
		int depth = 0;

		public Point(Node n) {
			this.n = n;
		}
	}
}
