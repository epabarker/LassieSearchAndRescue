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
import net.robotics.communication.Tile.TileType;

public class RobotEnv extends Environment {

	public static final int GSize = 6; // The bay is a 6x6 grid
	public static final int HOSPITAL  = 8; // hospital code in grid model
	public static final int VICTIM  = 16; // victim code in grid model
	public static final int ROBOT = 2; // robot code in grid model 

	private ArrayList<Location> victims = new ArrayList<Location>();
	private ArrayList<Location> toRescue = new ArrayList<Location>();
	
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
				pc.getMap().updateMap(TileType.Victim, x, y);
				Location loc1 = new Location(x,y);
				victims.add(loc1);
				System.out.println("adding victim at: "+x+","+y);
			} else if (action.getFunctor().equals("removeVictim")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				pc.getMap().updateMap(TileType.Empty, x, y);
				victims.remove(0);
				System.out.println("removing victim at: "+x+","+y);
			} else if (action.getFunctor().equals("addToBeRescued")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				pc.getMap().updateMap(TileType.NONCRITICAL, x, y);
				Location loc1 = new Location(x,y);
				toRescue.add(loc1);
				System.out.println("removing victim at: "+x+","+y);
			} else if (action.getFunctor().equals("addObstacle")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				pc.getMap().updateMap(TileType.OBSTACLE, x, y);
				System.out.println("adding obstacle at: "+x+","+y);
			} else if (action.getFunctor().equals("addHospital")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				pc.getMap().updateMap(TileType.Hospital, x, y);
				System.out.println("adding hospital at: "+x+","+y);
			} else if (action.getFunctor().equals("addRobot")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				//KNOWN MAP ADD ROBOT POSITION
				//model.setAgPos(0, x, y);
				//model.addRobot(x,y);
				
				pc.getRobotInfoPanel().getRobotInfo().setPos(x, y);
				pc.getMap().updateRobotPosition(x, y);
				System.out.println("adding robot at: "+x+","+y);
			} else if (action.getFunctor().equals("goHome")) {
				// Move robot to hospital square, and run "stop" code. Signify that you have finished.
				moveTo(0,0);
				System.out.println("executing: "+action+", going home (hospital)");

			} else if (action.getFunctor().equals("goHospital")) {
				// Move robot to hospital square
				moveTo(0,0);
				System.out.println("executing: "+action+", going to hospital");

			} else if (action.getFunctor().equals("takeVictim")) {
				// Assign victim location to robot location
				// Display colour signifying that a victim is being carried
				System.out.println("executing: "+action+", picking up victim");
				takeVictim();

			} else if (action.getFunctor().equals("nextVictim")) {
				Location loc1 = victims.get(0);
				int x = loc1.x;
				int y = loc1.y;
				moveTo(x,y);
				System.out.println("executing: "+action+", going to next victim!(changed)");

			} else if (action.getFunctor().equals("dropVictim")) {
				// Unassign victim location from robot location
				// Display colour signifying that a victim is no longer being carried
				System.out.println("executing: "+action+", dropping victim at hospital");
				dropVictim();

			} else if (action.getFunctor().equals("perceiveColour")) {
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method. 
				updatePercepts();
				System.out.println("executing: "+action+", perceiving colour!");
			} else if (action.getFunctor().equals("nextToBeRescued")) {
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.
				Location loc1 = toRescue.get(0);
				int x = loc1.x;
				int y = loc1.y;
				moveTo(x,y);
				System.out.println("executing: "+action+", going to victim!");
			} else if (action.getFunctor().equals("removeToBeRescued")) {
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.
				toRescue.remove(0);
				System.out.println("executing: "+action+", removing toberescued");
			}
			/*
               else if (action.getFunctor().equals("nextVictim")) {
                   model.moveTo(x,y);
               } else if ((action.getFunctor().equals("savePerceptBelief")) {
                   model.updatePercepts();
               }
			 */

			else {
				System.out.println("executing: "+action.getFunctor()+", but not implemented!");
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
		
		int x = pc.getRobotInfoPanel().getRobotInfo().getX();
		int y = pc.getRobotInfoPanel().getRobotInfo().getY();
        Literal pos1 = Literal.parseLiteral("location(r," + x + "," + y + ")");
        perceiveColor();
        addPercept(pos1);
        
		/*
		 * PERCIEVE COLOR
		 *
        Location paramedic = model.getAgPos(0);
        Literal pos1 = Literal.parseLiteral("location(r," + paramedic.x + "," + paramedic.y + ")");
        model.perceiveColor();
        addPercept(pos1);    
		 */  
	}
	
	// this is a test method that goes through the 5 scenarios of scanning colors of 5 possible victim locations and adding a percept of what it percieves
    void perceiveColor() {	   
 	   //to add test if statements with each loction of victims and returning a string of said color
 	   Location l1= new Location(2,3);
 	   Location l2= new Location(4,5);
 	   Location l3= new Location(5,1);
 	   
 	   Literal col;
 	   int rx = pc.getRobotInfoPanel().getRobotInfo().getX();
 	   int ry = pc.getRobotInfoPanel().getRobotInfo().getY();
 	   
 	   if (rx == l1.x && ry == l1.y) {
 		   col = Literal.parseLiteral("colour("+rx+","+ry+",burgandy)");
 		   addPercept(col);
 	   } else if (rx == l2.x && ry == l2.y) {
 		   col = Literal.parseLiteral("colour("+rx+","+ry+",cyan)");
 		   addPercept(col);
 	   } else if (rx == l3.x && ry == l3.y) {
 		   col = Literal.parseLiteral("colour("+rx+","+ry+",cyan)");
 		   addPercept(col);
 	   } else {
 		   col = Literal.parseLiteral("colour("+rx+","+ry+",white)");
 		   addPercept(col);
 	   }

    }

	void moveTo(int x, int y) {
    	
		pc.getMap().updateRobotPosition(x, y);
    	pc.getRobotInfoPanel().getRobotInfo().setPos(x, y);
		 
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
