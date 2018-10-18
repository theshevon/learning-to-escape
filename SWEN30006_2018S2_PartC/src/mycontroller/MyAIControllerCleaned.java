package mycontroller;

import java.util.HashMap;
import controller.CarController;
import tiles.*;
import tiles.MapTile;
import tiles.TrapTile;
import tiles.LavaTrap; 
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import java.util.ArrayList;


/**
 * Class used to control the car when escaping the maze
 * @author David Crowe
 */
public class MyAIControllerCleaned extends CarController{
	
	/**
	 * Class used to store information about the tiles. 
	 * Used when determining the shortest route to each tile.
	 */
	public class TileData{

		private int damage;
		private int distance; 
		private MapTile tile;
		private ArrayList<Coordinate> path;
		
		public TileData(MapTile tile) {
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
		 * @param list of coordinates making up the new path
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
		public void resetScores() {
			this.damage = Integer.MAX_VALUE;
			this.distance= Integer.MAX_VALUE;
		}
	}

	private final int LAVA_DAMAGE = 1;
	
	// the information currently known about the map
	public HashMap<Coordinate, TileData> currentMap = new HashMap<>();
	public ArrayList<Coordinate> keyTileCoordinates = new ArrayList<>();
	public ArrayList<Coordinate> edgeTileCoordinates = new ArrayList<>();
	public ArrayList<Coordinate> healthTileCoordinates = new ArrayList<>();
	public ArrayList<Coordinate> exitTileCoordinates = new ArrayList<>(); 
	
	// information about the car at this instance
	private WorldSpatial.Direction carDirection;
	private Coordinate carCoord;
	private float carSpeed;
	private boolean movingForward;
	
	// cleaned
	public MyAIControllerCleaned(Car car) {
		super(car);		

		// adds all the walls to the currentMap
		addToMap(currentMap, getMap(), MapTile.Type.WALL);
	}

	// cleaned
	@Override
	public void update() {
		
		// get the current information about the car, based on its location in the map
		carCoord = new Coordinate(getPosition());
		carDirection = getOrientation();
		carSpeed = getSpeed();
		
		// if the car is on a tile which has a key, remove it from the list of key tile coordinates
		if (keyTileCoordinates.contains(carCoord)) keyTileCoordinates = getUncollectedKeyCoordinates();
		
		// find unexplored regions 
		updateMapData();
		
		// determine where to move to
		determineNextMove();
	}

	// cleaned
	private void determineNextMove() {
				
		// find the lowest scoring tiles for each target(ie. key, edge, health, exit)
		Coordinate targetKey = selectLowestScoring(keyTileCoordinates);
		Coordinate targetEdge = selectLowestScoring(edgeTileCoordinates);
		Coordinate targetHealth = selectLowestScoring(healthTileCoordinates);
		
		if (getKeys().size() < numKeys()) {
			
			if (targetEdge != null && currentMap.get(targetEdge).getDamage() == 0) {
				findPath(targetEdge);
			}
			else if (targetKey != null) {
				findPath(targetKey);
			}
			else {
				findPath(targetEdge);
			}
		}
		
		else{
			
			Coordinate targetExit  = selectLowestScoring(exitTileCoordinates);
			
			if(targetExit != null) {
				findPath(targetExit);
			}
			else {
				findPath(targetEdge);
			}
		}

	}

	// cleaned
	private void calculateDistances() {

		resetScores();
		updateAdjacentTiles();
		
		ArrayList<Coordinate> unexploredCoords = getNonWallCoords();
		unexploredCoords.remove(carCoord);
		
		findShortestDistances(unexploredCoords);
	}
	
	// cleaned
	private void findShortestDistances(ArrayList<Coordinate> unexploredCoords) {
		
		while (unexploredCoords.size() != 0) {
			
			Coordinate currentCoord = selectLowestScoring(unexploredCoords);
			
			// possible path
			if (currentCoord != null) {
				updateScore(getNorth(currentCoord), currentCoord);
				updateScore(getSouth(currentCoord), currentCoord);
				updateScore(getEast(currentCoord), currentCoord);
				updateScore(getWest(currentCoord), currentCoord);
				unexploredCoords.remove(currentCoord);
			}
			else {
				// break if no possible coordinate found
				break;
			}
		}
	}
	
