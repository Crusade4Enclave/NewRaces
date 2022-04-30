// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.Enum.GameObjectType;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.net.client.msg.PerformActionMsg;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

import java.util.HashSet;

public class PersistentAoeJob extends AbstractEffectJob {

	private final AbstractEffectJob aej;
	private int iteration = 0;
	private  Vector3fImmutable targetLoc;
	private Vector3fImmutable lastLoc;

	public PersistentAoeJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, AbstractEffectJob aej, Vector3fImmutable targetLoc) {
		super(source, target, stackType, trains, action, power, eb);
		this.aej = aej;
		if (target != null && this.target.getObjectType() == GameObjectType.PlayerCharacter)
			this.targetLoc = this.target.getLoc();
		else
			this.targetLoc = targetLoc;
		this.lastLoc = targetLoc;
	}

	@Override
	protected void doJob() {

		if (this.aej == null || this.source == null || this.action == null || this.power == null || this.source == null || this.eb == null)
			return;

		if (!this.source.isAlive())
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
		else if (this.iteration < this.power.getChantIterations()) {


			this.skipSendEffect = true;
			this.iteration++;


			if (this.target != null){
				this.lastLoc = this.target.getLoc();
				this.targetLoc = this.target.getLoc();
			}

			String stackType = action.getStackType();
			stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(action.getUUID()) : stackType;
			HashSet<AbstractWorldObject> awolist = null;


			if (this.source instanceof PlayerCharacter)
				awolist = PowersManager.getAllTargets(null, this.targetLoc, (PlayerCharacter) this.source, this.power);
			else
				awolist = new HashSet<>();
			PerformActionMsg msg = new PerformActionMsg(power.getToken(), 9999, source
					.getObjectType().ordinal(), source.getObjectUUID(), source.getObjectType().ordinal(),
					source.getObjectUUID(), 0, 0, 0, 2, 0);


			for (AbstractWorldObject awo : awolist) {

				//judge the defense of the target



				if (awo == null
						|| PowersManager.testAttack((PlayerCharacter) this.source, awo, this.power, msg))
					continue;

				PowersManager.finishApplyPowerA((AbstractCharacter) this.source, awo, this.targetLoc, this.power, this.trains, true);
				if (this.target != null  && !this.target.isAlive()){
					this.target = null;
				}

			}
			if (AbstractWorldObject.IsAbstractCharacter(source))
				((AbstractCharacter) this.source).addPersistantAoe(stackType, (int) (this.power.getChantDuration() * 1000), this, this.eb, this.trains);
		} else
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
	}

	@Override
	protected void _cancelJob() {
	}

	public int getIteration() {
		return this.iteration;
	}

	public int inc() {
		this.iteration++;
		return this.iteration;
	}
}
