package airplane.sim;

import org.apache.log4j.Logger;

import java.awt.geom.Point2D;
import java.util.Objects;

import static airplane.sim.Player.calculateBearing;
import static java.lang.Math.sqrt;

public class FlightPathModel {

    public enum END_SETTER {
        user,       // a 'real' end that the user has specified
        program,    // a 'fake' end at the edge of the board
    }
    public enum DIRECTION {
        exact_opposite,
        general_opposite,
        general_same
    }

    private final Point2D.Double start;
    private Point2D.Double end;
    private Double gradient;
    private Double yIntercept;
    private Double length;
    private Double bearing;
    private END_SETTER endType;
    private final Logger logger = Logger.getLogger(this.getClass()); // for logging


    //constructors

    /**
     * For use when you know the start and the bearing. Puts in place a fake end (nominally the edge of the board)
     * @param start coordinate reflecting the start of your vector
     * @param bearing angle of travel
     */
    public FlightPathModel(Point2D.Double start, double bearing) {

        this.start = start;
        this.bearing = bearing;
        this.endType = END_SETTER.program;
        this.end = suggestEndingFrom(start, getBearing());

    }
    /**
     * For use when you have the start and end point only. Calculates the bearing.
     * @param start coordinate representing the start of your vector
     * @param end coordinate representing the end of your vector
     */
    public FlightPathModel(Point2D.Double start, Point2D.Double end) {

        this.start = start;
        this.end = end;
        this.endType = END_SETTER.user;
        this.bearing = getBearing();

    }



    //private helper methods required by constructor
    private Point2D.Double suggestEndingFrom(Point2D.Double start, double bearing) {

        //check if it's already done

        Point2D.Double TL = new Point2D.Double(0, 100);
        Point2D.Double TR = new Point2D.Double(100, 100);
        Point2D.Double BL = new Point2D.Double(0, 0);
        Point2D.Double BR = new Point2D.Double(100, 0);

        if (bearing < 45) {

            //then we're heading for top x axis
            end = locateIntersectionWith(new FlightPathModel(TL, TR));

        } else if (bearing < 135) {
            //then we're heading for right y axis
            end = locateIntersectionWith(new FlightPathModel(TR, BR));

        } else if (bearing < 225) {
            //then we're heading for bottom x axis
            end = locateIntersectionWith(new FlightPathModel(BL, BR));

        } else if (bearing < 315) {
            //then we're heading for left y axis
            end = locateIntersectionWith(new FlightPathModel(TL, BL));

        } else {
            //we're back in x axis condition
            end = locateIntersectionWith(new FlightPathModel(TL, TR));
        }

        endType = END_SETTER.program;

        return end;
    }         //suggests 'fake' ending to vector at the edge of the playable space


    //getter methods
    public Point2D.Double getStart() {

        return this.start;}                     //starting coordinate. Always set in constructor.
    public Point2D.Double getEnd()  {

        if (end != null) return end;

        else suggestEndingFrom(getStart(), getBearing());

        return end;
    }                      //ending coordinate. May be 'real' or 'fake' (see end type)
    public END_SETTER getEndType() { return endType;}                       //the method by which the end was set (i.e. by the programmer or by the program)

    public double getLength() {

        if (length != null) return length;

        //calculate the length of the non-hypotenuse sides
        double AC = start.getY() - end.getY();
        double BC = start.getX() - end.getX();

        //use this to calculate the hypotenuse
        return sqrt((AC * AC) + (BC * BC));

    }                            //length of the line stretching between start and end
    public double getBearing() {

        if (bearing != null) return bearing;

        bearing = calculateBearing(this.getStart(), this.getEnd());

        return bearing;

    }                           //angle of the line, from x coord to y coord
    public double getGradient() {       //check this

        if (gradient != null) return gradient;

        gradient = (this.getEnd().getY() - this.getStart().getY()) / (this.getEnd().getX() - this.getStart().getX());

        return gradient;

    }                           //angle of the line, from bottom to top


