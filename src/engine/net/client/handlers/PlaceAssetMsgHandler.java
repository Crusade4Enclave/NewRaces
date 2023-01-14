package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.*;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.RealmMap;
import engine.InterestManagement.WorldGrid;
import engine.db.archive.CityRecord;
import engine.db.archive.DataWarehouse;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.math.Bounds;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.CityZoneMsg;
import engine.net.client.msg.ClientNetMsg;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.PlaceAssetMsg;
import engine.net.client.msg.PlaceAssetMsg.PlacementInfo;
import engine.objects.*;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;
import sun.util.calendar.ZoneInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * @Summary: Processes application protocol message which requests
 *  creation of new city / buildings from seeds/deeds in inventory.
 */
public class PlaceAssetMsgHandler extends AbstractClientMsgHandler {

	// Useful constants
	// ActionType 1 = client request
	//            2 = Server confirms open window
	//            3 = Request to place asset
	//            4 = Server confirms/close window
	private static final int CLIENTREQ_UNKNOWN = 1;
	private static final int SERVER_OPENWINDOW = 2;
	private static final int CLIENTREQ_NEWBUILDING = 3;  // Request to place asset
	private static final int SERVER_CLOSEWINDOW = 4;

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public PlaceAssetMsgHandler() {

		super(PlaceAssetMsg.class);

	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration

		PlaceAssetMsg msg;
		Boolean buildingCreated;

		// Character location and session

		PlayerCharacter playerCharacter;
		PlacementInfo buildingList;
		Blueprint buildingBlueprint;

		// Tell compiler it's ok to trust us and parse
		// what we need from the message structure

		msg = (PlaceAssetMsg) baseMsg;

		// Action type 3 is a client requesting to place an object
		// For all other action types let's just early exit

		if (msg.getActionType() != CLIENTREQ_NEWBUILDING)
			return true;

		// assign our character

		playerCharacter = SessionManager.getPlayerCharacter(origin);

		// We need to figure out what exactly the player is attempting
		// to place, as some objects like tol/bane/walls are edge cases.
		// So let's get the first item in their list.

		buildingList = msg.getFirstPlacementInfo();

		// Early exit if null building list.

		if (buildingList == null) {
			Logger.error("Player " + playerCharacter.getCombinedName()
					+ " null building list on deed use");
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			closePlaceAssetWindow(origin);
			return true;
		}

		Item contract = null;

		for (Item inventoryItem : playerCharacter.getInventory()) {
			if (inventoryItem.getItemBase().getUseID() == buildingList.getBlueprintUUID()) {
				contract = inventoryItem;
				break;
			}
		}

		// Grab the blueprint from the uuid in the message

		buildingBlueprint = Blueprint.getBlueprint(buildingList.getBlueprintUUID());

		// Early exit if blueprint can't be retrieved for the object.

		if (buildingBlueprint == null) {
			Logger.error("Player " + playerCharacter.getCombinedName()
					+ " null blueprint UUID: " + buildingList.getBlueprintUUID() + " on deed use");
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			closePlaceAssetWindow(origin);
			return true;
		}

		// Let's now attempt to place the building
		buildingCreated = false;

		// Many buildings have particular validation and
		// post-creation cleanup requirements.

		boolean close = true;
		lock.writeLock().lock();
		boolean isSiege = false;
		try {
			switch (buildingBlueprint.getBuildingGroup()) {

				case TOL:
					if (contract == null)
						break;
					buildingCreated = placeTreeOfLife(playerCharacter, origin, msg);
					break;
				case WAREHOUSE:
					if (contract == null)
						break;
					if (!playerCharacter.getCharItemManager().doesCharOwnThisItem(contract.getObjectUUID()))
						break;
					buildingCreated = placeWarehouse(playerCharacter, origin, msg);
					break;
				case SIEGETENT:
				case BULWARK:
					if (contract == null)
						break;
					if (!playerCharacter.getCharItemManager().doesCharOwnThisItem(contract.getObjectUUID()))
						break;
					buildingCreated = placeSiegeEquip(playerCharacter, origin, msg);
					break;
				case SPIRE:
					if (contract == null)
						break;
					if (!playerCharacter.getCharItemManager().doesCharOwnThisItem(contract.getObjectUUID()))
						break;
					buildingCreated = placeSpire(playerCharacter, origin, msg);
					break;
				case SHRINE:
					if (contract == null)
						break;
					if (!playerCharacter.getCharItemManager().doesCharOwnThisItem(contract.getObjectUUID()))
						break;
					buildingCreated = placeShrine(playerCharacter, origin, msg);
					break;
				case BARRACK:
					if (contract == null)
						break;
					if (!playerCharacter.getCharItemManager().doesCharOwnThisItem(contract.getObjectUUID()))
						break;
					buildingCreated = placeBarrack(playerCharacter, origin, msg);
					break;
				case WALLSTRAIGHT:
				case WALLCORNER:
				case SMALLGATE:
				case ARTYTOWER:
				case WALLSTAIRS:
					buildingCreated = placeCityWalls(playerCharacter, origin, msg);
					close = false;
					break;
				default:
					if (contract == null)
						break;
					if (!playerCharacter.getCharItemManager().doesCharOwnThisItem(contract.getObjectUUID()))
						break;
					buildingCreated = placeSingleBuilding(playerCharacter, origin, msg);
					break;
			}
		} catch (Exception e) {
			Logger.error("PlaceAssetHandler", e.getMessage());
			e.printStackTrace();
		} finally {
			lock.writeLock().unlock();
		}

		// Update the player's last contract (What is this used for?)

		playerCharacter.setLastContract(msg.getContractID());

		// Remove the appropiate deed.
		if (buildingCreated == true)
			if (contract != null) {
				playerCharacter.getCharItemManager().delete(contract);
				playerCharacter.getCharItemManager().updateInventory();
			}

		// Close the window.  We're done!
		//DONT CLOSE THE WINDOW IF WALL KTHANX

		if (close)
			closePlaceAssetWindow(origin);
		return true;
	}

