package net.robotics.main;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.LED;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.internal.ev3.EV3LED;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.SampleProvider;
import net.robotics.screen.LCDRenderer;
import net.robotics.sensor.ColorSensorMonitor;
import net.robotics.sensor.ColorSensorMonitor.ColorNames;
import net.robotics.sensor.GyroMonitor;
import net.robotics.sensor.InfraredSensorMonitor;
import net.robotics.communication.RobotComms;
import net.robotics.communication.ServerSide;

public class Robot {
	public LCDRenderer screen;
	
	private ColorSensorMonitor leftColorSensor, rightColorSensor;
	private GyroMonitor gyroMonitor;
	private InfraredSensorMonitor irMonitor;
	
	private LED led;
	private SoundMonitor audio;
	private MovePilot pilot;
	private OdometryPoseProvider opp;
	
	private RobotComms robotComms;
	
	public int overrideVisitAmount = 0;
	
	private boolean mapFinished;
	
	public static Robot current;

	public static void main(String[] args) throws IOException{
		new Robot();
		current.startRobot();
	}

	public Robot() {
		current = this;
		
		Brick myEV3 = BrickFinder.getDefault();
		led = myEV3.getLED();
		audio = new SoundMonitor(myEV3.getAudio());
		screen = new LCDRenderer(LocalEV3.get().getGraphicsLCD());
		//server = new ServerSide();
		screen.clearScreen();
		screen.writeTo(new String[]{
				"Booting..."
		}, 0, 60, GraphicsLCD.LEFT, Font.getDefaultFont());
		
		leftColorSensor = new ColorSensorMonitor(this, new EV3ColorSensor(myEV3.getPort("S1")), 16);
		rightColorSensor = new ColorSensorMonitor(this, new EV3ColorSensor(myEV3.getPort("S4")), 16);
		gyroMonitor = new GyroMonitor(this, new EV3GyroSensor(myEV3.getPort("S2")), 1);
		irMonitor = new InfraredSensorMonitor(this, new EV3IRSensor(myEV3.getPort("S3")), Motor.C, 16);

		leftColorSensor.configure(false); //Only need to configure once as it sets a static object
		leftColorSensor.start();
		
		rightColorSensor.start();
		
		irMonitor.start();
		
		gyroMonitor.start();
		
		audio.start();

		
	}
	
