package mycontroller;

import tiles.*;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.ArrayList;
import java.util.HashMap;

import static mycontroller.Direction.*;

public class MapUpdater {

    private static final int LAVA_DAMAGE = 5;

    // add new tiles, checks for edges
    public static void updateBoard(Map map, Coordinate carCoord, HashMap<Coordinate, MapTile> view) {


        // add new possible edge tiles before map is updated
        addPossibleEdges(map, carCoord);

        // add the viewed tiles to the current map
        addToMap(map, view);

        // checks all edges, removing those that are not
        checkEdges(map);

    }

    //--------------------------------updateBoard Helper Functions-----------------------------------------------
    private static void addPossibleEdges(Map map, Coordinate carCoord) {
        //horizontal and vertical lines
        Coordinate hCoords;
        Coordinate vCoords;
        for( int offset1 : new int[]{-4,4}) {
            for( int offset2 = -4 ; offset2 <=4; offset2++) {

                hCoords= new Coordinate(carCoord.x+ offset1, carCoord.y+ offset2);

                //adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
                if(map.insideBoundries(hCoords) && !map.inExploredMap(hCoords)) {
                    map.getEdgeTileCoordinates().add(hCoords);
                }

            }
            for( int offset3 = -3 ; offset3 <=3; offset3++) {

                vCoords=  new Coordinate(carCoord.x+ offset3, carCoord.y+ offset1);
                //adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
                if(map.insideBoundries(vCoords) && !map.inExploredMap(vCoords)) {
                    map.getEdgeTileCoordinates().add(vCoords);
                }

            }
        }
    }

    //Adds all the provided Maptile's into the current map
    //Adds coord of points of interests (excluding edge tiles)
    public static void addToMap(Map map, HashMap<Coordinate, MapTile> ProvidedMap) {

        for( Coordinate coord : ProvidedMap.keySet()) {
            //adds useful coords to appropriate lists

            if(map.insideBoundries(coord) && !map.inExploredMap(coord)) {
                map.getCurrentMap().put(coord, new CoordinateData(ProvidedMap.get(coord)));
                checkForUseful(map, coord, ProvidedMap.get(coord));
            }
        }
    }

    //Only adds Maptile's of a certain type
    public static void addToMap(Map map, HashMap<Coordinate, MapTile> ProvidedMap, MapTile.Type type) {

        for( Coordinate coord : ProvidedMap.keySet()) {
            if(ProvidedMap.get(coord).getType() == type) {
                map.getCurrentMap().put(coord, new CoordinateData(ProvidedMap.get(coord)));
            }
        }
    }

