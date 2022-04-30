// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.poweractions;

import engine.Enum;
import engine.InterestManagement.WorldGrid;
import engine.ai.MobileFSM.STATE;
import engine.gameManager.DbManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.PetMsg;
import engine.objects.*;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;


public class CreateMobPowerAction extends AbstractPowerAction {

	private int mobID;
	private int mobLevel;

	public CreateMobPowerAction(ResultSet rs) throws SQLException {
		super(rs);

		this.mobID = rs.getInt("mobID");
		this.mobLevel = rs.getInt("mobLevel");
	}

	public int getMobID() {
		return this.mobID;
	}

	public int getMobLevel() {
		return this.mobLevel;
	}

	@Override
	protected void _startAction(AbstractCharacter source, AbstractWorldObject awo, Vector3fImmutable targetLoc, int trains, ActionsBase ab, PowersBase pb) {

		if (source == null || !(source.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)))
			return;

		PlayerCharacter owner = (PlayerCharacter) source;
		Mob currentPet = owner.getPet();
		Zone seaFloor = ZoneManager.getSeaFloor();
		Guild guild = Guild.getErrantGuild();
		ClientConnection origin = owner.getClientConnection();

		if (seaFloor == null || guild == null || origin == null)
			return;

		MobBase mobbase = MobBase.getMobBase(mobID);

		if (mobbase == null) {
			Logger.error("Attempt to summon pet with null mobbase: " + mobID);
			return;
		}

		if (mobbase.isNecroPet() && owner.inSafeZone())
			return;

		//create Pet
		Mob pet = Mob.createPet(mobID, guild, seaFloor, owner, (short)mobLevel);


		if(pet.getMobBaseID() == 12021 || pet.getMobBaseID() == 12022) { //is a necro pet
			if(currentPet!= null && !currentPet.isNecroPet() && !currentPet.isSiege()) {
				DbManager.removeFromCache(currentPet);
				WorldGrid.RemoveWorldObject(currentPet);
				currentPet.setState(STATE.Disabled);
				currentPet.setCombatTarget(null);

				if (currentPet.getParentZone() != null)
					currentPet.getParentZone().zoneMobSet.remove(currentPet);

				currentPet.getPlayerAgroMap().clear();

				try {
					currentPet.clearEffects();
				}catch(Exception e){
					Logger.error(e.getMessage());
				}

				//currentPet.disableIntelligence();
			}else if (currentPet != null && currentPet.isSiege()){
				currentPet.setMob();
				currentPet.setOwner(null);
				currentPet.setCombatTarget(null);

				if (currentPet.isAlive())
					WorldGrid.updateObject(currentPet);
			}
			//remove 10th pet
			
		
			owner.spawnNecroPet(pet);

		}
		else { //is not a necro pet
			if(currentPet != null) {
				if(!currentPet.isNecroPet() && !currentPet.isSiege()) {
					DbManager.removeFromCache(currentPet);
					currentPet.setCombatTarget(null);
					currentPet.setState(STATE.Disabled);

					currentPet.setOwner(null);
					WorldGrid.RemoveWorldObject(currentPet);

					currentPet.getParentZone().zoneMobSet.remove(currentPet);
					currentPet.getPlayerAgroMap().clear();
					currentPet.clearEffects();
					//currentPet.disableIntelligence();
				}
				else {
					if (currentPet.isSiege()){
						currentPet.setMob();
						currentPet.setOwner(null);
						currentPet.setCombatTarget(null);

						if (currentPet.isAlive())
							WorldGrid.updateObject(currentPet);
					}
					
				}
				PlayerCharacter.auditNecroPets(owner);
				PlayerCharacter.resetNecroPets(owner);
			}
		}
		/*		if(owner.getPet() != null) {
			if(owner.getPet().getMobBaseID() != 12021 && owner.getPet().getMobBaseID() != 12022) {
				//if not a necro pet, remove pet
				WorldGrid.removeWorldObject(owner.getPet());
				owner.getPet().disableIntelligence();
				Mob.removePet(owner.getPet().getUUID());
				owner.setPet(null);
		}
		else {
			//if it is a necro pet, add it to the line and set as mob
			owner.getPet().setMob();
		}
	}*/

		//	if (mobID == 12021 || mobID == 12022) //Necro Pets
		//	pet.setPet(owner, true);
		owner.setPet(pet);
		PetMsg pm = new PetMsg(5, pet);
		Dispatch dispatch = Dispatch.borrow(owner, pm);
		DispatchMessage.dispatchMsgDispatch(dispatch, engine.Enum.DispatchChannel.SECONDARY);
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
