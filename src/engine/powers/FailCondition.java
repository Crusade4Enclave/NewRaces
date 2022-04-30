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
import java.util.ArrayList;


public class FailCondition {

	private String IDString;
	private Boolean forPower;
	private String type;
	private float amount;
	private float ramp;
	private boolean rampAdd;

	// private String damageType1;
	// private String damageType2;
	// private String damageType3;

	/**
	 * No Table ID Constructor
	 */
	public FailCondition() {

	}

	/**
	 * ResultSet Constructor
	 */
	public FailCondition(ResultSet rs) throws SQLException {

		this.IDString = rs.getString("IDString");
		this.forPower = (rs.getString("powerOrEffect").equals("Power")) ? true : false;
		this.type = rs.getString("type");
		this.amount = rs.getFloat("amount");
		this.ramp = rs.getFloat("ramp");
		this.rampAdd = (rs.getInt("useAddFormula") == 1) ? true : false;
		// this.damageType1 = rs.getString("damageType1");
		// this.damageType2 = rs.getString("damageType2");
		// this.damageType3 = rs.getString("damageType3");
	}

	public static ArrayList<FailCondition> getAllFailConditions() {
		PreparedStatementShared ps = null;
		ArrayList<FailCondition> out = new ArrayList<>();
		try {
			ps = new PreparedStatementShared("SELECT * FROM failconditions");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				FailCondition toAdd = new FailCondition(rs);
				out.add(toAdd);
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e);

		} finally {
			ps.release();
		}
		return out;
	}

	public String getIDString() {
		return this.IDString;
	}

	public String getType() {
		return this.type;
	}

	public boolean forPower() {
		return this.forPower;
	}

	public float getAmount() {
		return this.amount;
	}

	public float getRamp() {
		return this.ramp;
	}

	public float getAmountForTrains(float trains) {
		if (this.rampAdd)
			return this.amount + (this.ramp * trains);
		else
			return this.amount * (1 + (this.ramp * trains));
	}

	public boolean useRampAdd() {
		return this.rampAdd;
	}

	// public String getDamageType1() {
	// return this.damageType1;
	// }

	// public String getDamageType2() {
	// return this.damageType2;
	// }

	// public String getDamageType3() {
	// return this.damageType3;
	// }
}