    private static void checkEdges(Map map) {
        boolean hasUnexplored = false;
        ArrayList<Coordinate> tempList = new ArrayList<Coordinate>();

        for( Coordinate coord: map.getEdgeTileCoordinates()) {

            //Ensures no walls or empty tiles can be edges
            if(map.getCurrentMap().get(coord).getTile().isType(MapTile.Type.WALL)
                    ||map.getCurrentMap().get(coord).getTile().isType(MapTile.Type.EMPTY)) {
                tempList.add(coord);
            }
            //Ensures each edge has at least one adjacent unexplored square
            else {
                for( int offset : new int[]{ -1, 1}) {
                    if( (map.insideBoundries(coord.x + offset, coord.y) && !map.inExploredMap(coord.x + offset, coord.y))
                            ||(map.insideBoundries(coord.x, coord.y+ offset) && !map.inExploredMap(coord.x , coord.y + offset))) {
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
            map.getEdgeTileCoordinates().remove(coord);
        }
    }

    private static void checkForUseful(Map map, Coordinate coord, MapTile tile){

        if(tile.isType(MapTile.Type.FINISH)) {
            map.getExitTileCoordinates().add(coord);
        }
        else if(tile.isType(MapTile.Type.TRAP)){

            if( ((TrapTile)tile).getTrap()== "lava" ) {
                if(((LavaTrap)tile).getKey()>0) {
                    map.getKeyTileCoordinates().add(coord);
                }
            }
            else if(((TrapTile)tile).getTrap()== "health" ) {
                map.getHealthTileCoordinates().add(coord);
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------






    public static void updateScores(Map map, float carSpeed, Coordinate carCoord,
                                    WorldSpatial.Direction carDirection, boolean movingForward) {
        map.resetScores();
        updateInitialCoords(map, carSpeed, carCoord, carDirection, movingForward);

        ArrayList<Coordinate> traversableCoords = removeGrass(map);
        traversableCoords.remove(carCoord);

        findShortestDistances(map, traversableCoords);
    }

    //--------------------------------updateScores Helper Functions-----------------------------------------------
    private static void updateInitialCoords(Map map, float carSpeed, Coordinate carCoord,
                                           WorldSpatial.Direction carDirection, boolean movingForward){

        //Initialize starting coordinate
        ArrayList<Coordinate> tempPath = new ArrayList<>();
        //tempPath.add(carCoord);
        map.getCurrentMap().get(carCoord).setDamage(0);
        map.getCurrentMap().get(carCoord).setDistance(0);


        //if moving
        //updateBoard front and sides as normal cost (in direction of movement)
        //updateBoard rear to be cost of coming to a halt +normal cost
        if(carSpeed>0) {
            //updateBoard behind
            int tempDamage = 0;
            if( map.getCurrentMap().get(carCoord).getTile() instanceof LavaTrap){
                tempDamage = LAVA_DAMAGE;
            }
            /*experiment with removing pointer to self*/
            if(movingForward) {
                updateScore(map, getBehind(carCoord, carDirection), carCoord ,tempDamage, 1, tempPath );
                updateScore(map, getFront(carCoord, carDirection), carCoord);
            }
            else {
                updateScore(map, getFront(carCoord, carDirection), carCoord ,tempDamage, 1, tempPath );
                updateScore(map, getBehind(carCoord, carDirection), carCoord);
            }
            //only updateBoard left and right if not on grass
            if(!(map.getCurrentMap().get(carCoord).getTile() instanceof GrassTrap)) {
                updateScore(map, getLeft(carCoord, carDirection), carCoord);
                updateScore(map, getRight(carCoord, carDirection), carCoord);
            }
        }

        //if stationary
        //updateBoard behind and front as normal cost
        //for sides find min of moving forwards than back + normal cost left or right
        else {
            updateScore(map, getFront(carCoord, carDirection),carCoord);
            updateScore(map, getBehind(carCoord, carDirection), carCoord);
        }

    }


    private static ArrayList<Coordinate> removeGrass(Map map) {
        ArrayList<Coordinate> tempList = new ArrayList<>();
        for(Coordinate coord : map.getAllCoords()) {
            if(!(map.getTile(coord) instanceof GrassTrap)){
                tempList.add(coord);
            }
        }
        return tempList;
    }


    private static void findShortestDistances(Map map, ArrayList<Coordinate> Coords) {
        Coordinate currentCoord;
        while(Coords.size() !=0) {
            currentCoord = Decisions.selectLowestScoring(Coords, map);
            //possible path
            if(currentCoord != null) {
                updateScore(map, getNorth(currentCoord), currentCoord);
                updateScore(map, getSouth(currentCoord), currentCoord);
                updateScore(map, getEast(currentCoord), currentCoord);
                updateScore(map, getWest(currentCoord), currentCoord);
                Coords.remove(currentCoord);
            }
            else {
                //breaks if no max value found
                break;
            }
        }
    }


    private static void updateScore(Map map, Coordinate subjectCoord , Coordinate sourceCoord) {

        if(map.getAllCoords().contains(sourceCoord)) {
            ArrayList<Coordinate> potentialPath = new ArrayList<Coordinate>();
            updateScore(map, subjectCoord, sourceCoord, map.getDamage(sourceCoord) ,
                    map.getDistance(sourceCoord), potentialPath );
        }

    }

    private static void updateScore(Map map, Coordinate subjectCoord , Coordinate sourceCoord, int damage, int distance, ArrayList<Coordinate> path ) {

        //checks if the tile is on the current map

        if(map.getAllCoords().contains(subjectCoord)) {
            ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
            int potentialDistance = distance;
            int potentialDamage = damage;
            potentialPath.add(sourceCoord);

            //only updateBoard if the tile is inside the current map is not a Wall or MudTrap
            if( map.getType(subjectCoord) != MapTile.Type.WALL && !(map.getTile(subjectCoord) instanceof MudTrap)) {


                //if grass tile, keep updating tiles in a straight line until a non grass tile is reached
                if(map.getTile(subjectCoord) instanceof GrassTrap) {
                    updateGrassScore(map, subjectCoord, sourceCoord, potentialDamage, potentialDistance, potentialPath);
                }
                else {
                    potentialDistance ++;
                    //If the tile is lava increase the potential damage of this route
                    if(map.getTile(subjectCoord) instanceof LavaTrap){
                        potentialDamage= potentialDamage + LAVA_DAMAGE;
                    }
                    map.getData(subjectCoord).updateTile(potentialDamage, potentialDistance, potentialPath);
                }
            }

        }
    }


    private static void updateGrassScore(Map map, Coordinate subjectCoord , Coordinate sourceCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> path) {
        int xdiff = subjectCoord.x -sourceCoord.x;
        int ydiff = subjectCoord.y - sourceCoord.y;
        ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
        Coordinate currentCoord= subjectCoord;

        //ensures that tiles outisde the map are not indexed
        while(map.getAllCoords().contains(currentCoord)) {

            potentialDistance++;
            potentialPath.add(currentCoord);

            if(map.getTile(currentCoord) instanceof GrassTrap) {
                //updates grass tiles
                map.getData(currentCoord).updateTile(potentialDamage, potentialDistance, potentialPath);
            }
            else if(map.getType(currentCoord) != MapTile.Type.WALL && !(map.getTile(currentCoord) instanceof MudTrap)) {
                //updates the tile reached after the grass tiles
                //If the tile is lava increase the potential damage of this route
                if(map.getTile(currentCoord) instanceof LavaTrap){
                    potentialDamage= potentialDamage + LAVA_DAMAGE;
                }
                map.getData(currentCoord).updateTile(potentialDamage, potentialDistance, potentialPath);
                break;
            }
            else {
                break;
            }
            //increments the coordinate in a straight line
            currentCoord = new Coordinate(currentCoord.x + xdiff, currentCoord.y + ydiff);
        }

    }


}
