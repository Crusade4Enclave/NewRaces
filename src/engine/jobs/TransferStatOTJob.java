// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.gameManager.PowersManager;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.powers.poweractions.TransferStatOTPowerAction;

public class TransferStatOTJob extends AbstractEffectJob {

	private final TransferStatOTPowerAction dot;
	private int iteration = 0;

	public TransferStatOTJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, TransferStatOTPowerAction dot) {
		super(source, target, stackType, trains, action, power, eb);
		this.dot = dot;
		this.iteration = action.getDurationInSeconds(trains) / this.dot.getNumIterations();
	}

	@Override
	protected void doJob() {
		if (this.dot == null || this.target == null || this.action == null || this.source == null || this.eb == null || this.action == null || this.power == null)
			return;
		if (!this.target.isAlive()){
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			return;
		}
			
		iteration--;
		
		if (iteration < 0){
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			return;
		}
		 
			this.skipSendEffect = true;
			
			String stackType = action.getStackType();
			stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(action.getUUID()) : stackType;
			this.target.addEffect(stackType, getTickLength(), this, this.eb, this.trains);
			if (AbstractWorldObject.IsAbstractCharacter(source))
				this.dot.runAction((AbstractCharacter)this.source, this.target, this.trains, this.action, this.power);
			
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
