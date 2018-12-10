package net.robotics.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import net.robotics.main.Robot;
import net.robotics.sensor.EuclideanColorSensorMonitor;
import net.robotics.sensor.EuclideanColorSensorMonitor.ColorNames;

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
		String[] commands = command.split(" ");
		
		if(commands[0].toUpperCase().contains("GETCOLOR")){
			ColorNames name = robot.getLeftColorSensor().getColor();
			if(name == ColorNames.UNKNOWN)
				name = ColorNames.GREEN;
			
			sendCommand("COLOR " + name);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("GETINFO")){
			sendCommand("RINFO " + robot.getRobotInfo());
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("MOVE")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			robot.move(d);
			sendCommand("MOVESUCCESS " + d);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("TURNTO")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			robot.turnToHeading(d);
			sendCommand("TURNEDTO " + d);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("GETDIST")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			sendCommand("DIST " + robot.getDistanceOnHeading(d));
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("PICKUP")){
			boolean d = false;
			if(commands.length > 1)
				d = Boolean.parseBoolean(commands[1]);
			
			robot.pickUpVictim(d);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("DROP")){
			robot.dropVictim();
			return;
		}
		
		sendCommand("INVALID COMMAND " + command);
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
