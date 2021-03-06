import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.logging.*;


public class ParamedicEnv extends Environment {
	
    public static final int GSize = 6; // The bay is a 6x6 grid
    public static final int HOSPITAL  = 8; // hospital code in grid model
    public static final int VICTIM  = 16; // victim code in grid model
    public static final int ROBOT = 2; // robot code in grid model 
    
    private ArrayList<Location> victims = new ArrayList<Location>();
    private ArrayList<Location> toRescue = new ArrayList<Location>();

    private Logger logger = Logger.getLogger("doctorParamedicConfig."+ParamedicEnv.class.getName());
    
    public static final Term foundV = Literal.parseLiteral("next(victim)");
    //public static final Term foundV = Literal.parseLiteral("rescue(X,Y)");
    
    //search - victimplan - rescue - requestvictimstatus() - startmission
    //
    
    // Create objects for visualising the bay.  
    // This is based on the Cleaning Robots code.
    private RobotBayModel model;
    private RobotBayView view;    

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        //addPercept(ASSyntax.parseLiteral("percept(demo)"));
        model = new RobotBayModel();
        view  = new RobotBayView(model);
        model.setView(view);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        try {
        	if (action.getFunctor().equals("addVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addVictim(x,y);
                Location loc1 = new Location(x,y);
                victims.add(loc1);
                logger.info("adding victim at: "+x+","+y);
            } else if (action.getFunctor().equals("removeVictim")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.removeVictim(x,y);
                victims.remove(0);
                logger.info("removing victim at: "+x+","+y);
            } else if (action.getFunctor().equals("addToBeRescued")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addToBeRescued(x,y);
                Location loc1 = new Location(x,y);
                toRescue.add(loc1);
                logger.info("removing victim at: "+x+","+y);
            } else if (action.getFunctor().equals("addObstacle")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addObstacle(x,y);
                logger.info("adding obstacle at: "+x+","+y);
            } else if (action.getFunctor().equals("addHospital")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.addHospital(x,y);
                logger.info("adding hospital at: "+x+","+y);
            } else if (action.getFunctor().equals("addRobot")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.setAgPos(0, x, y);
                model.addRobot(x,y);
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
            	logger.info("executing: "+action+", going to victim!");
            } else if (action.getFunctor().equals("removeToBeRescued")) {
            	// I'm not sure if we should have the method to perceive colour situated OUTSIDE of the updatePercepts method.
            	toRescue.remove(0);
            	logger.info("executing: "+action+", removing toberescued");
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

        Location paramedic = model.getAgPos(0);
        Literal pos1 = Literal.parseLiteral("location(r," + paramedic.x + "," + paramedic.y + ")");
        // String colourSensed = NEED TO ADD COLOUR SENSED HERE. But only actually sense a new colour when the method for sensing is called. 
        // Literal colour = Literal.parseLiteral("colour(" + paramedic.x + "," + paramedic.y + "," + colourSensed + ")");
        // addPercept(colour)
        model.perceiveColor();
        addPercept(pos1);       
    }
    
    void moveTo(int x, int y) {
    	Location rLoc = model.getAgPos(0);
    	int rx = rLoc.x;
    	int ry = rLoc.y;
    	model.removeRobot(rx, ry);
    	model.setAgPos(0,x,y);
    	
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
    
    // ======================================================================
    class RobotBayModel extends GridWorldModel {

        private RobotBayModel() {
            super(GSize, GSize, 1);	
            // Initial position of robot.
        }
        
        void addToBeRescued(int x, int y) {
			// TODO Auto-generated method stub
		}

		void addVictim(int x, int y) {
            add(VICTIM, x, y);
        }
        void removeVictim(int x, int y) {
        	remove(VICTIM, x, y);
        }
        void addHospital(int x, int y) {
            add(HOSPITAL, x, y);
        }
        void addObstacle(int x, int y) {
            add(OBSTACLE, x, y);
        }
        void addRobot(int x, int y) {
            add(ROBOT, x, y);
        }
        void removeRobot(int x, int y) {
            remove(ROBOT, x, y);
        }
        
        
        
       // this is a test method that goes through the 5 scenarios of scanning colors of 5 possible victim locations and adding a percept of what it percieves
       void perceiveColor() {	   
    	   //to add test if statements with each loction of victims and returning a string of said color
    	   Location l1= new Location(2,2);
    	   Location l2= new Location(4,5);
    	   Location l3= new Location(5,1);
    	   
    	   Literal col;
    	   int rx = model.getAgPos(0).x;
    	   int ry = model.getAgPos(0).y;
    	   
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
        
        
        
    }
    // ======================================================================
    // This is a simple rendering of the map from the actions of the paramedic
    // when getting details of the victim and obstacle locations
    // You should not feel that you should use this code, but it can be used to
    // visualise the bay layout, especially in the early parts of your solution.
    // However, you should implement your own code to visualise the map.
        
    class RobotBayView extends GridWorldView {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public RobotBayView(RobotBayModel model) {
            super(model, "COMP329 6x6 Robot Bay", 300);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }
        
        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            case ParamedicEnv.VICTIM:
                drawVictim(g, x, y);
                break;
            case ParamedicEnv.HOSPITAL:
                drawHospital(g, x, y);
                break;
            case ParamedicEnv.ROBOT:
                drawRobot(g, x, y);
                break;
           }
        }
        
        public void drawVictim(Graphics g, int x, int y) {
            //super.drawObstacle(g, x, y);
            g.setColor(Color.black);
            drawString(g, x, y, defaultFont, "V");
        }

        public void drawHospital(Graphics g, int x, int y) {
            //super.drawObstacle(g, x, y);
            g.setColor(Color.blue);
            drawString(g, x, y, defaultFont, "H");
        }
        
        public void drawRobot(Graphics g, int x, int y) {
            //super.drawObstacle(g, x, y);
            g.setColor(Color.red);
            drawString(g, x, y, defaultFont, "R");
        }
        
        
    }
}
    // ======================================================================
