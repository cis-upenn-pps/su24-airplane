package airplane.g8;

import java.awt.geom.Point2D;
import java.util.*;

import org.apache.log4j.Logger;
import airplane.sim.Plane;
import airplane.sim.Player;


public class Group8Player1 extends Player {

	private Logger logger = Logger.getLogger(this.getClass()); // for logging
	public List<double[]> updates;

	@Override
	public String getName() {
		return "Group 8 Player";
	}
	
	/*
	 * This is called at the beginning of a new simulation. 
	 * Each Plane object includes its current location (origin), destination, and
	 * current bearing, which is -1 to indicate that it's on the ground.
	 */
	@Override
	public void startNewGame(ArrayList<Plane> planes) {
		logger.info("Starting new game!");

	}

	public void simulateUpdates(ArrayList<Plane> planes) {
		updates = new ArrayList<>();
		int round = 0;
		int n = planes.size();
		while (true) {
			double[] bearings = new double[n];
			if (round == 0) {
				// do something for 1st step
				for (int i = 0; i < n; i++) {

				}

			} else {
				// look @ prev bearings, do something
				double[] prev = updates.get(round-1);
				for (int i = 0; i < n; i++) {
					Plane p = planes.get(i);

				}
			}
			round++;
		}
	}
	public class Point {
		public double x;
		public double y;
		public Point(double xArg, double yArg) {
			this.x = xArg;
			this.y = yArg;
		}
	}



//	static boolean planeTrajectoriesCollide(Plane p1, Plane p2) {
//		Point2D.Double orig1 = p1.getCurrentLocation();
//		Point2D.Double dest1 = p1.getDestination();
//		Point2D.Double orig2 = p2.getCurrentLocation();
//		Point2D.Double dest2 = p2.getDestination();
//		double a1 = area(orig1, dest1, orig2);
//		double a2 = area(orig1, dest1, dest2);
//		double a3 = area(orig2, dest2, orig1);
//		double a4 = area(orig2, dest2, dest1);
//
//
//		if (((a1 >= 0 && a2 < 0) || (a1 < 0 && a2 >= 0)) &&
//			(a3 >= 0 && a4 < 0) || (a3 < 0 && a4 >= 0)) {
//			return true;
//		}
//        return (a1 == 0. && onTrajectory(orig1, orig2, dest1)) ||
//                (a2 == 0. && onTrajectory(orig2, dest1, dest2)) ||
//                (a3 == 0. && onTrajectory(orig1, dest2, dest1)) ||
//                (a4 == 0. && onTrajectory(orig1, orig2, dest2));
//	}
//	static double area(Point2D.Double p, Point2D.Double q, Point2D.Double r) {
//		return (q.y - p.y) * (r.x - q.x) -
//				(q.x - p.x) * (r.y - q.y);
//
//	}
//	static boolean onTrajectory(Point2D.Double p, Point2D.Double q, Point2D.Double r) {
//		return (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) &&
//				q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y));
//	}
//
//	/*
//	 * This is called at each step of the simulation.
//	 * The List of Planes represents their current location, destination, and current
//	 * bearing; the bearings array just puts these all into one spot.
//	 * This method should return an updated array of bearings.
//	 */
//	@Override
//	public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
//		for (int i = 0; i < planes.size(); i++) {
//			Plane p = planes.get(i);
//			if (p.getBearing() == -1 && p.dependenciesHaveLanded(bearings))  {
//				bearings[i] = calculateBearing(p.getLocation(), p.getDestination());
//			} else {
//				for (int j = 0; j < planes.size(); j++) {
//					if (j == i) {
//						continue;
//					} else {
//						// do something
//					}
//				}
//			}
//		}
//
//		return bearings;
////		Set<Integer> inFlight = new HashSet<>();
////		// if any plane is in the air, then just keep things as-is
////		for (int i = 0; i < planes.size(); i++) {
////			Plane p = planes.get(i);
////		    if (p.getBearing() != -1 && p.getBearing() != -2) inFlight.add(i);
////		}
////
////		// if no plane is in the air, find the one with the earliest
////		// departure time and move that one in the right direction
////
////		if (inFlight.isEmpty())
////		{
////			int minTime = 10000;
////			int minIndex = 10000;
////			for (int i = 0; i < planes.size(); i++) {
////				Plane p = planes.get(i);
////				if (p.getDepartureTime() < minTime && p.getBearing() == -1 && p.dependenciesHaveLanded(bearings)) {
////					minIndex = i;
////					minTime = p.getDepartureTime();
////
////				}
////			}
////			// if it's not too early, then take off and head straight for the destination
////			if (round >= minTime) {
////				Plane p = planes.get(minIndex);
////				bearings[minIndex] = calculateBearing(p.getLocation(), p.getDestination());
////			}
////		}
////		for (int i = 0; i < planes.size(); i++) {
////			if (inFlight.contains(i)) continue;
////			Plane p1 = planes.get(i);
////			if (p1.getBearing() == -1 && p1.dependenciesHaveLanded(bearings)) {
////				boolean conflict = false;
////				Iterator<Integer> inFlightIter = inFlight.iterator();
////				while (inFlightIter.hasNext()) {
////					int j = inFlightIter.next();
////					Plane p2 = planes.get(j);
////
////					if (planeTrajectoriesCollide(p1, p2)) {
////						conflict = true;
////						break;
////					}
////				}
////				bearings[i] = calculateBearing(p1.getLocation(), p1.getDestination());
////				if (conflict) {
////					bearings[i] -= 10;
////					inFlight.add(i);
////				}
////			}
////		}
////		return bearings;
//
//
//	}
//



	private Point2D coast(Point2D origin, double distance, double bearing){
		double standardBearing = Math.toRadians(90) - Math.toRadians(bearing);
		double newx = origin.getX() + distance * Math.cos(standardBearing);
		double newy = origin.getY() - distance * Math.sin(standardBearing);
		return new Point2D.Double(newx,newy);
	}
	@Override
	public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {

		double[] outBearings = new double[bearings.length];
		for (int i = 0; i< bearings.length; i++)
			outBearings[i] = bearings[i];

		for (int i = 0; i < bearings.length; i++){
			if (outBearings[i] == -1.0){
				Plane p = planes.get(i);
				if (round >= p.getDepartureTime()){
					// p has not taken off yet. We must decide whether it should take off now.
					double pBearing = calculateBearing(p.getLocation(),p.getDestination());
					boolean safe = true;
					// determing whether taking off is safe. If there are problems, overwrite with false
					// Simulate
					double distance = p.getLocation().distance(p.getDestination());
					for (int t = 0; t<=distance; t++){
						Point2D location_t = coast(p.getLocation(),t,pBearing);
						for (int j = 0; j<outBearings.length; j++){

							if (outBearings[j] >=0){
								Plane plane_j = planes.get(j);
								Point2D plane_j_location_at_time_t = coast(plane_j.getLocation(),t,outBearings[j]);
								double landing_time_j = plane_j.getLocation().distance(plane_j.getDestination());
								if (t <= landing_time_j){
									if (location_t.distance(plane_j_location_at_time_t) <= 5.0){
										safe = false;
									}
								}
							}
						}
					}

					if (safe){
						outBearings[i] = pBearing;
					}
				}

			}
		}

		return outBearings;
	}
}
