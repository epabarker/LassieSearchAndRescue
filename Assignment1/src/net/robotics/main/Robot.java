package net.robotics.main;

import java.util.Dictionary;
import java.util.HashMap;

import org.omg.PortableInterceptor.PolicyFactoryOperations;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.internal.ev3.EV3LED;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;
import net.robotics.map.Map;
import net.robotics.screen.LCDRenderer;
import net.robotics.sensor.ColorSensorMonitor;
import net.robotics.sensor.ColorSensorMonitor.ColorNames;
import net.robotics.sensor.UltrasonicSensorMonitor;

public class Robot {



	/*private static RGBFloat GREEN = new RGBFloat(new RGBFloat(0f, 0.19f, 0f), new RGBFloat(0.15f, 0.25f, 0.15f));
	private static RGBFloat WHITE = new RGBFloat(new RGBFloat(0.22f, 0.22f, 0.22f), new RGBFloat(0.3f, 0.3f, 0.3f));
	private static RGBFloat BLACK = new RGBFloat(new RGBFloat(0.00f, 0.00f, 0.00f), new RGBFloat(0.05f, 0.05f, 0.05f));
	private static RGBFloat BLUE = new RGBFloat(new RGBFloat(0.00f, 0.00f, 0.05f), new RGBFloat(0.075f, 0.055f, 0.1f));
	private static RGBFloat YELLOW = new RGBFloat(new RGBFloat(0.25f, 0.15f, 0.00f), new RGBFloat(0.30f, 0.20f, 0.1f));
	private static RGBFloat RED = new RGBFloat(new RGBFloat(0.20f, 0.00f, 0.00f), new RGBFloat(0.25f, 0.05f, 0.075f));*/

	public LCDRenderer screen;
	private ColorSensorMonitor colorSensor;
	private UltrasonicSensorMonitor ultrasonicSensor;
	private EV3LED led;
	private MovePilot pilot;
	private OdometryPoseProvider opp;
	private Map map;

	public static void main(String[] args){
		Robot cs = new Robot();

		cs.mainLoop();

		cs.closeRobot();
	}

	public Robot() {
		Brick myEV3 = BrickFinder.getDefault();

		led = (EV3LED) myEV3.getLED();

		screen = new LCDRenderer(LocalEV3.get().getGraphicsLCD());

		colorSensor = new ColorSensorMonitor(this, new EV3ColorSensor(myEV3.getPort("S2")), 10);
		
		NXTRegulatedMotor motor = null;
		boolean uninit = false;
		while(!uninit){
			try {
				motor = Motor.C;
			} finally {
				uninit = true;
			}
		}
		
		
		ultrasonicSensor = new UltrasonicSensorMonitor(this, 
				new EV3UltrasonicSensor(myEV3.getPort("S3")), 
				motor, 100);
		
		setUpRobot();

		//screen.writeTo(new String[]{"Alive? "+(colorSensor != null)});

		this.map = new Map(6, 7);

		colorSensor.configure(true);
		colorSensor.start();


	}

	private void setUpRobot(){
		pilot = ChasConfig.getPilot();

		// Create a pose provider and link it to the move pilot
		opp = new OdometryPoseProvider(pilot);
	}

	public void closeProgram(){
		closeRobot();
		System.exit(0);
	}

	public void mainLoop(){
		int squares = 0;
		ColorNames prevColor = colorSensor.getColor();

		pilot.setLinearSpeed(10);

		screen.clearScreen();
		screen.drawMap(screen.getWidth()-8-map.getWidth()*16, 8, map);

		while(!Button.ESCAPE.isDown() && squares < 6){
			screen.clearScreen();
			if(map.canMove(map.getRobotX(), map.getRobotY()+1)){
				
				boolean F = ultrasonicSensor.isObjectDirectInFront();
				boolean L = ultrasonicSensor.rotate(90).isObjectDirectInFront();
				boolean R = ultrasonicSensor.rotate(-180).isObjectDirectInFront();
				
				map.updateTile(map.getRobotX(), map.getRobotY(), F);
				map.updateTile(map.getRobotX()-1, map.getRobotY(), L);
				map.updateTile(map.getRobotX()+1, map.getRobotY(), R);
				
				screen.drawMap(screen.getWidth()-8-map.getWidth()*16, 8, map);
				screen.writeTo(new String[]{
						"F: " + F,
						"L: " + L,
						"R: " + R
				}, 0, 60, GraphicsLCD.LEFT, Font.getDefaultFont());
				
				ultrasonicSensor.resetMotor();				
				
				MoveSquares(1);
				map.moveRobotPos(0, 1);
				squares++;
				//pilot.rotate(90);
			}
			
			

			

			//screen.clearScreen();
			//screen.writeTo(new String[]{"F? "+(colorSensor.getColorFrequency() != null)});



			/*if(colorSensor.getCurrentColor() == ColorNames.BLACK && prevColor != ColorNames.BLACK){
				squares++;
			}

			if(colorSensor.getCurrentColor() != prevColor){
				prevColor = colorSensor.getCurrentColor();
			}

			screen.clearScreen();
			screen.writeTo(new String[]{"Passed through "+squares+" squares.",
					"Color. " + colorSensor.getCurrentColor(),
					"Previous. " + prevColor
					}, screen.getWidth()/2, 0, GraphicsLCD.HCENTER, Font.getSmallFont());
			screen.drawEscapeButton("QUIT", 0, 100, 45, 45/2, 6);
			*/

			Button.waitForAnyPress();
		}
	}

	private void MoveSquares(int i){
		for (int j = 0; j < i; j++) {
			pilot.forward();
			ColorNames cn;
			do{
				cn = colorSensor.getCurrentColor();
			}while(cn != ColorNames.BLACK);
			pilot.travel(10f);
		}
	}

	public LCDRenderer getScreen(){
		return screen;
	}

	public void closeRobot(){
	}
}