package net.robotics.communication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import jason.environment.grid.Location;

public class FakeRobotComms extends Thread{
	
	public static int PORT = 9001;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
	
	public FakeRobotComms(Socket s) throws Exception{
		socket = s;
		
		System.out.println("STARTING SERVER");
		
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
			System.out.print("LISTENING");
			listen();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.run();
	}



	public void sendCommand(String command){
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("OUT: " + command);
        out.println(command);
	}
	
	int robotX = 3, robotY = 4, robotHeading = 2;
	
	Location[] burgandy = {
			new Location(1, 3)
	};
	
	
	Location[] cyan = {
			new Location(4, 3),
			new Location(0, 2),
	};
	
	Location[] obstacles = {
			new Location(1, 2),
			new Location(0, 3),
			new Location(1, 4),
			new Location(3, 3)

	};
	
	public String getColorUnderneath(){
		if(robotY == 0){
			if(robotX == 0)
				return "YELLOW";
			if(robotX == 5)
				return "GREEN";
		}
		
		for (Location location : burgandy) {
			if(location.x == robotX && location.y == robotY)
				return "BURGANDY";
		}
		
		for (Location location : cyan) {
			if(location.x == robotX && location.y == robotY)
				return "CYAN";
		}
		
		return "WHITE";
	}
	
	public boolean isObstacleInFront(){
		int dX = robotX;
		int dY = robotY;
		
		if(robotHeading == 0){
			dY += 1;
		} 
		if(robotHeading == 1){
			dX += 1;
		} 
		if(robotHeading == 2){
			dY -= 1;
		} 
		if(robotHeading == 3){
			dX -= 1;
		} 
		
		if(dX < 0 || dX > 5 || dY < 0 || dY > 5)
			return true;
		
		System.out.println("CHECKING OBSTACLE " + dX + "," + dY);
		
		for (Location location : obstacles) {
			if(location.x == dX && location.y == dY)
				return true;
		}
		
		return false;
		
	}
	
	private void move(int nHeading){
		robotHeading = nHeading;
		
		moveForward();
	}
	
	private void moveForward(){
		if(robotHeading == 0){
			robotY += 1;
			return;
		} 
		if(robotHeading == 1){
			robotX += 1;
			return;
		} 
		if(robotHeading == 2){
			robotY -= 1;
			return;
		} 
		if(robotHeading == 3){
			robotX -= 1;
			return;
		} 
	}
	
	public void handleCommands(String command){
		String[] commands = command.split(" ");
		
		if(commands[0].toUpperCase().contains("GETCOLOR")){
			//POLL FAKE MAP
			sendCommand("COLOR " + getColorUnderneath());
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("GETINFO")){
			sendCommand("RINFO ");
			
			return;
		}
		
		if(commands[0].toUpperCase().equalsIgnoreCase("MOVE")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			move(d);
			sendCommand("MOVESUCCESS " + d);
			
			return;
		}
		
		if(commands[0].toUpperCase().equalsIgnoreCase("IMOVE")){
			
			moveForward();
			sendCommand("MOVESUCCESS ");
			
			return;
		}
		
		if(commands[0].toUpperCase().equalsIgnoreCase("TURNTO")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			robotHeading = d;
			sendCommand("TURNEDTO " + d);
			
			return;
		}
		
		if(commands[0].toUpperCase().equalsIgnoreCase("ITURNTO")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			int newHeading = (robotHeading + d) % 4;
			robotHeading = newHeading;
			sendCommand("TURNEDTO " + d);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("GETDIST")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			float distance = 48;
			if(isObstacleInFront()){
				distance = 0;
			}
			
			sendCommand("DIST " + distance);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("PICKUP")){
			boolean d = false;
			if(commands.length > 1)
				d = Boolean.parseBoolean(commands[1]);
			
			//robot.pickUpVictim(d);
			
			return;
		}
		
		if(commands[0].toUpperCase().contains("DROP")){
			//robot.dropVictim();
			return;
		}
		
		if(commands[0].toUpperCase().contains("CORRECTHEADING")){
			int d = 0;
			if(commands.length > 1)
				d = Integer.parseInt(commands[1]);
			
			robotHeading = d;
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
	            	System.out.println("COMMAND RECIEVED: " + response);
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
		System.out.println(s.getInetAddress() + "/" + s.getLocalPort());
		FakeRobotComms client = new FakeRobotComms(s.accept());
        client.start();
	}
}
