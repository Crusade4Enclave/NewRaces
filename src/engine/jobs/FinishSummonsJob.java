// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.PowersManager;
import engine.job.AbstractScheduleJob;
import engine.job.JobContainer;
import engine.net.client.msg.ErrorPopupMsg;
import engine.objects.PlayerCharacter;

import java.util.concurrent.ConcurrentHashMap;

public class FinishSummonsJob extends AbstractScheduleJob {

	PlayerCharacter source;
	PlayerCharacter target;

	public FinishSummonsJob(PlayerCharacter source, PlayerCharacter target) {
		super();
		this.source = source;
		this.target = target;
	}

	@Override
	protected void doJob() {

		if (this.target == null)
			return;

		//clear summon timer

		ConcurrentHashMap<String, JobContainer> timers = this.target.getTimers();

		if (timers != null && timers.containsKey("Summon"))
			timers.remove("Summon");

		if (this.source == null || !this.source.isAlive() || !this.target.isAlive())
			return;

		// cannot summon a player in combat
		if (this.target.isCombat()) {

			ErrorPopupMsg.sendErrorMsg(this.source, "Cannot summon player in combat.");

			PowersManager.finishRecycleTime(428523680, this.source, false);
			return;
		}

		if (this.target.getBonuses() != null && this.target.getBonuses().getBool(ModType.BlockedPowerType, SourceType.SUMMON)){
			ErrorPopupMsg.sendErrorMsg(this.target, "You have been blocked from receiving summons!");
			ErrorPopupMsg.sendErrorMsg(this.source, "Target is blocked from receiving summons!");
			return;
		}

		if (this.source.getRegion() != null)
			this.target.setRegion(this.source.getRegion());
		//teleport target to source
		target.teleport(source.getLoc());
	}

	@Override
	protected void _cancelJob() {
	}

	public PlayerCharacter getSource() {
		return this.source;
	}

	public PlayerCharacter getTarget() {
		return this.target;
	}
}
