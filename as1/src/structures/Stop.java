package structures;

import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;

import gui.Location;

public class Stop {

	// the stop's position on the canvas
	private Point position;
	// the size of the stop on the canvas
	private final int STOP_SIZE = 8;
	// the stop's information
	private String id;
	private String name;
	private Location location;
	private HashMap<String, Connection> nextConnections;
	private HashMap<String, Connection> previousConnections;
	// checks whether to highlight this stop on the canvas or not
	private boolean isHighlighted = false;

	public Stop() {}
	
	public Stop(String id, String name, Location location) {
		this.id = id;
		this.name = name;
		this.location = location;
		nextConnections = new HashMap<>();
		previousConnections = new HashMap<>();
		position = new Point(0, 0);
	}

	/**
	 * draws the stops as an oval on the canvas.
	 * 
	 * @param g      the graphics object that will be used to draw the stop
	 * @param origin the current origin of the map
	 * @param scale  the current scale of the map
	 */
	public void draw(Graphics g, Location origin, double scale) {
		// update the stop's position, and draw the stop as an oval
		updatePoint(origin, scale);
		g.fillOval(position.x, position.y, STOP_SIZE, STOP_SIZE);
	}

	/**
	 * checks whether a point is within this stop or not.
	 * 
	 * @param p the point to check
	 * @return whether a point is within this stop or not
	 */
	public boolean within(Point p) {
		boolean withinX = p.x >= position.x && p.x <= (position.x + STOP_SIZE);
		boolean withinY = p.y >= position.y && p.y <= (position.y + STOP_SIZE);

		return withinX && withinY;
	}

	/**
	 * getters, and setters
	 */

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Location getLocation() {
		return location;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	public Point getPosition() {
		return position;
	}

	public int getSize() {
		return STOP_SIZE;
	}

	public void updatePoint(Location origin, double scale) {
		position = location.asPoint(origin, scale);
	}

	public void setHighlighted(boolean h) {
		isHighlighted = h;
	}

	/**
	 * adds connections to the next stops.
	 * 
	 * @param c the connection object that contains details about the next stop, and
	 *          the trip it's associated with
	 */
	public void addNextConnection(Connection c) {
		// set the combination of the trip's id, and the next stop's id as the key
		String key = c.getTrip().getId() + "_" + c.getDestination().getId();
		nextConnections.put(key, c);
	}

	/**
	 * adds connections to the previous stops.
	 * 
	 * @param c the connection object that contains details about the previous stop,
	 *          and the trip it's associated with
	 */
	public void addPreviousConnection(Connection c) {
		// set the combination of the trip's id, and the previous stop's id as the key
		String key = c.getTrip().getId() + "_" + c.getOrigin().getId();
		previousConnections.put(key, c);
	}

	public HashMap<String, Connection> getAllConnections() {
		HashMap<String, Connection> connections = new HashMap<>();
		connections.putAll(nextConnections);
		connections.putAll(previousConnections);
		return connections;
	}

	public HashMap<String, Connection> getNextConnections() {
		return nextConnections;
	}

	public HashMap<String, Connection> getPreviousConnections() {
		return previousConnections;
	}
}
