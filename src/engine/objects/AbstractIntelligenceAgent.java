// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM.STATE;
import engine.ai.StaticMobActions;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.msg.PetMsg;
import engine.server.MBServerStatics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public abstract class AbstractIntelligenceAgent extends AbstractCharacter {
	private boolean assist = false;
	private AbstractCharacter callForHelpAggro = null;
	private int type = 0; //Mob: 0, Pet: 1, Guard: 2
	public Vector3fImmutable lastBindLoc;
	private boolean clearAggro = false;


	public AbstractIntelligenceAgent(ResultSet rs) throws SQLException {
		super(rs);
	}

	public AbstractIntelligenceAgent(ResultSet rs, boolean isPlayer)
			throws SQLException {
		super(rs, isPlayer);
	}


	public AbstractIntelligenceAgent(ResultSet rs, 
			int UUID) throws SQLException {
		super(rs, UUID);
	}

	public AbstractIntelligenceAgent( String firstName,
			String lastName, short statStrCurrent, short statDexCurrent,
			short statConCurrent, short statIntCurrent, short statSpiCurrent,
			short level, int exp, boolean sit, boolean walk, boolean combat,
			Vector3fImmutable bindLoc, Vector3fImmutable currentLoc, Vector3fImmutable faceDir,
			short healthCurrent, short manaCurrent, short stamCurrent,
			Guild guild, byte runningTrains) {
		super(firstName, lastName, statStrCurrent, statDexCurrent, statConCurrent,
				statIntCurrent, statSpiCurrent, level, exp, bindLoc,
				currentLoc, faceDir, guild,
				runningTrains);
	}

	public AbstractIntelligenceAgent(String firstName,
			String lastName, short statStrCurrent, short statDexCurrent,
			short statConCurrent, short statIntCurrent, short statSpiCurrent,
			short level, int exp, boolean sit, boolean walk, boolean combat,
			Vector3fImmutable bindLoc, Vector3fImmutable currentLoc, Vector3fImmutable faceDir,
			short healthCurrent, short manaCurrent, short stamCurrent,
			Guild guild, byte runningTrains, int newUUID) {
		super(firstName, lastName, statStrCurrent, statDexCurrent, statConCurrent,
				statIntCurrent, statSpiCurrent, level, exp, bindLoc,
				currentLoc, faceDir, guild,
				runningTrains, newUUID);
	}

	@Override
	public void setObjectTypeMask(int mask) {
		mask |= MBServerStatics.MASK_IAGENT;
		super.setObjectTypeMask(mask);
	}

	/* AI Job Management */

	public MobBase getMobBase() {

		if (this.getObjectType().equals(GameObjectType.Mob))
			return this.getMobBase();
		return null;
	}

	public void setCallForHelpAggro(AbstractCharacter ac) {
		this.callForHelpAggro = ac;
	}

	public AbstractCharacter getCallForHelpAggro() {
		return callForHelpAggro;
	}

	public void setMob() {
		this.type = 0;
	}

	public void setPet(PlayerCharacter owner, boolean summoned) {
		if (summoned)
			this.type = 1; //summoned
		else
			this.type = 2; //charmed
		if (this.getObjectType().equals(GameObjectType.Mob)) {
			StaticMobActions.setOwner(((Mob)this),owner);
		}
	}

	public void setGuard() {
		this.type = 3;
	}

	public boolean isMob() {
		return (this.type == 0);
	}

	public boolean isPet() {
		return (this.type == 1 || this.type == 2);
	}

	public boolean isSummonedPet() {
		return (this.type == 1);
	}

	public boolean isCharmedPet() {
		return (this.type == 2);
	}

	public boolean isGuard() {
		return (this.type == 3);
	}

	public boolean assist() {
		return this.assist;
	}

	public void setAssist(boolean value) {
		this.assist = value;
	}

	public void toggleAssist() {
		this.assist = (this.assist) ? false : true;
	}

	public int getDBID() {

		if (this.getObjectType().equals(GameObjectType.Mob))
			return this.getDBID();
		return 0;
	}

	public boolean clearAggro() {
		return clearAggro;
	}

	public void setClearAggro(boolean value) {
		this.clearAggro = value;
	}

	public Vector3fImmutable getLastBindLoc() {
		if (this.lastBindLoc == null)
			this.lastBindLoc = this.getBindLoc();
		return this.lastBindLoc;
	}

	public PlayerCharacter getOwner() {

		if (this .getObjectType().equals(GameObjectType.Mob))
			return this.getOwner();
		return null;
	}

	public boolean getSafeZone() {
		ArrayList<Zone>allIn = ZoneManager.getAllZonesIn(this.getLoc());
		for (Zone zone : allIn) {
			if (zone.getSafeZone() == (byte)1)
				return true;
		}
		return false;
		//return this.safeZone;
	}

	public abstract AbstractWorldObject getFearedObject();

	public float getAggroRange() {
		float ret = MBServerStatics.AI_BASE_AGGRO_RANGE;
		if (this.bonuses != null)
			ret *= (1 +this.bonuses.getFloatPercentAll(ModType.ScanRange, SourceType.None));
		return ret;
	}

	public void dismiss() {

		if (this.isPet()) {

			if (this.isSummonedPet()) { //delete summoned pet

				WorldGrid.RemoveWorldObject(this);
				if (this.getObjectType() == GameObjectType.Mob){
					((Mob)this).state = STATE.Disabled;
					if (((Mob)this).parentZone != null)
						((Mob)this).parentZone.zoneMobSet.remove(this);
				}

			} else { //revert charmed pet
				this.setMob();
				this.setCombatTarget(null);
				//				if (this.isAlive())
				//					WorldServer.updateObject(this);
			}
			//clear owner
			PlayerCharacter owner = this.getOwner();

			//close pet window
			if (owner != null) {
				Mob pet = owner.getPet();
				PetMsg pm = new PetMsg(5, null);
				Dispatch dispatch = Dispatch.borrow(owner, pm);
				DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

				if (pet != null && pet.getObjectUUID() == this.getObjectUUID())
					owner.setPet(null);

				if (this.getObjectType().equals(GameObjectType.Mob))
					StaticMobActions.setOwner(((Mob)this),null);
			}


		}
	}
	
	
	
	
	
}

