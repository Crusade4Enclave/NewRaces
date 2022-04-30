// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.objects;

import engine.Enum.DamageType;
import engine.Enum.EffectSourceType;
import engine.gameManager.PowersManager;
import engine.job.AbstractJob;
import engine.job.AbstractScheduleJob;
import engine.job.JobContainer;
import engine.jobs.AbstractEffectJob;
import engine.jobs.DamageOverTimeJob;
import engine.jobs.NoTimeJob;
import engine.jobs.PersistentAoeJob;
import engine.net.ByteBufferWriter;
import engine.net.client.ClientConnection;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.powers.effectmodifiers.AbstractEffectModifier;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;


public class Effect {

	private JobContainer jc;

	//Fail Conditions
	private boolean cancelOnAttack;
	private boolean cancelOnAttackSwing;
	private boolean cancelOnCast;
	private boolean cancelOnCastSpell;
	private boolean cancelOnEquipChange;
	private boolean cancelOnLogout;
	private boolean cancelOnMove;
	private boolean cancelOnNewCharm;
	private boolean cancelOnSit;
	private boolean cancelOnTakeDamage;
	private boolean cancelOnTerritoryClaim;
	private boolean cancelOnUnEquip;
	private boolean cancelOnStun;
	private boolean bakedInStat = false;
	private boolean isStatic = false;
	private int effectSourceType = 0;
	private int effectSourceID = 0;
	

	private EffectsBase eb;
	private int trains;
	private float damageAmount = 0f;

	private AtomicBoolean cancel = new AtomicBoolean(false);
	
	//private AbstractWorldObject owner;

	/**
	 * Generic Constructor
	 */
	public Effect(JobContainer jc, EffectsBase eb, int trains) {
		this.jc = jc;
		this.cancelOnAttack = false;
		this.cancelOnAttackSwing = false;
		this.cancelOnCast = false;
		this.cancelOnCastSpell = false;
		this.cancelOnEquipChange = false;
		this.cancelOnLogout = false;
		this.cancelOnMove = false;
		this.cancelOnNewCharm = false;
		this.cancelOnSit = false;
		this.cancelOnTakeDamage = false;
		this.cancelOnTerritoryClaim = false;
		this.cancelOnUnEquip = false;
		this.cancelOnStun = false;
		this.eb = eb;
		this.trains = trains;
	}
	
	public Effect(JobContainer jc, EffectsBase eb, int trains,boolean isStatic) {
		this.jc = jc;
		this.cancelOnAttack = false;
		this.cancelOnAttackSwing = false;
		this.cancelOnCast = false;
		this.cancelOnCastSpell = false;
		this.cancelOnEquipChange = false;
		this.cancelOnLogout = false;
		this.cancelOnMove = false;
		this.cancelOnNewCharm = false;
		this.cancelOnSit = false;
		this.cancelOnTakeDamage = false;
		this.cancelOnTerritoryClaim = false;
		this.cancelOnUnEquip = false;
		this.cancelOnStun = false;
		this.eb = eb;
		this.trains = trains;
		this.isStatic = isStatic;
	}

	//called when effect ends. Send message to client to remove effect
	public void endEffect() {
		if (this.jc != null) {
			AbstractJob aj = jc.getJob();
			if (aj == null)
				return;
			if (aj instanceof AbstractEffectJob) {
				((AbstractEffectJob)aj).setSkipCancelEffect(false);
				((AbstractEffectJob)aj).endEffect();
			}
		}
	}
	
	public void endEffectNoPower() {
		if (this.jc != null) {
			AbstractJob aj = jc.getJob();
			if (aj == null)
				return;
			if (aj instanceof AbstractEffectJob) {
				((AbstractEffectJob)aj).setSkipCancelEffect(false);
				((AbstractEffectJob)aj).endEffectNoPower();
			}
		}
	}

	//Called when effect ends before timer done
	public void cancelJob() {
		if (this.jc != null) {
			AbstractJob aj = jc.getJob();
			if (aj == null)
				return;
			if (aj instanceof AbstractEffectJob)
				((AbstractEffectJob)aj).setSkipCancelEffect(false);
			if (aj instanceof AbstractScheduleJob) {
				((AbstractScheduleJob)aj).cancelJob();
			}
		}
	}

