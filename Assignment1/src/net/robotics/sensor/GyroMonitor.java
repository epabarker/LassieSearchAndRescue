package net.robotics.sensor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3IRSensor;
import net.robotics.main.Robot;
import net.robotics.screen.LCDRenderer;

public class GyroMonitor extends Thread{
	
	private EV3GyroSensor gyroSensor;
	private float[] sample;
	
	public float rotations[];
	public int amount;
	public int pointer;
	
	private int Delay;
	
	public GyroMonitor(Robot robot, EV3GyroSensor sensor, int Delay){
		this.setDaemon(true);
		this.gyroSensor = sensor;
		
		this.sample = new float[sensor.getAngleMode().sampleSize()];
		this.Delay = Delay;
		this.pointer = 0;
		
		this.rotations = new float[5];
	}
	
	public synchronized void start() {
		super.start();
	}

	public EV3GyroSensor getSensor(){
		return gyroSensor;
	}

	public void configure(){
	}
	
	public void resetGyro(){
		gyroSensor.reset();
	}
	
	public float getAngle(){
		gyroSensor.getAngleMode().fetchSample(sample, 0);
		return sample[0];
	}
	
	public float getMedianAngle(){
		float[] values = rotations.clone();
		Arrays.sort(values);
		return values[values.length/2];
	}
	
	public GyroMonitor clear(){
		for (int i = 0; i < rotations.length; i++) {
			rotations[i] = 0;
		}
		amount = 0;
		return this;
	}
	
	public void run() {
		
		
		while(true){
			
			float dist = getAngle();
			if(dist == Float.POSITIVE_INFINITY)
				continue;
			
			rotations[pointer] = dist;
			
			pointer++;
			if(pointer>=5)
				pointer = 0;
			
			amount++;
			
			
			try{
				sleep(Delay);
			} catch(Exception e){
				
			}
		}
	}
}
