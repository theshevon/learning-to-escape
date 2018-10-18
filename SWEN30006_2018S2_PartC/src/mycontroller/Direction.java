package mycontroller;

import utilities.Coordinate;
import world.WorldSpatial;

public class Direction {

    public static Coordinate getLeft(Coordinate coord , WorldSpatial.Direction orientation) {
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
    public static Coordinate getRight(Coordinate coord , WorldSpatial.Direction orientation) {
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
    public static Coordinate getFront(Coordinate coord , WorldSpatial.Direction orientation) {
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
    public static Coordinate getBehind(Coordinate coord , WorldSpatial.Direction orientation) {
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

    public static Coordinate getEast(Coordinate coord) {
        return new Coordinate(coord.x+1 , coord.y );
    }
    public static Coordinate getWest(Coordinate coord) {
        return new Coordinate(coord.x-1 , coord.y );
    }
    public static Coordinate getNorth(Coordinate coord) {
        return new Coordinate(coord.x , coord.y +1 );
    }
    public static Coordinate getSouth(Coordinate coord) {
        return new Coordinate(coord.x , coord.y -1 );
    }


}
