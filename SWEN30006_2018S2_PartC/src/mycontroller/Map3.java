package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class Map3 {


	/**
	 * Class used to store information about the tiles.
	 * Used when determining the shortest route to each tile.
	 */
	public class TileData3 {

		private int damage;
		private int distance;
		private MapTile tile;
		private ArrayList<Coordinate> path;

		public TileData3(MapTile tile) {
			this.tile = tile;
			damage = Integer.MAX_VALUE;
			distance = Integer.MAX_VALUE;
			path = new ArrayList<Coordinate>();
		}

		public MapTile getTile() {
			return tile;
		}

		public Integer getDamage() {
			return damage;
		}

		public void setDamage(Integer damage) {
			this.damage = damage;
		}

		public Integer getDistance() {
			return distance;
		}

		public void setDistance(Integer distance) {
			this.distance= distance;
		}

		/**
		 * Replaces the current path with an updated (cheaper) path
		 * param: list of coordinates making up the new path
		 */
		public void replacePath(ArrayList<Coordinate> path) {
			this.path = new ArrayList<Coordinate>(path);
		}

		/**
		 * Gets the path to a tile.
		 * @return a list of coordinates making up the path
		 */
		public ArrayList<Coordinate> getPath() {
			return path;
		}

		/**
		 * @return the type of the tile
		 */
		public MapTile.Type getType() {
			return tile.getType();
		}

		/**
		 * sets the damage and distance values to their default values
		 */
		public void clearScores() {
			this.damage = Integer.MAX_VALUE;
			this.distance= Integer.MAX_VALUE;
		}



		public void updateTile(Coordinate currentCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {


			if(potentialDamage <currentMap.get(currentCoord).getDamage()) {

				currentMap.get(currentCoord).setDamage(potentialDamage);
				currentMap.get(currentCoord).setDistance(potentialDistance);
				currentMap.get(currentCoord).replacePath(potentialPath);
			}
			else if(potentialDamage == currentMap.get(currentCoord).getDamage()) {

				if(potentialDistance < currentMap.get(currentCoord).getDistance()) {
					currentMap.get(currentCoord).setDamage(potentialDamage);
					currentMap.get(currentCoord).setDistance(potentialDistance);
					currentMap.get(currentCoord).replacePath(potentialPath);
				}
			}
		}

	}



	// the information currently known about the map
	private HashMap<Coordinate, TileData3> currentMap;
	private ArrayList<Coordinate> keyTileCoordinates;
	private ArrayList<Coordinate> edgeTileCoordinates;
	private ArrayList<Coordinate> healthTileCoordinates;
	private ArrayList<Coordinate> exitTileCoordinates;

	private int width;
	private int height;

	public Map3 (int width, int height) {
		this.width = width;
		this.height = height;
		currentMap = new HashMap<>();
		keyTileCoordinates = new ArrayList<>();
		edgeTileCoordinates = new ArrayList<>();
		healthTileCoordinates = new ArrayList<>();
		exitTileCoordinates = new ArrayList<>();
	}




	//---------------------------------Getters and Setters----------------------------------
	public ArrayList<Coordinate> getEdgeTileCoordinates() {
		return edgeTileCoordinates;
	}
	public ArrayList<Coordinate> getKeyTileCoordinates() {
		return keyTileCoordinates;
	}
	public void setKeyTileCoordinates(ArrayList<Coordinate> keyTileCoordinates) {
		this.keyTileCoordinates = keyTileCoordinates;
	}
	public ArrayList<Coordinate> getHealthTileCoordinates() {
		return healthTileCoordinates;
	}
	public ArrayList<Coordinate> getExitTileCoordinates() {
		return exitTileCoordinates;
	}
	public HashMap<Coordinate, TileData3> getCurrentMap() {
		return currentMap;
	}

	//checks if a coordinate is inside the map boundaries (Integers)
	public boolean insideBoundries(int x , int y) {
		return x < width && x >= 0 && y < height && y >= 0;
	}
	public boolean insideBoundries(Coordinate coord) {
		return insideBoundries(coord.x, coord.y);
	}

	//checks if coordinate is in the explored set
	public boolean inExploredMap(int x, int y) {
		return inExploredMap(new Coordinate(x, y));
	}
	public boolean inExploredMap(Coordinate coord) {
		return currentMap.containsKey(coord);
	}

}

