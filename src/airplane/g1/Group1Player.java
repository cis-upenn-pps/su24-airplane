package airplane.g1;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

import org.apache.log4j.Logger;
import airplane.sim.Plane;
import airplane.sim.Player;

public class Group1Player extends Player {

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public String getName() {
        return "Group 1 Player";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Starting new game!");
        planes.sort(Comparator.comparingInt(Plane::getDepartureTime));
        for(int i = 0; i < planes.size(); i++) {
            Plane p = planes.get(i);
            p.id = i;
        }
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
        planes.sort(Comparator.comparingInt(Plane::getDepartureTime));
        for (int i = 0; i < planes.size(); i++) {
            Plane p = planes.get(i);
            if(p.getDepartureTime() <= round && p.getBearing() == -1 && clearSky(planes, p)){
                double newBearing = calculateBearing(p.getLocation(), p.getDestination());
                bearings[i] = adjustBearing(p.getBearing() , newBearing);
            }else if(bearings[i] != -1 && bearings[i] != -2 && p.getDepartureTime() <= round) {
                double newBearing = collisionPrevention(planes, p);
                bearings[i] = adjustBearing(p.getBearing() , newBearing);
            }
        }

        if(checkPlaneNum(planes) == 0){
            for(int i = 0; i < planes.size(); i++) {
                Plane p = planes.get(i);
                if (p.getDepartureTime() <= round && p.getBearing() != -2) {
                    bearings[i] = calculateBearing(p.getLocation(), p.getDestination());
                    break;
                }
            }
        }

        return bearings;
    }



    public static final double MAX_BEARING_CHANGE = 9.9999;


    private double adjustBearing(double originalBearing, double newBearing) {
        if (originalBearing == -1) {
            return newBearing;
        }

        double change = newBearing - originalBearing;


        change = ((change + 540) % 360) - 180;

        if (change > MAX_BEARING_CHANGE) {
            return (originalBearing + MAX_BEARING_CHANGE) % 360;
        } else if (change < -MAX_BEARING_CHANGE) {
            return (originalBearing - MAX_BEARING_CHANGE + 360) % 360;
        } else {
            return (originalBearing + change + 360) % 360;
        }
    }

    public double collisionPrevention(ArrayList<Plane> planes, Plane targetPlane) {
        double newBearing = 0;
        TreeSet<Plane> sortedPlanes = new TreeSet<>((o1, o2) -> {
            double dist1 = calculateDistance(targetPlane, o1);
            double dist2 = calculateDistance(targetPlane, o2);
            if (dist1 < dist2) {
                return -1;
            } else if (dist1 > dist2) {
                return 1;
            } else {
                return 0;
            }
        });

        for (Plane i : planes) {
            if (i == targetPlane) {
                continue;
            }
            if (i.getBearing() >= 0 && calculateDistance(targetPlane, i) <= 20) {
                sortedPlanes.add(i);
            }
        }

        if(sortedPlanes.isEmpty()) {
            return calculateBearing(targetPlane.getLocation(), targetPlane.getDestination());
        }


        Plane nearestPlane = sortedPlanes.first();
        if (nearestPlane != null) {
            newBearing = modifyBearing(targetPlane, nearestPlane);
        }
        return newBearing;
    }

    private double modifyBearing(Plane targetPlane, Plane p2) {
        double bearing1 = targetPlane.getBearing();
        double bearing2 = p2.getBearing();
        if(bearing1 == bearing2){
            return calculateBearing(targetPlane.getLocation(), targetPlane.getDestination());
        }

        if(targetPlane.getDestination().equals(p2.getDestination())){
//            if(calculateDistanceToDest(targetPlane.getLocation(), targetPlane.getDestination()) < calculateDistanceToDest(p2.getLocation(), p2.getDestination())){
//                return calculateBearing(targetPlane.getLocation(), targetPlane.getDestination());
//            }else{
//                return (bearing1 + 10) % 360;
//            }
            return calculateBearing(targetPlane.getLocation(), targetPlane.getDestination());
        }


        if(calculateDistanceTwoPoints(targetPlane.getLocation(), targetPlane.getDestination()) < calculateDistanceTwoPoints(p2.getLocation(), p2.getDestination())){
            return calculateBearing(targetPlane.getLocation(), targetPlane.getDestination());
        }

        if(calculateDistanceTwoPoints(targetPlane.getDestination(), p2.getDestination()) > 10 &&
                calculateDistanceTwoPoints(targetPlane.getLocation(), targetPlane.getDestination()) < 15){
            return calculateBearing(targetPlane.getLocation(), targetPlane.getDestination());
        }


        return (bearing1 + 10) % 360;


    }


    private boolean clearSky(ArrayList<Plane> planes, Plane plane){
        for(int i = 0; i < planes.size(); i++){


            Plane p = planes.get(i);
            if(p != plane) {

                //check if they have the same location

                if (p.getX() == plane.getX() && p.getY() == plane.getY()) {
                    if (plane.getDepartureTime() >= p.getDepartureTime()) {
                        return false;
                    }
                }
                double distanceToDest = calculateDistanceTwoPoints(plane.getLocation(), plane.getDestination());
                double distanceToDest2 = calculateDistanceTwoPoints(p.getLocation(), p.getDestination());


                if (p.getBearing() == -2) {
                    continue;
                }

                if (plane.getDestination().equals(p.getDestination()) && plane.getDepartureTime() == p.getDepartureTime()) {
                    if (distanceToDest == distanceToDest2 && p.getBearing() == -1) {
                        if (plane.id > p.id) {
                            return false;
                        }
                    }
                }


                if (plane.getDestination().equals(p.getDestination()) && p.getBearing() != -1) {
                    if (distanceToDest < distanceToDest2 + 15) {
                        return false;
                    }
                }

                if(p.getBearing() == -1 && calculateDistance(plane, p) <= 30 && plane.getDepartureTime() == p.getDepartureTime()){
                    if(plane.id > p.id){
                        return false;
                    }
                }

                double distance = calculateDistance(plane, p);

                if (p.getBearing() == -1 && distance <= 30 && plane.getDepartureTime() > p.getDepartureTime()) {
                    return false;
                }

                if (p.getBearing() == -1 && distance <= 30 && plane.getDepartureTime() < p.getDepartureTime()) {
                    continue;
                }

                if(p.getBearing() == -1 && distance > 30){
                    continue;
                }

                if(calculateDistance(plane, p) <= 30 && p.getBearing() != -1){
//                    logger.info("33");
                    return false;
                }


            }
        }
        return true;
    }




    private int checkPlaneNum(ArrayList<Plane> planes) {
        int count = 0;
        for (Plane p : planes) {
            if (p.getBearing() != -1 && p.getBearing() != -2) {
                count++;
            }
        }
        return count;
    }


    private double calculateDistance(Plane p1, Plane p2) {
        double deltaX = p1.getX() - p2.getX();
        double deltaY = p1.getY() - p2.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private double calculateDistanceTwoPoints(Point2D.Double p1, Point2D.Double p2) {
        double deltaX = p1.getX() - p2.getX();
        double deltaY = p1.getY() - p2.getY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

}