package template;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import structures.ArticulationPoint;
import structures.MinimumSpanningTree;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	public Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	public Collection<Segment> segments;

	private final ArticulationPoint AP;
	private ArrayList<Node> APs = new ArrayList<>();
	private final MinimumSpanningTree tree;
	private ArrayList<Segment> highlightedSegments = new ArrayList<>();

	Node highlightedNode;
	Collection<Road> highlightedRoads = new HashSet<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
		AP = new ArticulationPoint();
		tree = new MinimumSpanningTree();
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		for (Segment s : segments) {
			g2.setStroke(new BasicStroke(1));
			g.setColor(Mapper.SEGMENT_COLOUR);
			s.draw(g2, origin, scale);
		}

		// draw the segments of all highlighted roads.
		g2.setColor(Mapper.HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		for (Road road : highlightedRoads) {
			for (Segment seg : road.components) {
				seg.draw(g2, origin, scale);
			}
		}

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted node, if it exists.
		if (highlightedNode != null) {
			g2.setColor(Mapper.HIGHLIGHT_COLOUR);
			highlightedNode.draw(g2, screen, origin, scale);
		}

		g2.setStroke(new BasicStroke(2));

		// highlight MST segments
		for (Segment segment : highlightedSegments) {
			g.setColor(Mapper.HIGHLIGHT_COLOUR);
			segment.draw(g2, origin, scale);
		}

		// highlight articulation points
		g.setColor(Color.GREEN);
		for (Node node : APs) {
			node.draw(g, screen, origin, scale);
		}
	}

	public void setHighlight(Node node) {
		this.highlightedNode = node;
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	public void highlightAPs() {
		reset();
		APs = AP.getPoints(new ArrayList<>(nodes.values()));
	}

	private void reset() {
		AP.reset();
		APs.clear();
	}

	public ArrayList<Node> getAPs() {
		return APs;
	}

	public void highlightMinimumSpanningTree() {
		highlightedSegments = tree.kruskalAlgorithm(this);
	}

	public ArrayList<Segment> getMinimumSpanningTree() {
		return highlightedSegments;
	}

	public ArrayList<Integer> getMSTDepths() {
		return tree.getDepths();
	}
	
	public ArrayList<Integer> getAPSizes() {
		return AP.getGraphSizes();
	}
}

// code for COMP261 assignments