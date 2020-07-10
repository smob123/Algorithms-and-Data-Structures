package structures;

import java.awt.Graphics;
import java.awt.Point;

public class Connection {
	// the trip that this connection is a part of
	private final Trip trip;
	// the origin and destination of the connection
	private final Stop origin;
	private final Stop destination;

	public Connection(Trip trip, Stop origin, Stop destination) {
		this.trip = trip;
		this.origin = origin;
		this.destination = destination;
	}

	public Trip getTrip() {
		return trip;
	}

	public Stop getOrigin() {
		return origin;
	}

	public Stop getDestination() {
		return destination;
	}

	/**
	 * draw the connection as a line on the canvas.
	 * @param g graphics object that will be used to draw the line
	 */
	public void draw(Graphics g) {
		// get the positions of the origin and destination
		Point originPosition = origin.getPosition();
		Point destPoint = destination.getPosition();
		// shift the line representing the connection by half the size of the stop's
		// size, in order to have the line point at the center of the stop
		int shift = origin.getSize() / 2;
		g.drawLine(originPosition.x + shift, originPosition.y + shift, destPoint.x + shift, destPoint.y + shift);
	}
}
