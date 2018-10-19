package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Set;
import java.math.*;

/**
 * Class used in determining best move to make in order to collect all the keys and escape the map
 */
public class DecisionMaker implements EscapeStrategy{
	
	public DecisionMaker() {};

	@Override
    public Coordinate determineNextMove(Map map, int nCollectedKeys, int totalKeys, Coordinate carCoord, float health) {

        // find the lowest scoring tiles for each target(ie. key, edge, health, exit)
		//keys are null if there are no reachable coords of that type 
        Coordinate targetKey = selectLowestScoring(map, map.getKeyTileCoordinates());
        Coordinate targetEdge = selectLowestScoring(map, map.getEdgeTileCoordinates());
        Coordinate targetHealth = selectLowestScoring(map, map.getHealthTileCoordinates());
        Coordinate targetExit  = selectLowestScoring(map, map.getExitTileCoordinates());

        Coordinate target = null;

        // If there's a key haven't collected and the location of the exit is known, set it as the target
        if (nCollectedKeys < totalKeys) {
            target = targetKey;
        }
        else{
            target = targetExit;
        }
       
        // If no tile is being targeted, target an edge
        if (target == null) {
            	target = targetEdge;
        }    
        //If there is still no reachable target , there is nowhere to go
        if(target == null) {
            System.out.println("Nowhere to go :(");
            System.exit(0);
        }
        // If the car is on a health tile and its health is less than the health needed to survive getting
        // the next key and returning back, keep the car at its current coordinate
        if (map.getTile(carCoord) instanceof HealthTrap && health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + Math.pow(MapHandler.LAVA_DAMAGE,2))) {
        	return carCoord;
        }

        // If the location of a health tile is known and the car's health is less than the health needed to 
        // survive getting the next key and returning back, keep the car at its current coordinate
        if (targetHealth != null && health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 5)) {
        	target = targetHealth;
        }
        return map.findPath(target, carCoord);

        // If the map cannot indefinitely be escaped, exit the system
        //try {
          //  return map.findPath(target, carCoord);
        //} catch (NullPointerException e) {
          //  System.out.println("Nowhere to go :(");
            //System.exit(0);
        //}

        //return null;
    }

    /**
     * Finds the MapTile with the lowest score (damage to distance ratio) from a list of MapTile coordinates
     * @param map The map containing all the information about tiles and their respective coordinates
     * @param coordinates List of coordinates of a certain category of MapTile(ie Key, Edge, Health, Exit)
     * @return Coordinate of the lowest scoring tile
     */
    public static Coordinate selectLowestScoring(Map map, ArrayList<Coordinate> coordinates){

        int tempDamage = Integer.MAX_VALUE;
        int tempDistance = Integer.MAX_VALUE;
        Coordinate nextCoord = null;

        // Find the coordinate of the tile with the lowest damage to distance ratio
        for (Coordinate coord : coordinates) {

            CoordinateData tile = map.getData(coord);

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
}
