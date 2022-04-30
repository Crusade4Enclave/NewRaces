package engine.objects;

// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



import engine.Enum.BuildingGroup;
import engine.gameManager.DbManager;
import engine.math.Vector2f;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/*  @Summary - Blueprint class is used for determining
 characteristics of instanced player owned
 structures such as available slots, upgrade
 cost/time and the target window symbol icon.
 */
public class Blueprint {

	public final static Vector2f IrikieForgeExtents = new Vector2f(32, 32);
	public final static Vector2f IrikieBarracksExtents = new Vector2f(32, 32);

	private static HashMap<Integer, Blueprint> _blueprints = new HashMap<>();
	private static HashMap<Integer, Integer> _doorNumbers = new HashMap<>();
	public static HashMap<Integer, Blueprint> _meshLookup = new HashMap<>();

	private final int blueprintUUID;
	private final String name;
	private final BuildingGroup buildingGroup;
	private final int icon;
	private final int maxRank;
	private final int maxSlots;
	private final int rank1UUID;
	private final int rank3UUID;
	private final int rank7UUID;
	private final int destroyedUUID;

	private Blueprint() {
		this.blueprintUUID = 0;
		this.name = "";
		this.icon = 0;
		this.buildingGroup = BuildingGroup.BANESTONE;
		this.maxRank = 0;
		this.maxSlots = 0;
		this.rank1UUID = 0;
		this.rank3UUID = 0;
		this.rank7UUID = 0;
		this.destroyedUUID = 0;
	}

	public Blueprint(ResultSet rs) throws SQLException {

		this.blueprintUUID = rs.getInt("Rank0UUID");
		this.name = rs.getString("MeshName");
		this.icon = rs.getInt("Icon");
		this.buildingGroup = BuildingGroup.valueOf(rs.getString("BuildingGroup"));
		this.maxRank = rs.getInt("MaxRank");
		this.maxSlots = rs.getInt("MaxSlots");
		this.rank1UUID = rs.getInt("Rank1UUID");
		this.rank3UUID = rs.getInt("Rank3UUID");
		this.rank7UUID = rs.getInt("Rank7UUID");
		this.destroyedUUID = rs.getInt("DestroyedUUID");

	}

	// Accessors

	public static Blueprint getBlueprint(int blueprintUUID) {

		return _blueprints.get(blueprintUUID);

	}

	public static BuildingGroup getBuildingGroup(int blueprintUUID) {

		Blueprint blueprint;

		blueprint = _blueprints.get(blueprintUUID);

        return blueprint.buildingGroup;
    }

	public static int getMaxShrines(int treeRank) {

		// Returns the number of allowed spires/shrines
		// for a given rank.

		int maxShrines;

		switch (treeRank) {
			case 0:
			case 1:
			case 2:
				maxShrines = 0;
				break;
			case 3:
			case 4:
				maxShrines = 1;
				break;
			case 5:
			case 6:
				maxShrines = 2;
				break;
			case 7:
			case 8:
				maxShrines = 3;
				break;
			default:
				maxShrines = 0;

		}

		return maxShrines;
	}

	public static void loadAllBlueprints() {

		_blueprints = DbManager.BlueprintQueries.LOAD_ALL_BLUEPRINTS();

	}

	// Method returns a blueprint based on a blueprintUUID

	public static void loadAllDoorNumbers() {

		_doorNumbers = DbManager.BlueprintQueries.LOAD_ALL_DOOR_NUMBERS();

	}

	public static int getDoorNumberbyMesh(int doorMeshUUID) {

		if (_doorNumbers.containsKey(doorMeshUUID))
			return _doorNumbers.get(doorMeshUUID);

		return 0;
	}

	public static boolean isMeshWallPiece(int meshUUID) {

		Blueprint buildingBlueprint = Blueprint.getBlueprint(meshUUID);

		if (buildingBlueprint == null)
			return false;

        switch (buildingBlueprint.buildingGroup) {
		case WALLSTRAIGHT:
		case ARTYTOWER:
		case WALLCORNER:
		case SMALLGATE:
		case WALLSTAIRS:
			return true;
		default:
			break;
		}
		return false;

	}

	// Method calculates available vendor slots
	// based upon the building's current rank

