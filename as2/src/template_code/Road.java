package template_code;

import java.util.Collection;
import java.util.HashSet;

/**
 * Road represents ... a road ... in our graph, which is some metadata and a
 * collection of Segments. We have lots of information about Roads, but don't
 * use much of it.
 * 
 * @author tony
 */
public class Road {
	public final int roadID;
	public final String name, city;
	public final Collection<Segment> components;
	public final boolean oneway;
	public final int speed;
	public final int roadClass;
	public final boolean notForCar;
	public final boolean notForPede;
	public final boolean notForBicy;

	public Road(int roadID, int type, String label, String city, int oneway, int speed, int roadclass, int notforcar,
			int notforpede, int notforbicy) {
		this.roadID = roadID;
		this.city = city;
		this.name = label;
		this.components = new HashSet<Segment>();
		this.oneway = oneway == 0 ? false : true;
		this.roadClass = roadclass;
		this.notForCar = notforcar == 0 ? false : true;
		this.notForPede = notforpede == 0 ? false : true;
		this.notForBicy = notforbicy == 0 ? false : true;

		switch (speed) {
		case 0:
			this.speed = 5;
			break;
		case 1:
			this.speed = 20;
			break;
		case 2:
			this.speed = 40;
			break;
		case 3:
			this.speed = 60;
			break;
		case 4:
			this.speed = 80;
			break;
		case 5:
			this.speed = 100;
			break;
		case 6:
			this.speed = 110;
			break;
		default:
			this.speed = (int) Double.POSITIVE_INFINITY;
			break;
		}
	}

	public void addSegment(Segment seg) {
		components.add(seg);
	}
}

// code for COMP261 assignments