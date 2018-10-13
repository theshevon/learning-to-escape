package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import com.badlogic.gdx.Input;

import controller.CarController;
import swen30006.driving.Simulation;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;

public class MyAIController2 extends CarController {

	// consider using a hash-map of type <Coordinate, Tile>
	private HashMap<Coordinate, MapTile.Type> allTileCoords = new HashMap<>();
	private ArrayList<Coordinate> keyTileCoords = new ArrayList<>();
	private ArrayList<Coordinate> visited = new ArrayList<>();
	private Coordinate curr_location;
	private boolean scanCompleted = false;

	// How many minimum units the wall is away from the player.
	private int wallSensitivity = 1;
		
	private boolean isFollowingWall = false; // This is set to true when the car starts sticking to a wall.
		
	// Car Speed to move at
	private final int CAR_MAX_SPEED = 100;
		
	public MyAIController2(Car car) {
		super(car);
	}

	@Override
	public void update() {
		
		// Gets what the car can see
		HashMap<Coordinate, MapTile> currentView = getView();

		// if we haven't visited the current location, scan the 9x9 sub-map around the car
		curr_location = new Coordinate(getPosition());
		if (!visited.contains(curr_location)){
			visited.add(curr_location);
			updateTileCoords(currentView);
			System.out.format("Tiles scanned: %d/%d\n", allTileCoords.keySet().size(), World.MAP_HEIGHT * World.MAP_WIDTH);
		};
		
		
		if (allTileCoords.size() == World.MAP_HEIGHT * World.MAP_WIDTH && !scanCompleted) {
			System.out.println("Map Fully Scanned- Time for Tequila!");
			scanCompleted = true;
		}
	
		// if we're on a health tile, stay there until health is fully replenished
		MapTile tile = currentView.get(curr_location);
		if (tile.isType(MapTile.Type.TRAP) && ((TrapTile)tile).getTrap() == "health" && getHealth()<100){
			applyBrake();
			return;
		}
		
		// hold onto the wall and move to do an initial scan of the map
		if (!scanCompleted || (scanCompleted && tile.isType(MapTile.Type.TRAP))) {
			
			// checkStateChange();
			if(getSpeed() < CAR_MAX_SPEED){       // Need speed to turn and progress toward the exit
				applyForwardAcceleration();       // Tough luck if there's a wall in the way
			}
			if (isFollowingWall) {
				// If wall no longer on left, turn left
				if(!checkFollowingWall(getOrientation(), currentView)) {
					turnLeft();
				} else {
					// If wall on left and wall straight ahead, turn right
					if(checkWallAhead(getOrientation(), currentView)) {
						turnRight();
					}
				}
			} else {
				// Start wall-following (with wall on left) as soon as we see a wall straight ahead
				if(checkWallAhead(getOrientation(), currentView)) {
					turnRight();
					isFollowingWall = true;
				}
			}
		}
	}

	private void updateTileCoords(HashMap<Coordinate, MapTile> currentView) {
		
		for (Coordinate coordinate: currentView.keySet()) {
			
			// only record tiles if we haven't already encountered them
			if (!allTileCoords.containsKey(coordinate)) {
				MapTile tile = currentView.get(coordinate);
				
				// ignore tiles outside map
				if (tile.isType(MapTile.Type.EMPTY)) {
					continue;
				}
				
				// else note down the tile coordinates and their locations
				allTileCoords.put(coordinate, tile.getType());
				
				// check if tile is a lava trap and contains a key. If so, record its coordinates
				if (tile.isType(MapTile.Type.TRAP) && ((TrapTile)tile).getTrap() == "lava" && ((LavaTrap)tile).getKey() > 0){
					keyTileCoords.add(coordinate);
				}
			}
		}
		
		//printInfo();
	}
	
	/**
	 * Comparator to sort coordinates in increasing (x,y) order
	 */
	private class CoordinatesComparator implements Comparator<Coordinate>{
		
		@Override
		public int compare(Coordinate c1, Coordinate c2) {
			if (c1.x != c2.x) {
				return c1.x - c2.x;
			}
			return c1.y - c2.y;
		}	
	} 

	/**
	 * Print all the info gathered about the map
	 */
	private void printInfo() {
		System.out.println("\n*******************MAP INFO*****************\n");
		
		ArrayList<Coordinate> coordinates = new ArrayList<>(allTileCoords.keySet());
		Collections.sort(coordinates, new CoordinatesComparator());
		
		// print coordinates and tile types of all tiles found
		for (Coordinate coord: coordinates) {
			System.out.format("Coordinate: %s, TileType: %s\n", coord.toString(), allTileCoords.get(coord).toString());
		}
		
		// print coordinates of tiles with keys in them
		if (keyTileCoords.size() > 0) {
			System.out.println("\n*******************KEY INFO******************\n");
			for (Coordinate coord: keyTileCoords) {
				System.out.println(coord.toString());
			}
		}
		
		System.out.println("No of Points found: " + coordinates.size());
		System.out.println("\n********************************************\n");
	}
	
	
	/**
	 * Check if you have a wall in front of you!
	 * @param orientation the orientation we are in based on WorldSpatial
	 * @param currentView what the car can currently see
	 * @return
	 */
	private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
		switch(orientation){
		case EAST:
			return checkEast(currentView);
		case NORTH:
			return checkNorth(currentView);
		case SOUTH:
			return checkSouth(currentView);
		case WEST:
			return checkWest(currentView);
		default:
			return false;
		}
	}
	
	/**
	 * Check if the wall is on your left hand side given your orientation
	 * @param orientation
	 * @param currentView
	 * @return
	 */
	private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {
		
		switch(orientation){
		case EAST:
			return checkNorth(currentView);
		case NORTH:
			return checkWest(currentView);
		case SOUTH:
			return checkEast(currentView);
		case WEST:
			return checkSouth(currentView);
		default:
			return false;
		}	
	}
	
	/**
	 * Method below just iterates through the list and check in the correct coordinates.
	 * i.e. Given your current position is 10,10
	 * checkEast will check up to wallSensitivity amount of tiles to the right.
	 * checkWest will check up to wallSensitivity amount of tiles to the left.
	 * checkNorth will check up to wallSensitivity amount of tiles to the top.
	 * checkSouth will check up to wallSensitivity amount of tiles below.
	 */
	public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
		// Check tiles to my right
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to my left
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles to towards the top
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
		// Check tiles towards the bottom
		Coordinate currentPosition = new Coordinate(getPosition());
		for(int i = 0; i <= wallSensitivity; i++){
			MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
			if(tile.isType(MapTile.Type.WALL)){
				return true;
			}
		}
		return false;
	}
	
}
