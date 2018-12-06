package net.robotics.communication;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

public class RobotEnv extends Environment {
	
    public static final int GSize = 6; // The bay is a 6x6 grid
    public static final int HOSPITAL  = 8; // hospital code in grid model
    public static final int VICTIM  = 16; // victim code in grid model
    public static final int ROBOT = 2; // robot code in grid model 
    
    private ArrayList<Location> victims = new ArrayList<Location>();
    private ArrayList<Location> toRescue = new ArrayList<Location>();

    private Logger logger = Logger.getLogger("doctorParamedicConfig."+RobotEnv.class.getName());
    
    public static final Term foundV = Literal.parseLiteral("next(victim)");
    //public static final Term foundV = Literal.parseLiteral("rescue(X,Y)");
    
    //search - victimplan - rescue - requestvictimstatus() - startmission
    //
    
    // Create objects for visualising the bay.  
    // This is based on the Cleaning Robots code.
    
    PCWindow pc;
    
    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        //addPercept(ASSyntax.parseLiteral("percept(demo)"));
        
		pc = new PCWindow("Robotics Assignment 2");
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        try {
        	if (action.getFunctor().equals("addVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                //MAP ADD VICTIM
                //model.addVictim(x,y);
                Location loc1 = new Location(x,y);
                victims.add(loc1);
                logger.info("adding victim at: "+x+","+y);
            } else if (action.getFunctor().equals("removeVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                //MAP REMOVE VICTIM
                //model.removeVictim(x,y);
                victims.remove(0);
                logger.info("removing victim at: "+x+","+y);
            } else if (action.getFunctor().equals("addToBeRescued")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                //MAP TO BE RESCUED
                //model.addToBeRescued(x,y);
                Location loc1 = new Location(x,y);
                toRescue.add(loc1);
                logger.info("removing victim at: "+x+","+y);
            } else if (action.getFunctor().equals("addObstacle")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                //MAP ADD OBSTACLE
                //model.addObstacle(x,y);
                logger.info("adding obstacle at: "+x+","+y);
            } else if (action.getFunctor().equals("addHospital")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                //MAP ADD HOSPITAL
                //model.addHospital(x,y);
                logger.info("adding hospital at: "+x+","+y);
            } else if (action.getFunctor().equals("addRobot")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                //SET AG POS??
                //ADD ROBOT
                //model.setAgPos(0, x, y);
                //model.addRobot(x,y);
                logger.info("adding robot at: "+x+","+y);
            } else if (action.getFunctor().equals("goHome")) {
            	// Move robot to hospital square, and run "stop" code. Signify that you have finished.
            	moveTo(0,0);
            	logger.info("executing: "+action+", going home (hospital)");
            	
            } else if (action.getFunctor().equals("goHospital")) {
                // Move robot to hospital square
            	moveTo(0,0);
            	logger.info("executing: "+action+", going to hospital");
            	
            } else if (action.getFunctor().equals("takeVictim")) {
            	// Assign victim location to robot location
            	// Display colour signifying that a victim is being carried
            	logger.info("executing: "+action+", picking up victim");
            	takeVictim();
            	
            } else if (action.getFunctor().equals("nextVictim")) {
            	Location loc1 = victims.get(0);
            	int x = loc1.x;
            	int y = loc1.y;
            	moveTo(x,y);
            	logger.info("executing: "+action+", going to next victim!(changed)");
            	
            } else if (action.getFunctor().equals("dropVictim")) {
            	// Unassign victim location from robot location
            	// Display colour signifying that a victim is no longer being carried
            	logger.info("executing: "+action+", dropping victim at hospital");
            	dropVictim();
            	
            } else if (action.getFunctor().equals("perceiveColour")) {
            	// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method. 
            	updatePercepts();
            	logger.info("executing: "+action+", perceiving colour!");
            } else if (action.getFunctor().equals("nextToBeRescued")) {
            	// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.
            	Location loc1 = toRescue.get(0);
            	int x = loc1.x;
            	int y = loc1.y;
            	moveTo(x,y);
            	logger.info("executing: "+action+", but not implemented!");
            }
            /*
            else if (action.getFunctor().equals("nextVictim")) {
                model.moveTo(x,y);
            } else if ((action.getFunctor().equals("savePerceptBelief")) {
                model.updatePercepts();
            }
            */
            
             else {
                logger.info("executing: "+action.getFunctor()+", but not implemented!");
                return true;
                // Note that technically we should return false here.  But that could lead to the
                // following Jason error (for example):
                // [ParamedicEnv] executing: addObstacle(2,2), but not implemented!
                // [paramedic] Could not finish intention: intention 6: 
                //    +location(obstacle,2,2)[source(doctor)] <- ... addObstacle(X,Y) / {X=2, Y=2, D=doctor}
                // This is due to the action failing, and there being no alternative.
                //next(victim) adding(percept)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
           
        informAgsEnvironmentChanged();
        return true;       
    }
    
    
    
    
    
    
    
   // needs to be configurred for the paramedic agent
    void updatePercepts() {
      clearPercepts();
      	/*
        Location paramedic = model.getAgPos(0);
        Literal pos1 = Literal.parseLiteral("location(r," + paramedic.x + "," + paramedic.y + ")");
        // String colourSensed = NEED TO ADD COLOUR SENSED HERE. But only actually sense a new colour when the method for sensing is called. 
        // Literal colour = Literal.parseLiteral("colour(" + paramedic.x + "," + paramedic.y + "," + colourSensed + ")");
        // addPercept(colour)
        model.perceiveColor();
        addPercept(pos1);    
        */  
    }
    
    void moveTo(int x, int y) {
    	/*
    	Location rLoc = model.getAgPos(0);
    	int rx = rLoc.x;
    	int ry = rLoc.y;
    	model.removeRobot(rx, ry);
    	model.setAgPos(0,x,y);
    	*/
    	updatePercepts();
    	//if (currentRobotLocation.x < x) {currentRobotLocation.x = x;}
    	//else if (currentRobotLocation.x > x){currentRobotLocation.x--;}
    	//If (currentRobotLocation.y < y) {currentRobotLocation.y++;}
    	//else (currentRobotLocation.y > y){currentRobotLocation.y--;}
    }
    
   void takeVictim() {
    	// Switch light on to say we are carrying victim
    }
    
   void dropVictim() {
    	// Switch light off to say we are not carrying a victim
    }
    

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
