package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class MapUpdater3 {


    // add new tiles, checks for edges
    public static void update(Map3 map, Coordinate carCoord, HashMap<Coordinate, MapTile> view) {


        // add new possible edge tiles before map is updated
        addPossibleEdges(map, carCoord);

        // add the viewed tiles to the current map
        addToMap(map, view);

        // checks all edges, removing those that are not
        checkEdges(map);

    }


    private static void addPossibleEdges(Map3 map, Coordinate carCoord) {
        //horizontal and vertical lines
        Coordinate hCoords;
        Coordinate vCoords;
        for( int offset1 : new int[]{-4,4}) {
            for( int offset2 = -4 ; offset2 <=4; offset2++) {

                hCoords= new Coordinate(carCoord.x+ offset1, carCoord.y+ offset2);

                //adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
                if(map.insideBoundries(hCoords) && !map.inExploredMap(hCoords)) {
                    map.getEdgeTileCoordinates().add(hCoords);
                }

            }
            for( int offset3 = -3 ; offset3 <=3; offset3++) {

                vCoords=  new Coordinate(carCoord.x+ offset3, carCoord.y+ offset1);
                //adds coordinates of edge tiles if they are not in past map ( as it hasn't been updated yet)
                if(map.insideBoundries(vCoords) && !map.inExploredMap(vCoords)) {
                    map.getEdgeTileCoordinates().add(vCoords);
                }

            }
        }
    }


    //Adds all the provided Maptile's into the current map
    //Adds coord of points of interests (excluding edge tiles)
    public static void addToMap(Map3 map, HashMap<Coordinate, MapTile> ProvidedMap) {

        for( Coordinate coord : ProvidedMap.keySet()) {
            //adds useful coords to appropriate lists

            if(map.insideBoundries(coord) && !map.inExploredMap(coord)) {
                map.getCurrentMap().put(coord, new TileData3(ProvidedMap.get(coord)));
                checkForUseful(map, coord, ProvidedMap.get(coord));
            }
        }
    }


    //Only adds Maptile's of a certain type
    public static void addToMap(Map3 map, HashMap<Coordinate, MapTile> ProvidedMap, MapTile.Type type) {

        for( Coordinate coord : ProvidedMap.keySet()) {
            if(ProvidedMap.get(coord).getType() == type) {
                map.getCurrentMap().put(coord, new TileData3(ProvidedMap.get(coord)));
            }
        }
    }


    private static void checkEdges(Map3 map) {
        boolean hasUnexplored = false;
        ArrayList<Coordinate> tempList = new ArrayList<Coordinate>();

        for( Coordinate coord: map.getEdgeTileCoordinates()) {

            //Ensures no walls or empty tiles can be edges
            if(map.getCurrentMap().get(coord).getTile().isType(MapTile.Type.WALL)
                    ||map.getCurrentMap().get(coord).getTile().isType(MapTile.Type.EMPTY)) {
                tempList.add(coord);
            }
            //Ensures each edge has at least one adjacent unexplored square
            else {
                for( int offset : new int[]{ -1, 1}) {
                    if( (map.insideBoundries(coord.x + offset, coord.y) && !map.inExploredMap(coord.x + offset, coord.y))
                            ||(map.insideBoundries(coord.x, coord.y+ offset) && !map.inExploredMap(coord.x , coord.y + offset))) {
                        hasUnexplored =true;

                    }
                }
                if(hasUnexplored == false) {
                    tempList.add(coord);
                }
                hasUnexplored = false;
            }
        }
        for(Coordinate coord : tempList) {
            map.getEdgeTileCoordinates().remove(coord);
        }
    }


    private static void checkForUseful(Map3 map, Coordinate coord, MapTile tile){

        if(tile.isType(MapTile.Type.FINISH)) {
            map.getExitTileCoordinates().add(coord);
        }
        else if(tile.isType(MapTile.Type.TRAP)){

            if( ((TrapTile)tile).getTrap()== "lava" ) {
                if(((LavaTrap)tile).getKey()>0) {
                    map.getKeyTileCoordinates().add(coord);
                }
            }
            else if(((TrapTile)tile).getTrap()== "health" ) {
                map.getHealthTileCoordinates().add(coord);
            }
        }
    }


}
