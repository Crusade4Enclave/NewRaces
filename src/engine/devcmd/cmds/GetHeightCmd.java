// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.InterestManagement.HeightMap;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.math.Vector2f;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;

public class GetHeightCmd extends AbstractDevCmd {

    public GetHeightCmd() {
        super("getHeight");
        this.addCmdString("height");
    }

    @Override
    protected void _doCmd(PlayerCharacter pc, String[] words,
                          AbstractGameObject target) {

        boolean end = true;

        float height = HeightMap.getWorldHeight(pc);

        this.throwbackInfo(pc, "Altitude : " + height);

        this.throwbackInfo(pc, "Character Height: " + pc.getCharacterHeight());
        this.throwbackInfo(pc, "Character Height to start swimming: " + pc.centerHeight);

        Zone zone = ZoneManager.findSmallestZone(pc.getLoc());
        this.throwbackInfo(pc, "Water Level : " + zone.getSeaLevel());
        this.throwbackInfo(pc, "Character Water Level Above : " + (pc.getCharacterHeight() + height - zone.getSeaLevel()) );

        if (end)
            return;

        Vector2f gridSquare;
        Vector2f gridOffset;
        Vector2f parentGrid;
        Vector2f parentLoc = new Vector2f(-1, -1);

        Zone currentZone = ZoneManager.findSmallestZone(pc.getLoc());

        if (currentZone == null)
            return;

        Zone parentZone = currentZone.getParent();

        HeightMap heightMap = currentZone.getHeightMap();


        //find the next parents heightmap if the currentzone heightmap is null.
        while (heightMap == null){

            if (currentZone == ZoneManager.getSeaFloor()){
                this.throwbackInfo(pc, "Could not find a heightmap to get height.");
                break;
            }

            this.throwbackError(pc, "Heightmap does not exist for " + currentZone.getName());
            this.throwbackInfo(pc, "Using parent zone instead: ");
            currentZone = currentZone.getParent();
            heightMap = currentZone.getHeightMap();
        }


        if ( (heightMap == null) || (currentZone == ZoneManager.getSeaFloor()) ) {
            this.throwbackInfo(pc, currentZone.getName() + " has no heightmap " );
            this.throwbackInfo(pc, "Current altitude: " + currentZone.absY );
            return;
        }

        Vector2f zoneLoc = ZoneManager.worldToZoneSpace(pc.getLoc(), currentZone);

        Vector3fImmutable seaFloorLocalLoc = ZoneManager.worldToLocal(pc.getLoc(), ZoneManager.getSeaFloor());
        this.throwbackInfo(pc, "SeaFloor Local : " + seaFloorLocalLoc.x + " , " + seaFloorLocalLoc.y);




        this.throwbackInfo(pc, "Local Zone Location : " + zoneLoc.x + " , " + zoneLoc.y);
        Vector3fImmutable localLocFromCenter = ZoneManager.worldToLocal(pc.getLoc(), currentZone);
        Vector3fImmutable parentLocFromCenter = ZoneManager.worldToLocal(pc.getLoc(), currentZone.getParent());
        this.throwbackInfo(pc, "Local Zone Location from center : " + localLocFromCenter);
        this.throwbackInfo(pc, "parent Zone Location from center : " + parentLocFromCenter);

        Vector2f parentZoneLoc = ZoneManager.worldToZoneSpace(pc.getLoc(), currentZone.getParent());
        this.throwbackInfo(pc, "Parent Zone Location from Bottom Left : " + parentZoneLoc);

        if ((parentZone != null ) && (parentZone.getHeightMap() != null)) {
            parentLoc = ZoneManager.worldToZoneSpace(pc.getLoc(), parentZone);
            parentGrid = parentZone.getHeightMap().getGridSquare( parentLoc);
        } else parentGrid = new Vector2f(-1,-1);

        gridSquare = heightMap.getGridSquare(zoneLoc);
        gridOffset = HeightMap.getGridOffset(gridSquare);

        float interaltitude = currentZone.getHeightMap().getInterpolatedTerrainHeight(zoneLoc);

        this.throwbackInfo(pc, currentZone.getName());
        this.throwbackInfo(pc, "Current Grid Square: " + gridSquare.x + " , " + gridSquare.y );
        this.throwbackInfo(pc, "Grid Offset: " + gridOffset.x + " , " + gridOffset.y);
        this.throwbackInfo(pc, "Parent Grid: " + parentGrid.x + " , " + parentGrid.y);

        if (parentGrid.x != -1) {
            float parentAltitude = parentZone.getHeightMap().getInterpolatedTerrainHeight(parentLoc);
            this.throwbackInfo(pc, "Parent ALTITUDE: " + (parentAltitude));
            this.throwbackInfo(pc, "Parent Interpolation: " + (parentAltitude + parentZone.getWorldAltitude()));
        }
        this.throwbackInfo(pc, "interpolated height: " + interaltitude);

        this.throwbackInfo(pc, "interpolated height with World: " + (interaltitude + currentZone.getWorldAltitude()));

        float realWorldAltitude = interaltitude + currentZone.getWorldAltitude();

        //OUTSET
        if (parentZone != null){
            float parentXRadius = currentZone.getBounds().getHalfExtents().x;
            float parentZRadius = currentZone.getBounds().getHalfExtents().y;

            float offsetX = Math.abs((localLocFromCenter.x / parentXRadius));
            float offsetZ = Math.abs((localLocFromCenter.z / parentZRadius));

            float bucketScaleX = 100/parentXRadius;
            float bucketScaleZ = 200/parentZRadius;

            float outsideGridSizeX = 1 - bucketScaleX; //32/256
            float outsideGridSizeZ = 1 - bucketScaleZ;
            float weight;

            double scale;


            if (offsetX > outsideGridSizeX && offsetX > offsetZ){
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

                outsetALt += currentZone.getParent().getAbsY();
                realWorldAltitude = outsetALt;

            }else if (offsetZ > outsideGridSizeZ){

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

                outsetALt += currentZone.getParent().getAbsY();
                realWorldAltitude = outsetALt;




            }
        }

        float strMod = pc.statStrBase - 40;

        strMod *= .00999999998f;

        strMod += 1f;

        float radius = 0;
        switch (pc.getRaceID()){
            case 2017:
                radius = 3.1415927f;
            case 2000:


        }
        strMod *= 1.5707964f;

        strMod += 3.1415927f;

        strMod -= .5f;



        realWorldAltitude += strMod;

        this.throwbackInfo(pc, "interpolated height with World: " + realWorldAltitude);




    }

    @Override
    protected String _getHelpString() {
        return "Temporarily Changes SubRace";
    }

    @Override
    protected String _getUsageString() {
        return "' /subrace mobBaseID";
    }

}
