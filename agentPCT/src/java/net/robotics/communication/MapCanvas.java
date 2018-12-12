package net.robotics.communication;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.sun.org.apache.bcel.internal.generic.NEW;

import net.robotics.communication.Tile.TileType;

public class MapCanvas extends JPanel {
	private static final long serialVersionUID = 1L;

	private int tileSize = 64;
	private final String name = "FENTON!";
	private KnownMap map;
	//private Map loadedMap;
	
	private int x, y, h;
	
	private ArrayList<Particle> particles = new ArrayList<>();
	
	private LinkedList<Tile> astarPath = new LinkedList<>();
	
	private Mode mode;
	
	private BufferedImage up, left, right, down;
	
	public enum Mode{
		ParticleFilter, RobotMove
	}


	public MapCanvas(KnownMap map) {
		this.map = map;
		
		try {
			up = ImageIO.read(new File("res/up.png"));
			left = ImageIO.read(new File("res/left.png"));
			right = ImageIO.read(new File("res/right.png"));
			down = ImageIO.read(new File("res/down.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.mode = Mode.ParticleFilter;
		
		map.getTile(5,  0).setType(TileType.GREENTILE);
		//this.setPreferredSize(new Dimension(600,600));
	}
	
	public void setMode(Mode mode){
		this.mode = mode;
	}
	
	public void updateRobotPosition(int x, int y, int h){
		this.x = x;
		this.y = y;
		this.h = h;
		paintComponent(this.getGraphics());
	}
	
	public void updatePath(LinkedList<Tile> path){
		this.astarPath = path;
		paintComponent(this.getGraphics());
	}
	
	public void setParticles(ArrayList<Particle> particles){
		this.particles = particles;
		paintComponent(this.getGraphics());
	}
	
	public void updateMap(TileType object, int x, int y){
		map.getTile(x, y).setType(object);
		paintComponent(this.getGraphics());
	}
	
	public KnownMap getMap(){
		return map;
	}

	private void drawMap(Graphics2D g){

		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		
		float xSize = this.getWidth()/map.getWidth();
		float ySize = this.getHeight()/map.getHeight();
		
		int strokeSize = (int) (xSize * 0.01);

		int gap = strokeSize / 2;
		
		xSize -= gap;
		ySize -= gap;
		
		int tileWidth = ((int)xSize + gap*2);
		int tileHeight = ((int)ySize + gap*2);
		
		//System.out.println(xSize + "/" + ySize );
		
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				g.setStroke(new BasicStroke(strokeSize));
				Tile t = map.getTile(x, y);
				
				int posX = x * tileWidth;
				int posY = (map.getHeight() - y - 1) * tileHeight;

				//System.out.println(x + "/" + y + "/"+t.getType());
				
				if(t.getType() == TileType.Hospital){
					g.setColor(Color.CYAN);
					g.fillRect(posX - gap, posY - gap, tileWidth, tileHeight);
				} else if(t.getType() == TileType.Victim){
					g.setColor(new Color(255, 137, 86));
					g.fillRect(posX - gap, posY - gap, tileWidth, tileHeight);
				} else if(t.getType() == TileType.OBSTACLE){
					g.setColor(Color.BLACK);
					g.fillRect(posX - gap, posY - gap, tileWidth, tileHeight);
				} else if(t.getType() == TileType.NONCRITICAL){
					g.setColor(Color.PINK);
					g.fillRect(posX - gap, posY - gap, tileWidth, tileHeight);
				} else {
					g.drawRect(posX, posY, (int)xSize, (int)ySize);
				}
				
				g.setColor(Color.BLACK);
			}
		}
		
		if(mode == Mode.ParticleFilter){
			for (Particle particle : particles) {
				g.setColor(Color.PINK);
				int width = 25;
				int height = 25;
				
				int posX = particle.getX() * tileWidth + (tileWidth/2 - width/2);
				int posY = (map.getHeight() - particle.getY() - 1) * tileHeight + (tileHeight/2 - height/2);;
				
				if(particle.getHeading() == 0){
					posY -= 20;
					posX += 0;
					g.drawImage(up, posX, posY, width, height, this);
				}
				if(particle.getHeading() == 1){
					posY -= 5;
					posX += 30;
					g.drawImage(right, posX, posY, width, height, this);
				}
				
				if(particle.getHeading() == 2) {
					posY += 15;
					g.drawImage(down, posX, posY, width, height, this);
				}
				
				if(particle.getHeading() == 3) {
					posY -= 5;
					posX -= 30;
					g.drawImage(left, posX, posY, width, height, this);
				}
			}
		}
		
		if(mode == Mode.RobotMove){
			Image chosenImage = up;
			if(h == 1)
				chosenImage = right;
			if(h == 2)
				chosenImage = down;
			if(h == 3)
				chosenImage = left;
			
			g.drawImage(chosenImage, x * ((int)xSize)  + (tileWidth/2 - 50/2), (map.getHeight() - y - 1)  * ((int)ySize)  + (tileHeight/2 - 50/2), 50, 50, this);
			
			

			g.setStroke(new BasicStroke(xSize * 0.1f));
			for (Tile tile : astarPath) {
				int posX = tile.getX() * tileWidth;
				int posY = (map.getHeight() - tile.getY() - 1) * tileHeight;
				g.setColor(new Color(251, 196, 12));
				g.drawRect(posX, posY, (int)xSize, (int)ySize);
			}
		}
	}




	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if(map == null)
			return;

		drawMap((Graphics2D) g);

	}
}