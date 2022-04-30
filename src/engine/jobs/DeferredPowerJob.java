// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.CombatManager;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.powers.poweractions.ApplyEffectPowerAction;
import engine.powers.poweractions.DeferredPowerPowerAction;

public class DeferredPowerJob extends AbstractEffectJob {

	private final DeferredPowerPowerAction def;

	public DeferredPowerJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, DeferredPowerPowerAction def) {
		super(source, target, stackType, trains, action, power, eb);
		this.def = def;
	}

	public DeferredPowerJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, ApplyEffectPowerAction def) {
		super(source, target, stackType, trains, action, power, eb);
		this.def = null;
	}

	@Override
	protected void doJob() {
		//Power ended with no attack, cancel weapon power boost
		if (this.source != null && this.source instanceof PlayerCharacter) {
			((PlayerCharacter) this.source).setWeaponPower(null);
		}
		PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
	}

	@Override
	protected void _cancelJob() {
		//Attack happened.
		PowersManager.cancelEffectTime(this.source, this.target, this.power, this.eb, this.action, this.trains, this);
	}

	public void attack(AbstractWorldObject tar, float attackRange) {

		if (this.source == null)
			return;

		if (!AbstractWorldObject.IsAbstractCharacter(tar))
			return;

		if (this.power == null)
			return;


		switch(this.source.getObjectType()){

		case PlayerCharacter:

			if (def == null) {
				//No powers applied, just reset weapon power.
				((PlayerCharacter) this.source).setWeaponPower(null);
				return;
			}
			float powerRange = this.power.getWeaponRange();

			// Wtf?  Method returns TRUE if rage test fails?  Seriously?

			//DO valid range check ONLY for weapon powers with range less than attack range.
			if (attackRange > powerRange)
				if (CombatManager.NotInRange((AbstractCharacter)this.source, tar, powerRange))
					return;

			//Range check passed, apply power and clear weapon power.
			((PlayerCharacter) this.source).setWeaponPower(null);


			//weapon powers with no deferedpoweraction have null Def, but still have bonuses applied already and will finish here.




			PowersManager.applyPower((AbstractCharacter) this.source, tar, Vector3fImmutable.ZERO, def.getDeferredPowerID(), this.trains, false);
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			break;
		case Mob:
			((Mob) this.source).setWeaponPower(null);
			if (def == null) {
				return;
			}

			PowersManager.applyPower((AbstractCharacter) this.source, tar, Vector3fImmutable.ZERO, def.getDeferredPowerID(), this.trains, false);
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			break;

		}

	}
}
