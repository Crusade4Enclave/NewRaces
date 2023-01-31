// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.ai.StaticMobActions;
import engine.db.handlers.*;
import engine.objects.*;
import engine.pooling.ConnectionPool;
import engine.server.MBServerStatics;
import engine.util.Hasher;
import org.pmw.tinylog.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentHashMap;

public enum DbManager {
	DBMANAGER;

	private static ConnectionPool connPool;
	public static Hasher hasher;

	//Local Object Caching

	private static final EnumMap<GameObjectType, ConcurrentHashMap<Integer, AbstractGameObject>> objectCache = new EnumMap<>(GameObjectType.class);

	public static boolean configureDatabaseLayer() {

		boolean worked = true;

		try {
			DbManager.connPool = new ConnectionPool();
			DbManager.connPool.fill(10);
			DBMANAGER.hasher = new Hasher();
		} catch (Exception e ) {
			e.printStackTrace();
			worked = false;
		}
		return worked;
	}

	public static AbstractGameObject getObject(GameObjectType objectType, int objectUUID) {

		AbstractGameObject outObject = null;

		switch (objectType) {
		case PlayerCharacter:
			outObject = PlayerCharacter.getPlayerCharacter(objectUUID);
			break;
		case NPC:
			outObject =  NPC.getNPC(objectUUID);
			break;
		case Mob:
			outObject = StaticMobActions.getFromCache(objectUUID);
			break;
		case Building:
			outObject = BuildingManager.getBuilding(objectUUID);
			break;
		case Guild:
			outObject = Guild.getGuild(objectUUID);
			break;
		case Item:
			outObject = Item.getFromCache(objectUUID);
			break;
		case MobLoot:
			outObject = MobLoot.getFromCache(objectUUID);
			break;
		case City:
			outObject = City.getCity(objectUUID);
			break;
			default:
				Logger.error("Attempt to retrieve nonexistant " + objectType +
						        " from object cache." );
				break;

		}

		return outObject;
	}

	public static int getPoolSize(){
		return connPool.getPoolSize();
	}

	public static boolean inCache(GameObjectType gameObjectType, int uuid) {

		if (objectCache.get(gameObjectType) == null)
			return false;

		return (objectCache.get(gameObjectType).containsKey(uuid));

	}

	public static AbstractGameObject getFromCache(GameObjectType gameObjectType, int uuid) {

		if (objectCache.get(gameObjectType) == null)
			return null;

		return objectCache.get(gameObjectType).get(uuid);

	}

	public static void removeFromCache(GameObjectType gameObjectType, int uuid) {

		AbstractGameObject abstractGameObject;

		if (objectCache.get(gameObjectType) == null)
			return;

		abstractGameObject = objectCache.get(gameObjectType).get(uuid);

		if (abstractGameObject == null)
			return;

		removeFromCache(abstractGameObject);

	}

	public static void removeFromCache(AbstractGameObject abstractGameObject) {

		if (abstractGameObject == null)
			return;

		if (objectCache.get(abstractGameObject.getObjectType()) == null)
			return;

		// Remove object from game cache

		objectCache.get(abstractGameObject.getObjectType()).remove(abstractGameObject.getObjectUUID());

		// Release bounds as we're dispensing with this object.

		if (abstractGameObject instanceof AbstractWorldObject) {
			AbstractWorldObject abstractWorldObject = (AbstractWorldObject)abstractGameObject;

			if (abstractWorldObject.getBounds() != null) {
				abstractWorldObject.getBounds().release();
				abstractWorldObject.setBounds(null);
			}
		}

	}

	public static boolean addToCache(AbstractGameObject gameObject) {

		boolean isWorldServer = ConfigManager.serverType.equals(Enum.ServerType.WORLDSERVER);

		if (!isWorldServer) {
			if (MBServerStatics.SKIP_CACHE_LOGIN)
				return true;
			if (MBServerStatics.SKIP_CACHE_LOGIN_PLAYER
					&& (gameObject.getObjectType() == GameObjectType.PlayerCharacter))
				return true;
			if (MBServerStatics.SKIP_CACHE_LOGIN_ITEM &&
					(gameObject.getObjectType() == GameObjectType.Item))
				return true;
		}

		// First time this object type has been cached.  Create the hashmap.

		if (objectCache.get(gameObject.getObjectType()) == null) {

			int initialCapacity;

			// Provide initial sizing hints

			switch (gameObject.getObjectType()) {
			case Building:
				initialCapacity = 46900;
				break;
			case Mob:
				initialCapacity = 11700;
				break;
			case NPC:
				initialCapacity = 900;
				break;
			case Zone:
				initialCapacity = 1070;
				break;
			case Account:
				initialCapacity = 10000;
				break;
			case Guild:
				initialCapacity = 100;
				break;
			case ItemContainer:
				initialCapacity = 100;
				break;
			case Item:
				initialCapacity = 1000;
				break;
			case MobLoot:
				initialCapacity = 10000;
				break;
			case PlayerCharacter:
				initialCapacity = 100;
				break;
			default:
				initialCapacity = 100; // Lookup api default should be ok for small maps
				break;
			}
			objectCache.put(gameObject.getObjectType(), new ConcurrentHashMap<>(initialCapacity));
		}

		// Add the object to the cache.  This will overwrite the current map entry.

		objectCache.get(gameObject.getObjectType()).put(gameObject.getObjectUUID(), gameObject);

		return true;
	}

