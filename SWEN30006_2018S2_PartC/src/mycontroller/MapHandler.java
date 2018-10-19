package mycontroller;

import tiles.*;
import utilities.Coordinate;
import world.WorldSpatial;
import java.util.ArrayList;
import java.util.HashMap;
import static mycontroller.Direction.*;

/**
 * Class that handles map updates and retrievals
 */
public class MapHandler {

    private static final int LAVA_DAMAGE = 5;
    private static final int VIEW_GRID_SIZE = 9;

    /**
     * Adds new tiles to the map and checks for possible edge tiles (ones adjacent to unexplored tiles)
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param carCoordinate Current coordinate of the car
     * @param view List of MapTiles in a grid of coordinates surrounding the car's current coordinate
     */
    public static void updateMap(Map map, Coordinate carCoordinate, HashMap<Coordinate, MapTile> view) {

        // Add new possible edge tiles before map is updated
        addPossibleEdges(map, carCoordinate);

        // Add the viewed tiles to the current map
        addToMap(map, view);

        // Check all edges coordinates and removing those that are not edges
        validateEdges(map);
    }
    
    /**
     * Adds the coordinates of the edges of the 9x9 grid that the car can view
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param carCoordinate Current coordinate of the car
     */
    private static void addPossibleEdges(Map map, Coordinate carCoordinate) {
		
		for (int xOffset = -VIEW_GRID_SIZE/2; xOffset <=VIEW_GRID_SIZE/2; xOffset++) {
			for (int yOffset = -VIEW_GRID_SIZE/2; yOffset <= VIEW_GRID_SIZE/2; yOffset ++) {
				
				// Ignore all non-boundary coordinates
				if (xOffset > -VIEW_GRID_SIZE/2 && xOffset < VIEW_GRID_SIZE/2 && yOffset >= -(VIEW_GRID_SIZE/2 - 1) && yOffset <= (VIEW_GRID_SIZE/2 - 1)) continue;
				
				Coordinate coordinate = new Coordinate(carCoordinate.x + xOffset, carCoordinate.y + yOffset);
				if (map.insideBoundaries(coordinate) && !map.inExploredMap(coordinate)) map.edgeTileCoordinates.add(coordinate);
			}
		}
	}


    /**
     * Adds all the provided MapTiles into the current map and notes coordinates of special tiles (excluding edge tiles) 
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param providedMap The map containing all the information about Wall tiles and their respective coordinates
     */
    public static void addToMap(Map map, HashMap<Coordinate, MapTile> providedMap) {

        for (Coordinate coordinate : providedMap.keySet()) {
        	
        	// Analyse unexplored tiles to check if they are useful
            if(map.insideBoundaries(coordinate) && !map.inExploredMap(coordinate)) {
                map.getCurrentMap().put(coordinate, new CoordinateData(providedMap.get(coordinate)));
                analyseTile(map, coordinate, providedMap.get(coordinate));
            }
        }
    }

    /**
     * Adds MapTiles of a certain type to the map
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param providedMap The initial map that was provided
     * @param type Type of MapTile
     */
    public static void addToMap(Map map, HashMap<Coordinate, MapTile> providedMap, MapTile.Type type) {

        for (Coordinate coordinate : providedMap.keySet()) {
            if (providedMap.get(coordinate).getType() == type) map.getCurrentMap().put(coordinate, new CoordinateData(providedMap.get(coordinate)));
        }
    }

    /**
     * Validates the edge tiles to ensure that all coordinates in the list are in fact edges
     * @param map A map containing all the information about tiles and their respective coordinates
     */
    private static void validateEdges(Map map) {
		
		boolean hasAdjacentUnexploredTile = false;
		ArrayList<Coordinate> nonEdgeCoordinates = new ArrayList<Coordinate>();
		
		for (Coordinate coordinate : map.getEdgeTileCoordinates()) {
			
			hasAdjacentUnexploredTile = false; 
			
			MapTile tile = map.getCurrentMap().get(coordinate).getTile();
			
			// Ensure that no walls or empty tiles can be edges
			if (tile.isType(MapTile.Type.WALL) || tile.isType(MapTile.Type.EMPTY)) {
				nonEdgeCoordinates.add(coordinate);
			}
			// Ensure each edge has at least one adjacent unexplored tile
			else {
				
				for (int offset : new int[]{-1, 1}) {
					if ((map.insideBoundaries(coordinate.x + offset, coordinate.y) && !map.inExploredMap(coordinate.x + offset, coordinate.y))
							|| (map.insideBoundaries(coordinate.x, coordinate.y+ offset) && !map.inExploredMap(coordinate.x , coordinate.y + offset))) {
						hasAdjacentUnexploredTile = true; 
					}
				}
				
				if (!hasAdjacentUnexploredTile) nonEdgeCoordinates.add(coordinate);
			}
		}
		
		// Remove all non-edges from the list of edge coordinates
		for (Coordinate coordinate : nonEdgeCoordinates) {
			map.getEdgeTileCoordinates().remove(coordinate);
		}
	}

