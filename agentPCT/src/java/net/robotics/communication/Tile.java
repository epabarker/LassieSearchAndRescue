package net.robotics.communication;

import java.util.ArrayList;
import java.util.Collection;

public class Tile {
	public enum TileType{
		Empty, OBSTACLE, Hospital, Victim, NONCRITICAL, NOVICTIM, GREENTILE
	}
	
	private int x, y;
	
	private boolean unreachable;
	private TileType type;
	
	public Tile(int x, int y){
		this.setX(x);
		this.setY(y);
		this.type = TileType.Empty;
		this.setUnreachable(false);
		
		//this.view(false);
		//this.view(true);
	}
	
	public Tile(int x, int y, TileType type){
		this(x, y);
		this.setType(type);
	}
	
	
	public void setType(TileType type){
		this.type = type;
	}
	
	public TileType getType(){
		return this.type;
	}
	
	// Need to set this to be unchangeable. Create boolean for permanent?
	// Then that can be used to specify whether or not a value is able to be changed. 

	public int getX() {
		return x;
	}

	private void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	private void setY(int y) {
		this.y = y;
	}
	
	public void setUnreachable(boolean isUnreachable){
		this.unreachable = isUnreachable;
	}
	
	
}