	public void cancelJob(boolean skipEffect) {
		if (this.jc != null) {
			AbstractJob aj = jc.getJob();
			if (aj == null)
				return;
			if (skipEffect && aj instanceof AbstractEffectJob) {
				((AbstractEffectJob)aj).setSkipCancelEffect(skipEffect);
			}
			if (aj instanceof AbstractScheduleJob) {
				((AbstractScheduleJob)aj).cancelJob();
			}
		}
	}

	public boolean applyBonus(Item item) {
		if (this.jc == null)
			return false;
		AbstractJob aj = jc.getJob();
		if (aj == null)
			return false;
		if (aj instanceof AbstractEffectJob) {
			AbstractEffectJob aej = (AbstractEffectJob)aj;
			EffectsBase eb = aej.getEffect();
			if (eb == null)
				return false;
			HashSet<AbstractEffectModifier> aems = eb.getModifiers();
			for(AbstractEffectModifier aem : aems)
				aem.applyBonus(item, aej.getTrains());
			return true;
		}
		return false;
	}

	public boolean applyBonus(Building building) {
		if (this.jc == null)
			return false;
		AbstractJob aj = jc.getJob();
		if (aj == null)
			return false;
		if (aj instanceof AbstractEffectJob) {
			AbstractEffectJob aej = (AbstractEffectJob)aj;
			EffectsBase eb = aej.getEffect();
			if (eb == null)
				return false;
			HashSet<AbstractEffectModifier> aems = eb.getModifiers();
			for(AbstractEffectModifier aem : aems)
				aem.applyBonus(building, aej.getTrains());
			return true;
		}
		return false;
	}

	public boolean applyBonus(AbstractCharacter ac) {
		if (this.jc == null)
			return false;
		AbstractJob aj = jc.getJob();
		if (aj == null)
			return false;
		if (aj instanceof AbstractEffectJob) {
			AbstractEffectJob aej = (AbstractEffectJob)aj;
			EffectsBase eb = aej.getEffect();
			if (eb == null)
				return false;
			HashSet<AbstractEffectModifier> aems = eb.getModifiers();
			for(AbstractEffectModifier aem : aems)
				aem.applyBonus(ac, aej.getTrains());
			return true;
		}
		return false;
	}

	public boolean applyBonus(Item item, AbstractCharacter ac) {
		if (this.jc == null)
			return false;
		AbstractJob aj = jc.getJob();
		if (aj == null)
			return false;
		if (aj instanceof AbstractEffectJob) {
			AbstractEffectJob aej = (AbstractEffectJob)aj;
			EffectsBase eb = aej.getEffect();
			if (eb == null)
				return false;
			HashSet<AbstractEffectModifier> aems = eb.getModifiers();
			for(AbstractEffectModifier aem : aems) {
				aem.applyBonus(item, aej.getTrains());
				aem.applyBonus(ac, aej.getTrains());
			}
			return true;
		}
		return false;
	}

	public HashSet<AbstractEffectModifier> getEffectModifiers() {
		if (this.jc == null)
			return null;
		AbstractJob aj = jc.getJob();
		if (aj == null)
			return null;
		if (aj instanceof AbstractEffectJob) {
			AbstractEffectJob aej = (AbstractEffectJob)aj;
			EffectsBase eb = aej.getEffect();
			if (eb == null)
				return null;
			return eb.getModifiers();
		}
		return null;
	}


