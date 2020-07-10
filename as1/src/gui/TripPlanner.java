package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import structures.Connection;
import structures.Graph;
import structures.QuadTree;
import structures.Stop;

public class TripPlanner extends GUI {

	private Graph graph = new Graph();
	private Location origin = new Location(-20, 20);
	private double scale = 10;
	private QuadTree tree;
	// keeps track of the mouse's position when it's clicked
	private Point mousePressPosition;
	private boolean hasReadFiles = false;

	@Override
	protected void redraw(Graphics g) {

		// verify that there are stops in the graph
		if (!hasReadFiles) {
			return;
		}

		tree = new QuadTree(new Point(0, 0), getDrawingAreaDimension());
		// get the list of stops, and connections from the graph
		HashMap<String, Stop> stops = graph.getStops();
		ArrayList<Connection> connections = graph.getConnections();

		// go through the stops, and draw them
		for (String stopId : stops.keySet()) {
			Stop stop = stops.get(stopId);
			// check if the current stop is highlighted, and change the graphic's color
			// accordingly
			if (stop.isHighlighted()) {
				g.setColor(Color.CYAN);
			} else {
				g.setColor(Color.GRAY);
			}

			stop.draw(g, origin, scale);
			tree.insert(stop);
		}

		/**
		 * uncomment this line to draw the quad-tree
		 */
		// tree.draw(g);

		// draw the connections
		for (Connection connection : connections) {
			Stop origin = connection.getOrigin();
			Stop destination = connection.getDestination();

			// check if the current connection's origin, or destination is highlighted, and
			// change the graphic's color accordingly
			if (origin.isHighlighted() || destination.isHighlighted()) {
				g.setColor(Color.CYAN);
			} else {
				g.setColor(Color.GRAY);
			}

			connection.draw(g);
		}
	}

	@Override
	protected void onClick(MouseEvent e) {
		HashMap<String, Stop> stops = graph.getStops();

		Object stopIds[] = stops.keySet().toArray();
		Point mousePosition = e.getPoint();

		// used to check if a point has been highlighted
		boolean highlight = true;

		for (int i = stopIds.length - 1; i >= 0; i--) {
			Stop currentStop = stops.get(stopIds[i].toString());

			// reset the highlighted boolean to false
			if (currentStop.isHighlighted()) {
				currentStop.setHighlighted(false);
			}

			// highlight the stop, if it's within the area that was clicked only if no other
			// point has been already highlighted
			if (currentStop.within(mousePosition) && highlight) {
				currentStop.setHighlighted(true);
				printStopInfo(currentStop);
				highlight = false;
			}
		}
	}

	@Override
	protected void onMouseWheelMoved(MouseWheelEvent e) {
		scale += -e.getWheelRotation();
	}

	@Override
	protected void onMousePressed(MouseEvent e) {
		// update the mouse's press position
		mousePressPosition = e.getPoint();
	}

	@Override
	protected void onMouseDragged(MouseEvent e) {
		Point newPosition = e.getPoint();

		// calculate the difference between the mouse's current position, and it's
		// starting position
		double xDiff = mousePressPosition.x - newPosition.x;
		double yDiff = mousePressPosition.y - newPosition.y;

		// move the origin slightly to the opposite direction that the mouse was dragged
		// in
		if (xDiff > 0 && yDiff > 0) {
			origin = origin.moveBy(0.1, -0.1);
		} else if (xDiff < 0 && yDiff < 0) {
			origin = origin.moveBy(-0.1, 0.1);
		} else if (xDiff > 0) {
			origin = origin.moveBy(0.1, 0);
		} else if (xDiff < 0) {
			origin = origin.moveBy(-0.1, 0);
		} else if (yDiff > 0) {
			origin = origin.moveBy(0, -0.1);
		} else {
			origin = origin.moveBy(0, 0.1);
		}
	}

	/**
	 * prints the stop's details on the message box.
	 * 
	 * @param stop the stop that we want to print its information
	 */
	private void printStopInfo(Stop stop) {
		String stopName = stop.getName();
		HashMap<String, Connection> connections = stop.getAllConnections();
		HashMap<String, Integer> tripIds = new HashMap<>();

		// get the name of the stop
		String stopInfo = "Stop Name: " + "\n" + stopName + "\n\n";

		// get the IDs of all the trips that go through the stop
		stopInfo += "Trip IDs:\n";
		for (String key : connections.keySet()) {
			Connection connection = connections.get(key);
			String currentTripId = connection.getTrip().getId();
			// check if the trip's id was already added to the set
			if (tripIds.get(currentTripId) == null) {
				stopInfo += currentTripId + "\n";
				tripIds.put(currentTripId, 1);
			}
		}

		getTextOutputArea().setText(stopInfo);
	}

	@Override
	protected void onSearch() {
		HashMap<String, Stop> stops = graph.getStops();

		// verify that the set of stops isn't empty
		if (stops.keySet().size() == 0) {
			return;
		}

		// search for stops by prefix
		String searchTerm = getSearchBox().getText();
		ArrayList<Stop> suggestions = graph.searchStopsByName(searchTerm.toUpperCase());

		String stopNames = "Search results:\n";

		// go through all the stops, and only highlight the ones from suggestions, all
		// the other stops will be unhighlighted
		for (String stopId : stops.keySet()) {
			Stop stop = stops.get(stopId);
			if (suggestions.contains(stop)) {
				stop.setHighlighted(true);
				stopNames += stop.getName() + "\n";
			} else {
				stop.setHighlighted(false);
			}
		}

		getTextOutputArea().setText(stopNames);
	}

	@Override
	protected void onMove(Move m) {
		switch (m) {
		case ZOOM_IN:
			scale++;
			break;
		case ZOOM_OUT:
			scale--;
			break;
		case NORTH:
			origin = origin.moveBy(0, 1);
			break;
		case SOUTH:
			origin = origin.moveBy(0, -1);
			break;
		case EAST:
			origin = origin.moveBy(1, 0);
			break;
		case WEST:
			origin = origin.moveBy(-1, 0);
			break;
		}
	}

	@Override
	protected void onLoad(File stopsFile, File tripsFile) {
		graph.readStopsFile(stopsFile);
		graph.readTripsFile(tripsFile);

		hasReadFiles = true;
	}

	public static void main(String[] args) {
		new TripPlanner();
	}
}
