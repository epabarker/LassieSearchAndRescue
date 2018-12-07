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
	private static final String ADDRESS = "192.168.70.64";

	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	private boolean Connected;

	private int id;
	
	private String lastMessage = "//";
	
	private boolean moveSuccess;
	
	private String color = "";

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
		output.println(command);
		
		moveSuccess = false;
		color = "";
	}

	public void handleCommands(String command){
		setLastMessage(command);
		
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

		if(command.contains("DIST")){
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
	
	public String getColor(){
		return color;
	}
}
