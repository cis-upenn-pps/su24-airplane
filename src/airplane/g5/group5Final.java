package airplane.g5;

import airplane.sim.Plane;
import airplane.sim.Player;
import airplane.sim.SimulationResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class group5Final extends Player {
    private Logger logger = Logger.getLogger(this.getClass()); // for logging
    int simulationRuns=0;

    @Override
    public String getName() {
        return "player delay group5Final";
    }

    @Override
    public void startNewGame(ArrayList<Plane> planes) {
        logger.info("Starting new game!");
    }

    @Override
    public double[] updatePlanes(ArrayList<Plane> planes, int round, double[] bearings) {

        loop:
        for (int i=0;i<planes.size();i++){
            if(round>=planes.get(i).getDepartureTime()){
                Plane curPlane=planes.get(i);
                if(curPlane.getBearing()==-1){
                    bearings[i] = calculateBearing(curPlane.getLocation(), curPlane.getDestination());
                    planes.get(i).setBearing(bearings[i]);
                    SimulationResult checkSimulation = this.startSimulation(planes,round);
                    if(checkSimulation.getReason()==4){
                        bearings[i] = -1;
                        planes.get(i).setBearing(bearings[i]);
                    }
                }
            }
        }

        if (planes.size()==1){return bearings;}

        return bearings;
    }

    public double distance(Plane plane1, Plane plane2){
        double out = plane1.getLocation().distance(plane2.getLocation());
        return out;
    }

    @Override
    protected double[] simulateUpdate(ArrayList<Plane> planes, int round, double[] bearings) {
        /*for (int i=0;i<planes.size();i++){
            Plane curPlane = planes.get(i);
            if(curPlane.getLocation().equals(curPlane.getDestination())){
                curPlane.setBearing(-2);
                bearings[i]=-2;
            }
        }*/
        simulationRuns+=1;
        if (simulationRuns>=10000){
            simulationRuns=0;
            stopSimulation();
        }
        return bearings;
    }

    private boolean checkCollision(int pos, ArrayList<Plane> planes){
        for(int i=0;i<planes.size();i++){
            if(i==pos){continue;}
            if(planes.get(i).getBearing()==-1 || planes.get(i).getBearing()==-2){continue;}
            if(planes.get(i).getLocation().distance(planes.get(pos).getLocation())<=5){
                return true;
            }
        }
        return false;
    }


}
