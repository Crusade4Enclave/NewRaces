// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.jobs.TrackJob;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.powers.ActionsBase;
import engine.powers.EffectsBase;
import engine.powers.PowersBase;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


public class TrackPowerAction extends AbstractPowerAction {

	private String effectID;
	private boolean trackPlayer;
	private boolean trackCorpse;
	private boolean trackAll;
	private boolean trackDragon;
	private boolean trackGiant;
	private boolean trackNPC;
	private boolean trackUndead;
	private boolean trackVampire;
	private int maxTrack;
	private EffectsBase effect;

	public TrackPowerAction(ResultSet rs, HashMap<String, EffectsBase> effects) throws SQLException {
		super(rs);

		this.effectID = rs.getString("effectID");
		int flags = rs.getInt("flags");
		this.trackPlayer = ((flags & 1024) == 1) ? true : false;
		this.trackCorpse = ((flags & 2048) == 1) ? true : false;
		String trackFilter = rs.getString("trackFilter");
		this.trackAll = trackFilter.equals("All") ? true : false;
		this.trackDragon = trackFilter.equals("Dragon") ? true : false;
		this.trackGiant = trackFilter.equals("Giant") ? true : false;
		this.trackNPC = trackFilter.equals("NPC") ? true : false;
		this.trackUndead = trackFilter.equals("Undead") ? true : false;
		this.trackVampire = trackFilter.equals("Vampire") ? true : false;

		this.maxTrack = rs.getInt("maxTrack");
		this.effect = effects.get(this.effectID);
	}

	public String getEffectID() {
		return this.effectID;
	}

	public boolean trackPlayer() {
		return this.trackPlayer;
	}

	public boolean trackCorpse() {
		return this.trackCorpse;
	}

	public boolean trackAll() {
		return this.trackAll;
	}

	public boolean trackDragon() {
		return this.trackDragon;
	}

	public boolean trackGiant() {
		return this.trackGiant;
	}

	public boolean trackNPC() {
		return this.trackNPC;
	}

	public boolean trackUndead() {
		return this.trackUndead;
	}

	public boolean trackVampire() {
		return this.trackVampire;
	}

	public int getMaxTrack() {
		return this.maxTrack;
	}

	public EffectsBase getEffect() {
		return this.effect;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {
		if (source == null || awo == null || this.effect == null || pb == null || ab == null) {
			//TODO log error here
			return;
		}

		//add schedule job to end it if needed and add effect to pc
		int duration = MBServerStatics.TRACK_ARROW_SENSITIVITY;
		String stackType = ab.getStackType();
		TrackJob eff = new TrackJob(source, awo, stackType, trains, ab, pb, this.effect, this);
		source.addEffect(stackType, duration, eff, this.effect, trains);
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
