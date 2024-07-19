package airplane.g0;

import airplane.sim.*;
import org.apache.log4j.Logger;


import java.awt.geom.Point2D;
import java.util.*;

public class Group2Player extends Player {


    private Logger logger = Logger.getLogger(this.getClass()); // for logging

    public String getName() {
        return "Group 2 Player";
    }


    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Starting new game!");
    }

    //set delayBufferRange based on what the range is of dependency planes
    public int delayBufferRange =9;

    //create the dependencies List
    public void dependenciesList(ArrayList<Plane> inFlightLists){

        for(int j= 0; j<inFlightLists.size(); j++) {

            Plane mp = inFlightLists.get(j);

            FlightPathModel mpm = new FlightPathModel(mp.getLocation(), mp.getDestination());
            int location = inFlightLists.indexOf(mp);

            for (int i = 0; i < location; i++) {

                Plane fp = inFlightLists.get(i);

                if(Math.abs(fp.getDepartureTime()-mp.getDepartureTime()) <=this.delayBufferRange) {

                    FlightPathModel fpm = new FlightPathModel(fp.getLocation(), fp.getDestination());

                    logger.info(mpm.intersectionSafe(fpm));

                    boolean intersectionBool = mpm.intersectionSafe(fpm);
                    boolean destinationSafeBool = mpm.destinationSafe(fpm);
                    boolean departureSafe = mpm.departureSafe(fpm);
                    boolean collisionSafe = mpm.collisionSafe(fpm);

                    if (!intersectionBool || !departureSafe || !destinationSafeBool || !collisionSafe) {
                        mp.addDependencyPlanes(fp);
                    }
                }


            }
        }

    }


    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {

        ArrayList<Plane> planeList = new ArrayList<>(planes);

        //create the dependencies list
        dependenciesList(planeList);

        //for each plane...
        for (int i = 0; i < planes.size(); i++) {

            Plane planeCurrent = planes.get(i);

            //if plane is not landed, and past departure, and all dependencies have landed
            int count =0;
            if (planeCurrent.getBearing() != -2 && planeCurrent.getDepartureTime() < round && planeCurrent.dependenciesAllLand()) {

                double proposedBearing = calculateBearing(planeCurrent.getLocation(), planeCurrent.getDestination());
                bearings[i] = proposedBearing;

            }


        }
        return bearings;


    }
}