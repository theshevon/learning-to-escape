package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

/**
 * Class used in determining coordinates relative to a given coordinate
 */
public class Direction {

	/**
	 * Returns the coordinate that's left of the given coordinate, relative to the orientation of the 
	 * car
	 * @param coordinate Current coordinate of the car
	 * @param orientation Current orientation of the car
	 * @return The coordinate left of the given coordinate, relative to the car's orientation
	 */
    public static Coordinate getLeft(Coordinate coordinate , WorldSpatial.Direction orientation) {
        switch (orientation){
            case EAST:
                return getNorth(coordinate);
            case NORTH:
                return getWest(coordinate);
            case SOUTH:
                return getEast(coordinate);
            case WEST:
                return getSouth(coordinate);
            default:
                return null;
        }
    }
    
    /**
	 * Returns the coordinate that's right of the given coordinate, relative to the orientation of the 
	 * car
	 * @param coordinate Current coordinate of the car
	 * @param orientation Current orientation of the car
	 * @return The coordinate right of the given coordinate, relative to the car's orientation 
	 */
    public static Coordinate getRight(Coordinate coordinate , WorldSpatial.Direction orientation) {
        switch (orientation){
            case EAST:
                return getSouth(coordinate);
            case NORTH:
                return getEast(coordinate);
            case SOUTH:
                return getWest(coordinate);
            case WEST:
                return getNorth(coordinate);
            default:
                return null;
        }
    }
    
    /**
	 * Returns the coordinate that's in front of the given coordinate, relative to the orientation of the 
	 * car
	 * @param coordinate Current coordinate of the car
	 * @param orientation Current orientation of the car
	 * @return The coordinate in front of the given coordinate, relative to the car's orientation
	 */
    public static Coordinate getFront(Coordinate coordinate , WorldSpatial.Direction orientation) {
        switch (orientation){
            case EAST:
                return getEast(coordinate);
            case NORTH:
                return getNorth(coordinate);
            case SOUTH:
                return getSouth(coordinate);
            case WEST:
                return getWest(coordinate);
            default:
                return null;
        }
    }
    
    /**
	 * Returns the coordinate that's behind the given coordinate, relative to the orientation of the 
	 * car
	 * @param coordinate Current coordinate of the car
	 * @param orientation Current orientation of the car
	 * @return The coordinate behind the given coordinate, relative to the car's orientation 
	 */
    public static Coordinate getBehind(Coordinate coordinate , WorldSpatial.Direction orientation) {
        switch (orientation){
            case EAST:
                return getWest(coordinate);
            case NORTH:
                return getSouth(coordinate);
            case SOUTH:
                return getNorth(coordinate);
            case WEST:
                return getEast(coordinate);
            default:
                return null;
        }
    }

    /**
	 * Returns the coordinate that's east of the given coordinate
	 * @param coordinate Current coordinate of the car
	 * @return The coordinate left of the given coordinate 
	 */
    public static Coordinate getEast(Coordinate coordinate) { return new Coordinate(coordinate.x+1, coordinate.y); }
    
    /**
	 * Returns the coordinate that's west of the given coordinate
	 * @param coordinate Current coordinate of the car
	 * @return The coordinate west of the given coordinate 
	 */
    public static Coordinate getWest(Coordinate coordinate) { return new Coordinate(coordinate.x-1, coordinate.y); }
    
    /**
	 * Returns the coordinate that's north of the given coordinate
	 * @param coordinate Current coordinate of the car
	 * @return The coordinate north of the given coordinate 
	 */
    public static Coordinate getNorth(Coordinate coordinate) { return new Coordinate(coordinate.x, coordinate.y+1); }
    
    /**
	 * Returns the coordinate that's south of the given coordinate
	 * @param coordinate Current coordinate of the car
	 * @return The coordinate south of the given coordinate 
	 */
    public static Coordinate getSouth(Coordinate coordinate) { return new Coordinate(coordinate.x, coordinate.y-1); }


}