	private void startRobot() {
		pilot = ChasConfig.getPilot();
		pilot.setLinearSpeed(11);
		
		// Create a pose provider and link it to the move pilot
		opp = new OdometryPoseProvider(pilot);
		
		createKeyListeners();
		
		try {
			ServerSocket s = new ServerSocket(RobotComms.PORT);
			robotComms = new RobotComms(this, s.accept());
			robotComms.listen();
		} catch (Exception e) {
        	screen.clearScreen();
        	screen.writeTo(new String[]{
				e.getStackTrace()[0].toString(),
				e.getStackTrace()[1].toString(),
				e.getStackTrace()[2].toString(),
				e.getStackTrace()[0].toString()
			}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		}
		
		//mainLoop();
	}
	
	public void closeProgram(){
		System.exit(0);
	}
	
	private void createKeyListeners(){
		Button.ESCAPE.addKeyListener(new KeyListener() {
			public void keyReleased(Key k) {
				Robot.current.closeProgram();
			}
			
			public void keyPressed(Key k) {
			}
		});
		
		
		Button.RIGHT.addKeyListener(new KeyListener() {
			public void keyReleased(Key k) {
				Robot.current.getScreen().cycleMode();
			}
			
			public void keyPressed(Key k) {
			}
		});
		
		Button.DOWN.addKeyListener(new KeyListener() {
			public void keyReleased(Key k) {
				Robot.current.resetScreen();
			}
			
			public void keyPressed(Key k) {
			}
		});
	}

	//localise
	public void localise(){
		pilot.setLinearSpeed(4);
		pilot.setLinearAcceleration(3);
		pilot.setAngularSpeed(40);
		pilot.setAngularAcceleration(30);
		
		boolean foundblack = false;
		ColorSensorMonitor onBlack, other;
		long timeSince = new Date().getTime();
		
		while(true){
			if(new Date().getTime() - timeSince > 200){
				timeSince = new Date().getTime();
				screen.clearScreen();
				screen.writeTo(new String[]{
						"L: " + leftColorSensor.getColor(),
						"R: " + rightColorSensor.getColor(),
						"LR: " + String.format("%.4f, %.4f", leftColorSensor.getRedColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHR()),
						"LG: " + String.format("%.4f, %.4f", leftColorSensor.getGreenColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHG()),
						"LB: " + String.format("%.4f, %.4f", leftColorSensor.getBlueColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHR()),
						"RR: " + String.format("%.4f, %.4f", rightColorSensor.getRedColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLR()),
						"RG: " + String.format("%.4f, %.4f", rightColorSensor.getGreenColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLG()),
						"RB: " + String.format("%.4f, %.4f", rightColorSensor.getBlueColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLB()),
				}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
			}
			
			if(leftColorSensor.getColor() == ColorNames.BLACK || rightColorSensor.getColor() == ColorNames.BLACK){
				if(leftColorSensor.getColor() == ColorNames.BLACK && rightColorSensor.getColor() != ColorNames.BLACK){
					
					if(!foundblack){
						foundblack = true;
						pilot.stop();
					}
					
					onBlack = leftColorSensor;
					other = rightColorSensor;
					
					pilot.rotate(-4);
					pilot.travel(0.1f);
				} else if(leftColorSensor.getColor() != ColorNames.BLACK && rightColorSensor.getColor() == ColorNames.BLACK){
					if(!foundblack){
						foundblack = true;
						pilot.stop();
					}
					
					other = leftColorSensor;
					onBlack = rightColorSensor;
					
					pilot.rotate(4);
					pilot.travel(0.1f);
				} else if(leftColorSensor.getColor() == ColorNames.BLACK && rightColorSensor.getColor() == ColorNames.BLACK){
					pilot.stop();

					screen.clearScreen();
					screen.writeTo(new String[]{
							"L: " + leftColorSensor.getColor(),
							"R: " + rightColorSensor.getColor(),
							"LR: " + String.format("%.4f, %.4f", leftColorSensor.getRedColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHR()),
							"LG: " + String.format("%.4f, %.4f", leftColorSensor.getGreenColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHG()),
							"LB: " + String.format("%.4f, %.4f", leftColorSensor.getBlueColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHR()),
							"RR: " + String.format("%.4f, %.4f", rightColorSensor.getRedColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLR()),
							"RG: " + String.format("%.4f, %.4f", rightColorSensor.getGreenColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLG()),
							"RB: " + String.format("%.4f, %.4f", rightColorSensor.getBlueColor(), ColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLB()),
							"Exited."
					}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
					
					break; //return if localised
				}
			} else {
				pilot.travel(3);
			}
			
			
		}
		
		pilot.travel(-6);
	}
		
    // ==================================================================================================
    // ================================  START ROBOT SERVER COMMANDS ====================================
    // ==================================================================================================

	
	//use IR sensor to get distance, get rotation amount
	public float getDistanceOnHeading(int heading){
		if(heading == 3)
			irMonitor.rotate(-90);
		if(heading == 1)
			irMonitor.rotate(90);
		
		irMonitor.clear();
		try {
			Thread.sleep(60*6);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(heading == 3)
			irMonitor.rotate(90);
		if(heading == 1)
			irMonitor.rotate(-90);
		
		return irMonitor.getDistance();
	}
	
	private int CurrentHeading = 0;
	
	//turn to heading
	public void turnToHeading(int desiredHeading) {
		pilot.setAngularSpeed(80);
		pilot.setAngularAcceleration(60);
		
		int headingDifference = desiredHeading - CurrentHeading;
		int rotationAmount = 0;
		
		switch(headingDifference) {
			case 1: 
			case -3:
				rotationAmount = 90;
				break;
			case 2: 
			case -2:
				rotationAmount = 180;
				break;
			case -1:
			case 3:
				rotationAmount = -90;
				break;
		}
		
		if(desiredHeading > 3)
			desiredHeading = 0;
		if(desiredHeading < 0)
			desiredHeading = 3;
		
		pilot.rotate(rotationAmount);
		
		CurrentHeading = desiredHeading;
	}
	
	//move in heading
	public boolean move(int heading){
		turnToHeading(heading);
		
		pilot.setLinearSpeed(8);
		pilot.setLinearAcceleration(6);
		
		pilot.forward();
		
		ColorNames lcn, rcn;
		do{
			lcn = leftColorSensor.getCurrentColor();
			rcn = rightColorSensor.getCurrentColor();
			/*if(CHECK IR SENSOR){
				pilot.travel(-4.0f);
				return false;
			}*/
		}while(lcn != ColorNames.BLACK && rcn != ColorNames.BLACK && !(pilot.getMovement().getDistanceTraveled() > 20f));
		
		if(lcn == ColorNames.BLACK || rcn == ColorNames.BLACK){
			pilot.travel(15.0f);
		} else {
			pilot.travel(5.0f);
		}
		
		return true;
	}
	
	//get Color underneath
	public String getColor(){
		ColorNames left = leftColorSensor.getCurrentColor();
		ColorNames right = rightColorSensor.getCurrentColor();
		return (left == ColorNames.UNKNOWN) ? right.toString() : left.toString();
	}
	
	//finishes robot
	public void finish(){
		closeProgram();
	}
	
	//VERIFIED
	//pick up victim only change lights
	public void pickUpVictim(boolean critical){
		getLED().setPattern((critical) ? EV3LED.COLOR_RED : EV3LED.COLOR_ORANGE, EV3LED.PATTERN_HEARTBEAT);
	}
	
	
	//VERIFIED
	//pick up victim only change lights
	public void dropVictim(){
		getLED().setPattern(EV3LED.COLOR_NONE, EV3LED.PATTERN_ON);
	}
    // ==================================================================================================
    // ================================  END ROBOT SERVER COMMANDS ======================================
    // ==================================================================================================

	
	public void mainLoop(){
		/*move(1);
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(0)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		move(2);
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(0)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		move(3);
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(0)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		move(0);
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(0)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(1)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(3)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		screen.clearScreen();
		screen.writeTo(new String[]{
				"C: " + getColor(),
				"D: " + getDistanceOnHeading(2)
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		Button.waitForAnyPress();
		finish();*/
		/*
		screen.clearScreen();
		screen.writeTo(new String[]{
				"G: " + gyroMonitor.getMedianAngle()
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		
		double ar = (Math.random()*15)-30;
		
		pilot.rotate(ar);
		
		screen.clearScreen();
		screen.writeTo(new String[]{
				"M1: " + gyroMonitor.getMedianAngle(),
				"A: " + ar
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		
		Button.waitForAnyPress();
		
		screen.clearScreen();
		screen.writeTo(new String[]{
				"M2: " + gyroMonitor.getMedianAngle(),
				"A: " + ar
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		
		Button.waitForAnyPress();
		
		screen.clearScreen();
		screen.writeTo(new String[]{
				"A1: " + gyroMonitor.getAngle(),
				"A: " + ar
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		
		Button.waitForAnyPress();
		
		screen.clearScreen();
		screen.writeTo(new String[]{
				"1: " + gyroMonitor.rotations[0],
				"2: " + gyroMonitor.rotations[1],
				"3: " + gyroMonitor.rotations[2],
				"4: " + gyroMonitor.rotations[3],
				"5: " + gyroMonitor.rotations[4],
				"A: " + ar
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		*/
	}
		
	public void resetScreen(){
		
		LocalEV3.get().getTextLCD().clear();
		LocalEV3.get().getGraphicsLCD().clear();
			
		screen.clearScreen();
		screen = new LCDRenderer(LocalEV3.get().getGraphicsLCD());
		screen.clearScreen();
	}

	public LCDRenderer getScreen(){
		return screen;
	}
	
	public OdometryPoseProvider getOpp() {
		return opp;
	}
	

	public MovePilot getPilot() {
		return pilot;
	}

	
	public ColorSensorMonitor getLeftColorSensor() {
		return leftColorSensor;
	}
	
	public ColorSensorMonitor getRightColorSensor() {
		return rightColorSensor;
	}
	
	public EV3LED getLED() {
		return (EV3LED) led;
	}
	
	public SoundMonitor getSoundMonitor() {
		return audio;
	}
}
