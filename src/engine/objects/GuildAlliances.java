// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum.AllianceType;
import engine.gameManager.DbManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildAlliances  {

	private int sourceGuild;
	private int allianceGuild;
	private boolean isRecommended;
	private boolean isAlly;
	private String recommender;

	/**
	 * ResultSet Constructor
	 */

	public GuildAlliances(ResultSet rs) throws SQLException {
		this.sourceGuild = rs.getInt("GuildID");
		this.allianceGuild = rs.getInt("OtherGuildID");
		this.isRecommended = rs.getBoolean("isRecommended");
		this.isAlly = rs.getBoolean("isAlliance");
		this.recommender =rs.getString("recommender");
	}

	public GuildAlliances(int sourceGuild, int allianceGuild, boolean isRecommended, boolean isAlly,
			String recommender) {
		super();
		this.sourceGuild = sourceGuild;
		this.allianceGuild = allianceGuild;
		this.isRecommended = isRecommended;
		this.isAlly = isAlly;
		this.recommender = recommender;
	}

	public int getSourceGuild() {
		return sourceGuild;
	}

	public int getAllianceGuild() {
		return allianceGuild;
	}

	public boolean isRecommended() {
		return isRecommended;
	}

	public boolean isAlly() {
		return isAlly;
	}

	public String getRecommender() {
		return recommender;
	}

	public synchronized boolean UpdateAlliance(final AllianceType allianceType, boolean updateRecommended){
		switch (allianceType){
		case Ally:
			if (updateRecommended){
				if (!DbManager.GuildQueries.UPDATE_ALLIANCE_AND_RECOMMENDED(this.sourceGuild, this.allianceGuild, true))
					return false;
				this.isAlly = true;
				this.isRecommended = false;
			}else{
				if (!DbManager.GuildQueries.UPDATE_ALLIANCE(this.sourceGuild, this.allianceGuild, true))
					return false;
				this.isAlly = true;
				this.isRecommended = false;
			}
			break;
		case Enemy:

			if (updateRecommended){
				if (!DbManager.GuildQueries.UPDATE_ALLIANCE_AND_RECOMMENDED(this.sourceGuild, this.allianceGuild, false))
					return false;
				this.isAlly = false;
				this.isRecommended = false;
			}else{
				if (!DbManager.GuildQueries.UPDATE_ALLIANCE(this.sourceGuild, this.allianceGuild, false))
					return false;
				this.isAlly = false;
				this.isRecommended = false;
			}
			break;

		}
		return true;
	}

}