    /**
     * Analyses a tile and if useful(exit/ key/ health), adds it to an appropriate list
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param coordinate The coordinate of the MapTile 
     * @param tile MapTile to be analysed
     */
    private static void analyseTile(Map map, Coordinate coordinate, MapTile tile){
		
		if (tile.isType(MapTile.Type.FINISH)) {
			map.getExitTileCoordinates().add(coordinate);
		}else if(tile.isType(MapTile.Type.TRAP)){
			
			if (((TrapTile)tile).getTrap() == "lava" && ((LavaTrap)tile).getKey() > 0) {
				map.getKeyTileCoordinates().add(coordinate);
			} else if(((TrapTile)tile).getTrap() == "health" ) {
				map.getHealthTileCoordinates().add(coordinate);
			}
		} 
	}

    /**
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param carSpeed The current speed of the car
     * @param carCoordinate The current coordinate of the car
     * @param carDirection The current orientation of the car
     * @param movingForward Whether or not the car is moving forward
     */
    public static void updateScores(Map map, float carSpeed, Coordinate carCoordinate,
                                    WorldSpatial.Direction carDirection, boolean movingForward) {
        
    	map.resetScores();
        updateAdjacentTiles(map, carSpeed, carCoordinate, carDirection, movingForward);

        ArrayList<Coordinate> traversableCoordinates = getNonGrassTileCoordinates(map);
        traversableCoordinates.remove(carCoordinate);

        findShortestDistances(map, traversableCoordinates);
    }

    /**
     * ???????????????????????????????????NEEDS TO BE COMMENTED?????????????????????????????????????????????
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param carSpeed The current speed of the car
     * @param carCoordinate The current coordinate of the car
     * @param carDirection The current orientation of the car
     * @param movingForward Whether or not the car is moving forward
     */
    private static void updateAdjacentTiles(Map map, float carSpeed, Coordinate carCoordinate,
            							WorldSpatial.Direction carDirection, boolean movingForward){

    	// Initialise starting coordinates
		ArrayList<Coordinate> tempPath = new ArrayList<>();

		map.getCurrentMap().get(carCoordinate).setDamage(0);
        map.getCurrentMap().get(carCoordinate).setDistance(0);
	
		// if moving
		// update front and sides as normal cost (in direction of movement)
		// update rear to be cost of coming to a halt + normal cost
		if (carSpeed > 0) {
			
			// update behind
			int tempDamage = 0;
			if (map.getCurrentMap().get(carCoordinate).getTile() instanceof LavaTrap) tempDamage = LAVA_DAMAGE;
			
			if(movingForward) {
                updateScore(map, getBehind(carCoordinate, carDirection), carCoordinate ,tempDamage, 1, tempPath);
                updateScore(map, getFront(carCoordinate, carDirection), carCoordinate);
            }
            else {
            	// Moving in reverse
                updateScore(map, getFront(carCoordinate, carDirection), carCoordinate ,tempDamage, 1, tempPath);
                updateScore(map, getBehind(carCoordinate, carDirection), carCoordinate);
            }
			
			// Only update left and right if not on grass
			if(!(map.getCurrentMap().get(carCoordinate).getTile() instanceof GrassTrap)) {
                updateScore(map, getLeft(carCoordinate, carDirection), carCoordinate);
                updateScore(map, getRight(carCoordinate, carDirection), carCoordinate);
            }
		}
		
		// if stationary
		// update behind and front with normal costs
		else {
			updateScore(map, getFront(carCoordinate, carDirection), carCoordinate);
			updateScore(map, getBehind(carCoordinate, carDirection), carCoordinate);
		}
	}

    /**
     * @param map A map containing all the information about tiles and their respective coordinates
     * @return A list of coordinates of non-grass tiles
     */
    private static ArrayList<Coordinate> getNonGrassTileCoordinates(Map map) {
        
    	ArrayList<Coordinate> nonGrassTileCoordinates = new ArrayList<>();
    	
    	// If the tile is not a grass tile, add it to the list
        for (Coordinate coordinate : map.getAllCoordinates()) {
            if (!(map.getTile(coordinate) instanceof GrassTrap)){
            	nonGrassTileCoordinates.add(coordinate);
            }
        }
        return nonGrassTileCoordinates;
    }

