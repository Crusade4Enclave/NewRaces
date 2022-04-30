// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.InterestManagement;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector2f;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractWorldObject;
import engine.objects.Zone;
import engine.server.MBServerStatics;
import engine.util.MapLoader;
import org.pmw.tinylog.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class HeightMap {

    // Class variables

    // Heightmap data for all zones.

    public static final HashMap<Integer, HeightMap> heightmapByLoadNum = new HashMap<>();

    // Bootstrap Tracking

    public static int heightMapsCreated = 0;
    public static HeightMap PlayerCityHeightMap;

    // Heightmap data for this heightmap

    public BufferedImage heightmapImage;

    private int heightMapID;
    private int maxHeight;
    private int fullExtentsX;
    private int fullExtentsY;

    private float bucketWidthX;
    private float bucketWidthY;
    private int zoneLoadID;
    private float seaLevel = 0;
    private float outsetX;
    private float outsetZ;
    private int[][] pixelColorValues;

    public HeightMap(ResultSet rs) throws SQLException {

        this.heightMapID = rs.getInt("heightMapID");
        this.maxHeight = rs.getInt("maxHeight");
        int halfExtentsX = rs.getInt("xRadius");
        int halfExtentsY = rs.getInt("zRadius");
        this.zoneLoadID = rs.getInt("zoneLoadID");
        this.seaLevel = rs.getFloat("seaLevel");
        this.outsetX = rs.getFloat("outsetX");
        this.outsetZ = rs.getFloat("outsetZ");


        // Cache the full extents to avoid the calculation

        this.fullExtentsX = halfExtentsX * 2;
        this.fullExtentsY = halfExtentsY * 2;

        this.heightmapImage = null;
        File imageFile = new File(MBServerStatics.DEFAULT_DATA_DIR + "heightmaps/" + this.heightMapID + ".bmp");

        // early exit if no image file was found.  Will log in caller.

        if (!imageFile.exists())
            return;

        // load the heightmap image.

        try {
            this.heightmapImage = ImageIO.read(imageFile);
        } catch (IOException e) {
            Logger.error("***Error loading heightmap data for heightmap " + this.heightMapID + e.toString());
        }

        // We needed to flip the image as OpenGL and Shadowbane both use the bottom left corner as origin.

        this.heightmapImage = MapLoader.flipImage(this.heightmapImage);

        // Calculate the data we do not load from table

        try {
            calculateBucketWidth();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Generate pixel array from image data

        generatePixelData();

        HeightMap.heightmapByLoadNum.put(this.zoneLoadID, this);

        heightMapsCreated++;
    }

    //Created for PlayerCities
    public HeightMap() {

        this.heightMapID = 999999;
        this.maxHeight = 5; // for real...
        int halfExtentsX = (int) Enum.CityBoundsType.ZONE.extents;
        int halfExtentsY = (int) Enum.CityBoundsType.ZONE.extents;
        this.zoneLoadID = 0;
        this.seaLevel = 0;
        this.outsetX = 128;
        this.outsetZ = 128;


        // Cache the full extents to avoid the calculation

        this.fullExtentsX = halfExtentsX * 2;
        this.fullExtentsY = halfExtentsY * 2;


        // load the heightmap image.


        // We needed to flip the image as OpenGL and Shadowbane both use the bottom left corner as origin.

        this.heightmapImage = null;

        // Calculate the data we do not load from table

        this.bucketWidthX = 1;
        this.bucketWidthY = 1;

        this.pixelColorValues = new int[this.fullExtentsX + 1][this.fullExtentsY+1];

        for (int y = 0; y <= this.fullExtentsY; y++) {
            for (int x = 0; x <= this.fullExtentsX; x++) {
                pixelColorValues[x][y] = 255;
            }
        }


        HeightMap.heightmapByLoadNum.put(this.zoneLoadID, this);
    }
    
    public HeightMap(Zone zone) {

        this.heightMapID = 999999;
        this.maxHeight = 0;
        int halfExtentsX = (int) zone.getBounds().getHalfExtents().x;
        int halfExtentsY = (int) zone.getBounds().getHalfExtents().y;
        this.zoneLoadID = 0;
        this.seaLevel = 0;
        this.outsetX = 0;
        this.outsetZ = 0;

        // Cache the full extents to avoid the calculation

        this.fullExtentsX = halfExtentsX * 2;
        this.fullExtentsY = halfExtentsY * 2;


        // We needed to flip the image as OpenGL and Shadowbane both use the bottom left corner as origin.

        this.heightmapImage = null;

        // Calculate the data we do not load from table

        this.bucketWidthX = 1;
        this.bucketWidthY = 1;

        this.pixelColorValues = new int[this.fullExtentsX+1][this.fullExtentsY+1];

        for (int y = 0; y <= this.fullExtentsY; y++) {
            for (int x = 0; x <= this.fullExtentsX; x++) {
                pixelColorValues[x][y] = 255;
            }
        }


        HeightMap.heightmapByLoadNum.put(this.zoneLoadID, this);
    }

    public static void GeneratePlayerCityHeightMap() {

        HeightMap.PlayerCityHeightMap = new HeightMap();

    }
    
    public static void GenerateCustomHeightMap(Zone zone) {

        HeightMap heightMap = new HeightMap(zone);
        
        HeightMap.heightmapByLoadNum.put(zone.getLoadNum(), heightMap);

    }

    public Vector2f getGridSquare(Vector2f zoneLoc) {

        if (zoneLoc.x < 0)
            zoneLoc.setX(0);

        if (zoneLoc.x > this.fullExtentsX - 1)
            zoneLoc.setX((this.fullExtentsX - 1) + .9999999f);

        if (zoneLoc.y < 0)
            zoneLoc.setY(0);

        if (zoneLoc.y > this.fullExtentsY - 1)
            zoneLoc.setY((this.fullExtentsY - 1) + .9999999f);

        float xBucket = (zoneLoc.x / this.bucketWidthX);
        float yBucket = (zoneLoc.y / this.bucketWidthY);

        return new Vector2f(xBucket, yBucket);
    }

    public float getInterpolatedTerrainHeight(Vector2f zoneLoc) {

        Vector2f gridSquare;

        if (zoneLoc.x < 0 || zoneLoc.x > this.fullExtentsX)
            return -1;

        if (zoneLoc.y < 0 || zoneLoc.y > this.fullExtentsY)
            return -1;
        
        int maxX = (int) (this.fullExtentsX / this.bucketWidthX);
        int maxY = (int) (this.fullExtentsY / this.bucketWidthY);

        //flip the Y so it grabs from the bottom left instead of top left.
        //zoneLoc.setY(maxZoneHeight - zoneLoc.y);

        gridSquare = getGridSquare(zoneLoc);

        int gridX = (int) gridSquare.x;
        int gridY = (int) (gridSquare.y);
        
        if (gridX > maxX)
        	gridX = maxX;
        if (gridY > maxY)
        	gridY = maxY;

        float offsetX = (gridSquare.x - gridX);
        float offsetY = gridSquare.y - gridY;

        //get height of the 4 vertices.

        float topLeftHeight = 0;
        float topRightHeight = 0;
        float bottomLeftHeight = 0;
        float bottomRightHeight = 0;
        
        int nextY = gridY +1;
        int nextX = gridX + 1;
        
        if (nextY > maxY)
        	nextY = gridY;
        
        if (nextX > maxX)
        	nextX = gridX;
        
        topLeftHeight = pixelColorValues[gridX][gridY];
        topRightHeight = pixelColorValues[nextX][gridY];
        bottomLeftHeight = pixelColorValues[gridX][nextY];
        bottomRightHeight = pixelColorValues[nextX][nextY];

        float interpolatedHeight;

        interpolatedHeight = topRightHeight * (1 - offsetY) * (offsetX);
        interpolatedHeight += (bottomRightHeight * offsetY * offsetX);
        interpolatedHeight += (bottomLeftHeight * (1 - offsetX) * offsetY);
        interpolatedHeight += (topLeftHeight * (1 - offsetX) * (1 - offsetY));

        interpolatedHeight *= (float) this.maxHeight / 256;  // Scale height

        return interpolatedHeight;
    }

    public static float getWorldHeight(AbstractWorldObject worldObject) {

        Vector2f parentLoc = new Vector2f(-1, -1);
        Zone currentZone = ZoneManager.findSmallestZone(worldObject.getLoc());

        if (currentZone == null)
            return worldObject.getAltitude();

        Zone parentZone = currentZone.getParent();
        HeightMap heightMap = currentZone.getHeightMap();

        //find the next parents heightmap if the currentzone heightmap is null.

        while (heightMap == null) {

            if (currentZone == ZoneManager.getSeaFloor()) {
                break;
            }
            currentZone = currentZone.getParent();
            heightMap = currentZone.getHeightMap();

            parentZone = currentZone.getParent();
        }
        if ((heightMap == null) || (currentZone == ZoneManager.getSeaFloor())) {

            return currentZone.getAbsY() + worldObject.getAltitude();
        }

        Vector2f zoneLoc = ZoneManager.worldToZoneSpace(worldObject.getLoc(), currentZone);
        Vector3fImmutable localLocFromCenter = ZoneManager.worldToLocal(worldObject.getLoc(), currentZone);

        if ((parentZone != null) && (parentZone.getHeightMap() != null)) {
            parentLoc = ZoneManager.worldToZoneSpace(worldObject.getLoc(), parentZone);
        }

        float interaltitude = currentZone.getHeightMap().getInterpolatedTerrainHeight(zoneLoc);

        float worldAltitude = currentZone.getWorldAltitude();

        float realWorldAltitude = interaltitude + worldAltitude;

        //OUTSET


        if (parentZone != null) {


            float parentXRadius = currentZone.getBounds().getHalfExtents().x;
            float parentZRadius = currentZone.getBounds().getHalfExtents().y;


            float offsetX = Math.abs((localLocFromCenter.x / parentXRadius));
            float offsetZ = Math.abs((localLocFromCenter.z / parentZRadius));

            float bucketScaleX = heightMap.outsetX / parentXRadius;
            float bucketScaleZ = heightMap.outsetZ / parentZRadius;


            if (bucketScaleX <= 0.40000001) {
                bucketScaleX = heightMap.outsetZ / parentXRadius;

            }

            if (bucketScaleX > 0.40000001)
                bucketScaleX = 0.40000001f;

            if (bucketScaleZ <= 0.40000001) {
                bucketScaleZ = heightMap.outsetX / parentZRadius;
            }

            if (bucketScaleZ > 0.40000001)
                bucketScaleZ = 0.40000001f;

            float outsideGridSizeX = 1 - bucketScaleX; //32/256
            float outsideGridSizeZ = 1 - bucketScaleZ;
            float weight;

            double scale;


            if (offsetX > outsideGridSizeX && offsetX > offsetZ) {
                weight = (offsetX - outsideGridSizeX) / bucketScaleX;
                scale = Math.atan2((.5 - weight) * 3.1415927, 1);

                float scaleChild = (float) ((scale + 1) * .5);
                float scaleParent = 1 - scaleChild;


                float parentAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(parentLoc);
                float parentCenterAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(ZoneManager.worldToZoneSpace(currentZone.getLoc(), parentZone));


                parentCenterAltitude += currentZone.getYCoord();
                parentCenterAltitude += interaltitude;

                float firstScale = parentAltitude * scaleParent;
                float secondScale = parentCenterAltitude * scaleChild;
                float outsetALt = firstScale + secondScale;

                outsetALt += currentZone.getParent().getWorldAltitude();
                realWorldAltitude = outsetALt;

            } else if (offsetZ > outsideGridSizeZ) {

                weight = (offsetZ - outsideGridSizeZ) / bucketScaleZ;
                scale = Math.atan2((.5 - weight) * 3.1415927, 1);

                float scaleChild = (float) ((scale + 1) * .5);
                float scaleParent = 1 - scaleChild;
                float parentAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(parentLoc);
                float parentCenterAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(ZoneManager.worldToZoneSpace(currentZone.getLoc(), parentZone));

                parentCenterAltitude += currentZone.getYCoord();
                parentCenterAltitude += interaltitude;
                float firstScale = parentAltitude * scaleParent;
                float secondScale = parentCenterAltitude * scaleChild;
                float outsetALt = firstScale + secondScale;

                outsetALt += currentZone.getParent().getWorldAltitude();
                realWorldAltitude = outsetALt;
            }
        }


        return realWorldAltitude;
    }

    public static float getWorldHeight(Vector3fImmutable worldLoc) {

        Vector2f parentLoc = new Vector2f(-1, -1);
        Zone currentZone = ZoneManager.findSmallestZone(worldLoc);

        if (currentZone == null)
            return 0;

        Zone parentZone = currentZone.getParent();
        HeightMap heightMap = currentZone.getHeightMap();
        //find the next parents heightmap if the currentzone heightmap is null.
        while (heightMap == null) {

            if (currentZone == ZoneManager.getSeaFloor()) {
                break;
            }
            currentZone = currentZone.getParent();
            heightMap = currentZone.getHeightMap();

            parentZone = currentZone.getParent();
        }
        if ((heightMap == null) || (currentZone == ZoneManager.getSeaFloor())) {

            return currentZone.getAbsY();
        }

        Vector2f zoneLoc = ZoneManager.worldToZoneSpace(worldLoc, currentZone);
        Vector3fImmutable localLocFromCenter = ZoneManager.worldToLocal(worldLoc, currentZone);

        if ((parentZone != null) && (parentZone.getHeightMap() != null)) {
            parentLoc = ZoneManager.worldToZoneSpace(worldLoc, parentZone);
        }

        float interaltitude = currentZone.getHeightMap().getInterpolatedTerrainHeight(zoneLoc);

        float worldAltitude = currentZone.getWorldAltitude();

        float realWorldAltitude = interaltitude + worldAltitude;

        //OUTSET


        if (parentZone != null) {

            //			if (currentZone.getHeightMap() != null && parentZone.getHeightMap() != null && parentZone.getParent() != null && parentZone.getParent().getHeightMap() != null)
            //				return realWorldAltitude;

            float parentXRadius = currentZone.getBounds().getHalfExtents().x;
            float parentZRadius = currentZone.getBounds().getHalfExtents().y;

            float offsetX = Math.abs((localLocFromCenter.x / parentXRadius));
            float offsetZ = Math.abs((localLocFromCenter.z / parentZRadius));

            float bucketScaleX = heightMap.outsetX / parentXRadius;
            float bucketScaleZ = heightMap.outsetZ / parentZRadius;

            float outsideGridSizeX = 1 - bucketScaleX; //32/256
            float outsideGridSizeZ = 1 - bucketScaleZ;
            float weight;

            double scale;


            if (offsetX > outsideGridSizeX && offsetX > offsetZ) {
                weight = (offsetX - outsideGridSizeX) / bucketScaleX;
                scale = Math.atan2((.5 - weight) * 3.1415927, 1);

                float scaleChild = (float) ((scale + 1) * .5);
                float scaleParent = 1 - scaleChild;


                float parentAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(parentLoc);
                float parentCenterAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(ZoneManager.worldToZoneSpace(currentZone.getLoc(), parentZone));


                parentCenterAltitude += currentZone.getYCoord();
                parentCenterAltitude += interaltitude;

                float firstScale = parentAltitude * scaleParent;
                float secondScale = parentCenterAltitude * scaleChild;
                float outsetALt = firstScale + secondScale;

                outsetALt += currentZone.getParent().getWorldAltitude();
                realWorldAltitude = outsetALt;

            } else if (offsetZ > outsideGridSizeZ) {

                weight = (offsetZ - outsideGridSizeZ) / bucketScaleZ;
                scale = Math.atan2((.5 - weight) * 3.1415927, 1);

                float scaleChild = (float) ((scale + 1) * .5);
                float scaleParent = 1 - scaleChild;
                float parentAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(parentLoc);
                float parentCenterAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(ZoneManager.worldToZoneSpace(currentZone.getLoc(), parentZone));

                parentCenterAltitude += currentZone.getYCoord();
                parentCenterAltitude += interaltitude;
                float firstScale = parentAltitude * scaleParent;
                float secondScale = parentCenterAltitude * scaleChild;
                float outsetALt = firstScale + secondScale;

                outsetALt += currentZone.getParent().getWorldAltitude();
                realWorldAltitude = outsetALt;
            }
        }


        return realWorldAltitude;
    }

    public float getInterpolatedTerrainHeight(Vector3fImmutable zoneLoc3f) {

        Vector2f zoneLoc = new Vector2f(zoneLoc3f.x, zoneLoc3f.z);

        Vector2f gridSquare;

        if (zoneLoc.x < 0 || zoneLoc.x > this.fullExtentsX)
            return -1;

        if (zoneLoc.y < 0 || zoneLoc.y > this.fullExtentsY)
            return -1;

        //flip the Y so it grabs from the bottom left instead of top left.
        //zoneLoc.setY(maxZoneHeight - zoneLoc.y);

        gridSquare = getGridSquare(zoneLoc);

        int gridX = (int) gridSquare.x;
        int gridY = (int) (gridSquare.y);

        float offsetX = (gridSquare.x - gridX);
        float offsetY = gridSquare.y - gridY;

        //get height of the 4 vertices.

        float topLeftHeight = pixelColorValues[gridX][gridY];
        float topRightHeight = pixelColorValues[gridX + 1][gridY];
        float bottomLeftHeight = pixelColorValues[gridX][gridY + 1];
        float bottomRightHeight = pixelColorValues[gridX + 1][gridY + 1];

        float interpolatedHeight;

        interpolatedHeight = topRightHeight * (1 - offsetY) * (offsetX);
        interpolatedHeight += (bottomRightHeight * offsetY * offsetX);
        interpolatedHeight += (bottomLeftHeight * (1 - offsetX) * offsetY);
        interpolatedHeight += (topLeftHeight * (1 - offsetX) * (1 - offsetY));

        interpolatedHeight *= (float) this.maxHeight / 256;  // Scale height

        return interpolatedHeight;
    }

    public static float getOutsetHeight(float interpolatedAltitude, Zone zone, Vector3fImmutable worldLocation) {

        Vector2f parentLoc;
        float outsetALt = 0;

        if (zone.getParent() == null || zone.getParent().getHeightMap() == null)
            return interpolatedAltitude + zone.getWorldAltitude();

        if (zone.getParent() != null && zone.getParent().getHeightMap() != null) {

            parentLoc = ZoneManager.worldToZoneSpace(worldLocation, zone.getParent());

            Vector3fImmutable localLocFromCenter = ZoneManager.worldToLocal(worldLocation, zone);

            float parentXRadius = zone.getBounds().getHalfExtents().x;
            float parentZRadius = zone.getBounds().getHalfExtents().y;

            float bucketScaleX = zone.getHeightMap().outsetX / parentXRadius;
            float bucketScaleZ = zone.getHeightMap().outsetZ / parentZRadius;

            float outsideGridSizeX = 1 - bucketScaleX; //32/256
            float outsideGridSizeZ = 1 - bucketScaleZ;

            float weight;
            double scale;

            float offsetX = Math.abs((localLocFromCenter.x / parentXRadius));
            float offsetZ = Math.abs((localLocFromCenter.z / parentZRadius));

            if (offsetX > outsideGridSizeX && offsetX > offsetZ) {
                weight = (offsetX - outsideGridSizeX) / bucketScaleX;
                scale = Math.atan2((.5 - weight) * 3.1415927, 1);

                float scaleChild = (float) ((scale + 1) * .5);
                float scaleParent = 1 - scaleChild;

                float parentAltitude = zone.getParent().getHeightMap().getInterpolatedTerrainHeight(parentLoc);
                float parentCenterAltitude = zone.getParent().getHeightMap().getInterpolatedTerrainHeight(ZoneManager.worldToZoneSpace(zone.getLoc(), zone.getParent()));

                parentCenterAltitude += zone.getYCoord();
                parentCenterAltitude += interpolatedAltitude;

                float firstScale = parentAltitude * scaleParent;
                float secondScale = parentCenterAltitude * scaleChild;
                outsetALt = firstScale + secondScale;

                outsetALt += zone.getParent().getAbsY();

            } else if (offsetZ > outsideGridSizeZ) {

                weight = (offsetZ - outsideGridSizeZ) / bucketScaleZ;
                scale = Math.atan2((.5 - weight) * 3.1415927, 1);

                float scaleChild = (float) ((scale + 1) * .5);
                float scaleParent = 1 - scaleChild;
                float parentAltitude = zone.getParent().getHeightMap().getInterpolatedTerrainHeight(parentLoc);
                float parentCenterAltitude = zone.getHeightMap().getInterpolatedTerrainHeight(ZoneManager.worldToZoneSpace(zone.getLoc(), zone));

                parentCenterAltitude += zone.getYCoord();
                parentCenterAltitude += interpolatedAltitude;

                float firstScale = parentAltitude * scaleParent;
                float secondScale = parentCenterAltitude * scaleChild;
                outsetALt = firstScale + secondScale;

                outsetALt += zone.getParent().getAbsY();
            }

        }

        return outsetALt;
    }

    private void generatePixelData() {

        Color color;

        // Generate altitude lookup table for this heightmap

        this.pixelColorValues = new int[this.heightmapImage.getWidth()][this.heightmapImage.getHeight()];

        for (int y = 0; y < this.heightmapImage.getHeight(); y++) {
            for (int x = 0; x < this.heightmapImage.getWidth(); x++) {

                color = new Color(this.heightmapImage.getRGB(x, y));
                pixelColorValues[x][y] = color.getRed();
            }
        }

    }

    public static Vector2f getGridOffset(Vector2f gridSquare) {

        int floorX = (int) gridSquare.x;
        int floorY = (int) gridSquare.y;

        return new Vector2f(gridSquare.x - floorX, gridSquare.y - floorY);

    }

    public float getScaledHeightForColor(float color) {

        return (color / 256) * this.maxHeight;
    }


    private void calculateBucketWidth() {


        switch (this.zoneLoadID) {
            case 100:
                this.bucketWidthX = 64.12524414f;
                this.bucketWidthY = 64.12524414f;
                break;
            case 200:
                this.bucketWidthX = 145.9599152f;
                this.bucketWidthY = 145.9599152f;
                break;
            case 3033:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3011:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3026:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3017:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3007:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3020:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3025:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3016:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3021:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3018:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3024:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3010:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3012:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3022:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3030:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3019:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3014:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 10500:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 10501:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 10503:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 10504:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 10505:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 10506:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 10507:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 10502:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 11006:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 11008:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 11036:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 10200:
                this.bucketWidthX = 2.000977039f;
                this.bucketWidthY = 2.000977039f;
                break;


            case 10120:
                this.bucketWidthX = 1.00048852f;
                this.bucketWidthY = 1.00048852f;
                break;

            case 10001:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10002:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10003:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10004:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10005:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10100:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;

            case 10006:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10007:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10008:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10009:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;

            case 10010:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;

            case 10201:
                this.bucketWidthX = 2.000977039f;
                this.bucketWidthY = 2.000977039f;
                break;

            case 10011:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10012:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10013:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10014:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;
            case 10015:
                this.bucketWidthX = 17.06666756f;
                this.bucketWidthY = 17.06666756f;
                break;

            case 3004:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3005:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;


            case 3003:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;


            case 400:
                this.bucketWidthX = 64.06256104f;
                this.bucketWidthY = 64.06256104f;
                break;

            case 3032:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3009:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;
            case 3023:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;
            case 3008:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;
            case 11009:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;
            case 500:
                this.bucketWidthX = 128.4012604f;
                this.bucketWidthY = 128.4012604f;
                break;

            case 3013:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;
            case 3006:
                this.bucketWidthX = 2.001116753f;
                this.bucketWidthY = 2.001116753f;
                break;

            case 3015:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 11010:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 10130:
                this.bucketWidthX = 1.00048852f;
                this.bucketWidthY = 1.00048852f;
                break;


            case 501:
                this.bucketWidthX = 130.0317535f;
                this.bucketWidthY = 130.0317535f;
                break;

            case 11032:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 300:
                this.bucketWidthX = 100.5235596f;
                this.bucketWidthY = 100.5235596f;
                break;

            case 3027:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3028:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 11016:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 310:
                this.bucketWidthX = 100.5235596f;
                this.bucketWidthY = 100.5235596f;
                break;

            case 3034:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 3035:
                this.bucketWidthX = 4.003910065f;
                this.bucketWidthY = 4.003910065f;
                break;

            case 11039:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 10066:
                this.bucketWidthX = 51.20000076f;
                this.bucketWidthY = 51.20000076f;
                break;

            case 502:

                this.bucketWidthX = 130.0317535f;
                this.bucketWidthY = 130.0317535f;
                break;

            default:
                // Re-enable for debugging.  Spammy in console.
                // Logger.info("Setting Zone : " + this.zoneLoadID + " with heightmap ID : " + heightMapID + " to default bucketwidth");
                break;
        }
    }

    public static void loadAlHeightMaps() {

        // Load the heightmaps into staging hashmap keyed by HashMapID

        DbManager.HeightMapQueries.LOAD_ALL_HEIGHTMAPS();

        //generate static player city heightmap.

        HeightMap.GeneratePlayerCityHeightMap();
        
  
        // Clear all heightmap image data as it's no longer needed.

        for (HeightMap heightMap : HeightMap.heightmapByLoadNum.values()) {
            heightMap.heightmapImage = null;
        }
        
        Logger.info(HeightMap.heightmapByLoadNum.size() + " Heightmaps cached.");
    }

    public float getBucketWidthX() {
        return bucketWidthX;
    }

    public float getBucketWidthY() {
        return bucketWidthY;
    }

    public int getHeightMapID() {
        return heightMapID;
    }

    public BufferedImage getHeightmapImage() {
        return heightmapImage;
    }

    public float getSeaLevel() {
        return seaLevel;
    }

    public static boolean isLocUnderwater(Vector3fImmutable currentLoc) {

        float localAltitude = HeightMap.getWorldHeight(currentLoc);
        Zone zone = ZoneManager.findSmallestZone(currentLoc);

        if (localAltitude < zone.getSeaLevel())
            return true;

        return false;
    }

}
