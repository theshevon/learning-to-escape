package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Map {


	// the information currently known about the map
	private HashMap<Coordinate, CoordinateData> currentMap;
	private ArrayList<Coordinate> keyTileCoordinates;
	private ArrayList<Coordinate> edgeTileCoordinates;
	private ArrayList<Coordinate> healthTileCoordinates;
	private ArrayList<Coordinate> exitTileCoordinates;

	private int width;
	private int height;

	public Map(int width, int height) {
		this.width = width;
		this.height = height;
		currentMap = new HashMap<>();
		keyTileCoordinates = new ArrayList<>();
		edgeTileCoordinates = new ArrayList<>();
		healthTileCoordinates = new ArrayList<>();
		exitTileCoordinates = new ArrayList<>();
	}



	public Coordinate findPath(Coordinate coord, Coordinate carCoord) {
		Coordinate currentCoord = coord;

		//System.out.println(currentCoord);
		Coordinate nextCoord = currentMap.get(currentCoord).getPath().get(0);
		//finds the tile leading to the next
		while (!nextCoord.equals(carCoord)) {
			currentCoord = nextCoord;
			//System.out.println(currentCoord);
			nextCoord = currentMap.get(currentCoord).getPath().get(0);

		}
		//System.out.println(currentMap.get(currentCoord).getPath().toString());
		if (currentMap.get(currentCoord).getPath().size() > 1) {
			//destination = Path(1)
			System.out.println(currentCoord);
			return (currentMap.get(currentCoord).getPath().get(1));
		} else {
			//destination is current coord
			return (currentCoord);
		}
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
	public HashMap<Coordinate, CoordinateData> getCurrentMap() {
		return currentMap;
	}

	public CoordinateData getData(Coordinate coord) {
		return currentMap.get(coord);
	}
	public Set<Coordinate> getAllCoords(){
		return currentMap.keySet();
	}
	public MapTile getTile(Coordinate coord) {
		return currentMap.get(coord).getTile();
	}
	public MapTile.Type getType(Coordinate coord) {
		return currentMap.get(coord).getType();
	}

	public int getDamage(Coordinate coord) {
		return currentMap.get(coord).getDamage();
	}
	public int getDistance(Coordinate coord) {
		return currentMap.get(coord).getDistance();
	}

	public void resetScores() {
		for(CoordinateData coord: currentMap.values()) {
			coord.resetScore();
		}
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

