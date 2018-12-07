package net.robotics.communication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
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
			} else if (action.getFunctor().equals("removeVictim")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				System.out.println("removing victim at: "+x+","+y);

				pc.getMap().updateMap(TileType.Empty, x, y);
				removeVictim(x, y);
			} else if (action.getFunctor().equals("addToBeRescued")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				System.out.println("removing victim at: "+x+","+y);

				pc.getMap().updateMap(TileType.NONCRITICAL, x, y);
				Location loc1 = new Location(x,y);
				toRescue.add(loc1);
			} else if (action.getFunctor().equals("addObstacle")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				System.out.println("adding obstacle at: "+x+","+y);

				pc.getMap().updateMap(TileType.OBSTACLE, x, y);
			} else if (action.getFunctor().equals("addHospital")) {
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				System.out.println("adding hospital at: "+x+","+y);

				pc.getMap().updateMap(TileType.Hospital, x, y);
			} else if (action.getFunctor().equals("addRobot")) {
				
				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				System.out.println("adding robot at: "+x+","+y);


				pc.getRobotInfoPanel().getRobotInfo().setPos(x, y);
				pc.getMap().updateRobotPosition(x, y);
			} else if (action.getFunctor().equals("goHome")) {
				// Move robot to hospital square, and run "stop" code. Signify that you have finished.
				
				System.out.println("executing: "+action+", going home (hospital)");

				
				int x = pc.getRobotInfoPanel().getRobotInfo().getX();
				int y = pc.getRobotInfoPanel().getRobotInfo().getY();
				
				if(x != 0 && y != 0){
	
					LinkedList<Tile> travelPath = AStarSearch.getPath(pc.getMap().getMap(), 
							pc.getMap().getMap().getTile(x, y), 
							pc.getMap().getMap().getTile(0, 0));
	
					while(travelPath.size() > 0){
						Tile nextTile = travelPath.pop();
	
						System.out.println("Moving to: " + nextTile.getX() + "/" + nextTile.getY());
	
						moveTo(nextTile.getX(),nextTile.getY());
					}
				}

			} else if (action.getFunctor().equals("goHospital")) {
				// Move robot to hospital square
				System.out.println("Executing: "+action+", going to hospital");


				int x = pc.getRobotInfoPanel().getRobotInfo().getX();
				int y = pc.getRobotInfoPanel().getRobotInfo().getY();

				LinkedList<Tile> travelPath = AStarSearch.getPath(pc.getMap().getMap(), 
						pc.getMap().getMap().getTile(x, y), 
						pc.getMap().getMap().getTile(0, 0));

				while(travelPath.size() > 0){
					Tile nextTile = travelPath.pop();

					System.out.println("HOSPITAL: Moving to: " + nextTile.getX() + "/" + nextTile.getY());

					moveTo(nextTile.getX(),nextTile.getY());
				}

				moveTo(0,0);

			} else if (action.getFunctor().equals("takeVictim")) {
				// Assign victim location to robot location
				// Display colour signifying that a victim is being carried
				System.out.println("executing: "+action+", picking up victim");
				takeVictim();

			} else if (action.getFunctor().equals("nextVictim")) {
				System.out.println("Executing: "+action+", going to next victim!(changed)");

				LinkedList<Tile> travelPath = null;

				int x = pc.getRobotInfoPanel().getRobotInfo().getX();
				int y = pc.getRobotInfoPanel().getRobotInfo().getY();

				System.out.println("-----ASTAR STARTING-----");
				for (int i = 0; i < victims.size(); i++) {
					Location victimLoc = victims.get(i);


					LinkedList<Tile> path = AStarSearch.getPath(pc.getMap().getMap(), 
							pc.getMap().getMap().getTile(x, y), 
							pc.getMap().getMap().getTile(victimLoc.x, victimLoc.y));

					/*if(travelPath != null && path != null)
						System.out.println(i + "/" + travelPath.size() + "/" + path.size());
					else if(path != null)
						System.out.println(i + "/" + travelPath + "/" + path.size());
					else if(travelPath != null)
						System.out.println(i + "/" + travelPath.size() + "/" + path);*/

					if(travelPath == null && path != null){
						travelPath = path;
						continue;
					} else if(travelPath.size() > path.size() && path != null) {
						travelPath = path;
					}
				}
				System.out.println("PICKED GOAL: " + travelPath.peekLast().getX() + "/" + travelPath.peekLast().getY());
				System.out.println("-----ASTAR FINISH-----");

				while(travelPath.size() > 0){
					Tile nextTile = travelPath.pop();

					System.out.println("NEXT VICTIM: Moving to: " + nextTile.getX() + "/" + nextTile.getY());

					moveTo(nextTile.getX(),nextTile.getY());
				}
				
				
				

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
				System.out.println("executing: "+action+", going to victim!");
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.
				LinkedList<Tile> travelPath = null;

				int x = pc.getRobotInfoPanel().getRobotInfo().getX();
				int y = pc.getRobotInfoPanel().getRobotInfo().getY();

				System.out.println("-----ASTAR STARTING-----");
				for (int i = 0; i < toRescue.size(); i++) {
					Location victimLoc = toRescue.get(i);


					LinkedList<Tile> path = AStarSearch.getPath(pc.getMap().getMap(), 
							pc.getMap().getMap().getTile(x, y), 
							pc.getMap().getMap().getTile(victimLoc.x, victimLoc.y));

					/*if(travelPath != null && path != null)
						System.out.println(i + "/" + travelPath.size() + "/" + path.size());
					else if(path != null)
						System.out.println(i + "/" + travelPath + "/" + path.size());
					else if(travelPath != null)
						System.out.println(i + "/" + travelPath.size() + "/" + path);*/

					if(travelPath == null && path != null){
						travelPath = path;
						continue;
					} else if(travelPath.size() > path.size() && path != null) {
						travelPath = path;
					}
				}
				System.out.println("PICKED GOAL: " + travelPath.peekLast().getX() + "/" + travelPath.peekLast().getY());
				System.out.println("-----ASTAR FINISH-----");

				while(travelPath.size() > 0){
					Tile nextTile = travelPath.pop();

					System.out.println("Moving to: " + nextTile.getX() + "/" + nextTile.getY());

					moveTo(nextTile.getX(),nextTile.getY());
				}
			} else if (action.getFunctor().equals("removeToBeRescued")) {
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.

				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();
				
				removeToBeRescued(x, y);
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
		System.out.println("END ACTION --- " + action.getFunctor());
		return true;       
	}
	
	void removeToBeRescued(int x, int y){
		for (int i = 0; i < toRescue.size(); i++) {
			Location l = toRescue.get(i);
			if(l.x == x && l.y == y){
				toRescue.remove(i);
				return;
			}
		}
	}

	void removeVictim(int x, int y){
		for (int i = 0; i < victims.size(); i++) {
			Location l = victims.get(i);
			if(l.x == x && l.y == y){
				victims.remove(i);
				return;
			}
		}
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
		Location l1= new Location(2,0);
		Location l2= new Location(2,2);
		Location l3= new Location(5,4);

		Literal col;
		int rx = pc.getRobotInfoPanel().getRobotInfo().getX();
		int ry = pc.getRobotInfoPanel().getRobotInfo().getY();

		if (rx == l1.x && ry == l1.y) {
			col = Literal.parseLiteral("colour("+rx+","+ry+",cyan)");
			addPercept(col);
		} else if (rx == l2.x && ry == l2.y) {
			col = Literal.parseLiteral("colour("+rx+","+ry+",cyan)");
			addPercept(col);
		} else if (rx == l3.x && ry == l3.y) {
			col = Literal.parseLiteral("colour("+rx+","+ry+",burgandy)");
			addPercept(col);
		} else {
			col = Literal.parseLiteral("colour("+rx+","+ry+",white)");
			addPercept(col);
		}

	}

	void moveTo(int x, int y) {
		
		System.out.println("MOVE: " + x + ", " + y);

		pc.getMap().updateRobotPosition(x, y);
		pc.getRobotInfoPanel().getRobotInfo().setPos(x, y);

		updatePercepts();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
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