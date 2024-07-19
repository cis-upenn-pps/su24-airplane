package airplane.g7;

import java.awt.geom.Point2D;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import airplane.sim.Plane;
import airplane.sim.Player;

public class Group7Player extends Player {

    private final Logger logger = Logger.getLogger(this.getClass()); // for logging
    private SecureRandom random = new SecureRandom();
    private ArrayList<Airline> airlines = new ArrayList<>();
    private ArrayList<Airline> bestAirlines = new ArrayList<>();

    private final double alpha = 5.0; // time weight
    private final double beta = 2.0; // power weight
    private final double gamma = 1.0; // delay weight

    private final double coolingRate = 1-1e-4;
    private final boolean ifBend = true;

    private final double curvature = 0.10;
    private final int maxPossibleDelay = 1000;

    private final double distanceThreshold = 5.05;
    private final double collisionPenalty = 1e10;

    private double bestCost = 0.0;
    private double bestTimeCost = 0.0;
    private double bestPowerCost = 0.0;
    private double bestDelayCost = 0.0;

    private double temperature = 1000;

    private class Airline {
        private int departureTime;
        private int arrivalTime;
        private ArrayList<Point2D.Double> path;
        private ArrayList<Double> bearings;

        public Airline() {
            this.departureTime = 0;
            this.arrivalTime = 0;
            this.path = new ArrayList<Point2D.Double>();
            this.bearings = new ArrayList<Double>();
        }

        public Airline(int departureTime, ArrayList<Point2D.Double> path, ArrayList<Double> bearings) {
            this.departureTime = departureTime;
            this.arrivalTime = departureTime + path.size();
            this.path = path;
            this.bearings = bearings;
        }

        public boolean isFlying(int round) {
            return (round >= departureTime && round < arrivalTime);
        }

        @Override
        public String toString() {
            return String.format("\nDeparture Time: %d, Arrival Time: %d, Path Size: %s, Bearings Size: %s;",
                    departureTime, arrivalTime, path.size(), bearings.size());
        }
    }

