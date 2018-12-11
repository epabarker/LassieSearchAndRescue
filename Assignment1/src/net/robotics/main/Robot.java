package net.robotics.main;


import java.io.IOException;
import java.io.InputStreamReader;
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
import lejos.robotics.geometry.Point;
import net.robotics.screen.LCDRenderer;
import net.robotics.sensor.EuclideanColorSensorMonitor;
import net.robotics.sensor.EuclideanColorSensorMonitor.ColorNames;
import net.robotics.sensor.GyroMonitor;
import net.robotics.sensor.InfraredSensorMonitor;
import net.robotics.communication.RobotComms;
import net.robotics.communication.ServerSide;

public class Robot {
	public LCDRenderer screen;

	private EuclideanColorSensorMonitor leftColorSensor, rightColorSensor;
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

		createKeyListeners();

		Brick myEV3 = BrickFinder.getDefault();
		led = myEV3.getLED();
		audio = new SoundMonitor(myEV3.getAudio());
		screen = new LCDRenderer(LocalEV3.get().getGraphicsLCD());
		//server = new ServerSide();
		screen.clearScreen();
		screen.writeTo(new String[]{
				"Booting..."
		}, 0, 60, GraphicsLCD.LEFT, Font.getDefaultFont());

		leftColorSensor = new EuclideanColorSensorMonitor(this, new EV3ColorSensor(myEV3.getPort("S1")));
		rightColorSensor = new EuclideanColorSensorMonitor(this, new EV3ColorSensor(myEV3.getPort("S4")));
		gyroMonitor = new GyroMonitor(this, new EV3GyroSensor(myEV3.getPort("S2")), 1);
		irMonitor = new InfraredSensorMonitor(this, new EV3IRSensor(myEV3.getPort("S3")), Motor.C, 16);

		leftColorSensor.configure(true); //Only need to configure once as it sets a static object

		irMonitor.start();

		gyroMonitor.start();

