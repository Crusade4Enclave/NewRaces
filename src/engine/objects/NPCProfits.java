// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.ProfitType;
import engine.gameManager.DbManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class NPCProfits  {


	public int	npcUID;
	public float	buyNormal;
	public float	buyGuild;
	public float	buyNation;
	public float	sellNormal;
	public float	sellGuild;
	public float	sellNation;
	
	public static NPCProfits defaultProfits = new NPCProfits(0,.33f,.33f,.33f,1,1,1);
	
 

	
	public static HashMap <Integer,NPCProfits> ProfitCache = new HashMap<>();

	/**
	 * ResultSet Constructor
	 */

	public NPCProfits(ResultSet rs) throws SQLException {
	
		npcUID = rs.getInt("npcUID");
		buyNormal = rs.getFloat("buy_normal");
		buyGuild = rs.getFloat("buy_guild");
		buyNation = rs.getFloat("buy_nation");
		sellNormal = rs.getFloat("sell_normal");
		sellGuild = rs.getFloat("sell_guild");
		sellNation = rs.getFloat("sell_nation");

	}

	public NPCProfits(int npcUID, float buyNormal, float buyGuild, float buyNation, float sellNormal,
			float sellGuild, float sellNation) {
		super();
		this.npcUID = npcUID;
		this.buyNormal = buyNormal;
		this.buyGuild = buyGuild;
		this.buyNation = buyNation;
		this.sellNormal = sellNormal;
		this.sellGuild = sellGuild;
		this.sellNation = sellNation;
	}
	
	public static boolean UpdateProfits(NPC npc, NPCProfits profit, ProfitType profitType, float value){
		
		try {
			if (!DbManager.NPCQueries.UPDATE_PROFITS(npc, profitType, value))
				return false;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		switch (profitType){
		case BuyNormal:
			profit.buyNormal = value;
			break;
		case BuyGuild:
			profit.buyGuild = value;
			break;
		case BuyNation:
			profit.buyNation = value;
			break;
		case SellNormal:
			profit.sellNormal = value;
			break;
		case SellGuild:
			profit.sellGuild = value;
			break;
		case SellNation:
			profit.sellNation = value;
			break;
			
		}
		return true;
	}


	public static boolean CreateProfits(NPC npc){
		DbManager.NPCQueries.CREATE_PROFITS(npc);
		NPCProfits profits = new NPCProfits(npc.getObjectUUID(),.33f,.33f,.33f,1,1,1);
		NPCProfits.ProfitCache.put(npc.getObjectUUID(), profits);
	return true;
	}
	
}
