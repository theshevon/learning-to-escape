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

public class MyAIController2 extends CarController {

	// consider using a hash-map of type <Coordinate, Tile>
	private HashMap<Coordinate, MapTile.Type> allTileCoords = new HashMap<>();
	private ArrayList<Coordinate> keyTileCoords = new ArrayList<>();
	private ArrayList<Coordinate> visited = new ArrayList<>();
	private Coordinate curr_location;

	public MyAIController2(Car car) {
		super(car);
	}

	@Override
	public void update() {
		
		// if we haven't visited the current location, scan the 9x9 submap around the car
		curr_location = new Coordinate(getPosition());
		if (!visited.contains(curr_location)){
			visited.add(curr_location);
			updateTileCoords();
		};
		
		// manual control
		Set<Integer> keys = Simulation.getKeys();
		Simulation.resetKeys();
		// System.out.print("Get Keys: ");			
		// System.out.println(keys);
		for (int k : keys)
			switch (k){
				case Input.Keys.B:
					applyBrake();
					break;
				case Input.Keys.UP:
					applyForwardAcceleration();
					break;
				case Input.Keys.DOWN:
					applyReverseAcceleration();
					break;
				case Input.Keys.LEFT:
					turnLeft();
					break;
				case Input.Keys.RIGHT:
					turnRight();
					break;
				default:
					// do nothing;
			}
	}

	private void updateTileCoords() {
		
		for (Coordinate coordinate: getView().keySet()) {
			
			// only record tiles if we haven't already encountered them
			if (!allTileCoords.containsKey(coordinate)) {
				MapTile tile = getView().get(coordinate);
				
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
		
		printInfo();
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
		
		// print coordinates of
		System.out.println("\n********************************************\n");
	}
	
	
}