		audio.start();


	}

	private void startRobot() {
		pilot = ChasConfig.getPilot();

		// Create a pose provider and link it to the move pilot
		opp = new OdometryPoseProvider(pilot);

		gyroMonitor.resetGyro();

		//mainLoop();

		try {
			screen.clearScreen();
			screen.writeTo(new String[]{
					"READY TO",
					"CONNECT"
			}, 0, 0, GraphicsLCD.LEFT, Font.getLargeFont());

			ServerSocket s = new ServerSocket(RobotComms.PORT);
			robotComms = new RobotComms(this, s.accept());
			gyroMonitor.resetGyro();
			robotComms.listen();
		} catch (Exception e) {
			screen.clearScreen();
			screen.writeTo(new String[]{
					e.getMessage(),
					e.getStackTrace()[0].getMethodName() + ":" + e.getStackTrace()[0].getLineNumber(),
					e.getStackTrace()[1].getMethodName() + ":" + e.getStackTrace()[1].getLineNumber(),
					e.getStackTrace()[2].getMethodName() + ":" + e.getStackTrace()[2].getLineNumber(),
					e.getStackTrace()[3].getMethodName() + ":" + e.getStackTrace()[3].getLineNumber(),
			}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		}


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
	public boolean localise(){

		double linSpeed = pilot.getLinearSpeed();
		double linAcc = pilot.getLinearAcceleration();
		double angularSpeed = pilot.getAngularSpeed();
		double angularAcc = pilot.getAngularAcceleration();

		pilot.setLinearSpeed(3);
		pilot.setLinearAcceleration(10);
		pilot.setAngularSpeed(40);
		pilot.setAngularAcceleration(30);



		long timeSince = new Date().getTime();

		gyroMonitor.resetGyro();

		ColorNames floorColor = leftColorSensor.getColor();
		if(floorColor == ColorNames.BLACK)
			floorColor = rightColorSensor.getColor();
		if(floorColor == ColorNames.BLACK){
			pilot.setLinearSpeed(linSpeed);
			pilot.setLinearAcceleration(linAcc);
			pilot.setAngularSpeed(angularSpeed);
			pilot.setAngularAcceleration(angularAcc);
			return true;
		}

		if(floorColor != ColorNames.WHITE){
			pilot.setLinearSpeed(linSpeed);
			pilot.setLinearAcceleration(linAcc);
			pilot.setAngularSpeed(angularSpeed);
			pilot.setAngularAcceleration(angularAcc);
			return false;
		}

		pilot.forward();

		while(true){
			if(new Date().getTime() - timeSince > 200){
				timeSince = new Date().getTime();
				screen.clearScreen();
				screen.writeTo(new String[]{
						"L: " + leftColorSensor.getColor(),
						"R: " + rightColorSensor.getColor(),
						"G: " + gyroMonitor.getAngle(),

				}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
			}

			boolean leftMetLine = leftColorSensor.getColor() != floorColor;
			boolean rightMetLine = rightColorSensor.getColor() != floorColor;
			boolean same = leftColorSensor.getColor() == rightColorSensor.getColor();

			if(leftMetLine || rightMetLine){
				if(leftMetLine && !rightMetLine){
					Motor.B.stop();
					Motor.D.forward();
				} else if(!leftMetLine && rightMetLine){
					Motor.D.stop();
					Motor.B.forward();
				} else if(same){
					pilot.stop();

					screen.clearScreen();
					screen.writeTo(new String[]{
							"L: " + leftColorSensor.getColor(),
							"R: " + rightColorSensor.getColor(),
							"LR: " + String.format("%.4f, %.4f", leftColorSensor.getRedColor(), EuclideanColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHR()),
							"LG: " + String.format("%.4f, %.4f", leftColorSensor.getGreenColor(), EuclideanColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHG()),
							"LB: " + String.format("%.4f, %.4f", leftColorSensor.getBlueColor(), EuclideanColorSensorMonitor.getColorRanges(ColorNames.BLACK).getHR()),
							"RR: " + String.format("%.4f, %.4f", rightColorSensor.getRedColor(), EuclideanColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLR()),
							"RG: " + String.format("%.4f, %.4f", rightColorSensor.getGreenColor(), EuclideanColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLG()),
							"RB: " + String.format("%.4f, %.4f", rightColorSensor.getBlueColor(), EuclideanColorSensorMonitor.getColorRanges(ColorNames.BLACK).getLB()),
							"Exited."
					}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());

					break; //return if localised
				} else {
					if(!Motor.D.isMoving() && !Motor.B.isMoving()){
						pilot.forward();
					}
				}
			}


		}

		pilot.travel(-8);

		pilot.setLinearSpeed(linSpeed);
		pilot.setLinearAcceleration(linAcc);
		pilot.setAngularSpeed(angularSpeed);
		pilot.setAngularAcceleration(angularAcc);
		return true;
	}

	// ==================================================================================================
	// ================================  START ROBOT SERVER COMMANDS ====================================
	// ==================================================================================================


	//use IR sensor to get distance, get rotation amount
	public float getDistanceOnHeading(int heading){
		if(heading == 3)
			irMonitor.rotate(90);
		if(heading == 1)
			irMonitor.rotate(-90);

		irMonitor.clear();
		try {
			Thread.sleep(60*6);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		float dist = irMonitor.getDistance();

		if(heading == 3)
			irMonitor.rotate(-90);
		if(heading == 1)
			irMonitor.rotate(90);

		return dist;
	}

	private int CurrentHeading = 0;
	private float expectedRotationAmount = 0;
	private int Revolutions = 0;
	
	public void correctHeading(int d) {
		this.CurrentHeading = d;
	}

	//turn to heading
	public void turnToHeading(int desiredHeading) {
		
		MoveProcess = "TURNING";
		
		double angularSpeed = pilot.getAngularSpeed();
		double angularAcc = pilot.getAngularAcceleration();
		pilot.setAngularSpeed(100);
		pilot.setAngularAcceleration(100);

		int headingDifference = ((desiredHeading - CurrentHeading) % 4) ;
		
		if(headingDifference == 0){
			return;
		}

		if(headingDifference == -3)
			headingDifference = 1;
		if(headingDifference == 3)
			headingDifference = -1;

		Revolutions += Math.abs(headingDifference);

		int rotationAmount = headingDifference * 90;

		expectedRotationAmount+=rotationAmount;

		pilot.rotate(rotationAmount);

		CurrentHeading = desiredHeading;

		pilot.setAngularSpeed(angularSpeed);
		pilot.setAngularAcceleration(angularAcc);

		correctHeadingAcc();
	}

	private void correctHeadingAcc(){
		
		MoveProcess = "CORRECTHEADINGACC";
		
		double angularSpeed = pilot.getAngularSpeed();
		double angularAcc = pilot.getAngularAcceleration();
		pilot.setAngularSpeed(50);
		pilot.setAngularAcceleration(50);

		float Angle = gyroMonitor.getAngle();

		if(Angle != expectedRotationAmount){
			for (int i = 0; i < 5; i++) {
				Angle = gyroMonitor.getAngle();
				float diff = (expectedRotationAmount - Angle);
				pilot.rotate(diff);

				screen.clearScreen();
				screen.writeTo(new String[]{
						"Diff: " + diff,
						"RotationAcc: " + expectedRotationAmount,
						"Angle: " + Angle,
				}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());

				if(gyroMonitor.getAngle() == expectedRotationAmount){
					break;
				}
			}
		}
		pilot.setAngularSpeed(angularSpeed);
		pilot.setAngularAcceleration(angularAcc);
	}

	int movements = 0;
	
	String MoveProcess = "Empty";

	public String getRobotInfo(){
		return CurrentHeading + " " + 
				gyroMonitor.getAngle() + " " + 
				expectedRotationAmount + " " + 
				leftColorSensor.getColor() + " " + 
				rightColorSensor.getColor() + " " +
				MoveProcess;
	}
	
	public void turnAmount(int amount) {
		MoveProcess = "ITURN";
		
		double angularSpeed = pilot.getAngularSpeed();
		double angularAcc = pilot.getAngularAcceleration();
		pilot.setAngularSpeed(700);
		pilot.setAngularAcceleration(400);
		
		amount = amount % 4;	
		
		int rotationAmount = amount * 90;
		
		if(amount == 3){
			rotationAmount = -90;
		}
		
		expectedRotationAmount+=rotationAmount;

		pilot.rotate(rotationAmount);
		
		pilot.setAngularSpeed(angularSpeed);
		pilot.setAngularAcceleration(angularAcc);

		correctHeadingAcc();
	}
	
	public boolean independentMove(){
		pilot.setLinearSpeed(60);
		pilot.setLinearAcceleration(30);
		
		pilot.travel(5.5f);

		Motor.B.setSpeed(50);
		Motor.D.setSpeed(50);

		Motor.B.setAcceleration(8000);
		Motor.D.setAcceleration(8000);

		Motor.B.forward();
		Motor.D.forward();

		float sGyro = -1000;

		MoveProcess = "IFINDINGLINE";
		
		ColorNames lcn, rcn;
		do{
			lcn = leftColorSensor.getColor();
			rcn = rightColorSensor.getColor();

			if(lcn == ColorNames.BLACK){
				Motor.B.stop();
				if(sGyro == -1000){
					MoveProcess = "IFINDINGOTHERLINE";
					sGyro = gyroMonitor.getAngle();
				}
			}

			if(rcn == ColorNames.BLACK){
				Motor.D.stop();
				if(sGyro == -1000){
					MoveProcess = "IFINDINGOTHERLINE";
					sGyro = gyroMonitor.getAngle();
				}
			}

			screen.clearScreen();
			screen.writeTo(new String[]{
					"l: " + sGyro,
					"r: " + gyroMonitor.getAngle(),
					"bb: " + (gyroMonitor.getAngle() - sGyro)
			}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
		}while(Motor.D.isMoving() || Motor.B.isMoving());

		gyroMonitor.resetGyro();
		expectedRotationAmount = 0;
		
		pilot.stop();

		MoveProcess = "IFINALISINGMOVEMENT";
		
		pilot.travel(16.5f);

		return true;
	}

	//move in heading
	public boolean move(int heading){
		int initialHeading = CurrentHeading;

		turnToHeading(heading);

		//IF WE GET EVERYTHING ELSE WORKING WE CAN IMPROVE THIS...
		//LOCALISE AGAINST PERPENDICULAR EDGE

		/*if(Revolutions > 7 || movements > 2){
			if(localise()){

				Revolutions = 0;
				gyroMonitor.resetGyro();
				expectedRotationAmount = 0;
				movements = 0;

				turnToHeading(CurrentHeading+1);

				float disIF = irMonitor.getMedianDistance();

				if(disIF > 24){
					localise();
				}

				turnToHeading(CurrentHeading-1);


			}
		}*/

		if(heading == initialHeading){
			movements++;
		}

		independentMove();

		return true;
	}

	//get Color underneath
	public String getColor(){
		ColorNames left = leftColorSensor.getColor();
		ColorNames right = rightColorSensor.getColor();
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
		//move(0);
		//move(0);
		//move(0);
		
		

		/*screen.clearScreen();
    	screen.writeTo(new String[]{
				"Angle: " + gyroMonitor.getAngle()
		}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());*/
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


	public EuclideanColorSensorMonitor getLeftColorSensor() {
		return leftColorSensor;
	}

	public EuclideanColorSensorMonitor getRightColorSensor() {
		return rightColorSensor;
	}

	public EV3LED getLED() {
		return (EV3LED) led;
	}

	public SoundMonitor getSoundMonitor() {
		return audio;
	}

	
}
