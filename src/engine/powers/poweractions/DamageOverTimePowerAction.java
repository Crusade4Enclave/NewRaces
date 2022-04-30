// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.jobs.DamageOverTimeJob;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class DamageOverTimePowerAction extends AbstractPowerAction {

	private String effectID;
	private int numIterations;
	private EffectsBase effect;

	public DamageOverTimePowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);

		this.effectID = rs.getString("effectID");
		this.numIterations = rs.getInt("numIterations");
		this.effect = effects.get(this.effectID);
	}

	public String getEffectID() {
		return this.effectID;
	}

	public int getNumIterations() {
		return this.numIterations;
	}

	public EffectsBase getEffect() {
		return this.effect;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		if (this.effect == null || pb == null || ab == null) {
			//TODO log error here
			return;
		}
		//add schedule job to end it if needed and add effect to pc
		int duration = ab.getDuration(trains);
		String stackType = ab.getStackType();
		DamageOverTimeJob eff = new DamageOverTimeJob(source, awo, stackType, trains, ab, pb, this.effect, this);
		int tick = eff.getTickLength();
		if (duration > 0) {
			if (stackType.equals("IgnoreStack"))
				awo.addEffect(Integer.toString(ab.getUUID()), eff.getTickLength(), eff, this.effect, trains);
			else
				awo.addEffect(stackType, tick, eff, this.effect, trains);
		}

		//start effect icon for client. Skip applying dot until first iteration.
		eff.setSkipApplyEffect(true);
		this.effect.startEffect(source, awo, trains, eff);
		eff.setSkipApplyEffect(false);
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
