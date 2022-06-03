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
				apa = null;
				switch (type)
				{
					default:
						Logger.error("valid type not found for poweraction of ID" + rs.getInt("ID"));
						break;
					case "ApplyEffect":
						apa = new ApplyEffectPowerAction(rs, effects);
						break;
					case "ApplyEffects":
						apa = new ApplyEffectsPowerAction(rs, effects);
						break;
					case "DeferredPower":
						apa = new DeferredPowerPowerAction(rs, effects);
						break;
					case "DamageOverTime":
						apa = new DamageOverTimePowerAction(rs, effects);
						break;
					case "Peek":
						apa = new PeekPowerAction(rs);
						break;
					case "Charm":
						apa = new CharmPowerAction(rs);
						break;
					case "Fear":
						apa = new FearPowerAction(rs);
						break;
					case "Confusion":
						apa = new ConfusionPowerAction(rs);
						break;
					case "RemoveEffect":
						apa = new RemoveEffectPowerAction(rs);
						break;
					case "Track":
						apa = new TrackPowerAction(rs, effects);
						break;
					case "DirectDamage":
						apa = new DirectDamagePowerAction(rs, effects);
						break;
					case "Transform":
						apa = new TransformPowerAction(rs, effects);
						break;
					case "CreateMob":
						apa = new CreateMobPowerAction(rs);
						break;
					case "Invis":
						apa = new InvisPowerAction(rs, effects);
						break;
					case "ClearNearbyAggro":
						apa = new ClearNearbyAggroPowerAction(rs);
						break;
					case "MobRecall":
						apa = new MobRecallPowerAction(rs);
						break;
					case "SetItemFlag":
						apa = new SetItemFlagPowerAction(rs);
						break;
					case "SimpleDamage":
						apa = new SimpleDamagePowerAction(rs);
						break;
					case "TransferStatOT":
						apa = new TransferStatOTPowerAction(rs, effects);
						break;
					case "TransferStat":
						apa = new TransferStatPowerAction(rs, effects);
						break;
					case "Teleport":
						apa = new TeleportPowerAction(rs);
						break;
					case "TreeChoke":
						apa = new TreeChokePowerAction(rs);
						break;
					case "Block":
						apa = new BlockPowerAction(rs);
						break;
					case "Resurrect":
						apa = new ResurrectPowerAction(rs);
						break;
					case "ClearAggro":
						apa = new ClearAggroPowerAction(rs);
						break;
					case "ClaimMine":
						apa = new ClaimMinePowerAction(rs);
						break;
					case "Recall":
						apa = new RecallPowerAction(rs);
						break;
					case "SpireDisable":
						apa = new SpireDisablePowerAction(rs);
						break;
					case "Steal":
						apa = new StealPowerAction(rs);
						break;
					case "Summon":
						apa = new SummonPowerAction(rs);
						break;
					case "RunegateTeleport":
						apa = new RunegateTeleportPowerAction(rs);
						break;
					case "OpenGate":
						apa = new OpenGatePowerAction(rs);
						break;
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