    @Override
    public String getName() {
        return "Group7Player";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Start Solving ... ");

        // 初始化Airlines，每个Airline对应一个Plane，记录一条直接从起点到终点的路径和航向以及起飞和到达时间
        for (Plane plane : planes) {
            ArrayList<Point2D.Double> path = new ArrayList<>();
            ArrayList<Double> bearings = new ArrayList<>();

            Point2D.Double start = new Point2D.Double(plane.getLocation().getX(), plane.getLocation().getY());
            Point2D.Double end = plane.getDestination();
            double bearing = calculateBearing(start, end);

            Point2D.Double current = start;
            while (!current.equals(end)) {
                path.add(current);
                bearings.add(bearing);
                current = moveTowards(current, bearing);
                if (current.distance(end) < 0.5) {
                    current = end;
                } else if (current.distance(end) > 100) {
                    logger.error("Distance Error!");
                    break;
                }
            }
            path.add(end);
            bearings.add(bearing);

            airlines.add(new Airline(plane.getDepartureTime(), path, bearings));
        }


        // 开始模拟退火

        double cost = 0.0;

        ArrayList<Airline> newAirlines = new ArrayList<>();

        bestCost = calculateCost(planes, airlines);

//        // 初始化时间统计变量
//        long totalDelayTime = 0;
//        long totalBendTime = 0;
//        long totalCostTime = 0;
//        int iterations = 0;

        while (temperature > 1) {
//            // 记录开始时间
//            long startDelayTime = System.nanoTime();

            // 随机延后未起飞飞机的起飞时间
            newAirlines = randomlyDelayAirlines(airlines);

//            // 记录结束时间并累加花费时间
//            long endDelayTime = System.nanoTime();
//            totalDelayTime += (endDelayTime - startDelayTime);

            // 检查是否需要弯曲
            if (ifBend) {
//                long startBendTime = System.nanoTime();

                // 随机弯曲曲线，更新path，bearings和arrivalTime
                newAirlines = randomlyBendAirlines(newAirlines, planes);

//                // 记录结束时间并累加花费时间
//                long endBendTime = System.nanoTime();
//                totalBendTime += (endBendTime - startBendTime);
            }

//            long startCostTime = System.nanoTime();

            // 计算cost
            cost = calculateCost(planes, newAirlines);

//            // 记录结束时间并累加花费时间
//            long endCostTime = System.nanoTime();
//            totalCostTime += (endCostTime - startCostTime);

            // 根据温度决定是否接收解
            if (acceptanceProbability(bestCost, cost, temperature) > random.nextDouble()) {
                bestAirlines = newAirlines;
                bestCost = cost;
            }

            // 温度下降
            temperature *= coolingRate;

//            // 记录迭代次数
//            iterations++;
        }

//        // 计算平均时间
//        double averageDelayTime = totalDelayTime / (double) iterations;
//        double averageBendTime = ifBend ? totalBendTime / (double) iterations : 0;
//        double averageCostTime = totalCostTime / (double) iterations;

//        // 打印平均时间
//        logger.info(String.format("Total Iterations: %5d, Average Delay Time: %.2f ns, Average Bend Time: %.2f ns, Average Cost Time: %.2f ns",
//                iterations, averageDelayTime, averageBendTime, averageCostTime));
        logger.info(String.format("Best Time Cost: %f, Best Power Cost: %f, Best Delay Cost: %f, Best Total Cost: %e, If Collision-free: %s;",
                bestTimeCost, bestPowerCost, bestDelayCost, bestCost, (bestCost <collisionPenalty) ? "True" : "False"));
//        logger.info("Best Airlines: " + bestAirlines.toString());
        logger.info("Finish Solving ...");
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {
        for (int i = 0; i < planes.size(); i++) {
            Plane plane = planes.get(i);
            Airline airline = bestAirlines.get(i);

            if (round < airline.departureTime) {
                bearings[i] = -1; // Not yet departed
            } else if (airline.departureTime<=round && round<airline.arrivalTime && bearings[i] != -2) {
                bearings[i] = ((round - airline.departureTime) < airline.bearings.size()-5) ? airline.bearings.get(round - airline.departureTime) : calculateBearing(plane.getLocation(), plane.getDestination());
            } else {
                bearings[i] = -2; // Arrived at destination
            }
        }
        return bearings;
    }

    private Point2D.Double moveTowards(Point2D.Double point, double bearing) {
        double radialBearing = Math.toRadians(bearing - 90);
        double newX = point.getX() + Math.cos(radialBearing);
        double newY = point.getY() + Math.sin(radialBearing);
        return new Point2D.Double(newX, newY);
    }

    private double calculateCost(ArrayList<Plane>planes, ArrayList<Airline>airlines) {
        double timeCost = 0.0;
        double powerCost = 0.0;
        double delayCost = 0.0;
        double totalCost = 0.0;

        for (int i = 0; i < planes.size(); i++) {
            Plane plane = planes.get(i);
            Airline airline = airlines.get(i);

            // 计算时间代价
            if (airline.arrivalTime - 1 > timeCost) {
                timeCost = airline.arrivalTime - 1;
            }

            // 计算能量代价
            powerCost += airline.path.size() - 1;

            // 计算延迟代价
            if (airline.departureTime > plane.getDepartureTime()) {
                delayCost += (airline.departureTime - plane.getDepartureTime());
            }
        }

        totalCost = alpha * timeCost + beta * powerCost + gamma * delayCost;

        double distance = 0.0;

        // i代表回合数，
        for (int i = 0; i < (int) timeCost; i++) {
            for (int j = 0; j < planes.size(); j++) {
                if (!airlines.get(j).isFlying(i)) {
                    continue;
                } else {
                    for (int k = 0; k < planes.size(); k++) {
                        if (!airlines.get(k).isFlying(i)) {
                            continue;
                        } else {
                            if (j == k) {
                                continue;
                            } else {
                                distance = airlines.get(j).path.get(i-airlines.get(j).departureTime).distance(airlines.get(k).path.get(i-airlines.get(k).departureTime));
                                if (distance < distanceThreshold) {
                                    totalCost += collisionPenalty;
                                }
                            }
                        }
                    }
                }
            }
        }

        logger.info(String.format("Temperature: %f, Time Cost: %f, Power Cost: %f, Delay Cost: %f, Total Cost: %e, Current Best Cost: %e, If Collision-free: %s;",
                temperature, timeCost, powerCost, delayCost, totalCost, bestCost, (bestCost <collisionPenalty) ? "True" : "False"));

        if (totalCost < bestCost) {
            bestTimeCost = timeCost;
            bestPowerCost = powerCost;
            bestDelayCost = delayCost;
        }

        return totalCost;
    }

    // 高温阶段:
    // 在算法的初期，温度较高。此时，即使新的解比当前解差，算法也有较高的概率接受这个较差的解。
    // 这使得算法能够进行较大范围的搜索，探索更多的解空间，从而避免陷入局部最优。
    // 低温阶段:
    // 随着算法的进行，温度逐渐降低。此时，只有当新的解显著优于当前解时，算法才会接受这个新的解。
    // 这使得算法在搜索的后期更倾向于细致地优化当前解，逐步收敛到全局最优解。
    private double acceptanceProbability(double currentCost, double newCost, double temperature) {
        if (newCost < currentCost) { // 如果新代价更低，直接接受
            return 1.0;
        }
        return Math.exp((currentCost - newCost) / temperature); // 否则根据温度计算接受概率
    }

    private ArrayList<Airline> randomlyDelayAirlines(ArrayList<Airline> airlines) {
        ArrayList<Airline> delayedAirlines = new ArrayList<>();
        for (Airline airline : airlines) {
            Airline delayedAirline = new Airline(airline.departureTime + random.nextInt(maxPossibleDelay), airline.path, airline.bearings);
            delayedAirlines.add(delayedAirline);
        }
        return delayedAirlines;
    }

    private ArrayList<Airline> randomlyBendAirlines(ArrayList<Airline> airlines, ArrayList<Plane> planes) {
        for (int i = 0; i < planes.size(); i++) {
            Plane plane = planes.get(i);
            Airline airline = airlines.get(i);
            airline.bearings = generateBearings(plane.getLocation(), plane.getDestination(), curvature, 10);
            airline.path = generatePath(plane.getLocation(), airline.bearings);
            airline.arrivalTime = airline.departureTime + airline.path.size();
        }
        return airlines;
    }

    private ArrayList<Double> generateBearings(Point2D.Double start, Point2D.Double end, double curvature, int num) {
        ArrayList<Point2D.Double> points = new ArrayList<>();
        ArrayList<Double> bearings = new ArrayList<>();

        Point2D.Double current = start;

        double x1 = start.getX();
        double y1 = start.getY();
        double x2 = end.getX();
        double y2 = end.getY();

        // Calculate the control point for the quadratic Bezier curve
        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;
        double controlX = midX + (y1 - y2) * curvature;
        double controlY = midY + (x2 - x1) * curvature;

        // 一系列采样点
        for (int i = 0; i <= num; i++) {
            double t = (double) i / num;
            double x = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * controlX + t * t * x2;
            double y = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * controlY + t * t * y2;
            points.add(new Point2D.Double(x, y));
        }

        bearings.add(calculateBearing(start, points.get(1)));
        // 在每个采样点之间生成航向
        for (int i = 0; i < points.size() - 2; i++) {
            ArrayList<Double> segBearings = generateSegmentBearings(current, points.get(i + 1));
            for (double bearing : segBearings) {
                current = moveTowards(current, bearing);
            }
            bearings.addAll(segBearings);
        }

        ArrayList<Double> segBearings = generateSegmentBearings(current, end);
        for (double bearing : segBearings) {
            current = moveTowards(current, bearing);
        }
        bearings.addAll(segBearings);

//        logger.info("Current: " + current.toString());

        return bearings;
    }

    private ArrayList<Point2D.Double> generatePath(Point2D.Double start, ArrayList<Double> bearings) {
        ArrayList<Point2D.Double> path = new ArrayList<>();
        path.add(start);

        Point2D.Double current = start;

        for (double bearing : bearings) {
            current = moveTowards(current, bearing);
            path.add(current);
        }

        return path;
    }

    private static ArrayList<Double> generateSegmentBearings(Point2D.Double start, Point2D.Double end) {
        ArrayList<Double> bearings = new ArrayList<>();

        // Calculate the distance between start and end points
        double distance = start.distance(end);

        // Calculate the bearing between start and end points
        double bearing = calculateBearing(start, end);

        // Calculate the number of steps (rounded distance)
        int numSteps = (int) Math.round(distance);

        // Populate the bearings list with the calculated bearing
        for (int i = 0; i < numSteps; i++) {
            bearings.add(bearing);
        }

        return bearings;
    }

    private ArrayList<Double> concatenateLists(ArrayList<Double> list1, ArrayList<Double> list2) {
        ArrayList<Double> concatenatedList = new ArrayList<>(list1);
        concatenatedList.addAll(list2);
        return concatenatedList;
    }
}
