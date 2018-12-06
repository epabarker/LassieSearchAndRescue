package net.robotics.sensor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lejos.hardware.Button;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.sensor.EV3ColorSensor;
import net.robotics.main.Robot;

public class EuclideanColorSensorMonitor{

	private static HashMap<ColorNames, RGBFloat> ColorKeys = new HashMap<>();
	private EV3ColorSensor colorSensor;
	private float[] colorSample;

	private Robot robot;
	
	private static int measures = 500;

	public static enum ColorNames{
		//GREEN,

		CYAN,
		GREEN,
		YELLOW,
		BURGANDY,
		BLACK,
		WHITE,
		//YELLOW,
		//RED,
		UNKNOWN
	}

	public EuclideanColorSensorMonitor(Robot robot, EV3ColorSensor sensor){
		this.colorSensor = sensor;
		this.colorSample = new float[this.colorSensor.getRGBMode().sampleSize()];
		this.robot = robot;
	}

	public void configure(boolean loadFromFile){
		int i = 0;

		Gson gson = new Gson();

		for (ColorNames colorname : ColorNames.values()) {
			if(colorname == ColorNames.UNKNOWN){
				continue;
			}

			if(!loadFromFile){
				i++;

				robot.screen.writeTo(new String[]{
						"Waiting for " + colorname,
						"Press any button",

				}, robot.screen.getWidth()/2, 0, GraphicsLCD.HCENTER, true);

				robot.screen.drawEnterButton("Next", robot.screen.getWidth()-30-2, robot.screen.getHeight()-30-2, 30, 30);

				robot.screen.drawDownButton("Skip", robot.screen.getWidth()-80-2, robot.screen.getHeight()-30-2, 45, 30);

				robot.screen.drawEscapeButton("Quit", 0, robot.screen.getHeight()-45/2-2, 45, 45/2, 6);

				Button.waitForAnyPress();

				if(Button.ESCAPE.isDown()){
					robot.closeProgram();
					Button.waitForAnyPress();
				}

				if(Button.DOWN.isDown())
					continue;

				robot.screen.clearScreen();
				float r = 0,g = 0,b  = 0;
				for (int j = 0; j < measures; j++) {
					float[] rgb = getRGB();
					r += rgb[0];
					g += rgb[1];
					b += rgb[2];
				}
				
				robot.screen.writeTo(new String[]{
						"Reading: " + colorname,
						"R("+r+")",
						"G("+g+")",
						"B("+b+")"
				}, robot.screen.getWidth()/2, 0, GraphicsLCD.HCENTER, true);

				r /= (float)measures;
				g /= (float)measures;
				b /= (float)measures;
				
				Button.waitForAnyPress();
				
				robot.screen.clearScreen();
				robot.screen.writeTo(new String[]{
						"Reading: " + colorname,
						"R("+r+")",
						"G("+g+")",
						"B("+b+")"
				}, robot.screen.getWidth()/2, 0, GraphicsLCD.HCENTER, true);

				ColorKeys.put(colorname, new RGBFloat(r, g, b));

				robot.screen.drawEnterButton("Next", robot.screen.getWidth()-30-2, robot.screen.getHeight()-30-2, 30, 30);

				robot.screen.drawEscapeButton("Quit", 0, robot.screen.getHeight()-45/2-2, 45, 45/2, 6);

				try (PrintWriter out = new PrintWriter(colorname.name()+".json")) {
					out.println(gson.toJson(ColorKeys.get(colorname)));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				do{
					Button.waitForAnyPress();

					if(Button.ESCAPE.isDown())
						robot.closeProgram();

				} while(!Button.ENTER.isDown());
			} else {
				try {
					ColorKeys.put(colorname, gson.fromJson(new String(Files.readAllBytes(Paths.get(colorname.name()+".json"))), RGBFloat.class));
				} catch (JsonSyntaxException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		robot.screen.clearScreen();
	}

	public float getRedColor(){
		colorSensor.getRGBMode().fetchSample(colorSample, 0);
		return colorSample[0];
	}

	public float getGreenColor(){
		colorSensor.getRGBMode().fetchSample(colorSample, 0);
		return colorSample[1];
	}

	public float getBlueColor(){
		colorSensor.getRGBMode().fetchSample(colorSample, 0);
		return colorSample[2];
	}

	public String getRGBColor(){
		colorSensor.getRGBMode().fetchSample(colorSample, 0);
		return "R(" + colorSample[0] + "), G(" + colorSample[1] + ") B(" + colorSample[2] + ")";
	}

	public float[] getRGB(){
		colorSensor.getRGBMode().fetchSample(colorSample, 0);
		return colorSample;
	}
	
	public static RGBFloat getColorRanges(ColorNames name){
		return ColorKeys.get(name);
	}

	public ColorNames getColor(){
		colorSensor.getRGBMode().fetchSample(colorSample, 0);
		RGBFloat color = new RGBFloat(colorSample[0], colorSample[1], colorSample[2]);
		
		ColorNames closestColor = ColorNames.UNKNOWN;
		double smallestDistance = Double.POSITIVE_INFINITY;
		
		for (ColorNames key : ColorKeys.keySet()) {
			if(key == ColorNames.UNKNOWN){
				continue;
			}

			RGBFloat comp = ColorKeys.get(key);
			float[] dist = comp.distanceTo(color);
			
			float r = dist[0] * dist[0];
			float g = dist[1] * dist[1];
			float b = dist[2] * dist[2];
			
			double rgb = r +g+b;
			
			double pythDist = Math.sqrt(rgb);
			//float pythDist = (float) Math.sqrt((double)(dist[0] * dist[0] +  dist[1] * dist[1] + dist[2] * dist[3]));
			if(pythDist < smallestDistance){
				smallestDistance = pythDist;
				closestColor = key;
			}
		}

		return closestColor;
	}
}
