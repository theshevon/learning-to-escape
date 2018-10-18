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
		public void clearScores() {
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
	
	public MyAIControllerCleaned(Car car) {
		super(car);		

		// adds all the walls to the currentMap
		addToMap(currentMap, getMap(), MapTile.Type.WALL);
	}

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

	// add new tiles, checks for edges
	private void updateMapData() {
		
		// add new possible edge tiles before map is updated
		addPossibleEdges();	
		
		// add the viewed tiles to the current map
		addToMap(currentMap, getView());

		// checks all edges, removing those that are not
		checkEdges();
		calculateDistances();
	}
	
	private void calculateDistances() {

		clearScores();
		updateInitialCoords();
		
		ArrayList<Coordinate> unexploredKeys = removeWalls();
		unexploredKeys.remove(carCoord);
		
		findShortestDistances(unexploredKeys);
		
		//clear previous scores
		//set initial conditions
		//store all possible nodes in a list
		//have different movement conditions per different tiles
		//pick the lowest scoring item, update the tiles surrounding it
		//if a grass tile is reached, iterate through until a non grass tile is reached, (mark the distance for each)
	}
	
	private void findShortestDistances(ArrayList<Coordinate> unexploredKeys) {
		Coordinate currentCoord;
		while(unexploredKeys.size() !=0) {
			currentCoord = selectLowestScoring(unexploredKeys);
			//possible path
			if(currentCoord != null) {
				updateScore(getNorth(currentCoord), currentCoord);
				updateScore(getSouth(currentCoord), currentCoord);
				updateScore(getEast(currentCoord), currentCoord);
				updateScore(getWest(currentCoord), currentCoord);
				unexploredKeys.remove(currentCoord);
			}
			else {
				//breaks if no max value found
				break;
			}
		}
	}
	
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

	private ArrayList<Coordinate> removeWalls() {
		ArrayList<Coordinate> tempList = new ArrayList<>();
		for(Coordinate coord : currentMap.keySet()) {
			if(currentMap.get(coord).getType() != MapTile.Type.WALL){
				tempList.add(coord);
			}
		}
		return tempList;
		
	}
	
	private void clearScores() {
		for(Coordinate coord: currentMap.keySet()) {
			currentMap.get(coord).clearScores();
		}
	}

	private void updateInitialCoords(){

		//Initialize starting coordinate
		ArrayList<Coordinate> tempPath = new ArrayList<>();
		//tempPath.add(carCoord);
		currentMap.get(carCoord).setDamage(0);
		currentMap.get(carCoord).setDistance(0);
	
		
		//if moving
		//update front and sides as normal cost (in direction of movement)
		//update rear to be cost of coming to a halt +normal cost
		if(carSpeed>0) {
			//update behind
			int tempDamage = 0;
			if( currentMap.get(carCoord).getTile() instanceof LavaTrap){
				tempDamage =LAVA_DAMAGE;
			}
			/*experiment with removing pointer to self*/
			if(movingForward) {
				updateScore(getBehind(carCoord, carDirection), carCoord ,tempDamage, 1, tempPath );
				updateScore(getFront(carCoord, carDirection), carCoord);
			}
			else {
				updateScore(getFront(carCoord, carDirection), carCoord ,tempDamage, 1, tempPath );
				updateScore(getBehind(carCoord, carDirection), carCoord);
			}
			//only update left and right if not on grass
			if(!(currentMap.get(carCoord).getTile() instanceof GrassTrap)) {
				updateScore(getLeft(carCoord, carDirection), carCoord);
				updateScore(getRight(carCoord, carDirection), carCoord);
			}
		}
		
		//if stationary
		//update behind and front as normal cost
		//for sides find min of moving forwards than back + normal cost left or right
		else {
			updateScore(getFront(carCoord, carDirection),carCoord);
			updateScore(getBehind(carCoord, carDirection), carCoord);
		}

	}

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
	
	private void findPath(Coordinate coord) {
		Coordinate currentCoord= coord ;
		
		//System.out.println(currentCoord);
		Coordinate nextCoord = currentMap.get(currentCoord).getPath().get(0);
			//finds the tile leading to the next
			while(!nextCoord.equals(carCoord)) {
				currentCoord = nextCoord;
				//System.out.println(currentCoord);
				nextCoord = currentMap.get(currentCoord).getPath().get(0);

			}
			//System.out.println(currentMap.get(currentCoord).getPath().toString());
			if(currentMap.get(currentCoord).getPath().size() >1){
				//destination = Path(1)
				System.out.println(currentCoord);
				peformMove(currentMap.get(currentCoord).getPath().get(1));
			}
			else {
				//destination is current coord
				peformMove(currentCoord);
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
	
	private void updateScore(Coordinate subjectCoord , Coordinate sourceCoord, int damage, int distance, ArrayList<Coordinate> path ) {

		//checks if the tile is on the current map
		if(currentMap.containsKey(subjectCoord)) {
			ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
			int potentialDistance = distance;
			int potentialDamage = damage;
			potentialPath.add(sourceCoord);


			//used for 
			TileData sourceData = currentMap.get(sourceCoord);
			TileData subjectData = currentMap.get(subjectCoord);
			

			
			//only update if the tile is inside the current map is not a Wall or MudTrap
			if( subjectData.getType() != MapTile.Type.WALL && !(subjectData.getTile() instanceof MudTrap)) {
				
			
				//if grass tile, keep updating tiles in a straight line until a non grass tile is reached
				if(sourceData.getTile() instanceof GrassTrap) {
					updateGrassScore(subjectCoord, sourceCoord, potentialDamage, potentialDistance, potentialPath);
				}
				else {
					potentialDistance ++;
					//If the tile is lava increase the potential damage of this route
					if(subjectData.getTile() instanceof LavaTrap){
						potentialDamage= potentialDamage + LAVA_DAMAGE;
					}
					updateTile(subjectCoord, potentialDamage, potentialDistance, potentialPath);
				}
			}

		}
	}
	
	private void updateScore(Coordinate subjectCoord , Coordinate sourceCoord) {

		if(currentMap.containsKey(sourceCoord)){
			ArrayList<Coordinate> potentialPath = new ArrayList<Coordinate>();
			updateScore(subjectCoord,sourceCoord, currentMap.get(sourceCoord).getDamage() , currentMap.get(sourceCoord).getDistance(), potentialPath );	
		}

	}
	
	private void updateGrassScore(Coordinate subjectCoord , Coordinate sourceCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> path) {
		int xdiff = subjectCoord.x -sourceCoord.x;
		int ydiff = subjectCoord.y - sourceCoord.y;
		ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
		Coordinate currentCoord= subjectCoord;
		
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
				updateTile(subjectCoord, potentialDamage, potentialDistance, potentialPath);
				break;
			}
			else {
				break;
			}
			//increments the coordinate in a straight line
			currentCoord = new Coordinate(currentCoord.x + xdiff, currentCoord.y + ydiff);
		}
		
	}
	
	private void updateTile(Coordinate currentCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {

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
	 
	private void addPossibleEdges() {
		//horizontal and vertical lines 
		Coordinate hCoords;
		Coordinate vCoords;
		for( int offset1 : new int[]{-4,4}) {
			for( int offset2 = -4 ; offset2 <=4; offset2++) {
				
				hCoords=  new Coordinate(carCoord.x+ offset1, carCoord.y+ offset2);

				//adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
				if(insideBoundries(hCoords) && !inExploredMap(hCoords)) {
					edgeTileCoordinates.add(hCoords);
				}

			}
			for( int offset3 = -3 ; offset3 <=3; offset3++) {
				
				vCoords=  new Coordinate(carCoord.x+ offset3, carCoord.y+ offset1);
				//adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
				if(insideBoundries(vCoords) && !inExploredMap(vCoords)) {
					edgeTileCoordinates.add(vCoords);
				}

			}
		}
	}
	
	private void checkEdges() {
		boolean hasUnexplored = false;
		ArrayList<Coordinate> tempList = new ArrayList<Coordinate>();
		
		for( Coordinate coord: edgeTileCoordinates) {
			
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
			edgeTileCoordinates.remove(coord);
		}
	}
	
	private void checkForUseful(Coordinate coord, MapTile tile){
		
		if(tile.isType(MapTile.Type.FINISH)) {
			exitTileCoordinates.add(coord);
		}
		else if(tile.isType(MapTile.Type.TRAP)){
			
			if( ((TrapTile)tile).getTrap()== "lava" ) {
				if(((LavaTrap)tile).getKey()>0) {
					keyTileCoordinates.add(coord);
				}
			}
			else if(((TrapTile)tile).getTrap()== "health" ) {
				healthTileCoordinates.add(coord);
			}
		} 
	}

	// =================================== MAP CLASS STUFF ================================================//
	
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
		return x < mapWidth() && x >= 0 && y < mapHeight() && y >= 0;
	}
	private boolean insideBoundries(Coordinate coord) {
		return insideBoundries(coord.x, coord.y);
	}
	
	//checks if coordinate is in the explored set
	private boolean inExploredMap(int x, int y) {
		return currentMap.containsKey(new Coordinate(x, y));
	}
	private boolean inExploredMap(Coordinate coord) {
		return currentMap.containsKey(coord);
	}
	
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
	
	private Coordinate getEast(Coordinate coord) {
		return new Coordinate(coord.x+1 , coord.y );
	}
	private Coordinate getWest(Coordinate coord) {
		return new Coordinate(coord.x-1 , coord.y );
	}
	private Coordinate getNorth(Coordinate coord) {
		return new Coordinate(coord.x , coord.y +1 );
	}
	private Coordinate getSouth(Coordinate coord) {
		return new Coordinate(coord.x , coord.y -1 );
	}

	

}
