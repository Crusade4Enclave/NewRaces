// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.PowersManager;
import engine.gameManager.SessionManager;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerBonuses;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;

import java.util.HashSet;


public class ChantJob extends AbstractEffectJob {

	private final AbstractEffectJob aej;
	private int iteration = 0;

	public ChantJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb, AbstractEffectJob aej) {
		super(source, target, stackType, trains, action, power, eb);
		this.aej = aej;
	}

	@Override
	protected void doJob() {
		if (this.aej == null || this.source == null || this.target == null || this.action == null || this.power == null || this.source == null || this.eb == null)
			return;
		PlayerBonuses bonuses = null;
		
		//if player isnt in game, do not run chant.
		if (this.source.getObjectType().equals(GameObjectType.PlayerCharacter)){
			if (SessionManager.getPlayerCharacterByID(this.source.getObjectUUID()) == null)
				return;
		}
		if (AbstractWorldObject.IsAbstractCharacter(source))
			bonuses = ((AbstractCharacter)source).getBonuses();
		if (!this.source.isAlive()) {
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			if (AbstractWorldObject.IsAbstractCharacter(source))
				((AbstractCharacter)source).cancelLastChant();
		} else if (bonuses != null && bonuses.getBool(ModType.Silenced, SourceType.None)) {
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			if (AbstractWorldObject.IsAbstractCharacter(source))
				((AbstractCharacter)source).cancelLastChant();
		}
		else if (AbstractWorldObject.IsAbstractCharacter(source) &&  ((AbstractCharacter)source).isSit()){
			return;
		}else if (this.iteration < this.power.getChantIterations() && AbstractWorldObject.IsAbstractCharacter(source)) {
			this.skipSendEffect = true;
			this.iteration++;

			// *** Refactor holy wtf batman

			String stackType = action.getStackType();
			stackType = (stackType.equals("IgnoreStack")) ? Integer.toString(action.getUUID()) : stackType;

			HashSet<AbstractWorldObject> awolist = null;
			if (this.source instanceof PlayerCharacter)
				awolist = PowersManager.getAllTargets(this.source, this.source.getLoc(), (PlayerCharacter)this.source, this.power);
			else
				awolist = new HashSet<>();
			for (AbstractWorldObject awo : awolist) {

				if (awo == null)
					continue;

				PowersManager.finishApplyPowerA((AbstractCharacter) this.source, awo, awo.getLoc(), this.power, this.trains, true);

			}

			if (AbstractWorldObject.IsAbstractCharacter(source))
				//handle invul
				if(power.getUUID() != 334)
					((AbstractCharacter)this.source).setLastChant((int)(this.power.getChantDuration()) * 1000, this);
				else
					((AbstractCharacter)this.source).setLastChant((int)(this.power.getChantDuration()) * 1000, this);
		} else {
			PowersManager.finishEffectTime(this.source, this.target, this.action, this.trains);
			if (AbstractWorldObject.IsAbstractCharacter(source)) {
				((AbstractCharacter)source).cancelLastChant();
			}
		}
	}

	@Override
	protected void _cancelJob() {
	}

}
