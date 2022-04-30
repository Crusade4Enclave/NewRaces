// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.objects.SpecialLoot;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class dbSpecialLootHandler extends dbHandlerBase {

	public dbSpecialLootHandler() {
		this.localClass = SpecialLoot.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public ArrayList<SpecialLoot> GET_SPECIALLOOT(int mobbaseID) {

		prepareCallable("SELECT * FROM `static_npc_mob_specialloot` WHERE `mobbaseID`=?");
		setInt(1, mobbaseID);
		return getObjectList();
	}

	public void GenerateSpecialLoot(){
		HashMap<Integer, ArrayList<SpecialLoot>> lootSets;
		SpecialLoot lootSetEntry;
		int	lootSetID;

		lootSets = new HashMap<>();
		int recordsRead = 0;

		prepareCallable("SELECT * FROM static_zone_npc_specialloot");

		try {
			ResultSet rs = executeQuery();

			while (rs.next()) {

				recordsRead++;

				lootSetID = rs.getInt("lootSet");
				lootSetEntry = new SpecialLoot(rs,true);

				if (lootSets.get(lootSetID) == null){
					ArrayList<SpecialLoot> lootList = new ArrayList<>();
					lootList.add(lootSetEntry);
					lootSets.put(lootSetID, lootList);
				}
				else{
					ArrayList<SpecialLoot>lootList = lootSets.get(lootSetID);
					lootList.add(lootSetEntry);
					lootSets.put(lootSetID, lootList);
				}
			}

			Logger.info( "read: " + recordsRead + " cached: " + lootSets.size());

		} catch (SQLException e) {
			Logger.error(e.getErrorCode() + ' ' + e.getMessage(), e);
		} finally {
			closeCallable();
		}
		SpecialLoot.LootMap = lootSets;
	}
}