    //methods to compare with external objects
    public DIRECTION compareDirectionWith(FlightPathModel that) {

        double buffer = 10;

        double difference = this.getBearing() - that.getBearing();

        if (difference < 0) difference = (+difference);
        if (difference > (180 - buffer) && difference < (180 + buffer)) return DIRECTION.exact_opposite;          //could be more precise
        if (this.getBearing() + 90 > that.getBearing() && this.getBearing() - 90 < that.getBearing()) return DIRECTION.general_same;
        else return DIRECTION.general_opposite;

    }
    public Point2D.Double locateIntersectionWith(FlightPathModel that) {

        //if they have the same bearing they will never intersect
        if (Objects.equals(this.bearing, that.bearing)) return null;

        double c1 = this.getStart().getY() - (this.getGradient() * this.getStart().getX());
        double c2 = that.getStart().getY() - (that.getGradient() * that.getStart().getX());

        if (this.getGradient() == Double.NEGATIVE_INFINITY || this.getGradient() == Double.POSITIVE_INFINITY) {
            double x = this.getStart().getX();
            double y = (that.getGradient() * x) + c2;

            if (x < 0 || x > 100 || y < 0 || y > 100) return null;

            return new Point2D.Double(x, y);
        }

        if (that.getGradient() == Double.NEGATIVE_INFINITY || that.getGradient() == Double.POSITIVE_INFINITY) {
            double x = that.getStart().getX();
            double y = (this.getGradient() * x) + c1;

            if (x < 0 || x > 100 || y < 0 || y > 100) return null;

            return new Point2D.Double(x, y);
        }

        if (this.getGradient() - that.getGradient() == 0) {
            double xstart = (this.getStart().getX() + that.getStart().getX())/2;
            double ystart = (this.getStart().getY() + that.getStart().getY())/2;;
            return new Point2D.Double(xstart, ystart);
//            double xend = ;
//            double yend = ;
        }

        //calculate the intersection
        double x = that.getStart().getY() - this.getStart().getY() / (this.getGradient() - that.getGradient());
        double y = x * this.getGradient() + this.getStart().getY();

        //if the intersection is out of bounds, we do not need to worry
        if (x < 0 || x > 100 || y < 0 || y > 100) return null;

        //if here, there is an intersection - return point
        return new Point2D.Double(x, y);

    }
    public Double calculateDistanceTo(Point2D.Double point) {

        FlightPathModel model = new FlightPathModel(this.getStart(), point);

        return model.getLength();
    }
    public Double calculateDistanceBetweenStartPoints(FlightPathModel that){
        double distanceBetweenStart = Math.sqrt(Math.pow((this.getStart().getX() - that.getStart().getX()), 2.0) + Math.pow((this.getStart().getY() - that.getStart().getX()), 2.0));
        return distanceBetweenStart;
    }
    public Double calculateDistanceBetweenEndPoints(FlightPathModel that){
        double distanceBetweenEnd = Math.sqrt(Math.pow((this.getEnd().getX() - that.getEnd().getX()), 2.0) + Math.pow((this.getEnd().getY() - that.getEnd().getX()), 2.0));
        return distanceBetweenEnd;
    }
    private boolean badIntersection(FlightPathModel that) {

        //calculate intersection point
        Point2D.Double intersection = this.locateIntersectionWith(that);

        //if no intersection at all, it can't be bad
        if (intersection == null) {
            return false;
        }

        //calculateDistancesToIntersection
        FlightPathModel d1 = new FlightPathModel(this.getStart(), intersection);
        FlightPathModel d2 = new FlightPathModel(that.getStart(), intersection);

        //calculate difference in distances
        double difference = d1.getLength() - d2.getLength();

        //distance should not be closer than buffer
        return difference < GameConfig.SAFETY_RADIUS+2;
    }


    //methods to change vector
    public Point2D.Double shortenVector(double distance) {

        //check if it's already done
        double radians = getBearing() * (Math.PI/180);

        double newX = distance * Math.sin(radians);
        double newY = distance * Math.cos(radians);

        end = new Point2D.Double(getStart().getX() + newX, getStart().getY() + newY);
        return end;

    }              //resets vector with 'fake' ending at the end of the specified distance


    //public methods to evaluate flight paths against one another
    public boolean collisionSafe(FlightPathModel that) {

        return !this.compareDirectionWith(that).equals(DIRECTION.general_opposite);
    }
    public boolean departureSafe(FlightPathModel that) {

        //departure point is unsafe if departure points are closer than 5 units apart
        return new FlightPathModel(this.start, that.start).getLength() > GameConfig.SAFETY_RADIUS;
    }
    public boolean destinationSafe(FlightPathModel that) {

        if (this.getEndType().equals(END_SETTER.program)) {
            logger.info("warning: this destination was not set by the user");
        }

        //departure point is unsafe if destination points are closer than 5 units apart
        return new FlightPathModel(this.getEnd(), that.getEnd()).getLength() > GameConfig.SAFETY_RADIUS;
    }
    public boolean intersectionSafe(FlightPathModel that) {

        if (this.getEndType().equals(END_SETTER.program)) {
            logger.info("warning: this destination was not set by the user");
        }

        //isolate destinations
        Point2D.Double dest1 = this.getEnd();
        Point2D.Double dest2 = that.getEnd();

        //definitely safe if there is a no bad intersection
        if (!this.badIntersection(that)) return true;

        //definitely safe if aimed at a shared destination
        return dest1 == dest2;

        //otherwise, assume to be unsafe

    }


}
