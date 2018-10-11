package mycontroller;

import java.util.HashMap;
import java.util.Set;

import com.badlogic.gdx.Input;

import controller.CarController;
import swen30006.driving.Simulation;
import tiles.*;
import tiles.MapTile;
import tiles.TrapTile;
import tiles.LavaTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import java.util.ArrayList;



public class MyAIController extends CarController{

	//Used to hold information about a tile. Used to find easiest path
	public class TileData{
		public TileData(MapTile tile) {
			this.tile= tile;
			damage = Integer.MAX_VALUE;
			distance = Integer.MAX_VALUE;
			path = new ArrayList<Coordinate>();
		}
		private int damage;
		private int distance;
		private float currentSpeed;
		private MapTile tile;
		public WorldSpatial.Direction direction;
		ArrayList<Coordinate> path;

		/* getters and setters */
		public MapTile getTile() {
			return tile;
		}
		public Integer getDamage() {
			return damage;
		}
		public Integer getDistance() {
			return distance;
		}
		public WorldSpatial.Direction getDirection(){
			return direction;
		}
		public void setDamage(Integer damage) {
			this.damage= damage;
		}
		public void clearScores() {
			this.damage = Integer.MAX_VALUE;
			this.distance= Integer.MAX_VALUE;
		}
		public void setDistance(Integer distance) {
			this.distance= distance;
		}
		public void setDirection(WorldSpatial.Direction direction) {
			this.direction= direction;
		}
		public void replacePath(ArrayList<Coordinate> path) {
			this.path = new ArrayList<Coordinate>(path);
		}
		public MapTile.Type getType() {
			return tile.getType();
		}

	}



	//The information currently known about the map
	public HashMap<Coordinate, TileData> currentMap;
	public ArrayList<Coordinate> keyTiles;
	public ArrayList<Coordinate> edgeTiles;
	public ArrayList<Coordinate> healthTiles;
	public ArrayList<Coordinate> exitTiles;
	//information about the car at this instance
	HashMap<Coordinate, MapTile> currentView;
	Coordinate carCoord;


	public MyAIController(Car car) {
		super(car);
		currentMap = new HashMap<Coordinate, TileData>();
		keyTiles = new ArrayList<Coordinate>();
		edgeTiles = new ArrayList<Coordinate>();
		healthTiles = new ArrayList<Coordinate>();
		exitTiles = new ArrayList<Coordinate>();
		//Adds all the walls to the currentMap
		addToMap(currentMap, getMap(), MapTile.Type.WALL);
	}


