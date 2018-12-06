package net.robotics.communication;

import java.util.ArrayList;
import java.util.Collection;

public class Tile {
	public enum TileType{
		Empty, Hospital, Victim
	}
	
	private float occupiedBelief;
	private int visited;
	private int x, y;
	
	private boolean permanent;
	private boolean unreachable;
	private TileType type;
	//
	
	private int empty, viewed;
	
	public Tile(int x, int y){
		this.permanent = false;
		this.occupiedBelief = 0.5f;
		this.setX(x);
		this.setY(y);
		this.viewed = 0;
		this.empty = 0;
		this.visited = 0;
		this.type = TileType.Empty;
		this.setUnreachable(false);
		
		//this.view(false);
		//this.view(true);
	}
	
	public Tile(int x, int y, TileType type){
		this(x, y);
		this.setType(type);
	}
	
	public void view(boolean isEmpty){
		this.viewed++;
		if(isEmpty)
			this.empty++;
	}
	
	
	public void setType(TileType type){
		this.type = type;
	}
	
	public TileType getType(){
		return this.type;
	}
	
	// Need to set this to be unchangeable. Create boolean for permanent?
	// Then that can be used to specify whether or not a value is able to be changed. 
	public void knownObstacle() {
		this.occupiedBelief = 1.0f;
		this.permanent = true;
	}
	
	public float getOccupiedBelief(){
		//if(visited == 0 && this.viewed != 0f){
		//	if(this.viewed == 0)
		//		return this.occupiedBelief;
		//	this.occupiedBelief = (float)(this.viewed-this.empty)/(float)this.viewed;
		//}
		return this.occupiedBelief;
	}
	
	public void visit(){
		this.visited++;
		this.occupiedBelief = 0f;
	}
	
	public int getVisitAmount(){
		return this.visited;
	}
	
	public int getEmpty(){
		return this.empty;
	}
	
	public int getViewed(){
		return this.viewed;
	}

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

	public boolean isReachable() {
		if(getVisitAmount() > 0)
			return true;
		
		return !unreachable;
	}
	
	
}
