// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers;

import engine.objects.PreparedStatementShared;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class PowerPrereq {

	private String effect;
	private String message;
	private boolean mainHand;
	private boolean required;

	/**
	 * No Table ID Constructor
	 */
	public PowerPrereq() {

	}

	/**
	 * ResultSet Constructor
	 */
	public PowerPrereq(ResultSet rs, int type) throws SQLException {

		//		this.IDString = rs.getString("IDString");
		if (type == 1) {
			this.effect = rs.getString("messageone");
			this.message = rs.getString("messagetwo");
			this.mainHand = false;
			this.required = false;
		} else if (type == 2) {
			String sl = rs.getString("messageone");
			if (sl.equals("RHELD"))
				this.mainHand = true;
			else if (sl.equals("LHELD"))
				this.mainHand = false;
			this.effect = "";
			this.message = rs.getString("messagetwo");
			this.required = (rs.getInt("required") == 1) ? true : false;
		} else { //targetEffectPrereq
			this.effect = rs.getString("messageone");
			this.message = "";
			this.mainHand = false;
			this.required = (rs.getInt("required") == 1) ? true : false;
		}
	}

	public static void getAllPowerPrereqs(HashMap<String, PowersBase> powers) {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_powercastprereq");
			ResultSet rs = ps.executeQuery();
			int type; String IDString; PowerPrereq toAdd; PowersBase pb;
			while (rs.next()) {
				IDString = rs.getString("IDString");
				pb = powers.get(IDString);
				if (pb != null) {
					type = rs.getInt("Type");
					toAdd = new PowerPrereq(rs, type);
					if (type == 1)
						pb.getEffectPrereqs().add(toAdd);
					else if (type == 2)
						pb.getEquipPrereqs().add(toAdd);
					else
						pb.getTargetEffectPrereqs().add(toAdd);
				}
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
	}

	public String getEffect() {
		return this.effect;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean mainHand() {
		return this.mainHand;
	}

	public boolean isRequired() {
		return this.required;
	}
}
