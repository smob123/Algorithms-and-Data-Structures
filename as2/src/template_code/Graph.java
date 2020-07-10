package template_code;

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

import structures.AStar;
import structures.Dijkstra;

/**
 * This represents the data structure storing all the roads, nodes, and
 * segments, as well as some information on which nodes and segments should be
 * highlighted.
 * 
 * @author tony
 */
public class Graph {
	// map node IDs to Nodes.
	Map<Integer, Node> nodes = new HashMap<>();
	// map road IDs to Roads.
	Map<Integer, Road> roads;
	// just some collection of Segments.
	Collection<Segment> segments;

	ArrayList<Segment> highlightedSegments = new ArrayList<>();

	Node originNode;
	Node destinationNode;
	Collection<Road> highlightedRoads = new HashSet<>();

	Dijkstra dijkstra = new Dijkstra();
	AStar aStar = new AStar();
	AStar.HEURESTIC_FUNCTION filter = AStar.HEURESTIC_FUNCTION.DISTANCE;
	ArrayList<AStar.RESTRICTIONS> restrictions = new ArrayList<>();

	public Graph(File nodes, File roads, File segments, File polygons) {
		this.nodes = Parser.parseNodes(nodes, this);
		this.roads = Parser.parseRoads(roads, this);
		this.segments = Parser.parseSegments(segments, this);
	}

	public void draw(Graphics g, Dimension screen, Location origin, double scale) {
		// a compatibility wart on swing is that it has to give out Graphics
		// objects, but Graphics2D objects are nicer to work with. Luckily
		// they're a subclass, and swing always gives them out anyway, so we can
		// just do this.
		Graphics2D g2 = (Graphics2D) g;

		// draw all the segments.
		for (Segment s : segments) {
			if (s.road.oneway) {
				g2.setColor(Color.RED);
			} else if (s.road.notForCar) {
				g2.setColor(new Color(214, 136, 0));
			} else if (s.road.notForPede) {
				g2.setColor(Color.PINK);
			} else if (s.road.notForBicy) {
				g2.setColor(Color.GREEN);
			} else {
				g2.setColor(Mapper.SEGMENT_COLOUR);
			}
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

		for (Segment segment : highlightedSegments) {
			segment.draw(g2, origin, scale);
		}

		// draw all the nodes.
		g2.setColor(Mapper.NODE_COLOUR);
		for (Node n : nodes.values())
			n.draw(g2, screen, origin, scale);

		// draw the highlighted node, if it exists.
		if (originNode != null) {
			g2.setColor(Color.GREEN);
			originNode.draw(g2, screen, origin, scale);
		}
		if (destinationNode != null) {
			g2.setColor(Color.RED);
			destinationNode.draw(g2, screen, origin, scale);
		}
	}

	public void setOrigin(Node node) {
		clearHighlighted();
		this.originNode = node;
		setHighlight(new ArrayList<>());
	}

	public void setDestination(Node node) {
		if (originNode == null) {
			return;
		}

		destinationNode = node;
		search();
	}

	private void search() {
		ArrayList<Node> nodes = aStar.search(this, originNode, destinationNode, filter, restrictions);
		// ArrayList<Node> nodes = dijkstra.search(this, originNode, destinationNode);

		highlightedSegments.clear();

		for (int i = 0; i < nodes.size() - 1; i++) {
			highlightSegment(nodes.get(i), nodes.get(i + 1));
		}
	}

	private void highlightSegment(Node start, Node end) {
		for (Segment segment : segments) {
			if ((segment.start == start && segment.end == end) || (segment.start == end && segment.end == start)) {
				highlightedSegments.add(segment);
				return;
			}
		}
	}

	public void setHighlight(Collection<Road> roads) {
		this.highlightedRoads = roads;
	}

	public void setFilter(AStar.HEURESTIC_FUNCTION f) {
		filter = f;
	}

	public void addRestriction(AStar.RESTRICTIONS restriction) {
		restrictions.add(restriction);
	}

	public void removeRestriction(AStar.RESTRICTIONS restriction) {
		restrictions.remove(restrictions.indexOf(restriction));
	}

	private void clearHighlighted() {
		originNode = null;
		destinationNode = null;
		highlightedSegments.clear();
	}

	public Collection<Segment> getSegments() {
		return segments;
	}
}

// code for COMP261 assignments