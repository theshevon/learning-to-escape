package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Map {

	public HashMap<Coordinate, CoordinateData> currentMap = new HashMap<>();
	
	// Coordinates of useful tiles
	public ArrayList<Coordinate> keyTileCoordinates = new ArrayList<>();
	public ArrayList<Coordinate> edgeTileCoordinates = new ArrayList<>();
	public ArrayList<Coordinate> healthTileCoordinates = new ArrayList<>();
	public ArrayList<Coordinate> exitTileCoordinates = new ArrayList<>(); 

	private final int WIDTH;
	private final int HEIGHT;

	public Map(int width, int height) {
		WIDTH = width;
		HEIGHT = height;
	}

	/**
	 * Iterates through the paths until one originates from carCoordinate
	 * returns the first coordinate along the path towards targetCoord
	 * @param targetCoordinate The coordinate that you want the car to move towards
	 * @param carCoordinate The current coordinate of the car
	 * @return returns the first coord in path
	 */
	public Coordinate findPath(Coordinate targetCoordinate, Coordinate carCoordinate) {
		
		Coordinate currentCoordinate = targetCoordinate;
		Coordinate nextCoordinate = currentMap.get(currentCoordinate).getPath().get(0);
		
		//finds the tile leading to the next
		while (!nextCoordinate.equals(carCoordinate)) {
			currentCoordinate = nextCoordinate;
			nextCoordinate = currentMap.get(currentCoordinate).getPath().get(0);

		}
		if (currentMap.get(currentCoordinate).getPath().size() > 1) {
			//destination = Path(1)
			return (currentMap.get(currentCoordinate).getPath().get(1));
		} else {
			//destination is current coordinate
			return (currentCoordinate);
		}
	}

	/**
	 * @return A List of coordinates of edge tiles
	 */
	public ArrayList<Coordinate> getEdgeTileCoordinates() {
		return edgeTileCoordinates;
	}
	
	/**
	 * @return A list of coordinates of key tiles
	 */
	public ArrayList<Coordinate> getKeyTileCoordinates() {
		return keyTileCoordinates;
	}
	
	/**
	 * Assigns an updated list for keyTileCoordinates 
	 * @param keyTileCoordinates List of updated key tile coordinates
	 */
	public void setKeyTileCoordinates(ArrayList<Coordinate> keyTileCoordinates) {
		this.keyTileCoordinates = keyTileCoordinates;
	}
	
	/**
	 * @return A list of coordinates of health tiles
	 */
	public ArrayList<Coordinate> getHealthTileCoordinates() {
		return healthTileCoordinates;
	}
	
	/**
	 * @return A list of coordinates of exit tiles
	 */
	public ArrayList<Coordinate> getExitTileCoordinates() {
		return exitTileCoordinates;
	}
	
	/**
	 * @return A hash-map with the CoordinateData for the coordinates on the map
	 */
	public HashMap<Coordinate, CoordinateData> getCurrentMap() {
		return currentMap;
	}
	
	/**
	 * @param coordinate A coordinate in the map
	 * @return The CoordinateData for the required coordinate
	 */
	public CoordinateData getData(Coordinate coordinate) {
		return currentMap.get(coordinate);
	}
	
	/**
	 * @return A Set of all the coordinates on the map
	 */
	public Set<Coordinate> getAllCoordinates(){
		return currentMap.keySet();
	}
	
	/**
	 * @param coordinate A coordinate in the map
	 * @return The MapTile at the required coordinate
	 */
	public MapTile getTile(Coordinate coordinate) {
		return currentMap.get(coordinate).getTile();
	}
	
	/**
	 * @param coordinate A coordinate in the map
	 * @return The type of MapTile at the required coordinate
	 */
	public MapTile.Type getType(Coordinate coordinate) {
		return currentMap.get(coordinate).getType();
	}

	/**
	 * @param coordinate A coordinate in the map
	 * @return Damage to reach coord 
	 */
	public int getDamage(Coordinate coordinate) {
		return currentMap.get(coordinate).getDamage();
	}
	
	/**
	 * @param coordinate A coordinate in the map
	 * @return Distance to reach distance
	 */
	public int getDistance(Coordinate coordinate) {
		return currentMap.get(coordinate).getDistance();
	}

	/**
	 * Resets the scores of all the tiles to their default values
	 */
	public void resetScores() {
		for (CoordinateData coordinate: currentMap.values()) {
			coordinate.resetScore();
		}
	}

	/**
	 * Checks if a coordinate is within the boundaries of the map
	 * @param x x-component of coordinate
	 * @param y y-component of coordinate
	 * @return True if within map boundaries
	 */
	public boolean insideBoundaries(int x , int y) {
		return x < WIDTH && x >= 0 && y < HEIGHT && y >= 0;
	}
	
	/**
	 * Checks if a coordinate is within the boundaries of the map
	 * @param coordinate A coordinate
	 * @return True if within map boundaries
	 */
	public boolean insideBoundaries(Coordinate coordinate) {
		return insideBoundaries(coordinate.x, coordinate.y);
	}

	/**
	 * Checks if a coordinate has been explored
	 * @param x x-component of coordinate
	 * @param y y-component of coordinate
	 * @return True if it has been explored
	 */
	public boolean inExploredMap(int x, int y) {
		return inExploredMap(new Coordinate(x, y));
	}
	
	/**
	 * Checks if a coordinate has been explored
	 * @param coordinate A coordinate
	 * @return True if it has been explored
	 */
	public boolean inExploredMap(Coordinate coordinate) {
		return currentMap.containsKey(coordinate);
	}

}

