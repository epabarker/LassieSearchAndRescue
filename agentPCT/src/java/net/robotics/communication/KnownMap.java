package net.robotics.communication;

import net.robotics.communication.Tile.TileType;

public class KnownMap {
	private Tile[][] tiles;
	private int width, height;	
	
	public KnownMap(int width, int height, Tile[] tiles) {
		this(width, height);
		int[][] oc = new int[tiles.length][2];
		for (int i = 0; i < tiles.length; i++) {
			if(tiles[i].getType() == TileType.Empty){
				oc[i][0] = tiles[i].getX();
				oc[i][1] = tiles[i].getY();
			} else {
				oc[i][0] = -1;
				oc[i][1] = -1;
				this.getTile(tiles[i].getX(), tiles[i].getY()).setType(tiles[i].getType());
			}
		}
		placeObstacles(oc);
	}
	
	public KnownMap(int width, int height) {
		this.setWidth(width);
		this.setHeight(height);
		this.setTiles(width, height);
	}
	
	public KnownMap(int width, int height, int[][]oc) {
		this(width, height);
		placeObstacles(oc);
	}
	
	private void setTiles(int width, int height) {
		this.tiles = new Tile[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				this.tiles[x][y] = new Tile(x, y);
			}
		}
	}
	
	public Tile getTile(int x, int y){
		if(x >= 0 && x < width && y >= 0 && y < height)
			return tiles[x][y];
		return null;
	}
	
	public void placeObstacles(int[][] oc) {
		for (int i = 0; i < oc.length; i++) {
			int x = oc[i][0];
			int y = oc[i][1];
			if(isPointIn(x, y)){
				tiles[x][y].setType(TileType.OBSTACLE);;
			}
		}
	}
	
	public int getWidth() {
		return width;
	}

	private void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}

	private void setHeight(int height) {
		this.height = height;
	}
	
	public boolean isPointIn(int x, int y) {
		if(x >= 0 && x < width && y >= 0 && y < height) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isObstacle(int x, int y) {
		if (getTile(x,y).getType() == TileType.OBSTACLE) {
			return true;
		} else {
			return false;
		}
	}
}
