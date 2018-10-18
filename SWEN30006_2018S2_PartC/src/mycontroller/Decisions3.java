package mycontroller;

import tiles.GrassTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MudTrap;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Set;

import static mycontroller.Direction3.*;


public class Decisions3 {

    private static final int LAVA_DAMAGE = 1;

    public static void determineNextMove(Map3 map, Coordinate carCoord, Set<Integer> keys) {

        calculateDistances(map, carCoord);

        // find the lowest scoring tiles for each target(ie. key, edge, health, exit)
        Coordinate targetKey = selectLowestScoring(map.getKeyTileCoordinates(), map);
        Coordinate targetEdge = selectLowestScoring(map.getEdgeTileCoordinates(), map);
        Coordinate targetHealth = selectLowestScoring(map.getHealthTileCoordinates(), map);

        if (keys.size() < numKeys()) {

            if (targetEdge != null && map.getCurrentMap().get(targetEdge).getDamage() == 0) {
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

            Coordinate targetExit  = selectLowestScoring(map.getExitTileCoordinates(), map);

            if(targetExit != null) {
                findPath(targetExit);
            }
            else {
                findPath(targetEdge);
            }
        }

    }


    private static void calculateDistances(Map3 map, Coordinate carCoord) {

        clearScores(map);
        updateInitialCoords(map);

        ArrayList<Coordinate> unexploredKeys = removeWalls();
        unexploredKeys.remove(carCoord);

        findShortestDistances(map, unexploredKeys);

        //clear previous scores
        //set initial conditions
        //store all possible nodes in a list
        //have different movement conditions per different tiles
        //pick the lowest scoring item, update the tiles surrounding it
        //if a grass tile is reached, iterate through until a non grass tile is reached, (mark the distance for each)
    }

    private static void findShortestDistances(Map3 map, ArrayList<Coordinate> unexploredKeys) {
        Coordinate currentCoord;
        while(unexploredKeys.size() !=0) {
            currentCoord = selectLowestScoring(unexploredKeys, map);
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

    // where the magic happens
    private static Coordinate selectLowestScoring(ArrayList<Coordinate> keyCoordinates, Map3 map){

        int tempDamage = Integer.MAX_VALUE;
        int tempDistance = Integer.MAX_VALUE;
        Coordinate nextCoord = null;

        // find the coordinate of the key tile with the lowest damage to distance ratio
        for (Coordinate coord : keyCoordinates) {

            Map3.TileData3 tile = map.getCurrentMap().get(coord);

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


    private static void updateScore(Coordinate subjectCoord , Coordinate sourceCoord, int damage, int distance, ArrayList<Coordinate> path ) {

        //checks if the tile is on the current map
        if(map.getCurrentMap().containsKey(subjectCoord)) {
            ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
            int potentialDistance = distance;
            int potentialDamage = damage;
            potentialPath.add(sourceCoord);


            //used for
            Map3.TileData3 sourceData = map.getCurrentMap().get(sourceCoord);
            Map3.TileData3 subjectData = map.getCurrentMap().get(subjectCoord);



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

    private static void updateScore(Coordinate subjectCoord , Coordinate sourceCoord) {

        if(currentMap.containsKey(sourceCoord)){
            ArrayList<Coordinate> potentialPath = new ArrayList<Coordinate>();
            updateScore(subjectCoord,sourceCoord, currentMap.get(sourceCoord).getDamage() , currentMap.get(sourceCoord).getDistance(), potentialPath );
        }

    }

    private static void updateGrassScore(Coordinate subjectCoord , Coordinate sourceCoord, int potentialDamage, int potentialDistance, ArrayList<Coordinate> path) {
        int xdiff = subjectCoord.x -sourceCoord.x;
        int ydiff = subjectCoord.y - sourceCoord.y;
        ArrayList<Coordinate> potentialPath = new ArrayList<>(path);
        Coordinate currentCoord= subjectCoord;

        //ensures that tiles outisde the map are not indexed
        while(currentMap.containsKey(currentCoord) ) {

            TileData3 currentData= currentMap.get(currentCoord);
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


    private static void clearScores(Map3 map) {
        for(Coordinate coord: map.getCurrentMap().keySet()) {
            map.getCurrentMap().get(coord).clearScores();
        }
    }


    private static void updateInitialCoords(Map3 map){

        //Initialize starting coordinate
        ArrayList<Coordinate> tempPath = new ArrayList<>();
        //tempPath.add(carCoord);
        map.getCurrentMap().get(carCoord).setDamage(0);
        map.getCurrentMap().get(carCoord).setDistance(0);


        //if moving
        //update front and sides as normal cost (in direction of movement)
        //update rear to be cost of coming to a halt +normal cost
        if(carSpeed>0) {
            //update behind
            int tempDamage = 0;
            if( map.getCurrentMap().get(carCoord).getTile() instanceof LavaTrap){
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
            if(!(map.getCurrentMap().get(carCoord).getTile() instanceof GrassTrap)) {
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

    private static ArrayList<Coordinate> removeWalls() {
        ArrayList<Coordinate> tempList = new ArrayList<>();
        for(Coordinate coord : currentMap.keySet()) {
            if(currentMap.get(coord).getType() != MapTile.Type.WALL){
                tempList.add(coord);
            }
        }
        return tempList;

    }


    private static void findPath(Coordinate coord) {
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

}
