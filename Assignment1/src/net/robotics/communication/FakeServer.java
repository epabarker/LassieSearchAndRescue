package net.robotics.communication;
import java.net.*;

import com.google.gson.Gson;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;

import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.GraphicsLCD;
import java.io.*;
import net.robotics.screen.LCDRenderer;
import net.robotics.main.Robot;
import net.robotics.screen.*;
//import net.robotics.

public class FakeServer extends Thread{

	public static final int port = 4645;
	ServerSocket server;

	Graphics2D graph;
	private int delay;
	Socket client;

	public FakeServer(int delay) {
		this.setDaemon(true);
		this.delay = delay;
	}


	public synchronized void start() {
		try {
			server = new ServerSocket(port);
			System.out.println("Started.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.start();
	}



	@Override
	public void run() {
		
		DataOutputStream out = null;
		Gson gson = new Gson();
		
		while(true){

			try{
				if(client == null){
					client = server.accept();
					System.out.println("Connected.");
					out = new DataOutputStream(client.getOutputStream());
				}
				
				//out.writeUTF(gson.toJson(map));
				System.out.println("Sent.");
				out.flush();
			} 
			catch(IOException e){
				System.out.println(e.toString());
			}

			try{
				sleep(delay);
			} catch(Exception e){

			}
		}
	}
	
	public void changeMap(){
		
	}



}
