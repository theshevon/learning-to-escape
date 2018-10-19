package mycontroller;

import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Set;

/**
 * Class used in determining best move to make in order to collect all the keys and escape the map
 */
public class DecisionMaker implements EscapeStrategy{
	
	//	private final int X = 5;
	//	private final int Y = 25;

	public DecisionMaker() {};

	@Override
    public Coordinate determineNextMove(Map map, int nCollectedKeys, int totalKeys, Coordinate carCoord, float health) {

        // find the lowest scoring tiles for each target(ie. key, edge, health, exit)
        Coordinate targetKey = selectLowestScoring(map, map.getKeyTileCoordinates());
        Coordinate targetEdge = selectLowestScoring(map, map.getEdgeTileCoordinates());
        Coordinate targetHealth = selectLowestScoring(map, map.getHealthTileCoordinates());

        Coordinate target = null;

        // If there's a key haven't collected and the location of the exit is known, set it as the target
        if (nCollectedKeys < totalKeys && targetKey != null) {
            target = targetKey;
        } else {
        	// If all keys have been collected and the location of the exit is known, set it as the target
            Coordinate targetExit  = selectLowestScoring(map, map.getExitTileCoordinates());
            if (targetExit != null) target = targetExit;
        }
       
        // If no tile is being targeted, target an edge
        if (target == null) {
        	target = targetEdge;
        }
        
        // If the car is on a health tile and its health is less than the health needed to survive getting
        // the next key and returning back, keep the car at its current coordinate
        if (map.getTile(carCoord) instanceof HealthTrap && health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 25)) {
        	return carCoord;
        }

        // If the location of a health tile is known and the car's health is less than the health needed to 
        // survive getting the next key and returning back, keep the car at its current coordinate
        if (targetHealth != null && health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 5)) {
        	target = targetHealth;
        }

        // If the map cannot indefinitely be escaped, exit the system
        try {
            return map.findPath(target, carCoord);
        } catch (NullPointerException e) {
            System.out.println("Nowhere to go :(");
            System.exit(0);
        }

        return null;
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
