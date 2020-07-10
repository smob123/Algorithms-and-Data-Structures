package structures;

import java.util.ArrayList;

public class Trip {
	private final String id;
	private ArrayList<Connection> connections = new ArrayList<>();

	public Trip(String id) {
		this.id = id;
	}

	/**
	 * getters, and setters
	 */

	public void setConnections(ArrayList<Connection> newConnections) {
		connections = newConnections;
	}

	public ArrayList<Connection> getConnections() {
		return connections;
	}

	public String getId() {
		return id;
	}
}
