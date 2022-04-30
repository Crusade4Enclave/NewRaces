// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.jobs;

import engine.job.AbstractScheduleJob;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;


public abstract class AbstractEffectJob extends AbstractScheduleJob {

	protected String stackType;
	protected AbstractWorldObject target;
	protected AbstractWorldObject source;
	protected int trains;
	protected ActionsBase action;
	protected PowersBase power;
	protected EffectsBase eb;
	protected boolean skipSendEffect=false;
	protected boolean skipApplyEffect=false;
	protected boolean isChant=false;
	protected boolean skipCancelEffect=false;
	private boolean noOverwrite;
	private int effectSourceType = 0;
	private int effectSourceID = 0;

	public AbstractEffectJob(AbstractWorldObject source, AbstractWorldObject target, String stackType, int trains, ActionsBase action, PowersBase power, EffectsBase eb) {
		super();
		this.source = source;
		this.target = target;
		this.stackType = stackType;
		this.trains = trains;
		this.action = action;
		this.power = power;
		this.eb = eb;
	}

	@Override
	protected abstract void doJob();
	@Override
	protected abstract void _cancelJob();

	public String getStackType() {
		return this.stackType;
	}

	public AbstractWorldObject getTarget() {
		return this.target;
	}

	public AbstractWorldObject getSource() {
		return this.target;
	}

	public int getTrains() {
		return this.trains;
	}

	public ActionsBase getAction() {
		return this.action;
	}

	public PowersBase getPower() {
		return this.power;
	}

	public int getPowerToken() {
		if (this.power == null)
			return 0;
		return this.power.getToken();
	}

	public EffectsBase getEffect() {
		return this.eb;
	}

	public boolean skipSendEffect() {
		return this.skipSendEffect;
	}

	public void setSkipSendEffect(boolean value) {
		this.skipSendEffect = value;
	}

	public boolean skipApplyEffect() {
		return this.skipApplyEffect;
	}

	public void setSkipApplyEffect(boolean value) {
		this.skipApplyEffect = value;
	}

	public boolean skipCancelEffect() {
		return this.skipCancelEffect;
	}

	public void setSkipCancelEffect(boolean value) {
		this.skipCancelEffect = value;
	}

	public boolean isChant() {
		return this.isChant;
	}

	public void setChant(boolean value) {
		this.isChant = value;
	}

	public void endEffect() {
		if (this.eb == null)
			return;
		this.eb.endEffect(this.source, this.target, this.trains, this.power, this);
	}
	
	public void endEffectNoPower() {
		if (this.eb == null)
			return;
		this.eb.endEffectNoPower(this.trains,this);
	}
	public boolean isNoOverwrite() {
		return noOverwrite;
	}

	public void setNoOverwrite(boolean noOverwrite) {
		this.noOverwrite = noOverwrite;
	}

	public int getEffectSourceType() {
		return effectSourceType;
	}

	public void setEffectSourceType(int effectSourceType) {
		this.effectSourceType = effectSourceType;
	}

	public int getEffectSourceID() {
		return effectSourceID;
	}

	public void setEffectSourceID(int effectSourceID) {
		this.effectSourceID = effectSourceID;
	}
}
