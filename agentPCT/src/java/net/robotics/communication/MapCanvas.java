package net.robotics.communication;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import net.robotics.communication.Tile.TileType;

public class MapCanvas extends JPanel {
	private static final long serialVersionUID = 1L;

	private int tileSize = 64;
	private final String name = "FENTON!";
	private KnownMap map;
	//private Map loadedMap;


	public MapCanvas() {

		//this.setPreferredSize(new Dimension(600,600));
	}

	public void UpdateMap(KnownMap map){
		this.map = map;
		paintComponent(this.getGraphics());
	}

	private void drawMap(Graphics2D g){

		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		
		float xSize = this.getWidth()/map.getWidth();
		float ySize = this.getHeight()/map.getHeight();
		
		int strokeSize = (int) (xSize * 0.1);

		int gap = strokeSize / 2;
		
		xSize -= gap;
		ySize -= gap;
		
		System.out.println(xSize + "/" + ySize );
		
		for (int x = 0; x < map.getWidth(); x++) {
			for (int y = 0; y < map.getHeight(); y++) {
				g.setStroke(new BasicStroke(strokeSize));
				Tile t = map.getTile(x, y);
				
				int posX = x * ((int)xSize + gap*2);
				int posY = y * ((int)ySize + gap*2);


				System.out.println(x + "/" + y + "/"+t.getType());
				
				if(map.notObstacle(x, y)){
					
					if(t.getType() == TileType.Hospital){
						g.setColor(Color.CYAN);
						g.fillRect(posX - gap, posY - gap, (int)xSize + 2*gap, (int)ySize + 2*gap);
					} else if(t.getType() == TileType.Victim){
						g.setColor(new Color(255, 137, 86));
						g.fillRect(posX - gap, posY - gap, (int)xSize + 2*gap, (int)ySize + 2*gap);
					} else {
						g.drawRect(posX, posY, (int)xSize, (int)ySize);
					}
				} else {
					g.setColor(Color.BLACK);
					g.fillRect(posX - gap, posY - gap, (int)xSize + 2*gap, (int)ySize + 2*gap);
				}
				
				g.setColor(Color.BLACK);
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