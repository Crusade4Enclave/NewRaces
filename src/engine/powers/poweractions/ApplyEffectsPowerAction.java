// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Effect;
import engine.objects.Item;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class ApplyEffectsPowerAction extends AbstractPowerAction {
	private String IDString;
	private String effectID;
	private String effectID2;
	private EffectsBase effect;
	private EffectsBase effect2;
	private EffectsBase effectParent;

	public ApplyEffectsPowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);
		this.IDString = rs.getString("IDString");
		this.effectID = rs.getString("effectID");
		this.effectID2 = rs.getString("effectID2");
		this.effect = effects.get(this.effectID);
		this.effect2 = effects.get(this.effectID2);
		this.effectParent = effects.get(this.IDString);
	
	}

	public String getEffectID() {
		return this.effectID;
	}

	public String getEffectID2() {
		return this.effectID2;
	}

	public EffectsBase getEffect() {
		return this.effect;
	}

	public EffectsBase getEffect2() {
		return this.effect2;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

	}

	protected void _applyEffectForItem(Item item, int trains) {
		if (item == null || this.effect == null)
			return;
		item.addEffectNoTimer(Integer.toString(this.effect.getUUID()), this.effect, trains,false);
		if (this.effect2 != null)
		item.addEffectNoTimer(Integer.toString(this.effect2.getUUID()), this.effect2, trains,false);
		item.addEffectNoTimer(Integer.toString(this.effectParent.getUUID()), this.effectParent, trains,false);
	}
	protected void _applyBakedInStatsForItem(Item item, int trains) {

		if (item == null)
			return;

		if (this.effect == null){
			Logger.error( "Unknown Token: EffectBase ID " + this.effectID + '.');
			return;
		}

		if (this.effect2 == null){
			Logger.error( "Unknown Token: EffectBase ID " + this.effectID2 + '.');
			return;
		}

		if (this.effectParent == null){
			Logger.error( "Unknown Token: EffectBase ID " + this.IDString + '.');
			return;
		}

		Effect eff = item.addEffectNoTimer(Integer.toString(this.effect.getUUID()), this.effect, trains,false);
		Effect eff2 = item.addEffectNoTimer(Integer.toString(this.effect2.getUUID()), this.effect2, trains,false);
		Effect eff3 = item.addEffectNoTimer(Integer.toString(this.effectParent.getUUID()), this.effectParent, trains,false);

		if (eff != null && eff2 != null && eff3 != null){
			eff3.setBakedInStat(true);
			item.getEffectNames().add(this.effect.getIDString());
			item.getEffectNames().add(this.effect2.getIDString());
			item.getEffectNames().add(this.effectParent.getIDString());
		}
	}
	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	public String getIDString() {
		return IDString;
	}

	public void setIDString(String iDString) {
		IDString = iDString;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
