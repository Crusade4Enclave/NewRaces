// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.job.JobScheduler;
import engine.jobs.EndFearJob;
import engine.math.Vector3fImmutable;
import engine.net.client.ClientConnection;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Mob;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FearPowerAction extends AbstractPowerAction {

	private int levelCap;
	private int levelCapRamp;

	public FearPowerAction(ResultSet rs) throws SQLException {
		super(rs);
		this.levelCap = rs.getInt("levelCap");
		this.levelCapRamp = rs.getInt("levelCapRamp");
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		if (source == null || awo == null || !(awo.getObjectType().equals(Enum.GameObjectType.Mob)) || !(source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)))
			return;

		PlayerCharacter owner = (PlayerCharacter) source;
		ClientConnection origin = owner.getClientConnection();
		if (origin == null)
			return;

		//verify is mob, not pet or guard
		Mob mob = (Mob) awo;
		if (!mob.isMob())
			return;

		//make sure mob isn't too high level
		int cap = this.levelCap + (this.levelCapRamp * trains);
		if (mob.getLevel() > cap || mob.getLevel() > 79)
			return;

		//Apply fear to mob
		int duration = 10 + ((int)(trains * 0.5));
		String stackType = ab.getStackType();
		EndFearJob efj = new EndFearJob(source, awo, stackType, trains, ab, pb, null);
		((Mob)awo).fearedObject = source;
		JobScheduler.getInstance().scheduleJob(efj, duration * 1000);
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
