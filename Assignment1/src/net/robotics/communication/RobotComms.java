package net.robotics.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import net.robotics.main.Robot;
import net.robotics.sensor.ColorSensorMonitor;
import net.robotics.sensor.ColorSensorMonitor.ColorNames;

public class RobotComms extends Thread{
	
	public static int PORT = 9001;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Robot robot;
	
	public RobotComms(Robot r, Socket s) throws Exception{
		robot = r;
		socket = s;
		in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}
	
	
	
	@Override
	public synchronized void start() {
		super.start();
	}



	@Override
	public void run() {
		try {
			listen();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.run();
	}



	public void sendCommand(String command){
        out.println(command);
	}
	
	public void handleCommands(String command){
		if(command.contains("GETCOLOR")){
			ColorNames name = robot.getLeftColorSensor().getCurrentColor();
			if(name == ColorNames.UNKNOWN)
				name = ColorNames.GREEN;
			
			sendCommand("COLOR " + name + "/" + 
					"LR: " + String.format("%.4f, %.4f ", robot.getLeftColorSensor().getRedColor(), ColorSensorMonitor.getColorRanges(name).getHR()) +
					"LG: " + String.format("%.4f, %.4f ", robot.getLeftColorSensor().getGreenColor(), ColorSensorMonitor.getColorRanges(name).getHG()) +
					"LB: " + String.format("%.4f, %.4f ", robot.getLeftColorSensor().getBlueColor(), ColorSensorMonitor.getColorRanges(name).getHR()) +
					"RR: " + String.format("%.4f, %.4f ", robot.getRightColorSensor().getRedColor(), ColorSensorMonitor.getColorRanges(name).getLR()) +
					"RG: " + String.format("%.4f, %.4f ", robot.getRightColorSensor().getGreenColor(), ColorSensorMonitor.getColorRanges(name).getLG()) +
					"RB: " + String.format("%.4f, %.4f ", robot.getRightColorSensor().getBlueColor(), ColorSensorMonitor.getColorRanges(name).getLB()));
		}
		
		if(command.contains("MOVE")){
			robot.move(0);
			sendCommand("MOVESUCCESS ");
		}
		
		if(command.contains("GETDIST")){
			sendCommand("DIST " + robot.getDistanceOnHeading(0));
		}
	}
	
	public void listen() throws Exception{
		String response;
		
		try {
			while(true){
	            response = in.readLine();
	            if(response != null){
	            	robot.screen.clearScreen();
	            	robot.screen.writeTo(new String[]{
						"L: " + response
					}, 0, 0, GraphicsLCD.LEFT, Font.getSmallFont());
	            } else
	            	continue;
	            
	            handleCommands(response);
	          
	            if(response.equalsIgnoreCase("exit"))
	            	break;
	        }
		} finally{
	        socket.close();
		}
	}
	
	public static void main(String[] args) throws Exception{
		ServerSocket s = new ServerSocket(PORT);
		RobotComms client = new RobotComms(null, s.accept());
        client.start();
	}
}
