package net.robotics.communication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.robotics.communication.Tile.TileType;

public class PCParticlePoseProvider {
	private ParticleSet tSet;

	private KnownMap map;
	private int xCells;
	private int yCells;
	private boolean debugMode = true;

	Random rand = new Random();

	private boolean moved;
	private boolean[] objsAround;
	private TileType tileType;


	// Random pose of x, y and heading
	private int[] poseChange = new int[3];

	public PCParticlePoseProvider(KnownMap map) {
		tSet = new ParticleSet(map.getWidth(), map.getHeight());

		this.map = map;
		this.xCells = map.getWidth();
		this.yCells = map.getHeight();

		ArrayList<Particle> toRemove = new ArrayList<Particle>();
		for (int i = 0; i < tSet.getParticleSet().size(); i++) {	
			Particle currParticle = tSet.getParticle(i);
			if (!map.isPointIn(currParticle.getX(), currParticle.getY()) || map.isObstacle(currParticle.getX(), currParticle.getY())) {
				toRemove.add(currParticle);
			}	
		}
		tSet.getParticleSet().removeAll(toRemove);

		poseChange[0] = 0;
		poseChange[1] = 0;
		poseChange[2] = 0;
		
		/*sense();
		
		cull();
		
		printRemainingParticles();*/

	}

	/*public void localise() {
		printI("Localise");
		while (!goodEstimate()) {
			mCL();
		}
	}*/


	public boolean goodEstimate() {
		if (tSet.getParticleSet().size() == 1) {
			printI("Good estimate found!");
			printI("x:" + Integer.toString(tSet.getParticle(0).getX()));
			printI("y:" + Integer.toString(tSet.getParticle(0).getY()));
			printI("h:" + Integer.toString(tSet.getParticle(0).getHeading()));
			
			return true;
		} else if (tSet.getParticleSet().size() == 0){
			printI("Good estimate not found");
			return false;
		} else {
			return false;
		}
	}

	
	private void cull(){
		
		ArrayList<Particle> toRemove = new ArrayList<Particle>();
		for (int i = 0; i < tSet.getParticleSet().size(); i++) {
			Particle currParticle = tSet.getParticle(i);
			int x = currParticle.getX();
			int y = currParticle.getY();
			int h = currParticle.getHeading();
			
			boolean right = objectInFrontInMap(x,y,(h + 1) % 4) == objsAround[0] || true; // RIGHT
			boolean infront = objectInFrontInMap(x,y,h) == objsAround[1]; // INFRONT
			boolean left = objectInFrontInMap(x,y,(h + 3) % 4) == objsAround[2] || true; // LEFT

			if (!(right && infront && left)) {
				toRemove.add(currParticle);
			}
			
			TileType particleTileType = map.getTile(x, y).getType();
			
			if(particleTileType != tileType && !(particleTileType == TileType.Victim && tileType == TileType.Empty)){
				printI("CULLING: " + x + ", " + y + ", for " + map.getTile(x, y).getType()  + " != " + tileType);
				toRemove.add(currParticle);
			}
		}
		tSet.getParticleSet().removeAll(toRemove);
	}

	private void moveUpdate(){
		// Then generate new samples
		// And remove incompatible particle states
		
		ArrayList<Particle> toRemove = new ArrayList<Particle>();
		
		for (int i = 0; i < tSet.getParticleSet().size(); i++) {
			Particle currParticle = tSet.getParticle(i);

			//double prob = currParticle.getWeight();
			
			currParticle.setHeading(addHeadings(currParticle.getHeading(), poseChange[2]));
			
			if(moved){
				if (currParticle.getHeading() == 0) {
					poseChange[0] = 0;
					poseChange[1] = 1;
				} else if (currParticle.getHeading() == 1) {
					poseChange[0] = 1;
					poseChange[1] = 0;
				} else if (currParticle.getHeading() == 2) {
					poseChange[0] = 0;
					poseChange[1] = -1;
				} else {
					poseChange[0] = -1;
					poseChange[1] = 0;
				}
			} else {
				poseChange[0] = 0;
				poseChange[1] = 0;
			}

			currParticle.setX(currParticle.getX() + poseChange[0]);
			currParticle.setY(currParticle.getY() + poseChange[1]);

			if (!map.isPointIn(currParticle.getX(), currParticle.getY()) || map.isObstacle(currParticle.getX(), currParticle.getY())) {
				//printI("CULLING: " + x + ", " + y + ", for " + map.getTile(x, y).getType()  + " != " + tileType);
				toRemove.add(currParticle);
			}
		}
		tSet.getParticleSet().removeAll(toRemove);
	}
	
	public Particle getLocation(){
		if(!goodEstimate())
			return null;
		
		return tSet.getParticle(0);
	}

	// This MCL is not currently probabilistic. It just eliminates incompatible states. 
	public boolean mCL(PCComms pc) {
		move(pc);

		moveUpdate();
		
		printRemainingParticles();
		// Sense
		sense(pc);
		// Then eliminate incompatible particle states. 
		cull();

		printRemainingParticles();
		
		return goodEstimate();
	}

