package net.robotics.communication;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
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

	private PCParticlePoseProvider mclLoc;

	private LinkedList<Tile> movePath = null;

	private Thread moveThread, mclThread;

	/** Called before the MAS execution with the args informed in .mas2j */
	@Override
	public void init(String[] args) {
		super.init(args);

		pc = new PCWindow("Robotics Assignment 2");

		mclLoc = new PCParticlePoseProvider(pc.getMap().getMap());

		moveThread = new Thread(){
			public void run() {
				super.run();
				for(;;){


					if(movePath == null){
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else if(movePath.size() > 0){
						Tile move = movePath.pop();
						moveTo(move.getX(), move.getY());
					} else {
						movePath = null;
					}

					
				}
			}		
		};

		moveThread.start();

		mclThread = new Thread(){
			public void run() {
				super.run();
				while (!mclLoc.goodEstimate()) {
					if(mclLoc.mCL(pc.getRobotInfoPanel().getPcComms())){
						Particle pos = mclLoc.getLocation();
						pc.getRobotInfoPanel().getRobotInfo().setPos(pos.getX(), pos.getY());
						pc.getRobotInfoPanel().getRobotInfo().setHeading(pos.getHeading());
						pc.getRobotInfoPanel().getRobotInfo().setLocationFound(true);
						break;
					}
				}
				
				pc.getRobotInfoPanel().getPcComms().sendCommand("CORRECTHEADING " + pc.getRobotInfoPanel().getRobotInfo().getHeading());
				
			}		
		};
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		try {
			if (action.getFunctor().equals("isConnected")) {
				if(pc.getRobotInfoPanel().isConnected()){
					Literal pos1 = Literal.parseLiteral("connected(yee)");
					addPercept(pos1);
				}
			} else if (action.getFunctor().equals("findLocation")) {
				if(!mclThread.isAlive() && !pc.getRobotInfoPanel().getRobotInfo().isLocationFound()){
					mclThread.start();
				} else if(pc.getRobotInfoPanel().getRobotInfo().isLocationFound()){
					updatePercepts();
				}
			} else if (action.getFunctor().equals("checkLocation")) {
				if(pc.getRobotInfoPanel().getRobotInfo().isLocationFound()){
					updatePercepts();
				}
			} else if (action.getFunctor().equals("addVictim")) {
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

				createPathTo(0,0);


			} else if (action.getFunctor().equals("goHospital")) {
				// Move robot to hospital square
				System.out.println("Executing: "+action+", going to hospital");

				createPathTo(0,0);

			} else if (action.getFunctor().equals("takeVictim")) {
				// Assign victim location to robot location
				// Display colour signifying that a victim is being carried
				System.out.println("executing: "+action+", picking up victim");

				int victim = (int)((NumberTerm)action.getTerm(0)).solve();
				
				takeVictim(victim == 1	);

			} else if (action.getFunctor().equals("nextVictim")) {
				System.out.println("Executing: "+action+", going to next victim!(changed)");

				LinkedList<Tile> travelPath = null;

				int x = pc.getRobotInfoPanel().getRobotInfo().getX();
				int y = pc.getRobotInfoPanel().getRobotInfo().getY();

				System.out.println("-----ASTAR STARTING-----");
				System.out.println("Victims left... " + victims.size());


				for (int i = 0; i < victims.size(); i++) {
					Location victimLoc = victims.get(i);

					LinkedList<Tile> path = AStarSearch.getPath(pc.getMap().getMap(), 
							pc.getMap().getMap().getTile(x, y), 
							pc.getMap().getMap().getTile(victimLoc.x, victimLoc.y));


					if(travelPath == null && path != null){
						travelPath = path;
						continue;
					} else if(travelPath.size() > path.size() && path != null) {
						travelPath = path;
					}
				}

				System.out.println("PICKED GOAL: " + travelPath.peekLast().getX() + "/" + travelPath.peekLast().getY());
				System.out.println("-----ASTAR FINISH-----");

				Literal pos1 = Literal.parseLiteral("target(" + travelPath.peekLast().getX() + "," + travelPath.peekLast().getY() + ")");
				addPercept(pos1);
				
				if(movePath == null)
					movePath = travelPath;
				else{
					System.out.println("CALLING MOVE WHILE PATH IS NOT EMPTY: NEXT VICTIM " + movePath.size() + " / " + movePath.peek().getX() + ", " +movePath.peekLast().getY());
					return false;
				}
			} else if (action.getFunctor().equals("dropVictim")) {
				// Unassign victim location from robot location
				// Display colour signifying that a victim is no longer being carried
				System.out.println("executing: "+action+", dropping victim at hospital");
				dropVictim();

			} else if (action.getFunctor().equals("perceiveColour")) {
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method. 

				updatePercepts();
				perceiveColor();

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

					if(travelPath == null && path != null){
						travelPath = path;
						continue;
					} else if(travelPath.size() > path.size() && path != null) {
						travelPath = path;
					}
				}
				System.out.println("PICKED GOAL: " + travelPath.peekLast().getX() + "/" + travelPath.peekLast().getY());
				System.out.println("-----ASTAR FINISH-----");

				Literal pos1 = Literal.parseLiteral("target(" + travelPath.peekLast().getX() + "," + travelPath.peekLast().getY() + ")");
				addPercept(pos1);

				if(movePath == null)
					movePath = travelPath;
				else {
					System.out.println("CALLING MOVE WHILE PATH IS NOT EMPTY: TO RESCUE VICTIM");
					return false;
				}


			} else if (action.getFunctor().equals("removeToBeRescued")) {
				// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.

				int x = (int)((NumberTerm)action.getTerm(0)).solve();
				int y = (int)((NumberTerm)action.getTerm(1)).solve();

				removeToBeRescued(x, y);
				System.out.println("executing: "+action+", removing toberescued");
			} else {
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
		addPercept(pos1);

	}

	// this is a test method that goes through the 5 scenarios of scanning colors of 5 possible victim locations and adding a percept of what it percieves
	void perceiveColor() {	   
		//to add test if statements with each loction of victims and returning a string of said color
		/*Location l1= new Location(2,0);
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
		}*/

		pc.getRobotInfoPanel().getPcComms().sendCommand("GETCOLOR");

		String isColor = pc.getRobotInfoPanel().getPcComms().getColor();

		while(isColor.isEmpty()){

			isColor = pc.getRobotInfoPanel().getPcComms().getColor();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Literal col;
		int rx = pc.getRobotInfoPanel().getRobotInfo().getX();
		int ry = pc.getRobotInfoPanel().getRobotInfo().getY();

		System.out.println("colour("+rx+","+ry+"," + pc.getRobotInfoPanel().getPcComms().getColor().toLowerCase() + ")");

		col = Literal.parseLiteral("colour("+rx+","+ry+"," + pc.getRobotInfoPanel().getPcComms().getColor().toLowerCase() + ")");


		addPercept(col);
	}

	void move(int heading){
		pc.getRobotInfoPanel().getPcComms().sendCommand("MOVE " + heading);


		while(!pc.getRobotInfoPanel().getPcComms().isMoveSuccess()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		pc.getRobotInfoPanel().getPcComms().clearLastMessage();
		System.out.println("OUTSUCCESS ");
	}

	void moveTo(int x, int y) {

		System.out.println("MOVE: " + x + ", " + y);

		int dx = pc.getRobotInfoPanel().getRobotInfo().getX() - x;
		int dy = pc.getRobotInfoPanel().getRobotInfo().getY() - y;

		int cardinalHeading = 0;

		if(dx < 0){
			cardinalHeading = 1;
		} else if(dx > 0){
			cardinalHeading = 3;
		} else if(dy > 0){
			cardinalHeading = 2;
		}

		if(!(dx == 0 && dy == 0)){
			move(cardinalHeading);
		}

		pc.getRobotInfoPanel().getRobotInfo().setPos(x, y);
		pc.getMap().updateRobotPosition(x, y);
	}

	private void createPathTo(int tx, int ty){
		int x = pc.getRobotInfoPanel().getRobotInfo().getX();
		int y = pc.getRobotInfoPanel().getRobotInfo().getY();


		LinkedList<Tile> travelPath = AStarSearch.getPath(pc.getMap().getMap(), 
				pc.getMap().getMap().getTile(x, y), 
				pc.getMap().getMap().getTile(tx, ty));

		if(movePath == null)
			movePath = travelPath;
		else{
			System.out.println("CALLING MOVE WHILE PATH IS NOT EMPTY: " + tx + "," + ty);
		}

		Literal pos1 = Literal.parseLiteral("target("+tx+","+ty+")");
		addPercept(pos1);
	}

	void takeVictim(boolean victim) {
		pc.getRobotInfoPanel().getPcComms().sendCommand("PICKUP " + (victim ? "true" : "false"));
		// Switch light on to say we are carrying victim
	}

	void dropVictim() {
		pc.getRobotInfoPanel().getPcComms().sendCommand("DROP");
		// Switch light off to say we are not carrying a victim
	}


	/** Called before the end of MAS execution */
	@Override
	public void stop() {
		super.stop();
	}
}