	@Override
	public void update() {
		//adds new squares to the explored map and updates the coordinates of points of interest
		updateMapData();

		//From manual controller
		Set<Integer> keys = Simulation.getKeys();
		Simulation.resetKeys();
		// System.out.print("Get Keys: ");
		// System.out.println(keys);
		for (int k : keys){
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
				case Input.Keys.P:
					printstuff();
				default:
			}
		}



	}

	//test function
	private void printstuff(){

		System.out.println("List of Edges");
		for(Coordinate coord : edgeTiles) {
			System.out.println(coord.toString());
		}
		System.out.println("current coord :" + carCoord);
		System.out.print("Keys:" + keyTiles.size());
		System.out.println("    health:" + healthTiles.size());
		System.out.print("exit:" + exitTiles.size());
		System.out.println("    edges:" + edgeTiles.size());
		System.out.println("total squares:" + mapWidth()*mapHeight());
		System.out.println("Explored squares(includes all walls): " + currentMap.size());

	}

	//adds new tiles, checks for edges
	private void updateMapData() {
		currentView = getView();
		carCoord = new Coordinate(getPosition());

		//adds new possible edge tiles before map is updated
		addPossibleEdges();
		//Adds the viewed tiles to the current map
		addToMap(currentMap, currentView);

		//checks all edges, removing those that are not
		checkEdges();
	}


	//adds possible edge tiles
	private void addPossibleEdges() {
		//horizontal and vertical lines
		Coordinate hCoords;
		Coordinate vCoords;
		for( int offset1 : new int[]{-4,4}) {
			for( int offset2 = -4 ; offset2 <=4; offset2++) {

				hCoords=  new Coordinate(carCoord.x+ offset1, carCoord.y+ offset2);

				//adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
				if(insideBoundries(hCoords) && !inExploredMap(hCoords)) {
					edgeTiles.add(hCoords);
				}

			}
			for( int offset3 = -3 ; offset3 <=3; offset3++) {

				vCoords=  new Coordinate(carCoord.x+ offset3, carCoord.y+ offset1);
				//adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
				if(insideBoundries(vCoords) && !inExploredMap(vCoords)) {
					edgeTiles.add(vCoords);
				}

			}
		}
	}
	//Removes any tiles that are no longer an edge ( i.e does not have an adjacent unexplored tile)
	private void checkEdges() {
		boolean hasUnexplored = false;
		ArrayList<Coordinate> tempList = new ArrayList<Coordinate>();

		for( Coordinate coord: edgeTiles) {

			//Ensures no walls or empty tiles can be edges
			if(currentMap.get(coord).getTile().isType(MapTile.Type.WALL)
					||currentMap.get(coord).getTile().isType(MapTile.Type.EMPTY)) {
				tempList.add(coord);
			}
			//Ensures each edge has at least one adjacent unexplored square
			else {
				for( int offset : new int[]{ -1, 1}) {
					if( (insideBoundries(coord.x + offset, coord.y) && !inExploredMap(coord.x + offset, coord.y))
							||(insideBoundries(coord.x, coord.y+ offset) && !inExploredMap(coord.x , coord.y + offset))) {
						hasUnexplored =true;

					}
				}
				if(hasUnexplored == false) {
					tempList.add(coord);
				}
				hasUnexplored = false;
			}
		}
		for(Coordinate coord : tempList) {
			edgeTiles.remove(coord);
		}
	}

	//checks if the coord is useful, adds to the appropriate list
	private void checkForUseful(Coordinate coord, MapTile tile){

		if(tile.isType(MapTile.Type.FINISH)) {
			exitTiles.add(coord);
		}
		else if(tile.isType(MapTile.Type.TRAP)){

			if( ((TrapTile)tile).getTrap()== "lava" ) {
				if(((LavaTrap)tile).getKey()>0) {
					keyTiles.add(coord);
				}
			}
			else if(((TrapTile)tile).getTrap()== "health" ) {
				healthTiles.add(coord);
			}
		}
	}


	//Adds all the provided Maptile's into the current map
	//Adds coord of points of interests (excluding edge tiles)
	private void addToMap(HashMap<Coordinate, TileData> currentMap, HashMap<Coordinate, MapTile> ProvidedMap) {

		for( Coordinate coord : ProvidedMap.keySet()) {
			//adds useful coords to appropriate lists

			if(insideBoundries(coord) && !inExploredMap(coord)) {
				currentMap.put(coord, new TileData(ProvidedMap.get(coord)));
				checkForUseful(coord, ProvidedMap.get(coord));
			}
		}
	}
	//Only adds Maptile's of a certain type
	private void addToMap(HashMap<Coordinate, TileData> currentMap, HashMap<Coordinate, MapTile> ProvidedMap, MapTile.Type type) {

		for( Coordinate coord : ProvidedMap.keySet()) {
			if(ProvidedMap.get(coord).getType() == type) {
				currentMap.put(coord, new TileData(ProvidedMap.get(coord)));
			}
		}
	}
	//checks if a coordinate is inside the map boundaries (Integers)
	private boolean insideBoundries(int x , int y) {
		if(x < mapWidth() & x >= 0 & y < mapHeight() &  y >= 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	//for Coordinates
	private boolean insideBoundries(Coordinate coord) {
		return insideBoundries(coord.x, coord.y);
	}

	//checks if coordinate is in the explored set
	private boolean inExploredMap(int x, int y) {
		if(currentMap.containsKey(new Coordinate(x, y))) {
			return true;
		}
		else {
			return false;
		}
	}
	//for Coordinate
	private boolean inExploredMap(Coordinate coord) {
		if(currentMap.containsKey(coord)){
			return true;
		}
		else {
			return false;
		}
	}


}
