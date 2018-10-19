package mycontroller;

import utilities.Coordinate;

/**
 * Interface used for determining particular escape strategy
 */
public interface EscapeStrategy {
	
	/**
	 * Determines the next coordinate to move to
	 * @param map The map containing all the information about tiles and their respective coordinates
	 * @param nCollectedKeys The number of keys that have already been collected
	 * @param totalKeys The number of keys needed to escape
	 * @param carCoord Current coordinate of the car
	 * @param health Current health of the car
	 * @return The next coordinate to move to
	 */
	 public abstract Coordinate determineNextMove(Map map, int nCollectedKeys, int totalKeys, Coordinate carCoord, float health);
}
