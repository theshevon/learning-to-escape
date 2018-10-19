package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Set;
import java.math.*;

/**
 * Class used in determining best move to make in order to collect all the keys and escape the map
 *Go for keys if there are still keys to find
 *If all the keys are found , go for exit
 *If there are no keys or exits , go for edges (ie explore the map )
 *Go for health tile if you don't have enough health to reach the target
 */
public class DecisionMaker implements EscapeStrategy{
	
	public DecisionMaker() {};

	@Override
    public Coordinate determineNextMove(Map map, int nCollectedKeys, int totalKeys, Coordinate carCoord, float health) {

        // find the lowest scoring tiles for each target(ie. key, edge, health, exit)
		//ie the lowest by damage then distance 
		//keys are null if there are no reachable coords of that type 
        Coordinate targetKey = selectLowestScoring(map, map.getKeyTileCoordinates());
        Coordinate targetEdge = selectLowestScoring(map, map.getEdgeTileCoordinates());
        Coordinate targetHealth = selectLowestScoring(map, map.getHealthTileCoordinates());
        Coordinate targetExit  = selectLowestScoring(map, map.getExitTileCoordinates());

        Coordinate target = null;

        // If there is still a key to collect set it as the target
        //If you have found all the keys, set target as exit 
        if (nCollectedKeys < totalKeys) {
            target = targetKey;
        }
        else{
            target = targetExit;
        }
       
        // If there are no reachable target, target an edge
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
        //5*LAVA_DAMAGE provides the car with an additional buffer of health to explore with 
        if (map.getTile(carCoord) instanceof HealthTrap && health != 100) {
        	if(health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 5*MapHandler.LAVA_DAMAGE)){
            	return carCoord;
        	}
        }

        // If the location of a health tile is known and the car's health is less than the health needed to 
        // survive getting the next key and returning back, keep the car at its current coordinate
        if (targetHealth != null && health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 5)) {
        	target = targetHealth;
        }
        return map.findPath(target, carCoord);
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