	//Send this effect to a client when loading a player
	public void sendEffect(ClientConnection cc) {
		if (this.jc == null || this.eb == null || cc == null)
			return;
		AbstractJob aj = this.jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob)))
			return;
		this.eb.sendEffect((AbstractEffectJob)aj, (this.jc.timeToExecutionLeft() / 1000), cc);
	}
	
	public void sendEffectNoPower(ClientConnection cc) {
		if (this.jc == null || this.eb == null || cc == null)
			return;
		AbstractJob aj = this.jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob)))
			return;
		this.eb.sendEffectNoPower((AbstractEffectJob)aj, (this.jc.timeToExecutionLeft() / 1000), cc);
	}
	
	public void sendSpireEffect(ClientConnection cc, boolean onEnter) {
		if (this.jc == null || this.eb == null || cc == null)
			return;
		AbstractJob aj = this.jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob)))
			return;
		int duration = 45;
		if (onEnter)
			duration = -1;
		this.eb.sendEffectNoPower((AbstractEffectJob)aj, duration, cc);
	}

	public void serializeForItem(ByteBufferWriter writer, Item item) {
		if (this.jc == null) {
			blankFill(writer);
			return;
		}
		AbstractJob aj = this.jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob))) {
			blankFill(writer);
			return;
		}
		AbstractEffectJob aej = (AbstractEffectJob)aj;
		PowersBase pb = aej.getPower();
		ActionsBase ab = aej.getAction();
		if (this.eb == null) {
			blankFill(writer);
			return;
		} else if (pb == null && !(this.jc.noTimer())) {
			blankFill(writer);
			return;
		}
		if (this.jc.noTimer()) {
			if (pb == null)
				writer.putInt(this.eb.getToken());
			else
				writer.putInt(pb.getToken());
			writer.putInt(aej.getTrains());
			writer.putInt(1);
			writer.put((byte)1);
			writer.putInt(item.getObjectType().ordinal());
			writer.putInt(item.getObjectUUID());
			
			writer.putString(item.getName());
			writer.putFloat(-1000f);
		} else {
			float duration = this.jc.timeToExecutionLeft() / 1000;
			writer.putInt(this.eb.getToken());
			writer.putInt(aej.getTrains());
			writer.putInt(0);
			writer.put((byte)0);
			writer.putInt(pb.getToken());
			writer.putString(pb.getName());
			writer.putFloat(duration);
		}
	}

	public void serializeForClientMsg(ByteBufferWriter writer) {
		AbstractJob aj = this.jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob))) {
			//TODO put error message here
			blankFill(writer);
			return;
		}
		AbstractEffectJob aej = (AbstractEffectJob)aj;
		PowersBase pb = aej.getPower();
		ActionsBase ab = aej.getAction();
		if (ab == null || pb == null || this.eb == null) {
			//TODO put error message here
			blankFill(writer);
			return;
		}
		
		if ( aej instanceof PersistentAoeJob){
			blankFill(writer);
			return;
		}

		float duration = this.jc.timeToExecutionLeft() / 1000;
		if (aej instanceof DamageOverTimeJob)
			duration = ab.getDurationInSeconds(aej.getTrains()) - (((DamageOverTimeJob)aej).getIteration()*5);
		
		

		writer.putInt(pb.getToken());
		writer.putInt(aej.getTrains());
		writer.putInt(0);
		writer.put((byte)0);
		writer.putInt(this.eb.getToken());
		writer.putString(pb.getName());
		writer.putFloat(duration);
	}
	
	public boolean serializeForLoad(ByteBufferWriter writer) {
		AbstractJob aj = this.jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob))) {
			return false;
		}
		
		
		AbstractEffectJob aej = (AbstractEffectJob)aj;
		PowersBase pb = aej.getPower();
		ActionsBase ab = aej.getAction();
		if (this.eb == null) {
			return false;
		}
		if ( aej instanceof PersistentAoeJob){
			return false;
		}
	

		float duration = this.jc.timeToExecutionLeft() / 1000;
		if (aej instanceof DamageOverTimeJob)
			if (ab != null)
			duration = ab.getDurationInSeconds(aej.getTrains()) - (((DamageOverTimeJob)aej).getIteration()*5);
		if (aej instanceof NoTimeJob)
			duration = -1;
		
		int sendToken = this.getEffectToken();
		
		if (aej.getAction() != null)
		if ( aej.getAction().getPowerAction() != null
				&& PowersManager.ActionTokenByIDString.containsKey(aej.getAction().getPowerAction().getIDString()))
			try{
				sendToken = PowersManager.ActionTokenByIDString.get(aej.getAction().getPowerAction().getIDString());
			}catch(Exception e){
				sendToken = this.getEffectToken();
			}
		

		writer.putInt(sendToken);
        writer.putInt(this.trains);
		writer.putInt(0); //?
		if (aej.getEffectSourceID() != 0){
			writer.put((byte) 1);
			writer.putInt(aej.getEffectSourceType());
			writer.putInt(aej.getEffectSourceID());
		}else{
			writer.put((byte)0);
			writer.putInt(pb != null ? pb.getToken() : 0);
		}
		writer.putString(pb != null ? pb.getName() : eb.getName());

		writer.putFloat(duration);
        return true;
	}

	private static void blankFill(ByteBufferWriter writer) {
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
		writer.put((byte)0);
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0);
	}

	public float getDuration() {
		float duration = 0f;
		if (this.jc != null)
			duration = this.jc.timeToExecutionLeft() / 1000;
		return duration;
	}

	public boolean containsSource(EffectSourceType source) {
		if (this.eb != null)
			return this.eb.containsSource(source);
		return false;
	}

	public JobContainer getJobContainer() {
		return this.jc;
	}

	public int getTrains() {
		return this.trains;
	}

	public void setTrains(int value) {
		this.trains = value;
	}

	public float getDamageAmount() {
		return this.damageAmount;
	}

	public AbstractJob getJob() {
		if (this.jc == null)
			return null;
		return jc.getJob();
	}

	public boolean bakedInStat() {
		return this.bakedInStat;
	}

	public void setBakedInStat(boolean value) {
		this.bakedInStat = value;
	}

	public PowersBase getPower() {
		if (this.jc == null)
			return null;
		AbstractJob aj = jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob)))
			return null;
		return ((AbstractEffectJob)aj).getPower();
	}

	public int getPowerToken() {
		if (this.jc == null)
			return 0;
		AbstractJob aj = jc.getJob();
		if (aj == null || (!(aj instanceof AbstractEffectJob)))
			return 0;
		PowersBase pb = ((AbstractEffectJob)aj).getPower();
		if (pb == null)
			return 0;
		return pb.getToken();
	}

	public int getEffectToken() {
		if (this.eb != null)
			return this.eb.getToken();
		return 0;
	}

	public EffectsBase getEffectsBase() {
		return this.eb;
	}

	public String getName() {
		if (this.jc == null)
			return "";
		AbstractJob aj = this.jc.getJob();
		if (aj == null || !(aj instanceof AbstractEffectJob))
			return "";
		AbstractEffectJob aej = (AbstractEffectJob)aj;
		PowersBase pb = aej.getPower();
		if (pb == null)
			return "";
		return pb.getName();
	}

	public boolean cancel() {
		return this.cancel.compareAndSet(false, true);
	}

	public boolean canceled() {
		return this.cancel.get();
	}

	public boolean cancelOnAttack() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnAttack();
	}

	public boolean cancelOnAttackSwing() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnAttackSwing();
	}

	public boolean cancelOnCast() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnCast();
	}

	public boolean cancelOnCastSpell() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnCastSpell();
	}

	public boolean cancelOnEquipChange() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnEquipChange();
	}

	public boolean cancelOnLogout() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnLogout();
	}

	public boolean cancelOnMove() {
		if (this.eb == null || this.cancelOnMove)
			return true;
		return this.eb.cancelOnMove();
	}

	public boolean cancelOnNewCharm() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnNewCharm();
	}

	public boolean cancelOnSit() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnSit();
	}

	public boolean cancelOnStun() {
		if (this.eb == null)
			return true;
		return this.cancelOnStun;
	}

	public boolean cancelOnTakeDamage() {
		if (this.eb == null)
			return true;
		if (this.eb.damageTypeSpecific()) {
			return false; //handled in call from resists
		} else {
			return this.eb.cancelOnTakeDamage();
		}
	}

	//Used for verifying when damage absorbers fails
	public boolean cancelOnTakeDamage(DamageType type, float amount) {
		if (!this.eb.cancelOnTakeDamage())
			return false;
		if (this.eb == null || amount < 0f)
			return false;
		if (this.eb.damageTypeSpecific()) {
			if (type == null)
				return false;
			if (this.eb.containsDamageType(type)) {
				this.damageAmount += amount;
                return this.damageAmount > this.eb.getDamageAmount(this.trains);
			} else
				return false;
		} else
			return false; //handled by call from AbstractCharacter
	}

	public boolean isDamageAbsorber() {
		if (this.eb == null)
			return false;
		if (!this.eb.cancelOnTakeDamage())
			return false;
        return this.eb.damageTypeSpecific();
    }

	public boolean cancelOnTerritoryClaim() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnTerritoryClaim();
	}

	public boolean cancelOnUnEquip() {
		if (this.eb == null)
			return true;
		return this.eb.cancelOnUnEquip();
	}

	public void setPAOE() {
		this.cancelOnStun = true;
		this.cancelOnMove = true;
	}

	public boolean isStatic() {
		return isStatic;
	}
	
	public void setIsStatic(boolean isStatic) {
		this.isStatic = isStatic;
		
	}

	public int getEffectSourceID() {
		return effectSourceID;
	}

	public void setEffectSourceID(int effectSourceID) {
		this.effectSourceID = effectSourceID;
	}

	public int getEffectSourceType() {
		return effectSourceType;
	}

	public void setEffectSourceType(int effectSourceType) {
		this.effectSourceType = effectSourceType;
	}


}