// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.jobs.TransferStatOTJob;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class TransferStatOTPowerAction extends TransferStatPowerAction {

	private int numIterations;

	public TransferStatOTPowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs, effects);

		this.numIterations = rs.getInt("numIterations");
	}

	public int getNumIterations() {
		return this.numIterations;
	}

	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3f targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		this.__startAction(source, awo, trains, ab, pb);
	}

	@Override
	protected void __startAction(AbstractCharacter source, AbstractWorldObject awo, int trains, ActionsBase ab, PowersBase pb) {
		if (this.effect == null || source == null || awo == null || ab == null || pb == null)
			return;

		//add schedule job to end it if needed and add effect to pc
		int duration = ab.getDuration(trains);
		String stackType = ab.getStackType();
		stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(ab.getUUID()) : stackType;
		TransferStatOTJob eff = new TransferStatOTJob(source, awo, stackType, trains, ab, pb, this.effect, this);
		int tick = eff.getTickLength();

		if (duration > 0)
			awo.addEffect(stackType, tick, eff, this.effect, trains);

		//start effect icon for client. Skip applying dot until first iteration.
		eff.setSkipApplyEffect(true);
		this.effect.startEffect(source, awo, trains, eff);
		eff.setSkipApplyEffect(false);
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
	}
}