	// Default method: Validates and places all buildings that do not
	//  require special treatment in some fashion.

	private boolean placeSingleBuilding(PlayerCharacter playerCharacter, ClientConnection origin, PlaceAssetMsg msg) {

		PlacementInfo buildingList;
		Zone serverZone;

		// Retrieve the building details we're placing

		buildingList = msg.getFirstPlacementInfo();

		serverZone = ZoneManager.findSmallestZone(buildingList.getLoc());
		// Early exit if something went horribly wrong
		// with locating the current or zone

		if (serverZone == null) {
			Logger.error("Null zone in placeSingleBuilding");
			return false;
		}

		// Method checks validation conditions arising when placing
		// buildings.  Player must be on a city grid, must be
		// inner council of the city's guild, etc.

		if (validateBuildingPlacement(serverZone, msg, origin, playerCharacter, buildingList) == false)
			return false; // Close window here?

		// Place the building
		if (createStructure(playerCharacter, buildingList, serverZone) == null) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return false;
		}

		return true;
	}

	private boolean placeWarehouse(PlayerCharacter player, ClientConnection origin, PlaceAssetMsg msg) {

		Zone serverZone;
		City cityObject;
		PlacementInfo buildingList;

		// Retrieve the building details we're placing

		buildingList = msg.getFirstPlacementInfo();

		// Setup working variables we'll need

		serverZone = ZoneManager.findSmallestZone(buildingList.getLoc());

		// Early exit if something went horribly wrong

		if (serverZone == null)
			return false;

		cityObject = City.getCity(serverZone.getPlayerCityUUID());

		// Early exit if something went horribly wrong

		if (cityObject == null)
			return false;

		// Method checks validation conditions arising when placing
		// buildings.  Player must be on a city grid, must be
		// inner council of the city's guild, etc.

		if (validateCityBuildingPlacement(serverZone, msg, origin, player, buildingList) == false)
			return false;

		if (cityObject.getWarehouse() != null) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 50, "");  //"You can only have one warehouse"
			return false;
		}

		// Create the warehouse object and it's entry in the database

		if (createWarehouse(player, msg.getFirstPlacementInfo(), serverZone) == false) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return false;
		}

		return true;
	}

	private boolean placeSiegeEquip(PlayerCharacter player, ClientConnection origin, PlaceAssetMsg msg) {

		Zone serverZone;
		Building siegeBuilding;
		PlacementInfo buildingList;
		City serverCity;

		// Retrieve the building details we're placing

		buildingList = msg.getFirstPlacementInfo();

		// Setup working variables we'll need

		serverZone = ZoneManager.findSmallestZone(buildingList.getLoc());

		// Early exit if something went horribly wrong
		// with locating the current city and/or zone

		if (serverZone == null) {
			Logger.error("Error obtaining reference to zone");
			return false;
		}

		// Checks validation conditions arising when placing
		// generic structures.

		if (validateBuildingPlacement(serverZone, msg, origin, player, buildingList) == false)
			return false;

		// If there is a bane placed, only the attackers and defenders can
		// place siege assets

		serverCity = ZoneManager.getCityAtLocation(buildingList.getLoc());

		//no city found
		//check if attacker city.
		if (serverCity == null){
			Bane bane = Bane.getBaneByAttackerGuild(player.getGuild());
			City attackerCity = null;
			if (bane != null)
				attackerCity = bane.getCity();

			if (attackerCity != null)
				if (buildingList.getLoc().isInsideCircle(attackerCity.getLoc(), Enum.CityBoundsType.SIEGE.extents))
					serverCity = attackerCity;
		}
		//no city found for attacker city,
		//check if defender city

		if (serverCity == null){
			if (player.getGuild().getOwnedCity() != null)
				if (buildingList.getLoc().isInsideCircle(player.getGuild().getOwnedCity().getLoc(), Enum.CityBoundsType.SIEGE.extents))
					serverCity = player.getGuild().getOwnedCity();
		}

		if ((serverCity != null) &&
				(serverCity.getBane() != null)) {

			// Set the server zone to the city zone in order to account for being inside
			// the siege bounds buffer area

			serverZone = serverCity.getParent();

			if ((player.getGuild().equals(serverCity.getBane().getOwner().getGuild()) == false)
					&& (player.getGuild().equals(serverCity.getGuild()) == false)) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 54, ""); // Must belong to attacker or defender
				return false;
			}
		}

		// cant place siege equipment off city zone.
		
		// Create the siege Building

		siegeBuilding = createStructure(player, msg.getFirstPlacementInfo(), serverZone);
		
		// Oops something went really wrong

		if (siegeBuilding == null)
			return false;

		// If there is a bane placed, we limit placement to  2x the stone rank's worth of attacker assets
		// and 1x the tree rank for defenders
		
		if (validateSiegeLimits(player, origin, serverCity.getBane()) == false)
			return true;
		
		// passes validation: can assign auto-protection to war asset

		if (serverCity.getBane() != null)
			if (serverCity.isLocationOnCityGrid(siegeBuilding.getBounds()))
				if (player.getGuild().equals(serverCity.getBane().getOwner().getGuild()))
					return true;

		siegeBuilding.setProtectionState(ProtectionState.PROTECTED);
		
		// No bane placed.  We're done!
		
		return true;
	}

	private  boolean validateSiegeLimits(PlayerCharacter playerCharacter, ClientConnection origin, Bane bane) {

		City serverCity = bane.getCity();
		HashSet<AbstractWorldObject> awoList;
		
		int maxAttackerAssets = serverCity.getBane().getStone().getRank() * 2;
		int maxDefenderAssets = serverCity.getRank();
		int numDefenderBuildings = 0;
		int numAttackerBuildings = 0;
		
		// Count bow for attackers and defenders

		awoList =  WorldGrid.getObjectsInRangePartial(serverCity, 1000, MBServerStatics.MASK_BUILDING);

		for (AbstractWorldObject awo : awoList) {
			Building building = (Building) awo;

		if (building.getBlueprint() != null)
			if (!building.getBlueprint().isSiegeEquip())
				continue;

		if (!building.getLoc().isInsideCircle(serverCity.getLoc(), Enum.CityBoundsType.SIEGE.extents))
			continue;

		if (building.getGuild() == null)
			continue;

		if (building.getGuild().isErrant())
			continue;

		if (!building.getGuild().equals(serverCity.getGuild()) && !building.getGuild().equals(serverCity.getBane().getOwner().getGuild()))
			continue;

		// Only count auto protected buildings
		if (building.getProtectionState() != ProtectionState.PROTECTED)
			continue;

		if (building.getGuild().equals(serverCity.getGuild()))
			numDefenderBuildings++;
		
		if (building.getGuild().equals(serverCity.getBane().getOwner().getGuild()))
			numAttackerBuildings++;
	
			// Validate bane limits on siege assets

			if (playerCharacter.getGuild().equals(serverCity.getGuild())) {
				//defender attempting to place asset
				if (numDefenderBuildings >= maxDefenderAssets) {
					PlaceAssetMsg.sendPlaceAssetError(origin, 62, "");
					return false;
				}
			}
			
			if (playerCharacter.getGuild().equals(serverCity.getBane().getStone().getGuild())) {
				//attacker attempting to place asset
				if (numAttackerBuildings >= maxAttackerAssets) {
					PlaceAssetMsg.sendPlaceAssetError(origin, 61, "");
					return false;
				}
			}
			
		}
		// Passed validation
		
		return  true;
	}
	
			
	private boolean placeTreeOfLife(PlayerCharacter playerCharacter, ClientConnection origin, PlaceAssetMsg msg) {

		Realm serverRealm;
		Zone serverZone;
		ArrayList<AbstractGameObject> cityObjects; // MySql result set
		PlacementInfo treeInfo;
		Building treeObject = null;
		City cityObject = null;
		Zone cityZone = null;
		Guild playerNation;
		PlacementInfo treePlacement = msg.getFirstPlacementInfo();

		// Setup working variables we'll need

		serverRealm = RealmMap.getRealmAtLocation(treePlacement.getLoc());
		serverZone = ZoneManager.findSmallestZone(treePlacement.getLoc());

		// Early exit if something went horribly wrong
		// with locating the current realm and/or zone

		if (serverRealm == null || serverZone == null)
			return false;

		// Method checks validation conditions arising when placing
		// trees

		if (validateTreeOfLifePlacement(playerCharacter, serverRealm, serverZone, origin, msg) == false)
			return false;

		// Retrieve tree info for the w value it's passing.

		treeInfo = msg.getFirstPlacementInfo();

		if (treeInfo == null) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return false;
		}

		Vector3fImmutable plantLoc = new Vector3fImmutable(treeInfo.getLoc().x,
				serverZone.getHeightMap().getInterpolatedTerrainHeight(treeInfo.getLoc()),
				treeInfo.getLoc().z);

		cityObjects = DbManager.CityQueries.CREATE_CITY(playerCharacter.getObjectUUID(), serverZone.getObjectUUID(),
				serverRealm.getRealmID(),
				plantLoc.x - serverZone.getAbsX(), plantLoc.y,
				plantLoc.z - serverZone.getAbsZ(), treeInfo.getRot().y, treeInfo.getW(), playerCharacter.getGuild().getName(), LocalDateTime.now());

		// Uh oh!
		if (cityObjects == null || cityObjects.isEmpty()) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return false;
		}

		// Assign our worker variables after figuring out what
		// is what in the result set.

		for (AbstractGameObject gameObject : cityObjects) {

			switch (gameObject.getObjectType()) {
				case Building:
					treeObject = (Building) gameObject;
					treeObject.runAfterLoad();
					break;
				case City:
					cityObject = (City) gameObject;
					break;
				case Zone:
					cityZone = (Zone) gameObject;
					break;
				default:
					// log some error here? *** Refactor
			}
		}

		//?? your not allowed to plant a tree if ur not an errant guild.
		// Desub from any previous nation.
		// This should be done automatically in a method inside Guild *** Refactor
		// Player is now a Soverign guild, configure them as such.

		playerCharacter.getGuild().setNation(playerCharacter.getGuild());
		playerNation = playerCharacter.getGuild();
		playerNation.setGuildState(GuildState.Sovereign);

		// Link the zone with the city and then add
		// to the appropritae hash tables and cache

		cityZone.setPlayerCity(true);

		if (cityZone.getParent() != null)
			cityZone.getParent().addNode(cityZone); //add as child to parent

		ZoneManager.addZone(cityZone.getObjectUUID(), cityZone);
		ZoneManager.addPlayerCityZone(cityZone);
		serverZone.addNode(cityZone);

		cityZone.generateWorldAltitude();

		cityObject.setParent(cityZone);
		cityObject.setObjectTypeMask(MBServerStatics.MASK_CITY); // *** Refactor : should have it already
		//Link the tree of life with the new zone

		treeObject.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
		treeObject.setParentZone(cityZone);
		MaintenanceManager.setMaintDateTime(treeObject, LocalDateTime.now().plusDays(7));

		// Update guild binds and tags
		//load the new city on the clients

		CityZoneMsg czm = new CityZoneMsg(1, treeObject.getLoc().x, treeObject.getLoc().y, treeObject.getLoc().z, cityObject.getCityName(), cityZone, Enum.CityBoundsType.ZONE.extents, Enum.CityBoundsType.ZONE.extents);
		DispatchMessage.dispatchMsgToAll(czm);

		GuildManager.updateAllGuildBinds(playerNation, cityObject);
		GuildManager.updateAllGuildTags(playerNation);

		// Send all the cities to the clients?
		// *** Refactor : figure out how to send like, one?

		City.lastCityUpdate = System.currentTimeMillis();
		WorldGrid.addObject(treeObject, playerCharacter);

		serverRealm.addCity(cityObject.getObjectUUID());
		playerNation.setCityUUID(cityObject.getObjectUUID());

		// Bypass warehouse entry if we're an admin

		if (playerCharacter.getAccount().status.equals(AccountStatus.ADMIN))
			return true;

		// Push this event to the data warehouse

		CityRecord cityRecord = CityRecord.borrow(cityObject, RecordEventType.CREATE);
		DataWarehouse.pushToWarehouse(cityRecord);

		return true;
	}

	private boolean placeSpire(PlayerCharacter playerCharacter, ClientConnection origin, PlaceAssetMsg msg) {

		Zone serverZone;
		Building spireBuilding;
		Blueprint blueprint;
		City cityObject;
		PlacementInfo buildingList;

		// Setup working variables we'll need

		buildingList = msg.getFirstPlacementInfo();

		serverZone = ZoneManager.findSmallestZone(buildingList.getLoc());

		// Early exit if something went horribly wrong
		// with locating the current realm and/or city

		if (serverZone == null)
			return false;

		cityObject = City.getCity(serverZone.getPlayerCityUUID());

		if (cityObject == null)
			return false;

		// Method checks validation conditions arising when placing
		// buildings.  Player must be on a city grid, must be
		// inner council of the city's guild, etc.

		if (validateCityBuildingPlacement(serverZone, msg, origin, playerCharacter, buildingList) == false)
			return false;

		// Loop through all buildings in this city looking for a spire of the.
		// same type we are placing.  There can be only one of each type

		int spireCount = 0;

		blueprint = Blueprint.getBlueprint(msg.getFirstPlacementInfo().getBlueprintUUID());

		for (Building building : serverZone.zoneBuildingSet) {

			if (building.getBlueprint().getBuildingGroup() == BuildingGroup.SPIRE) {

				if (building.getBlueprintUUID() == blueprint.getMeshForRank(0)) {
					PlaceAssetMsg.sendPlaceAssetError(origin, 46, "");  // "Spire of that type exists"
					return false;
				}
				spireCount++;
			}
		}

		// Too many spires for this tree's rank?

		if (spireCount >= Blueprint.getMaxShrines(cityObject.getTOL().getRank())) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 45, "");  //Tree cannot support anymore spires
			return false;
		}

		// Create the spire

		spireBuilding = createStructure(playerCharacter, msg.getFirstPlacementInfo(), serverZone);
		return spireBuilding != null;
	}

	private boolean placeShrine(PlayerCharacter playerCharacter, ClientConnection origin, PlaceAssetMsg msg) {

		Zone serverZone;
		Blueprint blueprint;
		City cityObject;
		PlacementInfo buildingList;

		// Setup working variables we'll need
		buildingList = msg.getFirstPlacementInfo();

		serverZone = ZoneManager.findSmallestZone(buildingList.getLoc());

		// Early exit if something went horribly wrong
		// with locating the current realm and/or zone
		if (serverZone == null)
			return false;

		// Method checks validation conditions arising when placing
		// buildings.  Player must be on a city grid, must be
		// inner council of the city's guild, etc.

		if (validateCityBuildingPlacement(serverZone, msg, origin, playerCharacter, buildingList) == false)
			return false;

		// Loop through all buildings in this city looking for a shrine of the.
		// same type we are placing.  There can be only one of each type

		int shrineCount = 0;

		cityObject = City.getCity(serverZone.getPlayerCityUUID());

		// Cannot place shrine in abanadoned city.  Shrines must be owned
		// by the tol owner not the person placing them.

		if (cityObject.getTOL().getOwnerUUID() == 0) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 42, "");  //Tree cannot support anymore shrines
			return false;
		}

		blueprint = Blueprint.getBlueprint(msg.getFirstPlacementInfo().getBlueprintUUID());

		if (blueprint == null){
			return false;
		}

		for (Building building : serverZone.zoneBuildingSet) {
			if (building.getBlueprint() == null)
				continue;

			if (building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE) {
				if (building.getBlueprintUUID() == blueprint.getMeshForRank(0)) {
					PlaceAssetMsg.sendPlaceAssetError(origin, 43, "");  // "shrine of that type exists"
					return false;
				}
				shrineCount++;
			}
		}

		// Too many shrines for this tree's rank?

		if (shrineCount >= Blueprint.getMaxShrines(cityObject.getTOL().getRank())) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 42, "");  //Tree cannot support anymore shrines
			return false;
		}

		// Create the shrine

		return createShrine((PlayerCharacter)cityObject.getTOL().getOwner(), msg.getFirstPlacementInfo(), serverZone);
	}

	private boolean placeBarrack(PlayerCharacter playerCharacter, ClientConnection origin, PlaceAssetMsg msg) {

		Zone serverZone;
		City cityObject;
		PlacementInfo buildingList;

		// Setup working variables we'll need
		buildingList = msg.getFirstPlacementInfo();

		serverZone = ZoneManager.findSmallestZone(buildingList.getLoc());

		// Early exit if something went horribly wrong
		// with locating the current realm and/or zone

		if (serverZone == null)
			return false;

		// Method checks validation conditions arising when placing
		// buildings.  Player must be on a city grid, must be
		// inner council of the city's guild, etc.

		if (validateCityBuildingPlacement(serverZone, msg, origin, playerCharacter, buildingList) == false)
			return false;

		// Loop through all buildings in this city counting barracks .

		int barracksCount = 0;

		cityObject = City.getCity(serverZone.getPlayerCityUUID());

		// Cannot place barracks in abanadoned city.

		if (cityObject.getTOL().getOwnerUUID() == 0) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 42, "");  //Tree cannot support anymore shrines
			return false;
		}

		for (Building building : serverZone.zoneBuildingSet) {
			if (building.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)
				barracksCount++;
		}

		// Too many shrines for this tree's rank?

		if (barracksCount >= cityObject.getTOL().getRank()) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 47, "");  //Tree cannot support anymore shrines
			return false;
		}

		// Create the shrine

		return createBarracks((PlayerCharacter)cityObject.getTOL().getOwner(), msg.getFirstPlacementInfo(), serverZone);
	}

	private boolean placeCityWalls(PlayerCharacter player, ClientConnection origin, PlaceAssetMsg msg) {

		// Member variables

		Zone serverZone;
		City cityObject;
		int placementCost = 0;
		CharacterItemManager itemMan;
		Item goldItem;
		Building wallPiece;

		// Setup working variables we'll need

		serverZone = ZoneManager.findSmallestZone(player.getLoc());

		// Early exit if something went horribly wrong

		if (serverZone == null)
			return false;


		if (player.getCharItemManager().getGoldTrading() > 0){
			ErrorPopupMsg.sendErrorPopup(player, 195);
			return false;
		}


		// Method checks validation conditions arising when placing
		// buildings.  Player must be on a city grid, must be
		// inner council of the city's guild, etc.

		if (validateCityBuildingPlacement(serverZone, msg, origin, player, msg.getFirstPlacementInfo()) == false)
			return false;

		cityObject = City.getCity(serverZone.getPlayerCityUUID());

		// We need to be able to access how much gold a character is carrying

		itemMan = player.getCharItemManager();

		if (itemMan == null)

			return false;

		goldItem = itemMan.getGoldInventory();

		// Grab list of walls we're placing

		ArrayList<PlacementInfo> walls = msg.getPlacementInfo();

		// Character must be able to afford walls

		for (PlacementInfo wall : walls) {
			placementCost += PlaceAssetMsg.getWallCost(wall.getBlueprintUUID());
		}

		// Early exit if not enough gold in character's inventory to place walls

		if (placementCost > goldItem.getNumOfItems()) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 28, ""); // Not enough gold
			return false;
		}

		placementCost = 0; // reset placement cost for fix bug with wall pieces somethings not taking gold out if forced an error.


		// Overlap check and wall deed verifications
		for (PlacementInfo wall : walls) {

			if (Blueprint.isMeshWallPiece(wall.getBlueprintUUID()) == false) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 48, "");  //"Assets (except walls) must be placed one at a time"
				continue;
			}

			// Ignore wall pieces not on the city grid
			if (cityObject.isLocationOnCityGrid(wall.getLoc()) == false) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 1, "Asset " + cityObject.getName() + " not on citygrid");
				continue;
			}

			// Does this wall collide with any other building?

			for (Building building : serverZone.zoneBuildingSet) {


				//TODO Clean up collision with placementInfo. don't need to create the same placementinfo bounds for collision checks on each building.
				if ((building.getBlueprintUUID() != 0) && (Bounds.collide(wall, building) == true)) {

					if (building.getRank() == -1) {
						building.removeFromCache();
						WorldGrid.RemoveWorldObject(building);
						WorldGrid.removeObject(building);
						building.getParentZone().getParent().zoneBuildingSet.remove(building);
						continue;
					}
					// remove gold from walls already placed before returning.

					PlaceAssetMsg.sendPlaceAssetError(origin, 3, building.getName());  //"Conflict between assets"
					return false;
				}
			}
			placementCost = PlaceAssetMsg.getWallCost(wall.getBlueprintUUID());

			if (!itemMan.modifyInventoryGold(-placementCost)){
				ChatManager.chatSystemInfo(player, player.getFirstName() + " can't has free moneys! no for real.. Thor.. seriously... I didnt fix it because you getting laid isnt important enough for me.");
				return false;
			}
			// Attempt to place wall piece

			wallPiece = createStructure(player, wall, serverZone);

			if (wallPiece == null) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
				continue;
			}

			// walls are auto protected
			wallPiece.setProtectionState(ProtectionState.PROTECTED);
			PlaceAssetMsg.sendPlaceAssetConfirmWall(origin,serverZone);

		}

		// Deduct gold from character's inventory


		return true;
	}

	private static void closePlaceAssetWindow(ClientConnection origin) {

		// Action type 4 is the server telling the client to
		// close the asset placement window.
		// This is believed to be a confirmation message to the client
		PlaceAssetMsg pam = new PlaceAssetMsg();
		pam.setActionType(4);
		Dispatch dispatch = Dispatch.borrow(origin.getPlayerCharacter(), pam);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	}

	// Method deletes one item from the player's inventory
	// based on the mesh UUID the deed/seed spawns

	private static void removeDeedByMeshUUID(PlayerCharacter player, int meshUUID) {

		CharacterItemManager inventoryManager;
		ArrayList<Item> itemList;

		inventoryManager = player.getCharItemManager();
		itemList = player.getInventory();

		for (Item inventoryItem : itemList) {
			if (inventoryItem.getItemBase().getUseID() == meshUUID) {
				inventoryManager.delete(inventoryItem);

				inventoryManager.updateInventory();
				return;
			}

		}
	}

	// Method validates the location we have selected for our new city

	private static boolean validateTreeOfLifePlacement(PlayerCharacter playerCharacter, Realm serverRealm, Zone serverZone,
													   ClientConnection origin, PlaceAssetMsg msg) {

		PlacementInfo placementInfo = msg.getFirstPlacementInfo();

		// Your guild already owns a tree

		if (playerCharacter.getGuild().getOwnedCity() != null) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "Your guild already owns a tree!");
			return false;
		}

		// Validate that the player is the leader of a guild

		if (GuildStatusController.isGuildLeader(playerCharacter.getGuildStatus()) == false) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 10, ""); // Must be a guild leader
			return false;
		}

		// Validate that the player is the leader of a guild
		// that is not currently Sovereign  *** BUG? Doesn't look right.  isGuildLeader()?

		if ((playerCharacter.getGuild().getGuildState() != GuildState.Sworn
				|| playerCharacter.getGuild().getGuildState() != GuildState.Errant) == false) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 17, ""); // Your is not an errant or soverign guild
			return false;
		}

		// All trees must be placed within a continent.

		if (!serverZone.isContininent()) {

			PlaceAssetMsg.sendPlaceAssetError(origin, 69, ""); // Tree must be within a territory
			return false;
		}

		RealmType realmType = RealmType.getRealmTypeByUUID(serverRealm.getRealmID());

		if (
				(realmType.equals(RealmType.MAELSTROM)) ||
						(realmType.equals(RealmType.OBLIVION))) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 57, playerCharacter.getName()); // No building may be placed within this territory
			return false;
		}

		// Cannot place a tree underwater

		if (HeightMap.isLocUnderwater(placementInfo.getLoc())) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 6, ""); // Cannot place underwater
			return false;
		}

		//Test city not too close to any other zone

		if (!ZoneManager.validTreePlacementLoc(serverZone, placementInfo.getLoc().x, placementInfo.getLoc().z)) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 39, ""); // Too close to another tree
			return false;
		}

		// Validate that Realm is not at it's city limit

		if (serverRealm.isRealmFull() == true) {
			int numCities;
			numCities = serverRealm.getNumCities();
			PlaceAssetMsg.sendPlaceAssetError(origin, 58, Integer.toString(numCities)); // This territory is full
			return false;
		}

		return true;
	}

	private Building createStructure(PlayerCharacter playerCharacter, PlacementInfo buildingInfo, Zone currentZone) {

		Blueprint blueprint;
		Building newMesh;
		DateTime completionDate;
		float vendorRotation;
		float buildingRotation;

		blueprint = Blueprint.getBlueprint(buildingInfo.getBlueprintUUID());

		if (blueprint == null) {
			Logger.error("CreateStucture: DB returned null blueprint.");
			return null;
		}

		// All seige buildings build in 15 minutes
		if ((blueprint.getBuildingGroup().equals(BuildingGroup.SIEGETENT))
				|| (blueprint.getBuildingGroup().equals(BuildingGroup.BULWARK)))
			completionDate = DateTime.now().plusMinutes(15);
		else
			completionDate = DateTime.now().plusHours(blueprint.getRankTime(1));

		Vector3fImmutable localLoc = new Vector3fImmutable(ZoneManager.worldToLocal(buildingInfo.getLoc(), currentZone));

		buildingRotation = buildingInfo.getRot().y;
		vendorRotation = buildingInfo.getW();

		// if W return is negative, this is a -90 rotation not a 90?

		newMesh = DbManager.BuildingQueries.CREATE_BUILDING(
				currentZone.getObjectUUID(), playerCharacter.getObjectUUID(), blueprint.getName(), blueprint.getMeshForRank(0),
				localLoc, 1.0f, blueprint.getMaxHealth(0), ProtectionState.NONE, 0, 0,
				completionDate, blueprint.getMeshForRank(0), vendorRotation, buildingRotation);

		// Make sure we have a valid mesh
		if (newMesh == null) {
			Logger.error("CreateStucture: DB returned null object.");
			return null;
		}

		newMesh.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
		MaintenanceManager.setMaintDateTime(newMesh, LocalDateTime.now().plusDays(7));

		WorldGrid.addObject(newMesh, playerCharacter);
		return newMesh;

	}

	private boolean createShrine(PlayerCharacter player, PlacementInfo buildingInfo, Zone currentZone) {

		Blueprint blueprint;
		Building newMesh;
		Shrine newShrine;
		City city;
		ShrineType shrineType;

		if (player == null)
			return false;

		blueprint = Blueprint.getBlueprint(buildingInfo.getBlueprintUUID());

		if (blueprint == null) {
			Logger.error("CreateShrine: DB returned null blueprint.");
			return false;
		}

		shrineType = Shrine.getShrineTypeByBlueprintUUID(blueprint.getBlueprintUUID());

		city = City.getCity(currentZone.getPlayerCityUUID());

		if (city == null)
			return false;

		if (!city.isLocationOnCityGrid(buildingInfo.getLoc()))
			return false;

		Vector3fImmutable localLoc = new Vector3fImmutable(ZoneManager.worldToLocal(buildingInfo.getLoc(), currentZone));

		float buildingRotation = buildingInfo.getRot().y;
		float vendorRotation = buildingInfo.getW();


		ArrayList<AbstractGameObject> shrineObjects = DbManager.ShrineQueries.CREATE_SHRINE(
				currentZone.getObjectUUID(), player.getObjectUUID(), blueprint.getName(), blueprint.getMeshForRank(0),
				localLoc, 1.0f, blueprint.getMaxHealth(0), ProtectionState.PROTECTED, 0, 0,
				DateTime.now().plusHours(blueprint.getRankTime(1)), blueprint.getMeshForRank(0), vendorRotation, buildingRotation, shrineType.name());

		if (shrineObjects == null) {
			PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return false;
		}

		for (AbstractGameObject ago : shrineObjects) {

			switch (ago.getObjectType()) {
				case Building:
					newMesh = (Building) ago;
					newMesh.runAfterLoad();
					newMesh.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
					MaintenanceManager.setMaintDateTime(newMesh, LocalDateTime.now().plusDays(7));
					WorldGrid.addObject(newMesh, player);
					break;
				case Shrine:
					newShrine = (Shrine) ago;
					newShrine.getShrineType().addShrineToServerList(newShrine);
					break;
				default:
					PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
					break;
			}
		}

		return true;
	}

	private boolean createBarracks(PlayerCharacter player, PlacementInfo buildingInfo, Zone currentZone) {

		Blueprint blueprint;
		Building newMesh;
		Shrine newShrine;
		City city;

		if (player == null)
			return false;

		blueprint = Blueprint.getBlueprint(buildingInfo.getBlueprintUUID());

		if (blueprint == null) {
			Logger.error("CreateShrine: DB returned null blueprint.");
			return false;
		}

		city = City.getCity(currentZone.getPlayerCityUUID());

		if (city == null)
			return false;

		if (!city.isLocationOnCityGrid(buildingInfo.getLoc()))
			return false;

		Vector3fImmutable localLoc = new Vector3fImmutable(ZoneManager.worldToLocal(buildingInfo.getLoc(), currentZone));

		float buildingRotation = buildingInfo.getRot().y;
		float vendorRotation = buildingInfo.getW();
		DateTime completionDate = DateTime.now().plusHours(blueprint.getRankTime(1));


		newMesh = DbManager.BuildingQueries.CREATE_BUILDING(
				currentZone.getObjectUUID(), player.getObjectUUID(), blueprint.getName(), blueprint.getMeshForRank(0),
				localLoc, 1.0f, blueprint.getMaxHealth(0), ProtectionState.PROTECTED, 0, 0,
				completionDate, blueprint.getMeshForRank(0), vendorRotation, buildingRotation);

		// Make sure we have a valid mesh
		if (newMesh == null) {
			Logger.error("CreateStucture: DB returned null object.");
			return false;
		}

		newMesh.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
		MaintenanceManager.setMaintDateTime(newMesh, LocalDateTime.now().plusDays(7));
		WorldGrid.addObject(newMesh, player);

		return true;
	}

	private boolean createWarehouse(PlayerCharacter player, PlacementInfo buildingInfo, Zone currentZone) {

		Blueprint blueprint;
		Building newMesh = null;
		ArrayList<AbstractGameObject> warehouseObjects;

		blueprint = Blueprint.getBlueprint(buildingInfo.getBlueprintUUID());

		if (blueprint == null) {
			Logger.error("CreateWarehouse: DB returned null blueprint.");
			return false;
		}

		Vector3fImmutable localLoc = new Vector3fImmutable(ZoneManager.worldToLocal(buildingInfo.getLoc(), currentZone));

		float buildingRotation = buildingInfo.getRot().y;
		float vendorRotation = buildingInfo.getW();

		warehouseObjects = DbManager.WarehouseQueries.CREATE_WAREHOUSE(
				currentZone.getObjectUUID(), player.getObjectUUID(), blueprint.getName(), blueprint.getMeshForRank(0),
				localLoc, 1.0f, blueprint.getMaxHealth(0), ProtectionState.NONE, 0, 0,
				DateTime.now().plusHours(blueprint.getRankTime(1)), blueprint.getMeshForRank(0), vendorRotation, buildingRotation);

		if (warehouseObjects == null) {
			PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
			return false;
		}

		// Load the building into the simulation

		for (AbstractGameObject ago : warehouseObjects) {

			if (ago.getObjectType() == GameObjectType.Building) {
				newMesh = (Building) ago;
				newMesh.setObjectTypeMask(MBServerStatics.MASK_BUILDING);
				MaintenanceManager.setMaintDateTime(newMesh, LocalDateTime.now().plusDays(7));
				WorldGrid.addObject(newMesh, player);
				newMesh.runAfterLoad();
			}
			else if (ago.getObjectType() == GameObjectType.Warehouse) {
				Warehouse warehouse = (Warehouse) ago;
				City city = City.getCity(currentZone.getPlayerCityUUID());
				if (city == null)
					return true;
				city.setWarehouseBuildingID(newMesh.getObjectUUID());
				Warehouse.warehouseByBuildingUUID.put(newMesh.getObjectUUID(), warehouse);
			}
		}

		return true;
	}

	// Validates that player is able to place buildings

	private static boolean validateBuildingPlacement(Zone serverZone, PlaceAssetMsg msg, ClientConnection origin, PlayerCharacter player, PlacementInfo placementInfo) {

		RealmType currentRealm;

		if(Blueprint.getBlueprint(placementInfo.getBlueprintUUID()).isSiegeEquip() == false)
		{
			if (serverZone.isPlayerCity() == false) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
				return false;
			}
			City city = ZoneManager.getCityAtLocation(placementInfo.getLoc());

			if (player.getGuild().equals(city.getGuild()) == false) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
				return false;
			}
			if (city.isLocationOnCityGrid(placementInfo.getLoc()) == false) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
				return false;
			}
		}
		else
		{
			City city = ZoneManager.getCityAtLocation(placementInfo.getLoc());

			if(city == null)
			{
				PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
				return false;
			}
			Bane bane = city.getBane();
			//check if player is owner/IC of tree or bane
			if (player.getGuild().equals(city.getGuild()) == true)
			{
				//is from owners guild
				if(GuildStatusController.isGuildLeader(player.getGuildStatus()) == false && GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
				{
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}
			}
			else
			{
				//is not from owners guild
				if(bane == null)
				{
					//bane was null
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}
				if(city == null)
				{
					//city was null
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}
				//check if player is from siege guild
				if(player.getGuild().equals(bane.getOwner().getGuild()) == false)
				{
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}

				//check if player is GL or IC of the bane guild
				if(GuildStatusController.isGuildLeader(player.getGuildStatus()) == false && GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false)
				{
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}

				//cannot place on grid until bane is live
				if(bane.getSiegePhase() != SiegePhase.WAR && city.isLocationOnCityGrid(placementInfo.getLoc()) == true)
				{
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}
				if(city.isLocationWithinSiegeBounds(placementInfo.getLoc()) == false && city.isLocationOnCityZone(placementInfo.getLoc()) == false)
				{
					PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName());
					return false;
				}
			}
		}
		// Retrieve the building details we're placing

		if (serverZone.isNPCCity() == true) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 15, ""); // Cannot place in a peace zone
			return false;
		}

		// Errant guilds cannot place assets

		if (player.getGuild().getGuildState() == GuildState.Errant) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "Only sovereign or sworn guilds may place assets.");
			return false;
		}

		// Player must be GL or IC of a guild to place buildings.

		if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false && GuildStatusController.isInnerCouncil(player.getGuildStatus()) == false) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 10, ""); // You must be a guild leader
			return false;
		}

		// Cannot place a building underwater

		if (HeightMap.isLocUnderwater(placementInfo.getLoc())) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 6, ""); // Cannot place underwater
			return false;
		}

		// Players cannot place buildings in mob zones.

		if ((serverZone.isMacroZone() == true)
				|| (serverZone.getParent().isMacroZone() == true)) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName()); // No building may be placed within this territory
			return false;
		}

		currentRealm = RealmType.getRealmTypeByUUID(RealmMap.getRealmIDAtLocation(player.getLoc()));

		if (
				(currentRealm.equals(RealmType.MAELSTROM)) ||
						(currentRealm.equals(RealmType.OBLIVION))) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 57, player.getName()); // No building may be placed within this territory
			return false;
		}

		// Cannot place assets on a dead tree

		if ((serverZone.isPlayerCity())
				&& (City.getCity(serverZone.getPlayerCityUUID()).getTOL().getRank() == -1)){
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "Cannot place asset on dead tree until world heals");
			return false;
		}

		// Overlap check

		for (Building building : serverZone.zoneBuildingSet) {

			if ((building.getBlueprintUUID() != 0) && (Bounds.collide(placementInfo, building) == true)) {

				// Ignore and remove from simulation if we are placing over rubble

				if (building.getRank() == -1) {

					if ((building.getBlueprintUUID() != 0)
							&& (building.getBlueprint().getBuildingGroup() == BuildingGroup.SHRINE)){
						Shrine.RemoveShrineFromCacheByBuilding(building);
						if (building.getCity() != null){

						}
					}

					building.removeFromCache();
					WorldGrid.RemoveWorldObject(building);
					WorldGrid.removeObject(building);
					building.getParentZone().zoneBuildingSet.remove(building);
					continue;
				}


				PlaceAssetMsg.sendPlaceAssetError(origin, 3, "");  // Conflict between proposed assets
				return false;
			}
		}

		return true;
	}

	private static boolean validateCityBuildingPlacement(Zone serverZone, PlaceAssetMsg msg, ClientConnection origin, PlayerCharacter player, PlacementInfo buildingInfo) {

		// Peform shared common validation first

		if (validateBuildingPlacement(serverZone, msg, origin, player, buildingInfo) == false)
			return false;

		// Must be a player city

		if (serverZone.isPlayerCity() == false) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 41, player.getName()); // Cannot place outisde a guild zone
			return false;
		}

		//Test zone has a city object

		City city = City.getCity(serverZone.getPlayerCityUUID());

		if (city == null) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 52, ""); //"no city to associate asset with"
			return false;
		}

		// City assets must be placed on the city grid

		if (!city.isLocationOnCityGrid(buildingInfo.getLoc())) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 1, "Assset must be placed on a City Grid");
			return false;
		}

		// Make sure it's not an errant tree

		if ( (city.getGuild() == null || city.getGuild().isErrant() == true)) {
			PlaceAssetMsg.sendPlaceAssetError(origin, 18, ""); //"There are no guild trees to be found"
			return false;
		}

		//Test player is in correct guild to place buildings

		if (!player.isCSR)
			if (player.getGuild().getObjectUUID() != city.getGuild().getObjectUUID()) {
				PlaceAssetMsg.sendPlaceAssetError(origin, 9, "");  //You must be a guild member to place this asset
				return false;
			}
		return true;
	}
}