    /**
     * ???????????????????????????????????NEEDS TO BE COMMENTED?????????????????????????????????????????????
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param Coordinates
     */
    private static void findShortestDistances(Map map, ArrayList<Coordinate> Coordinates) {
        Coordinate currentCoordinate;
        
        while (Coordinates.size() != 0) {
        	
            currentCoordinate = DecisionMaker.selectLowestScoring(map, Coordinates);
            if (currentCoordinate != null) {
                updateScore(map, getNorth(currentCoordinate), currentCoordinate);
                updateScore(map, getSouth(currentCoordinate), currentCoordinate);
                updateScore(map, getEast(currentCoordinate), currentCoordinate);
                updateScore(map, getWest(currentCoordinate), currentCoordinate);
                Coordinates.remove(currentCoordinate);
            }
            else {
                // break if no possible coordinate found
                break;
            }
        }
    }

    /**
     * ???????????????????????????????????NEEDS TO BE COMMENTED?????????????????????????????????????????????
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param targetCoordinate Coordinate of target point
     * @param sourceCoordinate Coordinate of starting point
     */
    private static void updateScore(Map map, Coordinate targetCoordinate, Coordinate sourceCoordinate) {

        if(map.getAllCoordinates().contains(sourceCoordinate)) {
            ArrayList<Coordinate> potentialPath = new ArrayList<Coordinate>();
            updateScore(map, targetCoordinate, sourceCoordinate, map.getDamage(sourceCoordinate) ,
                    map.getDistance(sourceCoordinate), potentialPath );
        }

    }

    /**
     * ???????????????????????????????????NEEDS TO BE COMMENTED?????????????????????????????????????????????
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param targetCoordinate Coordinate of target point
     * @param sourceCoordinate Coordinate of starting point
     * @param damage 
     * @param distance
     * @param path
     */
    private static void updateScore(Map map, Coordinate targetCoordinate, Coordinate sourceCoordinate, int damage, int distance, ArrayList<Coordinate> path ) {

        //checks if the tile is on the current map

        if (map.getAllCoordinates().contains(targetCoordinate)) {
            ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
            int potentialDistance = distance;
            int potentialDamage = damage;
            potentialPath.add(sourceCoordinate);

            //only updateBoard if the tile is inside the current map is not a Wall or MudTrap
            if( map.getType(targetCoordinate) != MapTile.Type.WALL && !(map.getTile(targetCoordinate) instanceof MudTrap)) {


                //if grass tile, keep updating tiles in a straight line until a non grass tile is reached
                if(map.getTile(targetCoordinate) instanceof GrassTrap) {
                    updateGrassScore(map, targetCoordinate, sourceCoordinate, potentialDamage, potentialDistance, potentialPath);
                }
                else {
                    potentialDistance ++;
                    //If the tile is lava increase the potential damage of this route
                    if(map.getTile(targetCoordinate) instanceof LavaTrap){
                        potentialDamage= potentialDamage + LAVA_DAMAGE;
                    }
                    map.getData(targetCoordinate).updateTile(potentialDamage, potentialDistance, potentialPath);
                }
            }

        }
    }


    /**
     *  ???????????????????????????????????NEEDS TO BE COMMENTED?????????????????????????????????????????????
     * @param map A map containing all the information about tiles and their respective coordinates
     * @param targetCoordinate Coordinate of target point
     * @param sourceCoordinate Coordinate of starting point
     * @param potentialDamage
     * @param potentialDistance
     * @param path
     */
    private static void updateGrassScore(Map map, Coordinate targetCoordinate, Coordinate sourceCoordinate, int potentialDamage, int potentialDistance, ArrayList<Coordinate> path) {
        int xdiff = targetCoordinate.x -sourceCoordinate.x;
        int ydiff = targetCoordinate.y - sourceCoordinate.y;
        ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
        Coordinate currentCoordinate= targetCoordinate;

        //ensures that tiles outisde the map are not indexed
        while(map.getAllCoordinates().contains(currentCoordinate)) {

            potentialDistance++;
            potentialPath.add(currentCoordinate);

            if(map.getTile(currentCoordinate) instanceof GrassTrap) {
                //updates grass tiles
                map.getData(currentCoordinate).updateTile(potentialDamage, potentialDistance, potentialPath);
            }
            else if(map.getType(currentCoordinate) != MapTile.Type.WALL && !(map.getTile(currentCoordinate) instanceof MudTrap)) {
                //updates the tile reached after the grass tiles
                //If the tile is lava increase the potential damage of this route
                if(map.getTile(currentCoordinate) instanceof LavaTrap){
                    potentialDamage= potentialDamage + LAVA_DAMAGE;
                }
                map.getData(currentCoordinate).updateTile(potentialDamage, potentialDistance, potentialPath);
                break;
            }
            else {
                break;
            }
            //increments the coordinate in a straight line
            currentCoordinate = new Coordinate(currentCoordinate.x + xdiff, currentCoordinate.y + ydiff);
        }

    }


}