package airplane.g6;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import airplane.sim.Plane;
import org.apache.log4j.Logger;

public class Group6Player extends airplane.sim.Player {
    private Logger logger = Logger.getLogger(this.getClass()); // for logging
    private int size;
    private double STEPSIZE = 15;
    private double degree = STEPSIZE;
    private Map<Point2D, Integer> blockLocation = new HashMap<>();

    @Override
    public String getName() {
        return "G6 Player";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        this.size = planes.size();
        logger.info("Start new game");
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
        int[] delay = new int[size];

        for (int i = 0; i < size; i++) {
            if (bearings[i] == -2) continue;
            for (int j = 0; j < size; j++) {
                if (j != i) {
                    if (bearings[j] == -2) continue;
                    Plane p1 = planes.get(i);
                    Plane p2 = planes.get(j);
                    Line2D line1 = new Line2D.Double(p1.getLocation(), p1.getDestination());
                    Line2D line2 = new Line2D.Double(p2.getLocation(), p2.getDestination());

                    if (bearings[j] == -1 && p1.getLocation().distance(p2.getLocation()) <= STEPSIZE ||
                        bearings[i] != -1 && p1.getDestination().distance(p2.getDestination()) <= STEPSIZE ||
                        line1.intersectsLine(line2) ||
                        checkAllBordering(p1, p2, 5) ||
                        checkAllBordering(p2, p1, 5)
                    ) {
                        delay[j] += size;
                    }
                }
            }

            Plane p1 = planes.get(i);
            if (p1.getDepartureTime() <= round - delay[i] - i) {
                if (bearingToDestination(p1) >= degree - STEPSIZE &&
                        bearingToDestination(p1) < degree) {
                    if (bearings[i] == -1) {
                        if (!blockLocation.containsKey(p1.getLocation())) {
                            blockLocation.put(p1.getLocation(), (int) STEPSIZE);
                            bearings[i] = bearingToDestination(p1);
                        } else {
                            blockLocation.put(p1.getLocation(), blockLocation.get(p1.getLocation()) - 1);
                            if (blockLocation.get(p1.getLocation()) == 0) {
                                blockLocation.remove(p1.getLocation());
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<Point2D, Integer> entry : blockLocation.entrySet()) {
            if (entry.getValue() == 0) blockLocation.remove(entry.getKey());
            blockLocation.put(entry.getKey(), entry.getValue() - 1);
        }

        boolean allValuesLessThanZero = true;
        for (int value : blockLocation.values()) {
            if (value >= 0) {
                allValuesLessThanZero = false;
                break;
            }
        }

        // If all values are less than 0, clear the map
        if (allValuesLessThanZero) {
            blockLocation.clear();
        }

        boolean allOut = true;
        int falseCount = 0;
        int countComplete = 0;
        for (int i = 0; i < size; i++) {
            if (bearings[i] < 0) {
                if (bearings[i] == -2) countComplete ++;
                falseCount ++;
                continue;
            }
            if (bearings[i] >= degree || bearings[i] < degree - STEPSIZE) {
                allOut = false;
                falseCount ++;
                break;
            }
        }

        if (allOut || falseCount == size) {
            logger.info("count: " + falseCount + ", size: " + size + ", complete: " + countComplete  + ", " + degree);
            degree += STEPSIZE;
            if (degree > 360) {
                degree %= 360;
            }
        }

        return bearings;
    }

    private double bearingToDestination(Plane p) {
        return calculateBearing(p.getLocation(), p.getDestination());
    }

    private boolean checkAllBordering(Plane p1, Plane p2, int multiplier) {
        Line2D line1 = new Line2D.Double(p1.getLocation(), p1.getDestination());

        return (
                line1.intersectsLine(new Line2D.Double(
                        p2.getLocation().getX(),
                        p2.getLocation().getY(),
                        p2.getDestination().getX() + multiplier * STEPSIZE,
                        p2.getDestination().getY())) ||
                line1.intersectsLine(new Line2D.Double(
                        p2.getLocation().getX(),
                        p2.getLocation().getY(),
                        p2.getDestination().getX() - multiplier * STEPSIZE,
                        p2.getDestination().getY())) ||
                line1.intersectsLine(new Line2D.Double(
                        p2.getLocation().getX() + multiplier * STEPSIZE,
                        p2.getLocation().getY(),
                        p2.getDestination().getX(),
                        p2.getDestination().getY())) ||
                line1.intersectsLine(new Line2D.Double(
                        p2.getLocation().getX() - multiplier * STEPSIZE,
                        p2.getLocation().getY(),
                        p2.getDestination().getX(),
                        p2.getDestination().getY())) ||
                    line1.intersectsLine(new Line2D.Double(
                            p2.getLocation().getX(),
                            p2.getLocation().getY(),
                            p2.getDestination().getX(),
                            p2.getDestination().getY() + multiplier * STEPSIZE)) ||
                    line1.intersectsLine(new Line2D.Double(
                            p2.getLocation().getX(),
                            p2.getLocation().getY(),
                            p2.getDestination().getX(),
                            p2.getDestination().getY() - multiplier * STEPSIZE)) ||
                    line1.intersectsLine(new Line2D.Double(
                            p2.getLocation().getX(),
                            p2.getLocation().getY() + multiplier * STEPSIZE,
                            p2.getDestination().getX(),
                            p2.getDestination().getY())) ||
                    line1.intersectsLine(new Line2D.Double(
                            p2.getLocation().getX(),
                            p2.getLocation().getY() - multiplier * STEPSIZE,
                            p2.getDestination().getX(),
                            p2.getDestination().getY()))
                );
    }

    private int getQuadrant(double bearing) {
        if (bearing >= 0 && bearing < 60) {
            return 1;
        } else if (bearing >= 60 && bearing < 120) {
            return 2;
        } else if (bearing >= 120 && bearing < 180) {
            return 3;
        }else if (bearing >= 180 && bearing < 240) {
            return 4;
        }else if (bearing >= 240 && bearing < 300) {
            return 5;
        }else {
            return 6;
        }
    }

//    @Override
    public double[] updatePlanesOld(ArrayList<Plane> planes, int round, double[] bearings) {
        int size = planes.size();
        int[] delay = new int[size];

        Map<Integer, Integer> quadrantDelayMap = new HashMap<>();
        quadrantDelayMap.put(1, 0);
        quadrantDelayMap.put(2, 2);
        quadrantDelayMap.put(3, 4);
        quadrantDelayMap.put(4, 6);
        quadrantDelayMap.put(5, 8);
        quadrantDelayMap.put(6, 10);

        // Determine the quadrant for each plane and assign delay
        for (int i = 0; i < size; i++) {
            delay[i] = 12* i;
            delay[i] += Math.max(0, delay[i] + quadrantDelayMap.get(getQuadrant(bearings[i])));

        }



        // single plane
        if (size == 1) {
            if (bearings[0] == -2) return bearings;
            bearings[0] = calculateBearing(planes.get(0).getLocation(), planes.get(0).getDestination());
            return bearings;
        }

        // otherwise
        for (int i = 0; i < size; i++) {
            if (bearings[i] == - 2) continue; // stopping condition

            // delay
            if (round >= delay[i]) {

                //if on the ground and plane landing at location soon
                boolean stop = false;
                if(bearings[i] == -1){
                    for(int j = 0; j < size; j++){
                        if(planes.get(j).getDestination().distance(planes.get(i).getLocation()) <= 10 &&  bearings[j] != -1 && bearings[j] != -2) {
                            stop = true;
                        }
                    }
                }
                if (stop) continue;


                bearings[i] = calculateBearing(planes.get(i).getLocation(), planes.get(i).getDestination());
                if (bearings[i] >= 360) bearings[i] -= 360;

                for (int j = 0; j < size; j++) {
                    if (j >= i) {
                        Plane p1 = planes.get(i);
                        Plane p2 = planes.get(j);

                        if (p1.getLocation().distance(p2.getLocation()) <= 150 && p1.dependenciesHaveLanded(bearings)) {

                            double newBearing = calculateBearing(planes.get(i).getLocation(), planes.get(i).getDestination()) + 10;
                            double multiplier = planes.get(i).getLocation().distance(planes.get(i).getDestination());
                            multiplier /= 1 + planes.get(i).getLocation().distance(planes.get(i).getDestination());

                            if (Math.abs(newBearing - bearings[i]) > 10) {

                                Line2D line1 = new Line2D.Double(p1.getLocation().getX(), p1.getLocation().getY(), p1.getDestination().getX(), p1.getDestination().getY());
                                Line2D line2 = new Line2D.Double(p2.getLocation().getX(), p2.getLocation().getY(), p2.getDestination().getX(), p2.getDestination().getY());

                                if (line2.intersectsLine(line1) ||
                                        (Math.abs(bearings[i] - bearings[j]) <= 225 && Math.abs(bearings[i] - bearings[j]) >= 135)
                                ) {
                                    bearings[i] += 9.2 * multiplier;
                                    if (bearings[i] >= 360) bearings[i] -= 360;
                                } else {
                                    bearings[i] += 1;
                                    if (bearings[i] >= 360) bearings[i] -= 360;
                                }
                            }
                        } else if (p1.getLocation().distance(p2.getLocation()) <= 50) {
                            double newBearing = calculateBearing(planes.get(i).getLocation(), planes.get(i).getDestination()) + 10;
                            double multiplier = planes.get(i).getLocation().distance(planes.get(i).getDestination());
                            multiplier /= 1 + planes.get(i).getLocation().distance(planes.get(i).getDestination());

                            if (Math.abs(newBearing - bearings[i]) >= 10) {
                                Line2D line1 = new Line2D.Double(p1.getLocation().getX(), p1.getLocation().getY(), p1.getDestination().getX(), p1.getDestination().getY());
                                Line2D line2 = new Line2D.Double(p2.getLocation().getX(), p2.getLocation().getY(), p2.getDestination().getX(), p2.getDestination().getY());

                                if (line2.intersectsLine(line1) && Math.abs(bearings[i] - bearings[j]) <= 225 && Math.abs(bearings[i] - bearings[j]) >= 135) {
                                    bearings[i] += 9.4 * multiplier;
                                    if (bearings[i] >= 360) bearings[i] -= 360;
                                } else {
                                    bearings[i] += 1;
                                    if (bearings[i] >= 360) bearings[i] -= 360;
                                }
                            } else {
                                bearings[i] += 1;
                                if (bearings[i] >= 360) bearings[i] -= 360;
                            }
                        }
                    }
                }
            }
        }

        return bearings;
    }

}
