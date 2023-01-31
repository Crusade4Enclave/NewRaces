// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.jobs.DeferredPowerJob;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class DeferredPowerPowerAction extends AbstractPowerAction {

	private String effectID;
	private String deferedPowerID;
	private EffectsBase effect;
	//	private EffectsBase deferedPower;

	public DeferredPowerPowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);

		this.effectID = rs.getString("effectID");
		this.deferedPowerID = rs.getString("deferredPowerID");
		this.effect = effects.get(this.effectID);
	}

	public String getEffectID() {
		return this.effectID;
	}

	public String getDeferredPowerID() {
		return this.deferedPowerID;
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

		String stackType = ab.getStackType();
		DeferredPowerJob eff = new DeferredPowerJob(source, awo, stackType, trains, ab, pb, this.effect, this);
		if (stackType.equals("IgnoreStack"))
			awo.addEffect(Integer.toString(ab.getUUID()), 10000, eff, this.effect, trains);
		else
			awo.addEffect(stackType, 10000, eff, this.effect, trains);

		switch (awo.getObjectType()){
		case PlayerCharacter:
			((PlayerCharacter)awo).setWeaponPower(eff);
			break;
		case Mob:
			((Mob)awo).weaponPower = eff;
			break;
		default:
			break;
		}


		this.effect.startEffect(source, awo, trains, eff);
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
