package mycontroller;


import tiles.HealthTrap;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.Set;


public class Decisions {


    public static Coordinate determineNextMove(Map map, Set<Integer> keys, int numKeys,
                                               Coordinate carCoord, float health) {

        // find the lowest scoring tiles for each target(ie. key, edge, health, exit)
        Coordinate targetKey = selectLowestScoring(map.getKeyTileCoordinates(), map);
        Coordinate targetEdge = selectLowestScoring(map.getEdgeTileCoordinates(), map);
        Coordinate targetHealth = selectLowestScoring(map.getHealthTileCoordinates(), map);

        Coordinate target;

        if (keys.size() < numKeys) {

            if (targetKey != null) {
                target = targetKey;
            }
            else {
                target = targetEdge;
            }
        }

        else{
            Coordinate targetExit  = selectLowestScoring(map.getExitTileCoordinates(), map);

            if(targetExit != null) {
                target = targetExit;
            }
            else {
                target = targetEdge;
            }
        }


        if (map.getTile(carCoord) instanceof HealthTrap) {
            if (health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 25)) {
                return carCoord;
            }
        }

        if (targetHealth != null) {
            if (health < (2 * map.getDamage(target) + map.getDamage(targetHealth) + 5)) {
                target = targetHealth;
            }
        }

        return map.findPath(target, carCoord);
    }


    // where the magic happens
    public static Coordinate selectLowestScoring(ArrayList<Coordinate> coordinates, Map map){

        int tempDamage = Integer.MAX_VALUE;
        int tempDistance = Integer.MAX_VALUE;
        Coordinate nextCoord = null;

        // find the coordinate of the tile with the lowest damage to distance ratio
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
