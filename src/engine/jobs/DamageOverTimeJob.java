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
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.powers.poweractions.DamageOverTimePowerAction;


public class DamageOverTimeJob extends AbstractEffectJob {

	private final DamageOverTimePowerAction dot;
	private int iteration = 0;
	private int liveCounter = 0;

	public DamageOverTimeJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, DamageOverTimePowerAction dot) {
		super(source, target, stackType, trains, action, power, eb);

		this.dot = dot;
		if (this.target != null && AbstractWorldObject.IsAbstractCharacter(target))
			this.liveCounter = ((AbstractCharacter)target).getLiveCounter();
		
		this.iteration = action.getDurationInSeconds(trains) / this.dot.getNumIterations();
	}

	@Override
	protected void doJob() {
		if (this.target.getObjectType().equals(GameObjectType.Building)
				&& ((Building)this.target).isVulnerable() == false) {
			_cancelJob();
			return;
		}
		
		
		if (this.dot == null || this.target == null || this.action == null || this.source == null || this.eb == null)
			return;
		if (AbstractWorldObject.IsAbstractCharacter(target) && ((AbstractCharacter)this.target).getLiveCounter() != liveCounter){
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			return;
		}
			if (!this.target.isAlive()){
				PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
				return;
			}
			
		this.iteration--;
		
		if (this.iteration < 0){
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			return;
		}
			this.skipSendEffect = true;
			String stackType = action.getStackType();
			if (stackType.equals("IgnoreStack")) 
				this.target.addEffect(Integer.toString(action.getUUID()), getTickLength(), this, this.eb, this.trains);
			else
				this.target.addEffect(stackType, getTickLength(), this, this.eb, this.trains);
			if (AbstractWorldObject.IsAbstractCharacter(source))
				eb.startEffect((AbstractCharacter)this.source, this.target, this.trains, this);
	}

	@Override
	protected void _cancelJob() {
		PowersManager.cancelEffectTime(this.source, this.target, this.power, this.eb, this.action, this.trains, this);
	}

	public int getIteration() {
		return this.iteration;
	}

	public int getTickLength() {
		return this.dot.getNumIterations() * 1000;
	}

	public int inc() {
		this.iteration++;
		return this.iteration;
	}
}
