package net.robotics.communication;

import net.robotics.communication.Tile.TileType;

public class testMCL {
	private static PCParticlePoseProvider particlePP;

	private static int xCells = 6;
	private static int yCells = 6;
	
	private static int[][] obstacleLocations = {
			{1,1},
			{4,4},
			{1,4},
			{4,1}
	};
	
	public static void main(String[] args) {
		KnownMap map = new KnownMap(xCells, yCells, obstacleLocations);
		map.getTile(0, 0).setType(TileType.Hospital);
		map.getTile(5, 0).setType(TileType.GREENTILE);
		particlePP = new PCParticlePoseProvider(map);
		//particlePP.localise();
	}
}
