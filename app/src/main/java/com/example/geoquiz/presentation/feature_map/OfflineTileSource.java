package com.example.geoquiz.presentation.feature_map;

import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;

public class OfflineTileSource extends BitmapTileSourceBase {

    private static final String[] TILE_FILES = {"map.png"}; // Your offline map files

    public OfflineTileSource() {
        super("OfflineMap", 0, 18, 256, ".png", TILE_FILES);
    }

    @Override
    public String getTileRelativeFilenameString(final long pMapTileIndex) {
        return getBasePath() + "/" + TILE_FILES[0];
    }
}
