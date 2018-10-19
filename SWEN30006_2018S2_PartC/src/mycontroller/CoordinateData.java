package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import java.util.ArrayList;

/**
 * Class used to store information about a coordinate.
 * Used when determining the shortest route to each coordinate.
 */
public class CoordinateData {

    private int damage;
    private int distance;
    private MapTile tile;
    private ArrayList<Coordinate> path;

    public CoordinateData(MapTile tile) {
        this.tile = tile;
        damage = Integer.MAX_VALUE;
        distance = Integer.MAX_VALUE;
        path = new ArrayList<Coordinate>();
    }

    /**
     * Compares the current values to the potential damage and distance of a route.
     * Only updates values if its a preferable route (sorts by damage then distance)
     * @param potentialDamage Damage taken to reach the coord 
     * @param potentialDistance Distance taken to reach the coord
     * @param potentialPath Path to next coord
     *      */
    public void updateTile(int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {

        if ((potentialDamage < damage) || (potentialDamage == damage && potentialDistance < distance)) {
            damage = potentialDamage;
            distance = potentialDistance;
            path = new ArrayList<Coordinate>(potentialPath);
        }
    }

    /**
     * @return The Map Tile at the coordinate
     */
    public MapTile getTile() {
        return tile;
    }
    
    /**
     * @return The damage taken to move to this coordinate
     */
    public int getDamage() {
        return damage;
    }
    
    /**
     * @param Sets the damage taken to move to this coordinate
     *      */
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    /**
     * @return The distance to this coordinate 
     */
    public int getDistance() {
        return distance;
    }
    
    /**
     * @param Sets the distance to this Coordinate
     */
    public void setDistance(int distance) {
        this.distance= distance;
    }


    /**
     * Replaces the current path with an updated (cheaper) path
     * @param List of coordinates making up the new path
     */
    public void replacePath(ArrayList<Coordinate> path) {
        this.path = new ArrayList<Coordinate>(path);
    }

    /**
     * Gets the path to a tile
     * @return A list of coordinates making up the path
     */
    public ArrayList<Coordinate> getPath() {
        return path;
    }

    /**
     * @return The type of the tile
     */
    public MapTile.Type getType() {
        return tile.getType();
    }

    /**
     * Sets the damage and distance values to their default values
     */
    public void resetScore() {
        this.damage = Integer.MAX_VALUE;
        this.distance= Integer.MAX_VALUE;
    }

}
