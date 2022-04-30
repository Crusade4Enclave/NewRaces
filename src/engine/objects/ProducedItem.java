// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.DispatchChannel;
import engine.gameManager.PowersManager;
import engine.net.DispatchMessage;
import engine.net.client.msg.ItemProductionMsg;
import engine.powers.EffectsBase;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ProducedItem  {

	private int ID;
	private int npcUID;
	private int itemBaseID;
	private DateTime dateToUpgrade;
	private boolean isRandom;

	private String prefix;
	private String suffix;
	private String name;
	private int amount;
	private int producedItemID = 0;
	private boolean inForge;
	private int value;
	
	private int playerID;



	/**
	 * ResultSet Constructor
	 */

	public ProducedItem(ResultSet rs) throws SQLException {
		this.ID = rs.getInt("ID");
		this.npcUID = rs.getInt("npcUID");
        this.itemBaseID = rs.getInt("itemBaseID");

        Date sqlDateTime = rs.getTimestamp("dateToUpgrade");

		if (sqlDateTime != null)
			this.dateToUpgrade = new DateTime(sqlDateTime);
		else
			dateToUpgrade = null;
        this.isRandom = rs.getBoolean("isRandom");
        this.prefix = rs.getString("prefix");
        this.suffix = rs.getString("suffix");
        this.name = rs.getString("name");
		this.inForge = rs.getBoolean("inForge");
		this.value = rs.getInt("value");
		this.playerID = rs.getInt("playerID");
	}

	public ProducedItem(int ID,int npcUID, int itemBaseID, DateTime dateToUpgrade, boolean isRandom, String prefix, String suffix, String name, int playerID) {
		super();
		this.ID = ID;
		this.npcUID = npcUID;
        this.itemBaseID = itemBaseID;
        this.dateToUpgrade = dateToUpgrade;
		this.isRandom = isRandom;
		this.prefix = prefix;
		this.suffix = suffix;
		this.name = name;
		this.value = 0;
		this.playerID = playerID;



	}

	public int getNpcUID() {
		return npcUID;
	}
	public DateTime getDateToUpgrade() {
		return dateToUpgrade;
	}

	public int getItemBaseID() {
		return itemBaseID;
	}

	public void setItemBaseID(int itemBaseID) {
		this.itemBaseID = itemBaseID;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public void setRandom(boolean isRandom) {
		this.isRandom = isRandom;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}


	public int getProducedItemID() {
		return producedItemID;
	}

	public void setProducedItemID(int producedItemID) {
		this.producedItemID = producedItemID;
	}

	public boolean isInForge() {
		return inForge;
	}

	public void setInForge(boolean inForge) {
		this.inForge = inForge;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public boolean finishProduction(){
		NPC npc = NPC.getFromCache(this.getNpcUID());

		if (npc == null)
			return false;


		//update the client to ID the item in the window when item finishes rolling.
		//If there is more than 1 item left to roll, complete the item and throw it in inventory
		//and reproduce next item.
	
			try{

				if(this.getAmount() > 1){
					this.setAmount(this.getAmount() - 1);
					npc.completeItem(this.getProducedItemID());

					int pToken = 0;
					int sToken = 0;

					if (!this.isRandom()){
						EffectsBase eb = PowersManager.getEffectByIDString(this.getPrefix());
						if (eb != null)
							pToken = eb.getToken();
						eb = PowersManager.getEffectByIDString(this.getSuffix());
						if (eb != null)
							sToken = eb.getToken();

					}

					Item item = npc.produceItem(0,this.getAmount(),this.isRandom(),pToken,sToken,this.getName(),this.getItemBaseID());
					
					if (item == null)
						return false;

				}else{
					
					//update item to complete
					MobLoot targetItem = MobLoot.getFromCache(this.getProducedItemID());
					
					if (targetItem == null)
						return false;
					
					ItemProductionMsg outMsg = new ItemProductionMsg(npc.getBuilding(), npc, targetItem, 8, true);
					
	
		
					DispatchMessage.dispatchMsgToInterestArea(npc, outMsg, DispatchChannel.SECONDARY, 700, false, false);
				}
				
			}catch(Exception e){
				Logger.error(e);
				return false;
			}
			return true;
	}

	public int getPlayerID() {
		return playerID;
	}
	


}
