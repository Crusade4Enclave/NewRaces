// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers;

import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.Enum.StackType;
import engine.gameManager.PowersManager;
import engine.objects.*;
import engine.powers.poweractions.AbstractPowerAction;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class ActionsBase {

	public int UUID;
		public String IDString;
	public String effectID;
	public int minTrains;
	public int maxTrains;
	public float duration;
	public float ramp;
	public boolean addFormula;
	public String stackType;
	public StackType stackTypeType;
	public int stackOrder;

	public boolean greaterThanEqual = false;
	public boolean always = false;
	public boolean greaterThan = false;
	public String stackPriority;

	private AbstractPowerAction powerAction;

	/**
	 * No Table ID Constructor
	 */
	public ActionsBase() {

	}

	/**
	 * ResultSet Constructor
	 */
	public ActionsBase(ResultSet rs, HashMap<String, AbstractPowerAction> apa) throws SQLException {

		this.UUID = rs.getInt("ID");
				this.IDString = rs.getString("powerID");
		this.effectID = rs.getString("effectID");
		this.minTrains = rs.getInt("minTrains");
		this.maxTrains = rs.getInt("maxTrains");
		this.duration = rs.getFloat("duration");
		this.ramp = rs.getFloat("ramp");
		this.addFormula = (rs.getInt("useAddFormula") == 1) ? true : false;
		this.stackType = rs.getString("stackType");
		this.stackTypeType = StackType.GetStackType(this.stackType);
		this.stackOrder = rs.getInt("stackOrder");
		this.stackPriority = rs.getString("stackPriority");

		switch (stackPriority) {
		case "GreaterThanOrEqualTo":
			this.greaterThanEqual = true;
			break;
		case "Always":
			this.always = true;
			break;
		case "GreaterThan":
			this.greaterThan = true;
			break;
		}
		this.powerAction = apa.get(this.effectID);
	}

	protected ActionsBase(int uUID, String effectID, int minTrains, int maxTrains, float duration, float ramp,
			boolean addFormula, String stackType, int stackOrder, boolean greaterThanEqual, boolean always,
			boolean greaterThan, AbstractPowerAction powerAction) {
		super();
		UUID = uUID;
		this.effectID = effectID;
		this.minTrains = minTrains;
		this.maxTrains = maxTrains;
		this.duration = duration;
		this.ramp = ramp;
		this.addFormula = addFormula;
		this.stackType = stackType;
		this.stackTypeType = StackType.GetStackType(this.stackType);
		if (this.stackTypeType == null)
			Logger.info("Invalid Stack Type " + this.stackTypeType + " for " + this.effectID);
		this.stackOrder = stackOrder;
		this.greaterThanEqual = greaterThanEqual;
		this.always = always;
		this.greaterThan = greaterThan;
		this.powerAction = powerAction;
		
		if (this.greaterThanEqual)
			this.stackPriority = "GreaterThanOrEqualTo";
		else if(this.always)
			this.stackPriority = "Always";
		else if (this.greaterThan)
			this.stackPriority = "GreaterThan";
	
	}

	//	public static ArrayList<ActionsBase> getActionsBase(String ID) {
	//		PreparedStatementShared ps = null;
	//		ArrayList<ActionsBase> out = new ArrayList<ActionsBase>();
	//		try {
	//			ps = new PreparedStatementShared("SELECT * FROM actions where powerID = ?");
	//			ps.setString(1, ID);
	//			ResultSet rs = ps.executeQuery();
	//			while (rs.next()) {
	//				ActionsBase toAdd = new ActionsBase(rs);
	//				out.add(toAdd);
	//			}
	//			rs.close();
	//		} catch (Exception e) {
	//			Logger.error("ActionsBase", e);
	//		} finally {
	//			ps.release();
	//		}
	//		return out;
	//	}

	public static void getActionsBase(HashMap<String, PowersBase> powers, HashMap<String, AbstractPowerAction> apa) {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_action");
			ResultSet rs = ps.executeQuery();
			String IDString; ActionsBase toAdd; PowersBase pb;
			while (rs.next()) {
				IDString = rs.getString("powerID");
				pb = powers.get(IDString);
				if (pb != null) {
					toAdd = new ActionsBase(rs, apa);
					pb.getActions().add(toAdd);
				}
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
		
		int gateID = 5000;
		for (String IDString : Runegate.GetAllOpenGateIDStrings()){
			gateID++;
			ActionsBase openGateActionBase = new ActionsBase(gateID, "OPENGATE", 5, 9999, 0, 0, true, "IgnoreStack", 0, true, false, false, PowersManager.getPowerActionByIDString("OPENGATE"));
		
			PowersBase openGatePower = powers.get(IDString);
			
			if (openGatePower == null){
				Logger.error( "no powerbase for action " + IDString);
				break;
			}
			openGatePower.getActions().add(openGateActionBase);
		}
	}
	

	public int getUUID() {
		return this.UUID;
	}

	public String getEffectID() {
		return this.effectID;
	}

	public int getMinTrains() {
		return this.minTrains;
	}

	public int getMaxTrains() {
		return this.maxTrains;
	}

	public float getDuration() {
		return this.duration;
	}

	public AbstractPowerAction getPowerAction() {
		return this.powerAction;
	}

	public int getDuration(int trains) {
		if (this.addFormula)
			return (int)((this.duration + (this.ramp * trains)) * 1000);
		else
			return (int)((this.duration * (1 + (this.ramp * trains))) * 1000);
	}

	public float getDurationAsFloat(int trains) {
		if (this.addFormula)
			return ((this.duration + (this.ramp * trains)) * 1000);
		else
			return ((this.duration * (1 + (this.ramp * trains))) * 1000);
	}

	public int getDurationInSeconds(int trains) {
		if (this.addFormula)
			return (int)(this.duration + (this.ramp * trains));
		else
			return (int)(this.duration * (1 + (this.ramp * trains)));
	}

	public String getStackType() {
		return this.stackType;
	}

	public int getStackOrder() {
		return this.stackOrder;
	}

	public boolean greaterThanEqual() {
		return this.greaterThanEqual;
	}

	public boolean greaterThan() {
		return this.greaterThan;
	}

	public boolean always() {
		return this.always;
	}

	//Add blocked types here
	public boolean blocked(AbstractWorldObject awo, PowersBase pb, int trains) {
		if (AbstractWorldObject.IsAbstractCharacter(awo)) {
			AbstractCharacter ac = (AbstractCharacter) awo;
			PlayerBonuses bonus = ac.getBonuses();
			if (bonus == null)
				return false;
			
			//TODO make this more efficient then testing strings
			if (this.stackType.equals("Stun") && bonus.getBool(ModType.ImmuneTo, SourceType.Stun))
				return true; //Currently stun immune. Skip stun
			else if(this.stackType.equals("Snare") && bonus.getBool(ModType.ImmuneTo, SourceType.Snare))
				return true; //Currently snare immune. Skip snare
			else if(this.stackType.equals("Blindness") && bonus.getBool(ModType.ImmuneTo, SourceType.Blind))
				return true; //Currently blind immune. Skip blind
			else if(this.stackType.equals("PowerInhibitor") && bonus.getBool(ModType.ImmuneTo, SourceType.Powerblock))
				return true; //Currently power block immune. Skip power block
			else if (this.stackType.equals("Root") && bonus.getBool(ModType.ImmuneTo, SourceType.Root))
				return true;
			//			else if (pb.isHeal() && (bonus.getByte("immuneTo.Heal")) >= trains)
			//				return true; //Currently shadowmantled. Skip heals
			else if (this.stackType.equals("Flight") && bonus.getBool(ModType.NoMod, SourceType.Fly))
				return true;
			else if (this.stackType.equals("Track") && bonus.getBool(ModType.CannotTrack, SourceType.None))
				return true;
			else return pb.vampDrain() && bonus.getBool(ModType.BlockedPowerType, SourceType.VAMPDRAIN);
		}
		return false;
	}
}
