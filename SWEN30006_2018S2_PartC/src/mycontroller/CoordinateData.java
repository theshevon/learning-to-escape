package mycontroller;

import tiles.GrassTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MudTrap;
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



    public void updateTile(int potentialDamage, int potentialDistance, ArrayList<Coordinate> potentialPath) {


        if(potentialDamage < damage) {
            // code duplication????
            damage = potentialDamage;
            distance = potentialDistance;
            path = new ArrayList<Coordinate>(potentialPath);
        }
        else if(potentialDamage == damage) {

            if(potentialDistance < distance) {
                // code duplication????
                //damage = potentialDamage;
                distance = potentialDistance;
                path = new ArrayList<Coordinate>(potentialPath);
            }
        }
    }




    //------------------------Getters and Setters------------------------------------

    public MapTile getTile() {
        return tile;
    }
    public Integer getDamage() {
        return damage;
    }
    public void setDamage(Integer damage) {
        this.damage = damage;
    }
    public Integer getDistance() {
        return distance;
    }
    public void setDistance(Integer distance) {
        this.distance= distance;
    }


    /**
     * Replaces the current path with an updated (cheaper) path
     * param: list of coordinates making up the new path
     */
    public void replacePath(ArrayList<Coordinate> path) {
        this.path = new ArrayList<Coordinate>(path);
    }

    /**
     * Gets the path to a tile.
     * @return a list of coordinates making up the path
     */
    public ArrayList<Coordinate> getPath() {
        return path;
    }

    /**
     * @return the type of the tile
     */
    public MapTile.Type getType() {
        return tile.getType();
    }

    /**
     * sets the damage and distance values to their default values
     */
    public void resetScore() {
        this.damage = Integer.MAX_VALUE;
        this.distance= Integer.MAX_VALUE;
    }

}