	// cleaned
	private Coordinate selectLowestScoring(ArrayList<Coordinate> keyCoordinates){
		
		int tempDamage = Integer.MAX_VALUE;
		int tempDistance = Integer.MAX_VALUE;
		Coordinate nextCoord = null;
		 
		// find the coordinate of the key tile with the lowest damage to distance ratio
		for (Coordinate coord : keyCoordinates) {
			
			TileData tile = currentMap.get(coord);
			
			if ((tile.getDamage() < tempDamage) ||
					(tile.getDamage() == tempDamage && tempDamage < Integer.MAX_VALUE && 
					tile.getDistance() < tempDistance)) {
				
				tempDamage = tile.getDamage();
				tempDistance = tile.getDistance();
				nextCoord = coord;
			}
		}
			
		return nextCoord;
	}

	// cleaned
	private ArrayList<Coordinate> getNonWallCoords() {
		
		ArrayList<Coordinate> nonWallCoords = new ArrayList<>();
		
		for (Coordinate coord : currentMap.keySet()) {
			if (currentMap.get(coord).getType() != MapTile.Type.WALL){
				nonWallCoords.add(coord);
			}
		}
		return nonWallCoords;
	}
	
	// cleaned
	private void resetScores() {
		for (Coordinate coord : currentMap.keySet()) {
			currentMap.get(coord).resetScores();
		}
	}

	// cleaned
	private void updateAdjacentTiles(){

		ArrayList<Coordinate> tempPath = new ArrayList<>();

		currentMap.get(carCoord).setDamage(0);
		currentMap.get(carCoord).setDistance(0);
	
		
		// if moving
		// update front and sides as normal cost (in direction of movement)
		// update rear to be cost of coming to a halt + normal cost
		if(carSpeed > 0) {
			
			//update behind
			int tempDamage = 0;
			if (currentMap.get(carCoord).getTile() instanceof LavaTrap) tempDamage = LAVA_DAMAGE;
			
			if (movingForward) {
				updateScore(getBehind(carCoord, carDirection), carCoord, tempDamage, 1, tempPath);
				updateScore(getFront(carCoord, carDirection), carCoord);
			}else {
				// moving in reverse
				updateScore(getFront(carCoord, carDirection), carCoord, tempDamage, 1, tempPath);
				updateScore(getBehind(carCoord, carDirection), carCoord);
			}
			
			// only update left and right if not on grass
			if (!(currentMap.get(carCoord).getTile() instanceof GrassTrap)) {
				updateScore(getLeft(carCoord, carDirection), carCoord);
				updateScore(getRight(carCoord, carDirection), carCoord);
			}
		}
		
		// if stationary
		// update behind and front as normal cost
		else {
			updateScore(getFront(carCoord, carDirection),carCoord);
			updateScore(getBehind(carCoord, carDirection), carCoord);
		}

	}

	// cleaned
	/**
	 * @return a list of coordinates of the tiles that contain keys that have not yet been collected
	 */
	private ArrayList<Coordinate> getUncollectedKeyCoordinates() {
		
		ArrayList<Coordinate> remainingKeyCoordinates = new ArrayList<Coordinate>();
		
		for (Coordinate coordinate : keyTileCoordinates) {
			
			if(!getKeys().contains(((LavaTrap)currentMap.get(coordinate).getTile()).getKey())) {
				remainingKeyCoordinates.add(coordinate);
			}
		}
		
		return remainingKeyCoordinates;
	}
	
	// cleaned
	private void findPath(Coordinate coord) {
		
		Coordinate currentCoord = coord;
		Coordinate nextCoord = currentMap.get(currentCoord).getPath().get(0);
		
		// finds the tile leading to the next
		while (!nextCoord.equals(carCoord)) {
			currentCoord = nextCoord;
			nextCoord = currentMap.get(currentCoord).getPath().get(0);
		}
		
		if (currentMap.get(currentCoord).getPath().size() > 1){
			performMove(currentMap.get(currentCoord).getPath().get(1));
		}else {
			//destination is current coord
			performMove(currentCoord);
		}
	}
	
