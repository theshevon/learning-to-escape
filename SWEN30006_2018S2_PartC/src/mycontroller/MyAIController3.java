package mycontroller;

import controller.CarController;
import tiles.*;
import tiles.MapTile;
import tiles.LavaTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import java.util.ArrayList;

import static mycontroller.Direction3.*;


/**
 * Class used to control the car when escaping the maze
 * @author David Crowe
 */
public class MyAIController3 extends CarController{



	// information about the car at this instance
	private WorldSpatial.Direction carDirection;
	private Coordinate carCoord;
	private float carSpeed;
	private boolean movingForward;

	private Map3 map;
	
	public MyAIController3(Car car) {
		super(car);		

		map = new Map3(mapWidth(), mapHeight());
		// adds walls to map
		MapUpdater3.addToMap(map, getMap(), MapTile.Type.WALL);
	}

	@Override
	public void update() {
		
		// get the current information about the car, based on its location in the map
		carCoord = new Coordinate(getPosition());
		carDirection = getOrientation();
		carSpeed = getSpeed();

		//-------------------------------- hmmmmmmmmm -----------------------------------
		// if the car is on a tile which has a key, remove it from the list of key tile coordinates
		if (map.getKeyTileCoordinates().contains(carCoord)) {
			map.setKeyTileCoordinates(getUncollectedKeyCoordinates());
		}
		//-----------------------------------------------------------------------------------


		// find unexplored regions


		MapUpdater3.update(map, carCoord, getView());

		// determine where to move to
		Decisions3.determineNextMove(map, carCoord, getKeys());
	}


	
	private void peformMove(Coordinate destination) {
		//need to slow down
		if(carSpeed >0) {
			if(destination.equals(carCoord)) {
				applyBrake();
			}
			else if(destination.equals(getLeft(carCoord, carDirection))) {
				turnLeft();
			}
			else if(destination.equals(getRight(carCoord, carDirection))) {
				turnRight();

			}
			else if(destination.equals(getFront(carCoord, carDirection))) {
				if(!movingForward) {
					applyBrake();
				}
			}
			else if(destination.equals(getBehind(carCoord, carDirection))) {
				if(movingForward) {
					applyBrake();;
				}
		
			}
		}
		else {
			if(destination.equals(carCoord)) {
				applyBrake();
			}
			else if(destination.equals(getFront(carCoord, carDirection))) {
				movingForward= true;
				applyForwardAcceleration();
			}
			else if(destination.equals(getBehind(carCoord, carDirection))) {
				movingForward =false;
				applyReverseAcceleration();
			}
		}
	}


	/**
	 * @return a list of coordinates of the tiles that contain keys that have not yet been collected
	 */
	public ArrayList<Coordinate> getUncollectedKeyCoordinates() {

		ArrayList<Coordinate> remainingKeyCoordinates = new ArrayList<Coordinate>();

		for (Coordinate coordinate : map.getKeyTileCoordinates()) {

			if(!getKeys().contains(((LavaTrap)map.getCurrentMap().get(coordinate).getTile()).getKey())) {
				remainingKeyCoordinates.add(coordinate);
			}
		}

		return remainingKeyCoordinates;
	}


}
