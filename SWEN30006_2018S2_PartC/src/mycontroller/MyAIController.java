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
		public ArrayList<Coordinate> getPath() {
			return path;
		}
		
	}

	boolean movingForward;
	int LAVADAMAGE= 1;
	//The information currently known about the map
	public HashMap<Coordinate, TileData> currentMap;
	public ArrayList<Coordinate> keyTiles;
	public ArrayList<Coordinate> edgeTiles;
	public ArrayList<Coordinate> healthTiles;
	public ArrayList<Coordinate> exitTiles; 
	//information about the car at this instance
	HashMap<Coordinate, MapTile> currentView;
	Coordinate carCoord;
	float carSpeed;
	WorldSpatial.Direction carDirection;


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
		
		//updates necessary info
		carCoord = new Coordinate(getPosition());
		carDirection = getOrientation();
		carSpeed = getSpeed();
		
		//adds new squares to the explored map and updates the coordinates of points of interest 
		updateMapData();
		determineMove();
		
		
	}
	
	//logic for determining next move , note currently doesn't look for health tiles
	// selectLowestScoring(healthTiles) can be used
	//currently goes straight for keys when it finds them
	private void determineMove() {
		if(getKeys().size()< numKeys()) {
			keyTiles = removeFoundKeys(keyTiles);
			if(keyTiles.size() >0  && selectLowestScoring(keyTiles)!= null) {
				//System.out.println("Looking for keys");
				//System.out.println(currentMap.get(selectLowestScoring(keyTiles)).getPath());
				//System.out.println(selectLowestScoring(keyTiles).toString());
				findPath(selectLowestScoring(keyTiles));
			}
			else {
				//System.out.println("Looking for edges");
				findPath(selectLowestScoring(edgeTiles));
			}
		}
		else{
			if(exitTiles.size() >0  && selectLowestScoring(exitTiles)!= null) {
				findPath(selectLowestScoring(exitTiles));
			}
			else {
				//System.out.println("Looking for edges");
				findPath(selectLowestScoring(edgeTiles));
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
		printInfo();

	}
	//test function
	private void printInfo() {
		for(Coordinate coord :healthTiles) {
			System.out.println("Health at : " + coord.toString() + "  Distance of : " + currentMap.get(coord).getDamage());
		}
		for(Coordinate coord :keyTiles) {
			System.out.println("Keys at : " + coord.toString() + "  Distance of : " + currentMap.get(coord).getDamage());
		}
		for(Coordinate coord :exitTiles) {
			System.out.println("exitTiles at : " + coord.toString() + "  Distance of : " + currentMap.get(coord).getDamage());
		}
	}
	private void printMap() {
		for(Coordinate key : currentMap.keySet()) {
			System.out.println(key.toString() + "   " + currentMap.get(key).getDamage());
		}
		System.out.println("current coord" + carCoord);
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
		calculateDistances();
	}
	
	private void calculateDistances() {

		ArrayList<Coordinate> unexploredKeys;
		clearScores();
		updateInitialCoords();
		unexploredKeys = removeWalls();
		iterate(unexploredKeys);
		
		
		//clear previous scores
		//set initial conditions
		//store all possible nodes in a list
		//have different movement conditions per different tiles
		//pick the lowest scoring item, update the tiles surrounding it
		//if a grass tile is reached, iterate through until a non grass tile is reached, (mark the distance for each)
		
		
	}
	
	private void iterate(ArrayList<Coordinate> unexploredKeys) {
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
	
	//returns lowest scoring item , returns null if lowest scoring is infinity
	private Coordinate selectLowestScoring(ArrayList<Coordinate> listOfKeys){
		int tempDamage = Integer.MAX_VALUE;
		int tempDistance = Integer.MAX_VALUE;
		Coordinate NextCoord = null;
		for(Coordinate key : listOfKeys) {
			if( currentMap.get(key).getDamage() < tempDamage) {
				tempDamage = currentMap.get(key).getDamage();
				tempDistance = currentMap.get(key).getDistance();
				NextCoord = key;
			}
			else if (currentMap.get(key).getDamage() == tempDamage && tempDamage < Integer.MAX_VALUE) {
				if( currentMap.get(key).getDistance() < tempDistance) {
					tempDamage = currentMap.get(key).getDamage();
					tempDistance = currentMap.get(key).getDistance();
					NextCoord = key;
				}
			}
		}
		return NextCoord;
	}

	private ArrayList<Coordinate> removeWalls() {
		ArrayList<Coordinate> tempList = new ArrayList<>();
		for(Coordinate coord : currentMap.keySet()) {
			if(currentMap.get(coord).getType() != MapTile.Type.WALL && coord != carCoord){
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
				tempDamage = LAVADAMAGE;
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
			updateScore(carCoord, getFront(carCoord, carDirection));
			updateScore(carCoord, getBehind(carCoord, carDirection));
		}

	}

	private ArrayList<Coordinate> removeFoundKeys(ArrayList<Coordinate> listOfKeys) {
		ArrayList<Coordinate> tempList = new ArrayList<Coordinate>();
		for( Coordinate key : keyTiles) {
			if(!getKeys().contains(((LavaTrap)currentMap.get(key).getTile()).getKey())) {
				tempList.add(key);
			}
		}
		return tempList;
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
				if(movingForward) {
					turnLeft();
				}
				else {
					turnRight();
				}
			}
			else if(destination.equals(getRight(carCoord, carDirection))) {
				if(movingForward) {
					turnRight();
				}
				else {
					turnLeft();
				}
			}
			else if(destination.equals(getFront(carCoord, carDirection))) {
				if(!movingForward) {
					applyForwardAcceleration();
				}
			}
			else if(destination.equals(getBehind(carCoord, carDirection))) {
				if(movingForward) {
					applyReverseAcceleration();
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
					int xdiff = subjectCoord.x -sourceCoord.x;
					int ydiff = subjectCoord.y - sourceCoord.y;
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
							if(subjectData.getTile() instanceof LavaTrap){
								potentialDamage= potentialDamage + LAVADAMAGE;
								
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
				else {
					potentialDistance ++;
					//If the tile is lava increase the potential damage of this route
					if(subjectData.getTile() instanceof LavaTrap){
						potentialDamage= potentialDamage + LAVADAMAGE;
						
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
	//updates the tile with the given path if preferable
	private void updateTile(Coordinate currentCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {


		if(potentialDamage <currentMap.get(currentCoord).getDamage()) {
			currentMap.get(currentCoord).setDamage(potentialDamage);
			currentMap.get(currentCoord).setDistance(potentialDistance);
			currentMap.get(currentCoord).replacePath(potentialPath);
		}
		else if(potentialDamage == currentMap.get(currentCoord).getDamage()) {
			if(potentialDistance < currentMap.get(currentCoord).getDistance()) {
				currentMap.get(currentCoord).setDistance(potentialDistance);
				currentMap.get(currentCoord).replacePath(potentialPath);
			}
		}
	}
	
	
	//helper functions *************************************************
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
	//***********************************

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