	// cleaned
	private void performMove(Coordinate destination) {
		
		// stop if we've reached the destination
		if (destination.equals(carCoord)) {
			applyBrake();
		}
		
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
	
	private void updateScore(Coordinate targetCoord , Coordinate sourceCoord, int damage, int distance, ArrayList<Coordinate> path ) {

		//checks if the tile is on the current map
		if (currentMap.containsKey(targetCoord)) {
			ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
			int potentialDistance = distance;
			int potentialDamage = damage;
			potentialPath.add(sourceCoord);


			//used for 
			TileData sourceData = currentMap.get(sourceCoord);
			TileData subjectData = currentMap.get(targetCoord);
			

			
			//only update if the tile is inside the current map is not a Wall or MudTrap
			if( subjectData.getType() != MapTile.Type.WALL && !(subjectData.getTile() instanceof MudTrap)) {
				
			
				//if grass tile, keep updating tiles in a straight line until a non grass tile is reached
				if(sourceData.getTile() instanceof GrassTrap) {
					updateGrassScore(targetCoord, sourceCoord, potentialDamage, potentialDistance, potentialPath);
				}
				else {
					potentialDistance ++;
					//If the tile is lava increase the potential damage of this route
					if(subjectData.getTile() instanceof LavaTrap){
						potentialDamage= potentialDamage + LAVA_DAMAGE;
					}
					updateTile(targetCoord, potentialDamage, potentialDistance, potentialPath);
				}
			}

		}
	}
	
	private void updateScore(Coordinate targetCoord , Coordinate sourceCoord) {

		if (currentMap.containsKey(sourceCoord)){
			ArrayList<Coordinate> potentialPath = new ArrayList<Coordinate>();
			updateScore(targetCoord,sourceCoord, currentMap.get(sourceCoord).getDamage(), currentMap.get(sourceCoord).getDistance(), potentialPath);	
		}

	}
	
	private void updateGrassScore(Coordinate targetCoord , Coordinate sourceCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> path) {
		int xdiff = targetCoord.x -sourceCoord.x;
		int ydiff = targetCoord.y - sourceCoord.y;
		ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
		Coordinate currentCoord= targetCoord;
		
		//ensures that tiles outisde the map are not indexed
		while(currentMap.containsKey(currentCoord) ) {
			
			TileData currentData= currentMap.get(currentCoord);
			potentialDistance++;
			potentialPath.add(currentCoord);
			
			if(currentData.getTile() instanceof GrassTrap) {
				//updates grass tiles
				updateTile(currentCoord, potentialDamage, potentialDistance, potentialPath);	
			}
			else if(currentData.getType() != MapTile.Type.WALL && !(currentData.getTile() instanceof MudTrap)) {
				//updates the tile reached after the grass tiles
				//If the tile is lava increase the potential damage of this route
				if(currentData.getTile() instanceof LavaTrap){
					potentialDamage= potentialDamage + LAVA_DAMAGE;
				}
				updateTile(targetCoord, potentialDamage, potentialDistance, potentialPath);
				break;
			}
			else {
				break;
			}
			//increments the coordinate in a straight line
			currentCoord = new Coordinate(currentCoord.x + xdiff, currentCoord.y + ydiff);
		}
		
	}
	
	// cleaned
	private void updateTile(Coordinate currentCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {

		TileData tileData = currentMap.get(currentCoord);
		int currDamage = tileData.getDamage();
		int currDistance = tileData.getDistance();
		
		if ((potentialDamage < currDamage) ||
				(potentialDamage == currDamage && potentialDistance < currDistance)) {

			currentMap.get(currentCoord).setDamage(potentialDamage);
			currentMap.get(currentCoord).setDistance(potentialDistance);
			currentMap.get(currentCoord).replacePath(potentialPath);
		}
	}
	 
	// cleaned
	// adds new tiles to the map and finds new edges
	private void updateMapData() {
			
		// add new possible edge tiles before map is updated
		addPossibleEdges();	
			
		// add the viewed tiles to the current map
		addToMap(currentMap, getView());

		// checks all edges, removing those that are not
		validateEdges();
		
		// finds the distance to each tile
		calculateDistances();
	}
		
	// cleaned
	private void addPossibleEdges() {
			
		for (int xOffset = -4; xOffset <=4; xOffset++) {
			for (int yOffset = -4; yOffset <= 4; yOffset ++) {
					
				if (xOffset > -4 && xOffset < 4 && yOffset >= -3 && yOffset <= 3) continue;
				
				Coordinate coord = new Coordinate(carCoord.x + xOffset, carCoord.y + yOffset);
				if (insideBoundaries(coord) && !inExploredMap(coord)) edgeTileCoordinates.add(coord);
			}
		}
		
	}
	
	// cleaned
	private void validateEdges() {
		
		boolean hasAdjacentUnexploredTile = false;
		ArrayList<Coordinate> nonEdgeCoordinates = new ArrayList<Coordinate>();
		
		for (Coordinate coord : edgeTileCoordinates) {
			
			hasAdjacentUnexploredTile = false; 
			
			MapTile tile = currentMap.get(coord).getTile();
			
			// ensure that no walls or empty tiles can be edges
			if (tile.isType(MapTile.Type.WALL) || tile.isType(MapTile.Type.EMPTY)) {
				nonEdgeCoordinates.add(coord);
			}

			// ensures each edge has at least one adjacent unexplored tile
			else {
				for(int offset : new int[]{-1, 1}) {
					if ((insideBoundaries(coord.x + offset, coord.y) && !inExploredMap(coord.x + offset, coord.y))
					    || (insideBoundaries(coord.x, coord.y+ offset) && !inExploredMap(coord.x , coord.y + offset))) {
						hasAdjacentUnexploredTile = true; 
					}
				}
				
				if(!hasAdjacentUnexploredTile) nonEdgeCoordinates.add(coord);
			}
		}
		
		// remove all non-edges from the list of edge coordinates
		for (Coordinate coord : nonEdgeCoordinates) {
			edgeTileCoordinates.remove(coord);
		}
	}
	
	// cleaned 
	private void analyseTile(Coordinate coord, MapTile tile){
		
		if (tile.isType(MapTile.Type.FINISH)) {
			exitTileCoordinates.add(coord);
		}else if(tile.isType(MapTile.Type.TRAP)){
			
			if (((TrapTile)tile).getTrap() == "lava" && ((LavaTrap)tile).getKey() > 0) {
				keyTileCoordinates.add(coord);
			} else if(((TrapTile)tile).getTrap() == "health" ) {
				healthTileCoordinates.add(coord);
			}
		} 
	}
	
	// =================================== MAP CLASS STUFF ================================================//
	
	// cleaned
	//Adds all the provided Maptile's into the current map
	//Adds coord of points of interests (excluding edge tiles)
	private void addToMap(HashMap<Coordinate, TileData> currentMap, HashMap<Coordinate, MapTile> currentView) {
		
		for (Coordinate coord : currentView.keySet()) {
			
			// adds useful tile coords to appropriate lists
			if (insideBoundaries(coord) && !inExploredMap(coord)) {
				currentMap.put(coord, new TileData(currentView.get(coord)));
				analyseTile(coord, currentView.get(coord));
			}
		}
	}
	
	// cleaned
	// only adds tiles of a certain type
	private void addToMap(HashMap<Coordinate, TileData> currentMap, HashMap<Coordinate, MapTile> fullMap, MapTile.Type type) {
		
		for (Coordinate coord : fullMap.keySet()) {
			if (fullMap.get(coord).getType() == type) currentMap.put(coord, new TileData(fullMap.get(coord)));
		}
	}
		
	// cleaned
	// checks if a coordinate is inside the map boundaries (Integers)
	private boolean insideBoundaries(int x , int y) { return x < mapWidth() && x >= 0 && y < mapHeight() && y >= 0; }
	private boolean insideBoundaries(Coordinate coord) { return insideBoundaries(coord.x, coord.y); }
	
	// cleaned
	// checks if coordinate is in the explored set
	private boolean inExploredMap(int x, int y) { return currentMap.containsKey(new Coordinate(x, y)); }
	private boolean inExploredMap(Coordinate coord) { return currentMap.containsKey(coord); }
	
	// cleaned
	private Coordinate getLeft(Coordinate coord , WorldSpatial.Direction orientation) {
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
	private Coordinate getRight(Coordinate coord , WorldSpatial.Direction orientation) {
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
	private Coordinate getFront(Coordinate coord , WorldSpatial.Direction orientation) {
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
	private Coordinate getBehind(Coordinate coord , WorldSpatial.Direction orientation) {
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
	
	// cleaned
	private Coordinate getEast(Coordinate coord) { return new Coordinate(coord.x+1 , coord.y); }
	private Coordinate getWest(Coordinate coord) { return new Coordinate(coord.x-1 , coord.y); }
	private Coordinate getNorth(Coordinate coord) { return new Coordinate(coord.x , coord.y+1); }
	private Coordinate getSouth(Coordinate coord) { return new Coordinate(coord.x , coord.y-1); }
}
