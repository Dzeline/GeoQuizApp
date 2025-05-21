package com.example.geoquiz.presentation.feature_map;

import org.osmdroid.tileprovider.tilesource.XYTileSource;
/**
 * Defines a tile source that expects tiles to be stored locally in the
 * osmdroid tile cache directory, under a subfolder matching this source's name.
 * Example: <osmdroid_base_path>/tiles/OfflineMapTiles/Z/X/Y.png
 *
 * The name "OfflineMapTiles" is chosen here to be distinct and clear.
 * You must ensure your tile files are stored in a directory with this name
 * inside the OSMDroid tiles directory (e.g., .../osmdroid/tiles/OfflineMapTiles/).
 */
public class OfflineTileSource extends XYTileSource {

    public OfflineTileSource() {
        super("OfflineMapTiles", // Name of the tile source, used for directory names in cache
                0,                 // Minimum Zoom Level (adjust as per your available tiles)
                18,                // Maximum Zoom Level (adjust as per your available tiles)
                256,               // Tile Size in Pixels
                ".png",            // File extension of tiles
                new String[]{},    // Base URLs: Empty, as we are loading from local file system/cache
                "Offline Tiles - Map Data © OpenStreetMap contributors"); // Copyright/Attribution
    }


}