	public static java.util.Collection<AbstractGameObject> getList(GameObjectType gameObjectType) {

		if (objectCache.get(gameObjectType) == null)
			return null;

		return objectCache.get(gameObjectType).values();
	}

	public static PreparedStatement prepareStatement(String sql) throws SQLException {
		return getConn().prepareStatement(sql, 1);
	}

	// Omg refactor this out, somebody!

	public static ConcurrentHashMap<Integer, AbstractGameObject> getMap(
			GameObjectType gameObjectType) {

		if (objectCache.get(gameObjectType) == null)
			return null;

		return objectCache.get(gameObjectType);

	}

	public static void printCacheCount(PlayerCharacter pc) {
		ChatManager.chatSystemInfo(pc, "Cache Lists");

		for (GameObjectType gameObjectType : GameObjectType.values()) {

			if (objectCache.get(gameObjectType) == null)
				continue;

			String ret = gameObjectType.name() + ": " + objectCache.get(gameObjectType).size();
			ChatManager.chatSystemInfo(pc, ret + '\n');
		}
	}

	/**
	 * @return the conn
	 */
	//XXX I think we have a severe resource leak here! No one is putting the connections back!
	public static Connection getConn() {
		Connection conn = DbManager.connPool.get();
		try {
			if (!conn.isClosed())
				DbManager.connPool.put(conn);
		} catch (SQLException e) {
			Logger.error( e.toString());
		}
		return conn;
	}

	public static final dbAccountHandler AccountQueries = new dbAccountHandler();
	public static final dbBaneHandler BaneQueries = new dbBaneHandler();
	public static final dbBaseClassHandler BaseClassQueries = new dbBaseClassHandler();
	public static final dbBuildingHandler BuildingQueries = new dbBuildingHandler();
	public static final dbBuildingLocationHandler BuildingLocationQueries = new dbBuildingLocationHandler();
	public static final dbCharacterPowerHandler CharacterPowerQueries = new dbCharacterPowerHandler();
	public static final dbCharacterRuneHandler CharacterRuneQueries = new dbCharacterRuneHandler();
	public static final dbCharacterSkillHandler CharacterSkillQueries = new dbCharacterSkillHandler();
	public static final dbCityHandler CityQueries = new dbCityHandler();
	public static final dbContractHandler ContractQueries = new dbContractHandler();
	public static final dbWarehouseHandler WarehouseQueries = new dbWarehouseHandler();
	public static final dbCSSessionHandler CSSessionQueries = new dbCSSessionHandler();
	public static final dbEnchantmentHandler EnchantmentQueries = new dbEnchantmentHandler();
	public static final dbEffectsResourceCostHandler EffectsResourceCostsQueries = new dbEffectsResourceCostHandler();
	public static final dbGuildHandler GuildQueries = new dbGuildHandler();
	public static final dbItemHandler ItemQueries = new dbItemHandler();
	public static final dbItemBaseHandler ItemBaseQueries = new dbItemBaseHandler();
	public static final dbKitHandler KitQueries = new dbKitHandler();
	public static final dbLootTableHandler LootQueries = new dbLootTableHandler();
	public static final dbMenuHandler MenuQueries = new dbMenuHandler();
	public static final dbMineHandler MineQueries = new dbMineHandler();
	public static final dbMobHandler MobQueries = new dbMobHandler();
	public static final dbMobBaseHandler MobBaseQueries = new dbMobBaseHandler();
	public static final dbNPCHandler NPCQueries = new dbNPCHandler();
	public static final dbPlayerCharacterHandler PlayerCharacterQueries = new dbPlayerCharacterHandler();
	public static final dbPromotionClassHandler PromotionQueries = new dbPromotionClassHandler();
	public static final dbRaceHandler RaceQueries = new dbRaceHandler();
	public static final dbResistHandler ResistQueries = new dbResistHandler();
	public static final dbRuneBaseAttributeHandler RuneBaseAttributeQueries = new dbRuneBaseAttributeHandler();
	public static final dbRuneBaseEffectHandler RuneBaseEffectQueries = new dbRuneBaseEffectHandler();
	public static final dbRuneBaseHandler RuneBaseQueries = new dbRuneBaseHandler();
	public static final dbSkillBaseHandler SkillsBaseQueries = new dbSkillBaseHandler();
	public static final dbSkillReqHandler SkillReqQueries = new dbSkillReqHandler();
	public static final dbSpecialLootHandler SpecialLootQueries = new dbSpecialLootHandler();
	public static final dbVendorDialogHandler VendorDialogQueries = new dbVendorDialogHandler();
	public static final dbZoneHandler ZoneQueries = new dbZoneHandler();
	public static final dbRealmHandler RealmQueries = new dbRealmHandler();
	public static final dbBlueprintHandler BlueprintQueries = new dbBlueprintHandler();
	public static final dbBoonHandler BoonQueries = new dbBoonHandler();
	public static final dbShrineHandler ShrineQueries = new dbShrineHandler();
	public static final dbHeightMapHandler HeightMapQueries = new dbHeightMapHandler();
}
