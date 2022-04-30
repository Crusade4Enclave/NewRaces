// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.jobs.FinishEffectTimeJob;
import engine.jobs.PersistentAoeJob;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class DirectDamagePowerAction extends AbstractPowerAction {

	private String effectID;
	private EffectsBase effect;

	public DirectDamagePowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);

		this.effectID = rs.getString("effectID");
		this.effect = effects.get(this.effectID);
	}

	public String getEffectID() {
		return this.effectID;
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
		FinishEffectTimeJob eff = new FinishEffectTimeJob(source, awo, stackType, trains, ab, pb, this.effect);
		eff.setSkipSendEffect(true);	
		if (duration > 0) {
			if (stackType.equals("IgnoreStack"))
				awo.addEffect(Integer.toString(ab.getUUID()), duration, eff, this.effect, trains);
			else
				awo.addEffect(stackType, duration, eff, this.effect, trains);
		}

		//		//if chant, start cycle
		//		if (pb.isChant() && targetLoc.x != 0f && targetLoc.z != 0f) {
		//			PersistentAoeJob paoe = new PersistentAoeJob(source, stackType, trains, ab, pb, effect, eff, targetLoc);
		//			source.addPersistantAoe(stackType, (int)(pb.getChantDuration() * 1000), paoe, effect, trains);
		//			eff.setChant(true);
		//		}

		this.effect.startEffect(source, awo, trains, eff);
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		String stackType = ab.getStackType();
		stackType = stackType.equals("IgnoreStack") ? Integer.toString(ab.getUUID()) : stackType;
		FinishEffectTimeJob eff = new FinishEffectTimeJob(source, target, stackType, trains, ab, pb, this.effect);
		if (targetLoc.x != 0f && targetLoc.z != 0f) {
			PersistentAoeJob paoe = new PersistentAoeJob(source,target, stackType, trains, ab, pb, effect, eff, targetLoc);
			source.addPersistantAoe(stackType, (int)(pb.getChantDuration() * 1000), paoe, effect, trains);
			eff.setChant(true);
		}
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int numTrains, ActionsBase ab, PowersBase pb, int duration) {
		// TODO Auto-generated method stub

	}
}
