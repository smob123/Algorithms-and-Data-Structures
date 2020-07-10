package structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gui.Location;

public class Graph {
	private HashMap<String, Stop> stops = new HashMap<>();
	ArrayList<Connection> connections = new ArrayList<>();
	private Trie trie = new Trie();

	/**
	 * reads a stops file, parses its data, and returns them as a list.
	 * 
	 * @param stopsFile the file containing a list of stops
	 */
	public void readStopsFile(File stopsFile) {
		// initialize the buffer reader to read the file line by line
		FileReader fr = null;
		try {
			fr = new FileReader(stopsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(fr);

		// current line in the file
		String line;

		try {
			// ignore the first line
			reader.readLine();

			while ((line = reader.readLine()) != null) {
				// split the stop's information
				String[] parts = line.split("\t");
				String id = parts[0];
				String name = parts[1];
				double lat = new Double(parts[2]);
				double lon = new Double(parts[3]);

				// get the stop's location
				Location location = Location.newFromLatLon(lat, lon);
				Stop stop = new Stop(id, name, location);
				// add the stop to the hashmap by its id
				stops.put(stop.getId(), stop);
				// add the stop by name to the trie
				trie.insert(name.toUpperCase(), stop);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * reads a tips file, parses its data, and returns them as a list of
	 * connections.
	 * 
	 * @param tripsFile the file containing a list of trips
	 */
	public void readTripsFile(File tripsFile) {
		FileReader fr = null;
		try {
			fr = new FileReader(tripsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(fr);

		String line;

		try {
			// ignore the first line
			reader.readLine();
			while ((line = reader.readLine()) != null) {
				String[] stopIds = line.split("\t");
				ArrayList<Connection> stopConnections = new ArrayList<>();

				// get the trip id
				String tripId = stopIds[0];
				Trip trip = new Trip(tripId);

				// add connections to all the stops
				for (int i = 1; i < stopIds.length; i++) {
					// get the stop by its id
					String id = stopIds[i];
					Stop stop = stops.get(id);

					// add next connection
					if (i < stopIds.length - 1) {
						String nextStopId = stopIds[i + 1];
						Stop nextStop = stops.get(nextStopId);

						Connection connection = new Connection(trip, stop, nextStop);
						stopConnections.add(connection);

						stop.addNextConnection(connection);
					}

					// add previous connection
					if (i > 1) {
						String previousStopId = stopIds[i - 1];
						Stop previousStop = stops.get(previousStopId);

						Connection connection = new Connection(trip, stop, previousStop);
						stopConnections.add(connection);

						stop.addPreviousConnection(connection);
					}
				}

				// add all the connections to the current trip object, and to the list of all
				// connections
				trip.setConnections(stopConnections);
				connections.addAll(stopConnections);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * searches for stops in the trie structure by prefix.
	 * 
	 * @param searchTerm the prefix that will be used to search for stops
	 * @return a list of stops that match the prefix
	 */
	public ArrayList<Stop> searchStopsByName(String searchTerm) {
		return trie.getSuggestions(searchTerm);
	}

	/**
	 * getters
	 */

	public HashMap<String, Stop> getStops() {
		return stops;
	}

	public ArrayList<Connection> getConnections() {
		return connections;
	}

	public Trie getTrie() {
		return trie;
	}
}
