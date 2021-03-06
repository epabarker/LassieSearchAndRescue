package net.robotics.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class PCComms extends Thread {

	private static final int PORT = 9001;
	private static final String ADDRESS = "0.0.0.0";

	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	private boolean Connected;

	private int id;
	
	private String lastMessage = "//";
	
	private boolean moveSuccess, turnSuccess;
	
	private String color = "";
	
	private float irDist = -1f;
	
	private String robotInfo;

	public PCComms(String serverAddress, int id) throws IOException{
		this.id = id;
		this.socket = new Socket(serverAddress, PORT);
		input = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(socket.getOutputStream(), true);
		System.out.println("INITING...");
	}

	public synchronized void start() {
		System.out.println("STARTING...");
		super.start();
	}

	public void sendCommand(String command){
		waitForCommand();
		
		output.println(command);
		
		turnSuccess = false;
		moveSuccess = false;
		color = "";
		setIrDist(-1f);
	}

	public void handleCommands(String command){
		waiting = false;
		setLastMessage(command);
		
		String[] commands = command.split(" ");
		
		if(command.contains("COLOR")){
			System.out.println("C " + command);
			color = command.split(" ")[1];
			return;
		}

		if(command.contains("MOVESUCCESS")){
			System.out.println("M " + command);
			moveSuccess = true;
			return;
		}
		
		if(command.contains("TURNEDTO")){
			System.out.println("T " + command);
			turnSuccess = true;
			return;
		}
		
		

		if(command.contains("DIST")){
			System.out.println("F " + command);
			setIrDist(Float.parseFloat(command.split(" ")[1]));
			return;
		}
		
		if(command.contains("RINFO")){
			Integer currentHeading = 0;
			if(commands.length > 1)
				currentHeading = Integer.parseInt(commands[1]);
			
			float gyroAngle = 0;
			if(commands.length > 2)
				gyroAngle = Float.parseFloat(commands[2]);
			
			float exptAngle = 0;
			if(commands.length > 3)
				exptAngle = Float.parseFloat(commands[3]);
			
			String lCN = "EMPTY";
			if(commands.length > 4)
				lCN = commands[4];
			
			String rCN = "EMPTY";
			if(commands.length > 5)
				rCN = commands[5];
			
			String mp = "MPEMPTY";
			if(commands.length > 6)
				mp = commands[6];
			
			setRobotInfo("Current Heading: " + currentHeading +  "\n" +
					"Gyro Angle: " + gyroAngle +  "\n" +
					"Expected Angle: " + exptAngle +  "\n" +
					"Left Color Sensor: " + lCN +  "\n" +
					"Right Color Sensor: " + rCN +  "\n" +
					"Move Process: " + mp);
			
			System.out.println("F " + command);
			return;
		}

		System.out.println("UNKNOWN COMMAND: " + command);
	}

	@Override
	public void run() {
		System.out.println("STARTED ON " + socket.getInetAddress() + ":" + socket.getPort());

		while(true){
			String text;
			try {
				text = input.readLine();
				if(text != null){
					System.out.println("COMMAND: " + text);
					handleCommands(text);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args){
		System.out.println("MAIN START...");
		PCComms pc;
		System.out.println("SERVER SOCKET WAITING...");

		try {
			pc = new PCComms(ADDRESS, 0);
			pc.start();
			
			String command = "";
			Scanner scanner;
			while(!command.contains("exit")){
				scanner = new Scanner(System.in);
				command = scanner.nextLine();
				pc.sendCommand(command);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}

	}

	public boolean isConnected() {
		return Connected;
	}

	public void setConnected(boolean connected) {
		Connected = connected;
	}

	public String getLastMessage() {
		return lastMessage;
	}
	
	public void clearLastMessage(){
		lastMessage = "//";
	}

	private void setLastMessage(String lastMessage) {
		System.out.println("SETTING LAST MESSAGE: " + lastMessage);
		this.lastMessage = lastMessage;
	}
	
	public boolean isMoveSuccess(){
		return moveSuccess;
	}
	
	public boolean isTurnSuccess(){
		return turnSuccess;
	}
	
	public String getColor(){
		return color;
	}

	public float getIrDist() {
		return irDist;
	}

	public void setIrDist(float irDist) {
		this.irDist = irDist;
	}

	public String getRobotInfo() {
		return robotInfo;
	}

	public void setRobotInfo(String robotInfo) {
		this.robotInfo = robotInfo;
	}
	
	private boolean waiting;
	
	public void waitForCommand(){
		waiting = true;
	}

	public boolean isWaitingForCommand() {
		return waiting;
	}
}