	public static int getNpcMaintCost(int rank) {
		int maintCost = Integer.MAX_VALUE;

		maintCost = (9730 * rank) + 1890;

		return maintCost;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public int getMaxSlots() {
        if (this.buildingGroup != null && this.buildingGroup.equals(BuildingGroup.BARRACK))
			return 1;
		return maxSlots;
	}

	// Method returns a mesh UUID for this blueprint
	// based upon a given rank.

	public BuildingGroup getBuildingGroup() {
		return this.buildingGroup;
	}

	// Method returns a cost to upgrade a building to a given rank
	// based upon this blueprint's maintenance group

	public int getMaxHealth(int currentRank) {

		int maxHealth;

		// Return 0 health for a destroyed building
		// or 1 for a destroyed mine (cleint looting restriction)

		if (currentRank == -1) {

            return this.buildingGroup == BuildingGroup.MINE ? 1 : 0;
		}

		// Return 15k for a constructing mesh

		if (currentRank == 0)
			return 15000;

		switch (this.buildingGroup) {

		case TOL:
			maxHealth = (70000 * currentRank) + 10000;
			break;
		case BARRACK:
			maxHealth = (35000 * currentRank) + 5000;
			break;
		case BANESTONE:
			maxHealth = (170000 * currentRank) - 120000;
			break;
		case CHURCH:
			maxHealth = (28000 * currentRank) + 4000;
			break;
		case MAGICSHOP:
		case FORGE:
		case INN:
		case TAILOR:
			maxHealth = (17500 * currentRank) + 2500;
			break;
		case VILLA:
		case ESTATE:
		case FORTRESS:
			maxHealth = 300000;
			break;
		case CITADEL:
			maxHealth = 500000;
			break;
		case SPIRE:
			maxHealth = (37000 * currentRank) - 9000;
			break;
		case GENERICNOUPGRADE:
		case SHACK:
		case SIEGETENT:
			maxHealth = 40000;
			break;
		case BULWARK:
			if (currentRank == 1)
				maxHealth = 110000;
			else
				maxHealth = 40000;
			break;
		case WALLSTRAIGHT:
		case WALLSTRAIGHTTOWER:
		case WALLSTAIRS:
			maxHealth = 1000000;
			break;
		case WALLCORNER:
		case ARTYTOWER:
			maxHealth = 900000;
			break;
		case SMALLGATE:
			maxHealth = 1100000;
			break;
		case AMAZONHALL:
		case CATHEDRAL:
		case GREATHALL:
		case KEEP:
		case THIEFHALL:
		case TEMPLEHALL:
		case WIZARDHALL:
		case ELVENHALL:
		case ELVENSANCTUM:
		case IREKEIHALL:
		case FORESTHALL:
			maxHealth = (28000 * currentRank) + 4000;
			break;
		case MINE:
			maxHealth = 125000;
			break;
		case RUNEGATE:
			maxHealth = 100000;
			break;
		case SHRINE:
			maxHealth = 100000;
			break;
		case WAREHOUSE:
			maxHealth = 40000;
			break;

		default:
			maxHealth = 40000;
			break;

		}
		return maxHealth;
	}

	// Returns number of vendor slots available
	// for the building's current rank.

	public int getSlotsForRank(int currentRank) {

		int availableSlots;

		// Early exit for buildings not yet constructed

		if (currentRank == 0)
			return 0;

		// Early exit for buildings with single or no slots

		if (this.maxSlots <= 1)
			return maxSlots;

		if (this.maxRank == 1 && currentRank == 1)
			return getMaxSlots();

		switch (currentRank) {

		case 1:
		case 2:
			availableSlots = 1;
			break;
		case 3:
			case 4:
			case 5:
			case 6:
			availableSlots = 2;
			break;
		case 7:
			availableSlots = 3;
			break;
		case 8:
			availableSlots = 1;
			break;
		default:
			availableSlots = 0;
			break;
		}

		return availableSlots;
	}

	// Returns the half extents of this blueprint's
	// bounding box, based upon it's buildinggroup

	public int getIcon() {
		return this.icon;
	}

	public String getName() {
		return this.name;
	}

	public int getMeshForRank(int targetRank) {

		int targetMesh = this.blueprintUUID;

		// The Blueprint UUID is the 'constructing' mesh so
		// we return that value if the rank passed is 0.

        if ((maxRank == 1) && (this.rank1UUID == 0)) {
            return blueprintUUID;
        }

		// Set the return value to the proper mesh UID for rank

		switch (targetRank) {

		case -1:
			targetMesh = this.destroyedUUID; // -1 Rank is a destroyed mesh
			break;
		case 0:
			targetMesh = this.blueprintUUID; // Rank 0 is the 'constructing' mesh
			break;
		case 1:
		case 2:
			targetMesh = this.rank1UUID;
			break;
		case 3:
		case 4:
		case 5:
		case 6:
			targetMesh = this.rank3UUID;
			break;
		case 7:
		case 8:
			targetMesh = this.rank7UUID;
			break;
		default:
			break;
		}

		return targetMesh;
	}

	public int getRankCost(int targetRank) {

		// Set a MAXINT rankcost in case something goes wrong

		int rankCost = Integer.MAX_VALUE;

		// Sanity chack for retrieving a rankcost outside proper range

        if ((targetRank > maxRank) || (targetRank < 0)) {
			Logger.error( "Attempt to retrieve rankcost for rank of" + targetRank);
			return rankCost;
		}

		// Select linear equation for rank cost based upon the
		// buildings current Maintenance BuildingGroup.

		switch (this.buildingGroup) {

		case GENERICNOUPGRADE:
		case WALLSTRAIGHT:
		case WALLSTAIRS:
		case WALLCORNER:
		case SMALLGATE:
		case ARTYTOWER:
		case SIEGETENT:
		case BULWARK:
		case BANESTONE:
		case SHACK:
			break; // This set cannot be upgraded.  Returns max integer.

		case TOL:
			rankCost = (880000 * targetRank) - 440000;
			break;
		case BARRACK:
		case VILLA:
		case ESTATE:
		case FORTRESS:
		case CITADEL:
			rankCost = (451000 * targetRank) - 308000;
			break;
		case CHURCH:
			rankCost = (682000 * targetRank) - 110000;
			break;
		case FORGE:
		case INN:
		case TAILOR:
		case MAGICSHOP:
			rankCost = (440000 * targetRank) - 550000;
			break;
		case SPIRE:
			rankCost = (176000 * targetRank) - 88000;
			break;
		case AMAZONHALL:
		case CATHEDRAL:
		case GREATHALL:
		case KEEP:
		case THIEFHALL:
		case TEMPLEHALL:
		case WIZARDHALL:
		case ELVENHALL:
		case ELVENSANCTUM:
		case IREKEIHALL:
		case FORESTHALL:
			rankCost = (682000 * targetRank) - 110000;
			break;
		default:
            Logger.error("Attempt to retrieve rankcost without MaintGroup for " + this.buildingGroup.name());
			break;
		}

		return rankCost;
	}

	public int getRankTime(int targetRank) {

		// Set a very long rankTime in case something goes wrong

		int rankTime = (Integer.MAX_VALUE / 2);

		// Set all initial construction to a default of 4 hours.

		if (targetRank == 1)
			return 4;

		// Sanity chack for retrieving a ranktime outside proper range

        if ((targetRank > maxRank) || (targetRank < 1)) {
			Logger.error( "Attempt to retrieve ranktime for rank of" + targetRank);
			return rankTime;
		}

		// Select equation for rank time based upon the
		// buildings current Maintenance BuildingGroup.  These values
		// are expressed in hours

		switch (this.buildingGroup) {

		case GENERICNOUPGRADE:
			break; // Cannot be upgraded
		case VILLA:
		case ESTATE:
		case FORTRESS:
		case CITADEL:
			rankTime = (7 * targetRank) - 7;
			break;
		case TOL:
			rankTime = (7 * targetRank) - 7;
			break;
		case BARRACK:
			rankTime = (7 * targetRank) - 7;
			break;
		case CHURCH:
			rankTime = (7 * targetRank) - 7;
			break;
		case FORGE:
		case INN:
		case TAILOR:
		case MAGICSHOP:
			rankTime = (7 * targetRank) - 7;
			break;
		case SPIRE:
			rankTime = (4 * targetRank) + 4;
			break;
		case AMAZONHALL:
		case CATHEDRAL:
		case GREATHALL:
		case KEEP:
		case THIEFHALL:
		case TEMPLEHALL:
		case WIZARDHALL:
		case ELVENHALL:
		case ELVENSANCTUM:
		case IREKEIHALL:
		case FORESTHALL:
			rankTime = (7 * targetRank) - 7;
			break;
		default:
			Logger.error("Attempt to retrieve ranktime without MaintGroup");
			break;
		}

		return rankTime;
	}

	public Vector2f getExtents() {

        if (blueprintUUID == 1302600)
			return Blueprint.IrikieForgeExtents;
		else if (blueprintUUID == 1300600)
			return Blueprint.IrikieBarracksExtents;

		return this.buildingGroup.getExtents();

	}

	public boolean isWallPiece() {

        switch (this.buildingGroup) {
		case WALLSTRAIGHT:
		case WALLSTAIRS:
		case ARTYTOWER:
		case WALLCORNER:
		case SMALLGATE:
			return true;
		default:
			break;
		}
		return false;
	}

	public boolean isSiegeEquip() {

        switch (this.buildingGroup) {
		case BULWARK:
		case SIEGETENT:
			return true;
		default:
			break;
		}
		return false;

	}

	public int getBlueprintUUID() {
		return blueprintUUID;
	}


	@Override
	public boolean equals(Object object) {

		if ((object instanceof Blueprint) == false)
			return false;

		Blueprint blueprint = (Blueprint) object;

        return this.blueprintUUID == blueprint.blueprintUUID;
	}

	@Override
	public int hashCode() {

		return this.blueprintUUID ;
	}

	public int getMaintCost(int rank) {

		int maintCost = Integer.MAX_VALUE;

        switch (this.buildingGroup) {
		case TOL:
		case BARRACK:
			maintCost = (61500 * rank) + 19500;
			break;
		case SPIRE:
			maintCost = (4800 * rank) + 1200;
			break;
		default:
            if (maxRank == 1)
				maintCost = 22500;
			else
				maintCost = (15900 * rank) + 3300;
			break;
		}

		return maintCost;
	}
}
