package mycontroller;

import controller.CarController;
import tiles.MapTile;
import tiles.LavaTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import java.util.ArrayList;

import static mycontroller.Direction.*;

/**
 * Class used to control the car when escaping the maze
 */
public class MyAIController extends CarController{

	// Information about the car at this instance
	private WorldSpatial.Direction carDirection;
	private Coordinate carCoord;
	private float carSpeed;
	private boolean movingForward;

	private Map map;
	private DecisionMaker decisionMaker = new DecisionMaker();
	
	public MyAIController(Car car) {
		
		super(car);

		map = new Map(mapWidth(), mapHeight());
		
		// Add walls to map
		MapHandler.addToMap(map, getMap(), MapTile.Type.WALL);
	}

	
	@Override
	public void update() {
		
		// Get the current information about the car, based on its location in the map
		carCoord = new Coordinate(getPosition());
		carDirection = getOrientation();
		carSpeed = getSpeed();

		// If the car is on a tile which has a key, remove it from the list of key tile coordinates
		if (map.getKeyTileCoordinates().contains(carCoord)) {
			map.setKeyTileCoordinates(getUncollectedKeyCoordinates());
		}

		// Find unexplored regions
		MapHandler.updateMap(map, carCoord, getView());

		// Update the 'scores' for each tile that we've found on the map
		MapHandler.updateScores(map, carSpeed, carCoord, carDirection, movingForward);

		// Determine where to move to next
		performMove(decisionMaker.determineNextMove(map, getKeys().size(), numKeys(), carCoord, getHealth()));
	}

	/**
	 * Performs an acceleration/deceleration/braking/steering operation for the car
	 * @param destination The next coordinate to move to
	 */
	private void performMove(Coordinate destination) {
			
		// Stop if we've reached the destination
		if (destination.equals(carCoord)) {
			applyBrake();
			return;
		}
		
		// Perform an appropriate action based on the location of the destination relative to the car's
		// current position
		if (carSpeed > 0) {
			if (destination.equals(getLeft(carCoord, carDirection))) {
				turnLeft();
			} else if (destination.equals(getRight(carCoord, carDirection))) {
				turnRight();
			} else if (destination.equals(getFront(carCoord, carDirection))) {
				if (!movingForward) applyBrake();
			} else if (destination.equals(getBehind(carCoord, carDirection))) {
				if (movingForward) applyBrake();
			}
		} else {
			if (destination.equals(getFront(carCoord, carDirection))) {
				movingForward = true;
				applyForwardAcceleration();
			} else if (destination.equals(getBehind(carCoord, carDirection))) {
				movingForward = false;
				applyReverseAcceleration();
			}
		}
	}


	/**
	 * Scans through the list of key tile coordinates and returns the coordinates of the key tiles that
	 * have not been visited yet
	 * @return A list of coordinates of the tiles that contain keys that have not yet been collected
	 */
	private ArrayList<Coordinate> getUncollectedKeyCoordinates() {

		ArrayList<Coordinate> remainingKeyCoordinates = new ArrayList<Coordinate>();

		for (Coordinate coordinate : map.getKeyTileCoordinates()) {
			
			// If we haven't collected a particular key, add its coordinate to the list
			if(!getKeys().contains(((LavaTrap)map.getCurrentMap().get(coordinate).getTile()).getKey())) {
				remainingKeyCoordinates.add(coordinate);
			}
		}

		return remainingKeyCoordinates;
	}
}
