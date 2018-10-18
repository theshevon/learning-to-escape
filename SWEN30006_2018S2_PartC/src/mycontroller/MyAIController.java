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
	
	public interface MapUpdater{
		public abstract void clearMap(Map map);
		public abstract void updateMap(Map map , Coordinate carCoord, WorldSpatial.Direction carDirection , float carSpeed, boolean movingForward);
	}
	
	//Used to hold information about a tile. Used to find easiest path
	public class TileData{

		private int damage;
		private int distance; 
		private MapTile tile;
		//public WorldSpatial.Direction direction; 
		ArrayList<Coordinate> path;
		public TileData(MapTile tile) {
			this.tile= tile;
			damage = Integer.MAX_VALUE;
			distance = Integer.MAX_VALUE; 
			path = new ArrayList<Coordinate>();
		}
		
		/* getters and setters */
		public MapTile getTile() {
			return tile;
		}
		public int getDamage() {
			return damage;
		}
		public int getDistance() {
			return distance;
		}
		public void setDamage(int damage) {
			 this.damage= damage;
		}
		public void clearScores() {
			this.damage = Integer.MAX_VALUE;
			this.distance= Integer.MAX_VALUE;
		}
		public void setDistance(int distance) {
			 this.distance= distance;
		}

		public void replacePath(ArrayList<Coordinate> path) {
			this.path = new ArrayList<Coordinate>(path);
		}
		public MapTile.Type getType() {
			return tile.getType();
		}
		public ArrayList<Coordinate> getPath() {
			return path;
		}
		
	}

	public class Map{
		
		
		//The information currently known about the map
		public HashMap<Coordinate, TileData> currentMap;
		public ArrayList<Coordinate> keyTiles;
		public ArrayList<Coordinate> edgeTiles;
		public ArrayList<Coordinate> healthTiles;
		public ArrayList<Coordinate> exitTiles; 
		

		public Map() {
			currentMap = new HashMap<Coordinate, TileData>();
			keyTiles = new ArrayList<Coordinate>();
			edgeTiles = new ArrayList<Coordinate>();
			healthTiles = new ArrayList<Coordinate>();
			exitTiles = new ArrayList<Coordinate>();
		}
		
		//Adds all the provided Maptile's into the current map
		//Adds coord of points of interests (excluding edge tiles)
		public void addToMap(HashMap<Coordinate, MapTile> ProvidedMap) {
			
			for( Coordinate coord : ProvidedMap.keySet()) {
				//adds useful coords to appropriate lists

				if(insideBoundries(coord) && !inExploredMap(coord)) {
					currentMap.put(coord, new TileData(ProvidedMap.get(coord)));
					checkForUseful(coord, ProvidedMap.get(coord));
					//adds potential edge
					edgeTiles.add(coord);
				}
				//removes any coords that are not edges
				validateEdges();
			}
		}
		//Only adds Maptile's of a certain type
		public void addToMap(HashMap<Coordinate, MapTile> ProvidedMap, MapTile.Type type) {
			
			HashMap<Coordinate, MapTile> tempMap = new HashMap<Coordinate, MapTile>();
			
			for( Coordinate coord : ProvidedMap.keySet()) {
				if(ProvidedMap.get(coord).getType() == type) {
					tempMap.put(coord, ProvidedMap.get(coord));
				}
			}
			addToMap(tempMap);
		}
		
	
		//Removes any tiles that are no longer an edge ( i.e does not have an adjacent unexplored tile)
		private void validateEdges() {
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

		
		private void clearScores() {
			for(Coordinate coord: currentMap.keySet()) {
				currentMap.get(coord).clearScores();
			}
		}
		
		
		public ArrayList<Coordinate> getEdgeTiles(){
			return edgeTiles;
		}
		public ArrayList<Coordinate> getExitTiles(){
			return exitTiles;
		}
		public ArrayList<Coordinate> getHealthTiles(){
			return exitTiles;
		}
		
		public ArrayList<Coordinate> getUnexploredKeys(Set<Integer> discoveredKeys) {
			ArrayList<Coordinate> tempList = new ArrayList<Coordinate>();
			for( Coordinate key : keyTiles) {
				if(!discoveredKeys.contains(((LavaTrap)currentMap.get(key).getTile()).getKey())) {
					tempList.add(key);
				}
			}
			return tempList;
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
		public ArrayList<Coordinate> getPath(Coordinate coord) {
			return currentMap.get(coord).getPath();
		}
		
		public void setDamage(Coordinate coord , int damage) {
			 currentMap.get(coord).setDamage(damage);
		}
		public void setDistance(Coordinate coord, int distance) {
			 currentMap.get(coord).setDistance(distance);
		}
		public void replacePath(Coordinate coord, ArrayList<Coordinate> path) {
			currentMap.get(coord).replacePath(path);
		}
		public boolean containsCoord(Coordinate coord) {
			return currentMap.containsKey(coord);
	
		}
		
		
	} 
	
	public class DamageUpdater  implements MapUpdater {
		
		public DamageUpdater() {
			
		}
		public void updateMap(Map map , Coordinate carCoord, WorldSpatial.Direction carDirection , float carSpeed, boolean movingForward ) {

			//clear previous scores
			//set initial conditions
			//store all possible nodes in a list
			//have different movement conditions per different tiles
			//pick the lowest scoring item, update the tiles surrounding it
			//if a grass tile is reached, iterate through until a non grass tile is reached, (mark the distance for each)
			
			
			map.clearScores();
			ArrayList<Coordinate> unexploredKeys;
			updateInitialCoords(map , carCoord ,  carDirection ,carSpeed, movingForward);
			unexploredKeys = getDrivableCoords(map);
			unexploredKeys.remove(carCoord);
			findShortestDistances(map , unexploredKeys);

			
			
		}
		public void clearMap(Map map) {
			
		}
		private void updateInitialCoords(Map map , Coordinate sourceCoord , WorldSpatial.Direction direction , float speed, boolean movingForward){

			//Initialize starting coordinate
			ArrayList<Coordinate> tempPath = new ArrayList<>();
			//tempPath.add(carCoord);
			map.setDamage(sourceCoord,0);
			map.setDistance(sourceCoord,0);
				
			//if moving
			//update front and sides as normal cost (in direction of movement)
			//update rear to be cost of coming to a halt +normal cost
			if(speed>0) {
				//update behind
				int tempDamage = 0;
				if( map.getTile(sourceCoord) instanceof LavaTrap){
					tempDamage =LAVADAMAGE;
				}
				/*experiment with removing pointer to self*/
				if(movingForward) {
					updateScore(map,getBehind(sourceCoord, direction), sourceCoord ,tempDamage, 1, tempPath );
					updateScore(map, getFront(sourceCoord, direction), sourceCoord);
				}
				else {
					updateScore(map, getFront(sourceCoord, direction), sourceCoord ,tempDamage, 1, tempPath );
					updateScore(map, getBehind(sourceCoord, direction), sourceCoord);
				}
				//only update left and right if not on grass
				if(!(map.getTile(sourceCoord) instanceof GrassTrap)) {
					updateScore(map, getLeft(sourceCoord, direction), sourceCoord);
					updateScore(map, getRight(sourceCoord, direction), sourceCoord);
				}
			}
			//if stationary
			//update behind and front as normal cost
			//for sides find min of moving forwards than back + normal cost left or right
			else {
				updateScore(map, getFront(sourceCoord, direction),sourceCoord);
				updateScore(map, getBehind(sourceCoord, direction), sourceCoord);
			}

		}
		
		private void updateScore(Map map, Coordinate subjectCoord , Coordinate sourceCoord) {

			if(map.containsCoord(sourceCoord)){
				ArrayList<Coordinate> potentialPath = new ArrayList<Coordinate>();
				updateScore(map , subjectCoord,sourceCoord, map.getDamage(sourceCoord) , map.getDistance(sourceCoord), potentialPath );	
			}

		}

		private void updateScore(Map map, Coordinate subjectCoord , Coordinate sourceCoord, int damage, int distance, ArrayList<Coordinate> path ) {

			//checks if the tile is on the current map
			
			if(map.containsCoord(subjectCoord)) {
				ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
				int potentialDistance = distance;
				int potentialDamage = damage;
				potentialPath.add(sourceCoord);
				
				//only update if the tile is inside the current map is not a Wall or MudTrap
				if( map.getType(subjectCoord) != MapTile.Type.WALL && !(map.getTile(subjectCoord) instanceof MudTrap)) {
					
				
					//if grass tile, keep updating tiles in a straight line until a non grass tile is reached
					if(map.getTile(subjectCoord) instanceof GrassTrap) {
						updateGrassScore(map, subjectCoord, sourceCoord, potentialDamage, potentialDistance, potentialPath);
					}
					else {
						potentialDistance ++;
						//If the tile is lava increase the potential damage of this route
						if(map.getTile(subjectCoord) instanceof LavaTrap){
							potentialDamage= potentialDamage + LAVADAMAGE;
						}
						updateTile(map, subjectCoord, potentialDamage, potentialDistance, potentialPath);
					}
				}

			}
		}
			//removes any already found keys
			
		
		private void updateGrassScore(Map map, Coordinate subjectCoord , Coordinate sourceCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> path) {
			int xdiff = subjectCoord.x -sourceCoord.x;
			int ydiff = subjectCoord.y - sourceCoord.y;
			ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
			Coordinate currentCoord= subjectCoord;
			
			//ensures that tiles outisde the map are not indexed
			while(map.containsCoord(currentCoord) ) {
				
				potentialDistance++;
				potentialPath.add(currentCoord);
				
				if(map.getTile(currentCoord) instanceof GrassTrap) {
					//updates grass tiles
					updateTile(map, currentCoord, potentialDamage, potentialDistance, potentialPath);	
				}
				else if(map.getType(currentCoord) != MapTile.Type.WALL && !(map.getTile(currentCoord) instanceof MudTrap)) {
					//updates the tile reached after the grass tiles
					//If the tile is lava increase the potential damage of this route
					if(map.getTile(currentCoord) instanceof LavaTrap){
						potentialDamage= potentialDamage + LAVADAMAGE;
					}
					updateTile(map, subjectCoord, potentialDamage, potentialDistance, potentialPath);
					break;
				}
				else {
					break;
				}
				//increments the coordinate in a straight line
				currentCoord = new Coordinate(currentCoord.x + xdiff, currentCoord.y + ydiff);
			}
			
		}
		//updates the tile with the given path if preferable
		private void updateTile(Map map, Coordinate currentCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {

			if(potentialDamage <map.getDamage(currentCoord)) {

				map.setDamage(currentCoord, potentialDamage);
				map.setDistance(currentCoord, potentialDistance);
				map.replacePath(currentCoord, potentialPath);
			}
			else if(potentialDamage == map.getDamage(currentCoord)) {

				if(potentialDistance < map.getDistance(currentCoord)) {
					map.setDamage(currentCoord, potentialDamage);
					map.setDistance(currentCoord, potentialDistance);
					map.replacePath(currentCoord, potentialPath);
				}
			}
		}
		
		
		private ArrayList<Coordinate> getDrivableCoords(Map map) {
			ArrayList<Coordinate> tempList = new ArrayList<>();
			for(Coordinate coord : map.getAllCoords()) {
				if(map.getType(coord) != MapTile.Type.WALL){
					tempList.add(coord);
				}
			}
			return tempList;
			
		}
		
		private void findShortestDistances(Map map , ArrayList<Coordinate> unexploredKeys) {
			Coordinate currentCoord;
			while(unexploredKeys.size() !=0) {
				currentCoord = selectLowestScoring(unexploredKeys);
				//possible path
				if(currentCoord != null) {

					updateScore(map , getNorth(currentCoord), currentCoord);
					updateScore(map , getSouth(currentCoord), currentCoord);
					updateScore(map, getEast(currentCoord), currentCoord);
					updateScore(map , getWest(currentCoord), currentCoord);

					unexploredKeys.remove(currentCoord);
				}
				else {
					// breaks if no max value found
					break;
				}
			}
		}
		
	}
	
	
	
	private boolean movingForward;
	int LAVADAMAGE= 1;

	//information about the car at this instance
	private HashMap<Coordinate, MapTile> currentView;
	private Coordinate carCoord;
	private float carSpeed;
	private WorldSpatial.Direction carDirection;
	private Map currentMap;
	private DamageUpdater damageUpdater;
	
	public MyAIController(Car car) {
		super(car);
		//need to remove walls
		currentMap= new Map();
		currentMap.addToMap(getMap(), MapTile.Type.WALL);
		//currentMap.addToMap(getMap(), MapTile.Type.WALL);
		damageUpdater = new DamageUpdater();

		//Adds all the walls to the currentMap
	}

	
	@Override
	public void update() {
		
		//updates necessary info
		carCoord = new Coordinate(getPosition());
		carDirection = getOrientation();
		carSpeed = getSpeed();
		currentView = getView();
		//adds new squares to the explored map and updates the coordinates of points of interest 
		currentMap.addToMap(currentView);
		damageUpdater.updateMap(currentMap, carCoord, carDirection, carSpeed, movingForward);
		determineMove();
		
	}
	
	//logic for determining next move , note currently doesn't look for health tiles
	// selectLowestScoring(healthTiles) can be used
	private void determineMove() {
		
		//holds the lowest scoring coordinate in each category
		Coordinate targetKey = selectLowestScoring(currentMap.getUnexploredKeys(getKeys()));
		Coordinate targetEdge = selectLowestScoring(currentMap.getEdgeTiles());
		Coordinate targetHealth = selectLowestScoring(currentMap.getHealthTiles());
		Coordinate targetExit  = selectLowestScoring(currentMap.getExitTiles());

		//if all the keys have been found
		if(getKeys().size()< numKeys()) {
			
			
			if(targetEdge != null && currentMap.getDamage(targetEdge) == 0) {
				findPath(targetEdge);
			}
			else if(targetKey!= null) {
				findPath(targetKey);
			}
			else {
				findPath(targetEdge);
			}
		}
		//if there are still keys to get
		else{
			if(targetExit!= null) {
				findPath(targetExit);
			}
			else {
				findPath(targetEdge);
			}
		}

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

	
	
	private void findPath(Coordinate coord) {
		Coordinate currentCoord= coord ;
		
		//System.out.println(currentCoord);
		Coordinate nextCoord = currentMap.getPath(currentCoord).get(0);
			//finds the tile leading to the next
			while(!nextCoord.equals(carCoord)) {
				currentCoord = nextCoord;
				//System.out.println(currentCoord);
				nextCoord = currentMap.getPath(currentCoord).get(0);

			}
			//System.out.println(currentMap.get(currentCoord).getPath().toString());
			if(currentMap.getPath(currentCoord).size() >1){
				//destination = Path(1)
				System.out.println(carCoord);
				System.out.println(currentMap.getPath(currentCoord));
				peformMove(currentMap.getPath(currentCoord).get(1));
			}
			else {
				//destination is current coord
				peformMove(currentCoord);
			}
	}
	
	

	//returns lowest scoring item , returns null if lowest scoring is infinity
	private Coordinate selectLowestScoring(ArrayList<Coordinate> listOfKeys){
		int tempDamage = Integer.MAX_VALUE;
		int tempDistance = Integer.MAX_VALUE;
		Coordinate NextCoord = null;
		for(Coordinate key : listOfKeys) {
			if( currentMap.getDamage(key) < tempDamage) {
				tempDamage = currentMap.getDamage(key);
				tempDistance = currentMap.getDistance(key);
				NextCoord = key;
			}
			else if (currentMap.getDamage(key) == tempDamage && tempDamage < Integer.MAX_VALUE) {
				if( currentMap.getDistance(key) < tempDistance) {
					tempDamage = currentMap.getDamage(key);
					tempDistance = currentMap.getDistance(key);
					NextCoord = key;
				}
			}
		}
		return NextCoord;
	}


	
	
	
	
	
	
	//**************** helper functions ***********************************
	

	//checks if a coordinate is inside the map boundaries (Integers)
	public boolean insideBoundries(int x , int y) {
		if(x < mapWidth() & x >= 0 & y < mapHeight() &  y >= 0 ) {
			return true; 
		}
		else {
			return false; 
		}
	}
	public boolean insideBoundries(Coordinate coord) {
		return insideBoundries(coord.x, coord.y);
	}
	
	//checks if coordinate is in the explored set
	public boolean inExploredMap(int x, int y) {
		if(currentMap.containsCoord(new Coordinate(x, y))) {
			return true;
		}
		else {
			return false;
		}
	}
	public boolean inExploredMap(Coordinate coord) {
		if( currentMap.containsCoord(coord)){
			return true;
		}
		else {
			return false;
		}
	}
	
	public Coordinate getLeft(Coordinate coord , WorldSpatial.Direction orientation) {
		switch(orientation){
		case EAST:
			return getNorth(coord);
		case NORTH:
			return getWest(coord);
		case SOUTH:
			return getEast(coord);
		case WEST:
			return getSouth(coord);
		default:
			return null;
		}	
	}
	public Coordinate getRight(Coordinate coord , WorldSpatial.Direction orientation) {
		switch(orientation){
		case EAST:
			return getSouth(coord);
		case NORTH:
			return getEast(coord);
		case SOUTH:
			return getWest(coord);
		case WEST:
			return getNorth(coord);
		default:
			return null;
		}	
	}
	public Coordinate getFront(Coordinate coord , WorldSpatial.Direction orientation) {
		switch(orientation){
		case EAST:
			return getEast(coord);
		case NORTH:
			return getNorth(coord);
		case SOUTH:
			return getSouth(coord);
		case WEST:
			return getWest(coord);
		default:
			return null;
		}	
	}
	public Coordinate getBehind(Coordinate coord , WorldSpatial.Direction orientation) {
		switch(orientation){
		case EAST:
			return getWest(coord);
		case NORTH:
			return getSouth(coord);
		case SOUTH:
			return getNorth(coord);
		case WEST:
			return getEast(coord);
		default:
			return null;
		}	
	}
	
	public Coordinate getEast(Coordinate coord) {
		return new Coordinate(coord.x+1 , coord.y );
	}
	public Coordinate getWest(Coordinate coord) {
		return new Coordinate(coord.x-1 , coord.y );
	}
	public Coordinate getNorth(Coordinate coord) {
		return new Coordinate(coord.x , coord.y +1 );
	}
	public Coordinate getSouth(Coordinate coord) {
		return new Coordinate(coord.x , coord.y -1 );
	}
	//***********************************************************************
	

}