	private void printRemainingParticles() {

		for (int i = 0; i < tSet.getParticleSet().size(); i++) {
			Particle currParticle = tSet.getParticle(i);

			String sI = Integer.toString(i);
			String x = Integer.toString(currParticle.getX());
			String y = Integer.toString(currParticle.getY());
			String h = Integer.toString(currParticle.getHeading());
			String t = map.getTile(currParticle.getX(), currParticle.getY()).getType().toString();


			printI("NO:" + sI + " x:" + x + " y:" + y + " h:" + h + " tt: " + t);
		}
	}

	private void printParticle(Particle particle) {
		String x = Integer.toString(particle.getX());
		String y = Integer.toString(particle.getY());
		String h = Integer.toString(particle.getHeading());
		printI("x:" + x + " y:" + y + " h:" + h);
	}


	private void move(PCComms pc) {
		
		printI("Move");
		iceSlide(pc);
		//randomRotate(pc);
		//moveForward(pc);
	}
	
	private void iceSlide(PCComms pc) {
		
		poseChange[2] = 0;
		
		if (!(getIRSensorLocObstace(pc, 0) < 24f)) {
			pc.sendCommand("IMOVE");


			while(!pc.isMoveSuccess()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			pc.clearLastMessage();
			System.out.println("OUTSUCCESS ");
			
			moved = true;
		} else {

			int heading = ThreadLocalRandom.current().nextInt(1, 2 + 1);
			
			if(heading == 2){
				heading += 1;
			}
			
			System.out.println("Rotating: " + heading);
			
			rotate(pc, heading);
			
			moved = false;
		}
		
	}
	
	private void rotate(PCComms pc, int heading){
		pc.sendCommand("ITURNTO " + heading);
		
		poseChange[2] = heading;


		while(!pc.isTurnSuccess()){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		pc.clearLastMessage();
		System.out.println("OUTSUCCESS ");
	}

	private void randomRotate(PCComms pc) {
		int heading = ThreadLocalRandom.current().nextInt(0, 3 + 1);
		
		rotate(pc, heading);
	}

	private void moveForward(PCComms pc) {

		if (!(getIRSensorLocObstace(pc, 0) < 24f)) {
			
			pc.sendCommand("IMOVE");


			while(!pc.isMoveSuccess()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			pc.clearLastMessage();
			System.out.println("OUTSUCCESS ");
			
			moved = true;
			
		} else {
			moved = false;
		}
	}

	private void sense(PCComms pc) {
		printI("Sense");
		
		pc.sendCommand("GETCOLOR");

		String isColor = pc.getColor();

		while(isColor.isEmpty()){

			isColor = pc.getColor();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(isColor.equalsIgnoreCase("GREEN")){
			printI("GREEN Recognised");
			tileType = TileType.GREENTILE;
		} else if(isColor.equalsIgnoreCase("YELLOW")){
			printI("Yellow Recognised");
			tileType = TileType.Hospital;
		} else if(isColor.equalsIgnoreCase("CYAN") || isColor.equalsIgnoreCase("BURGANDY")){
			printI("CYAN OR BURGANDY Recognised");
			tileType = TileType.Victim;
		} else {
			printI(isColor + " not Recognised");
			tileType = TileType.Empty;
		}

		objsAround = isObjectAroundRobot(pc);
	}
	
	private float getIRSensorLocObstace(PCComms pc, int h){
		pc.sendCommand("GETDIST " + h);

		float irDist = pc.getIrDist();
		
		while(irDist == -1){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			irDist = pc.getIrDist();
		}

		pc.clearLastMessage();
		System.out.println("OUTSUCCESS ");
		
		return irDist;
	}
	
	private boolean[] isObjectAroundRobot(PCComms pc){
		boolean[] objAround = new boolean[3];
		objAround[0] = getIRSensorLocObstace(pc, 1) < 24f; //RIGHT
		objAround[1] = getIRSensorLocObstace(pc, 0) < 24f; //FORWARD
		objAround[2] = getIRSensorLocObstace(pc, 3) < 24f; //LEFT
		
		return objAround;
	}

	private boolean objectInFrontInMap(int x, int y, int h) {	
		int nX;
		int nY;

		if (h == 0) {
			nX = x;
			nY = y+1;
		} else if (h == 1) {
			nX = x+1;
			nY = y;
		} else if (h == 2) {
			nX = x;
			nY = y-1;
		} else {
			nX = x-1;
			nY = y;
		}
		if (nX >= 0 && nX < xCells && nY >=0 && nY < yCells) {
			if (map.isObstacle(nX, nY)) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	public int addHeadings(int h1, int h2) {
		int sum = h1 + h2;
		return sum % 4;
	}

	private void printI(String x) {
		if (debugMode == true) {
			System.out.println(x);
		}
	}

	public ArrayList<Particle> getParticles() {
		return tSet.getParticleSet();
	}
}
