// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Item;
import engine.objects.PreparedStatementShared;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public abstract class AbstractPowerAction {

	protected PowersBase parent;
	protected int UUID;
	protected String IDString;
	protected String type;
	protected boolean isAggressive;
	protected long validItemFlags;

	/**
	 * No Table ID Constructor
	 */
	public AbstractPowerAction() {

	}

	/**
	 * ResultSet Constructor
	 */
	public AbstractPowerAction(ResultSet rs) throws SQLException {

		this.UUID = rs.getInt("ID");
		this.IDString = rs.getString("IDString");
		this.type = rs.getString("type");
		int flags = rs.getInt("flags");
		this.isAggressive = ((flags & 128) != 0) ? true : false;
	}

	public AbstractPowerAction( int uUID, String iDString, String type, boolean isAggressive,
			long validItemFlags) {
		super();
		UUID = uUID;
		IDString = iDString;
		this.type = type;
		this.isAggressive = false;
	}

	public static void getAllPowerActions(HashMap<String, AbstractPowerAction> powerActions, HashMap<Integer, AbstractPowerAction> powerActionsByID, HashMap<String, EffectsBase> effects) {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_poweraction");
			ResultSet rs = ps.executeQuery();
			String IDString, type;
			while (rs.next()) {
				AbstractPowerAction apa;
				type = rs.getString("type");
				IDString = rs.getString("IDString");
				int token = DbManager.hasher.SBStringHash(IDString);
				//cache token, used for applying effects.
				PowersManager.ActionTokenByIDString.put(IDString, token);
				if (type.equals("ApplyEffect"))
					apa = new ApplyEffectPowerAction(rs, effects);
				else if (type.equals("ApplyEffects"))
					apa = new ApplyEffectsPowerAction(rs, effects);
				else if (type.equals("DeferredPower"))
					apa = new DeferredPowerPowerAction(rs, effects);
				else if (type.equals("DamageOverTime"))
					apa = new DamageOverTimePowerAction(rs, effects);
				else if (type.equals("Peek"))
					apa = new PeekPowerAction(rs);
				else if (type.equals("Charm"))
					apa = new CharmPowerAction(rs);
				else if (type.equals("Fear"))
					apa = new FearPowerAction(rs);
				else if (type.equals("Confusion"))
					apa = new ConfusionPowerAction(rs);
				else if (type.equals("RemoveEffect"))
					apa = new RemoveEffectPowerAction(rs);
				else if (type.equals("Track"))
					apa = new TrackPowerAction(rs, effects);
				else if (type.equals("DirectDamage"))
					apa = new DirectDamagePowerAction(rs, effects);
				else if (type.equals("Transform"))
					apa = new TransformPowerAction(rs, effects);
				else if (type.equals("CreateMob"))
					apa = new CreateMobPowerAction(rs);
				else if (type.equals("Invis"))
					apa = new InvisPowerAction(rs, effects);
				else if (type.equals("ClearNearbyAggro"))
					apa = new ClearNearbyAggroPowerAction(rs);
				else if (type.equals("MobRecall"))
					apa = new MobRecallPowerAction(rs);
				else if (type.equals("SetItemFlag"))
					apa = new SetItemFlagPowerAction(rs);
				else if (type.equals("SimpleDamage"))
					apa = new SimpleDamagePowerAction(rs);
				else if (type.equals("TransferStatOT"))
					apa = new TransferStatOTPowerAction(rs, effects);
				else if (type.equals("TransferStat"))
					apa = new TransferStatPowerAction(rs, effects);
				else if (type.equals("Teleport"))
					apa = new TeleportPowerAction(rs);
				else if (type.equals("TreeChoke"))
					apa = new TreeChokePowerAction(rs);
				else if (type.equals("Block"))
					apa = new BlockPowerAction(rs);
				else if (type.equals("Resurrect"))
					apa = new ResurrectPowerAction(rs);
				else if (type.equals("ClearAggro"))
					apa = new ClearAggroPowerAction(rs);
				else if (type.equals("ClaimMine"))
					apa = new ClaimMinePowerAction(rs);
				else if (type.equals("Recall"))
					apa = new RecallPowerAction(rs);
				else if (type.equals("SpireDisable"))
					apa = new SpireDisablePowerAction(rs);
				else if (type.equals("Steal"))
					apa = new StealPowerAction(rs);
				else if (type.equals("Summon"))
					apa = new SummonPowerAction(rs);
				else if (type.equals("RunegateTeleport"))
					apa = new RunegateTeleportPowerAction(rs);
				else if (type.equals("RunegateTeleport"))
					apa = new RunegateTeleportPowerAction(rs);
				else if (type.equals("OpenGate"))
					apa = new OpenGatePowerAction(rs);
				else {
					Logger.error("valid type not found for poweraction of ID" + rs.getInt("ID"));
					continue;
				}
				powerActions.put(IDString, apa);
				powerActionsByID.put(apa.UUID, apa);
				apa.validItemFlags = 0;
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
		
		
		
		//Add OpenGatePowerAction
		AbstractPowerAction openGateAction = new OpenGatePowerAction(5000, "OPENGATE", "OpenGate", false, 0);
		powerActions.put("OPENGATE", openGateAction);
		powerActionsByID.put(openGateAction.UUID, openGateAction);
	}

	public void startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int numTrains, ActionsBase ab, PowersBase pb) {
		this._startAction(source, awo, targetLoc, numTrains, ab, pb);
	}

	public void startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		this._startAction(source, awo, targetLoc, numTrains, ab, pb,duration);
	}

	protected abstract void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int numTrains, ActionsBase ab, PowersBase pb);

	protected abstract void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int numTrains, ActionsBase ab, PowersBase pb, int duration);

	public void handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int numTrains, ActionsBase ab, PowersBase pb) {
		this._handleChant(source, target, targetLoc, numTrains, ab, pb);
	}

	protected abstract void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int numTrains, ActionsBase ab, PowersBase pb);

	public int getUUID() {
		return this.UUID;
	}

	public String getIDString() {
		return this.IDString;
	}

	//	public String getMessageType() {
	//		return this.type;
	//	}

	public boolean isAggressive() {
		return this.isAggressive;
	}

	public PowersBase getParent() {
		return this.parent;
	}

	public void setParent(PowersBase value) {
		this.parent = value;
	}

	public void applyEffectForItem(Item item, int trains) {
		if (this instanceof ApplyEffectPowerAction)
			((ApplyEffectPowerAction)this)._applyEffectForItem(item, trains);
		if (this instanceof ApplyEffectsPowerAction)
			((ApplyEffectsPowerAction)this)._applyEffectForItem(item, trains);
	}
	public void applyBakedInStatsForItem(Item item, int trains) {
		if (this instanceof ApplyEffectPowerAction)
			((ApplyEffectPowerAction)this)._applyBakedInStatsForItem(item, trains);
		if (this instanceof ApplyEffectsPowerAction)
			((ApplyEffectsPowerAction)this)._applyBakedInStatsForItem(item, trains);
	}

	public EffectsBase getEffectsBase() {
		if (this instanceof ApplyEffectPowerAction)
			return ((ApplyEffectPowerAction)this).getEffect();
		else if (this instanceof ApplyEffectsPowerAction)
			return ((ApplyEffectsPowerAction)this).getEffect();
		return null;
	}

	public EffectsBase getEffectsBase2() {
		if (this instanceof ApplyEffectsPowerAction)
			return ((ApplyEffectsPowerAction)this).getEffect2();
		return null;
	}

	public static void loadValidItemFlags(HashMap<String, AbstractPowerAction> powerActions) {
		PreparedStatementShared ps = null;
		try {
			ps = new PreparedStatementShared("SELECT * FROM `static_power_effect_allowed_item`");
			ResultSet rs = ps.executeQuery();
			String IDS;
			long flags;
			while (rs.next()) {
				AbstractPowerAction apa;
				flags = rs.getLong("flags");
				IDS = rs.getString("IDString");
				if (powerActions.containsKey(IDS)) {
					apa = powerActions.get(IDS);
					apa.validItemFlags = flags;
				} else {
					Logger.error("Unable to find PowerAction " + IDS);
					continue;
				}
			}
			rs.close();
		} catch (Exception e) {
			Logger.error(e.toString());
		} finally {
			ps.release();
		}

	}

	//These functions verify a powerAction is valid for an item type
	public long getValidItemFlags() {
		return this.validItemFlags;
	}

	public String getType() {
		return type;
	}

}
