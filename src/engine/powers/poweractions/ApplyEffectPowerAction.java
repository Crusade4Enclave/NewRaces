// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.ai.MobileFSM;
import engine.gameManager.ChatManager;
import engine.jobs.ChantJob;
import engine.jobs.DeferredPowerJob;
import engine.jobs.FinishEffectTimeJob;
import engine.math.Vector3fImmutable;
import engine.net.DispatchMessage;
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ApplyEffectPowerAction extends AbstractPowerAction {

	private String effectID;
	private EffectsBase effect;
	private String effectParentID;
	private EffectsBase effectParent;

	public ApplyEffectPowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);
		this.effectParentID = rs.getString("IDString");
		this.effectParent = effects.get(this.effectParentID);
		this.effectID = rs.getString("effectID");
		this.effect = effects.get(this.effectID);
	}

	public ApplyEffectPowerAction(ResultSet rs, EffectsBase effect) throws SQLException {
		super(rs);

		this.effectID = rs.getString("effectID");
		this.effect = effect;
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
		int duration = 0;
		//		if (pb.isChant())
		//			duration = (int)pb.getChantDuration() * 1000;
		//		else
		duration = ab.getDuration(trains);
		String stackType = ab.getStackType();
		if (stackType.equals("WeaponMove")) {
			DeferredPowerJob eff = new DeferredPowerJob(source, awo, stackType, trains, ab, pb, this.effect, this);
			if (stackType.equals("IgnoreStack"))
				awo.addEffect(Integer.toString(ab.getUUID()), 10000, eff, this.effect, trains);
			else
				awo.addEffect(stackType, 10000, eff, this.effect, trains);
			if (awo.getObjectType().equals(GameObjectType.PlayerCharacter))
				((PlayerCharacter)awo).setWeaponPower(eff);
			this.effect.startEffect(source, awo, trains, eff);
		} else {
			FinishEffectTimeJob eff = new FinishEffectTimeJob(source, awo, stackType, trains, ab, pb, effect);
			
			if (blockInvul(pb, source, awo)) {
				this.effect.endEffect(source, awo, trains, pb, eff);
				return;
			}
			
//			 Effect lastEff = awo.getEffects().get(eff.getStackType());
//				
//				if (lastEff != null && lastEff.getPowerToken() == eff.getPowerToken())
//					lastEff.cancelJob(true);

			if (duration > 0) {
				if (stackType.equals("IgnoreStack"))
					awo.addEffect(Integer.toString(ab.getUUID()), duration, eff, effect, trains);
				else
					awo.addEffect(stackType, duration, eff, effect, trains);
			} else
				awo.applyAllBonuses();
			//			//TODO if chant, start cycle
			//			if (pb.isChant() && source.equals(awo)) {
			//				ChantJob cj = new ChantJob(source, awo, stackType, trains, ab, pb, effect, eff);
			//				source.setLastChant((int)(pb.getChantDuration()-2) * 1000, cj);
			//				eff.setChant(true);
			//			}

			if (this.effectID.equals("TAUNT")){

				if (awo != null && awo.getObjectType() == GameObjectType.Mob){
					MobileFSM.setAggro((Mob)awo,source.getObjectUUID());
					ChatSystemMsg msg = ChatManager.CombatInfo(source, awo);
					DispatchMessage.sendToAllInRange(source, msg);
				}
			}
			this.effect.startEffect(source, awo, trains, eff);
		}
	}

	protected void _applyEffectForItem(Item item, int trains) {
		if (item == null || this.effect == null)
			return;
		item.addEffectNoTimer(Integer.toString(this.effect.getUUID()), this.effect, trains,false);
		item.addEffectNoTimer(Integer.toString(this.effectParent.getUUID()), this.effectParent, trains,false);
	}
	protected void _applyBakedInStatsForItem(Item item, int trains) {
		if (item == null)
			return;
		if (this.effect == null){
			Logger.error( "Unknown Token: EffectBase ID " + this.effectID + '.');
			return;
		}

		if (this.effectParent == null){
			Logger.error("Unknown Token: EffectBase ID " + this.effectParentID + '.');
			return;
		}
		Effect eff = item.addEffectNoTimer(Integer.toString(this.effect.getUUID()), this.effect, trains,false);
		Effect eff3 = item.addEffectNoTimer(Integer.toString(this.effectParent.getUUID()), this.effectParent, trains,false);
		if (eff != null  && eff3 != null){
			eff3.setBakedInStat(true);
			item.getEffectNames().add(this.effect.getIDString());
			item.getEffectNames().add(this.effectParent.getIDString());
		}
	}

	@Override
	protected void _handleChant(AbstractCharacter source, AbstractWorldObject target, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		if (source != null) {
			PlayerBonuses bonuses = source.getBonuses();
			
			if (bonuses == null)
				return;
			
			boolean noSilence = bonuses.getBool(ModType.Silenced, SourceType.None);
			
			if (noSilence)
				return;

		}
		String stackType = ab.stackType;
		stackType = stackType.equals("IgnoreStack") ? Integer.toString(ab.getUUID()) : stackType;

		FinishEffectTimeJob eff = new FinishEffectTimeJob(source, target, stackType, trains, ab, pb, effect);
		ChantJob cj = new ChantJob(source, target, stackType, trains, ab, pb, effect, eff);
		//handle invul
		if(pb.getUUID() != 334)
			source.setLastChant((int)(pb.getChantDuration()) * 1000, cj);
		else
			source.setLastChant((int)(pb.getChantDuration()) * 1000, cj);
		eff.setChant(true);
	}

	private static boolean blockInvul(PowersBase pb, AbstractCharacter source, AbstractWorldObject awo) {
		if (awo == null || pb == null || source == null)
			return false;

		if (source.getObjectUUID() == awo.getObjectUUID())
			return false;

		if (!AbstractWorldObject.IsAbstractCharacter(awo))
			return false;

		AbstractCharacter ac = (AbstractCharacter) awo;


		return false;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc,
			int trains, ActionsBase ab, PowersBase pb, int duration) {
		if (this.effect == null || pb == null || ab == null) {
			//TODO log error here
			return;
		}

		//add schedule job to end it if needed and add effect to pc
		//		if (pb.isChant())
		//			duration = (int)pb.getChantDuration() * 1000;
		//		else

		String stackType = ab.getStackType();
		if (stackType.equals("WeaponMove")) {
			DeferredPowerJob eff = new DeferredPowerJob(source, awo, stackType, trains, ab, pb, this.effect, this);
			if (stackType.equals("IgnoreStack"))
				awo.addEffect(Integer.toString(ab.getUUID()), 10000, eff, this.effect, trains);
			else
				awo.addEffect(stackType, 10000, eff, this.effect, trains);
			if (awo.getObjectType().equals(GameObjectType.PlayerCharacter))
				((PlayerCharacter)awo).setWeaponPower(eff);
			this.effect.startEffect(source, awo, trains, eff);
		} else {
			FinishEffectTimeJob eff = new FinishEffectTimeJob(source, awo, stackType, trains, ab, pb, effect);

			if (blockInvul(pb, source, awo)) {
				this.effect.endEffect(source, awo, trains, pb, eff);
				return;
			}

			if (duration > 0) {
				if (stackType.equals("IgnoreStack"))
					awo.addEffect(Integer.toString(ab.getUUID()), duration, eff, effect, trains);
				else
					awo.addEffect(stackType, duration, eff, effect, trains);
			} else
				awo.applyAllBonuses();
			//			//TODO if chant, start cycle
			//			if (pb.isChant() && source.equals(awo)) {
			//				ChantJob cj = new ChantJob(source, awo, stackType, trains, ab, pb, effect, eff);
			//				source.setLastChant((int)(pb.getChantDuration()-2) * 1000, cj);
			//				eff.setChant(true);
			//			}

			if (this.effectID.equals("TAUNT")){

				if (awo != null && awo.getObjectType() == GameObjectType.Mob){
					MobileFSM.setAggro((Mob)awo,source.getObjectUUID());
					ChatSystemMsg msg = ChatManager.CombatInfo(source, awo);
					DispatchMessage.sendToAllInRange(source, msg);
				}
			}
			this.effect.startEffect(source, awo, trains, eff);
		}

	}

}
