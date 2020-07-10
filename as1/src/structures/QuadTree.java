package structures;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

public class QuadTree {

	// the tree's children
	private QuadTree ne, nw, se, sw;
	// the tree's position
	private Point position;
	// the tree's size
	private Dimension size;
	// the node that's associated with the tree
	private Node node = null;
	// checks if the tree has been divided or not
	private boolean isDivided = false;

	public QuadTree(Point position, Dimension size) {
		this.position = position;
		this.size = size;
	}

	public void draw(Graphics g) {
		drawNodes(g);
	}

	private void drawNodes(Graphics g) {
		// check if the node has been initialized
		if (node == null) {
			return;
		}
		// draw the tree
		g.drawRect(position.x, position.y, size.width, size.height);

		// draw its descendants
		if (isDivided) {
			ne.drawNodes(g);
			nw.drawNodes(g);
			se.drawNodes(g);
			sw.drawNodes(g);
		}
	}

	public void insert(Stop stop) {
		// check if the stop is within this tree's boundaries
		if (!isWithin(stop.getPosition())) {
			return;
		}

		// check if node isn't null
		if (node == null) {
			node = new Node(stop);
			return;
		}

		// otherwise divide the tree
		if (!isDivided) {
			divide();
			isDivided = true;
		}

		// try to insert the stop into one of the tree's descendants
		nw.insert(stop);
		ne.insert(stop);
		sw.insert(stop);
		se.insert(stop);
	}

	/**
	 * gets a stop at a given location.
	 * 
	 * @param p the location to look for a stop at
	 * @return a stop at a given location.
	 */
	public Stop getStopAt(Point p) {
		// check if node isn't null
		if (node == null) {
			return null;
		}

		// check if the point is within the tree
		if (!isWithin(p)) {
			return null;
		}

		// check if the point is within a stop
		if (node.stop.within(p)) {
			return node.stop;
		}

		// try to find the stop in one of the tree's descendants
		if (isDivided) {
			nw.getStopAt(p);
			ne.getStopAt(p);
			sw.getStopAt(p);
			se.getStopAt(p);
		}

		return null;
	}

	/**
	 * checks whether a stop is within the tree's boundaries or not.
	 * 
	 * @param stop the stop to check
	 * @return whether a stop is within the tree's boundaries or not
	 */
	private boolean isWithin(Point stopPosition) {
		Stop s = new Stop();
		int stopSize = s.getSize();
		boolean withinX = stopPosition.x >= position.x && (stopPosition.x + stopSize) <= (position.x + size.width);
		boolean withinY = stopPosition.y >= position.y && (stopPosition.y + stopSize) <= (position.y + size.height);

		return withinX && withinY;
	}

	/**
	 * divides the tree into smaller subtrees.
	 */
	private void divide() {
		// the size of each subtree
		Dimension childSize = new Dimension(size.width / 2, size.height / 2);

		// initialize the tree's children

		Point nwPosition = new Point(position.x, position.y);
		nw = new QuadTree(nwPosition, childSize);

		Point nePosition = new Point(position.x + (size.width / 2), position.y);
		ne = new QuadTree(nePosition, childSize);

		Point sePosition = new Point(position.x, position.y + (size.height / 2));
		se = new QuadTree(sePosition, childSize);

		Point swPosition = new Point(position.x + (size.width / 2), position.y + (size.height / 2));
		sw = new QuadTree(swPosition, childSize);
	}

	private class Node {
		Point position;
		Stop stop;

		public Node(Stop stop) {
			this.stop = stop;
			this.position = stop.getPosition();
		}
	}
}
