// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.objects.*;

public class SetRankCmd extends AbstractDevCmd {

	public SetRankCmd() {
		super("setRank");
		this.addCmdString("setrank");
		this.addCmdString("rank");
	}

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words,
			AbstractGameObject target) {

		int targetRank;
		int uuid = 0;

		if (words.length == 2) {
			try {
				uuid = Integer.parseInt(words[0]);
				targetRank = Integer.parseInt(words[1]);
			} catch (NumberFormatException e) {
				this.sendUsage(player);
				return; // NaN
			}
		} else if (words.length == 1) {
			try {
				targetRank = Integer.parseInt(words[0]);
			} catch (NumberFormatException e) {
				this.sendUsage(player);
				return; // NaN
			}
		} else {
			this.sendUsage(player);
			return;
		}

		if (target != null){
			switch(target.getObjectType()){
			case Building:
				Building targetBuilding = (Building)target;
				Blueprint blueprint = targetBuilding.getBlueprint();

				if (blueprint == null) {
					targetBuilding.setRank(targetRank);
					ChatManager.chatSayInfo(player, "Building ranked without blueprint" + targetBuilding.getObjectUUID());
					return;
				}

				if (targetRank > blueprint.getMaxRank()) {
					throwbackError(player, "Attempt to set Invalid Rank" + targetBuilding.getObjectUUID());
					return;
				}

				// Set the current targetRank
				int lastMeshID = targetBuilding.getMeshUUID();
				targetBuilding.setRank(targetRank);

				ChatManager.chatSayInfo(player, "Rank set for building with ID " + targetBuilding.getObjectUUID() + " to rank " + targetRank);
				break;
			case NPC:
				NPC toRank = (NPC)target;
				toRank.setRank(targetRank * 10);
				toRank.setUpgradeDateTime(null);
				WorldGrid.updateObject(toRank);
				break;
			case Mob:
				Mob toRankCaptain = (Mob)target;
				if (toRankCaptain.getContract() != null){
					toRankCaptain.setRank(targetRank * 10);
					Mob.setUpgradeDateTime(toRankCaptain, null);
					WorldGrid.updateObject(toRankCaptain);
				}

				break;
			}

		}else{
			Building targetBuilding = null;
			if (uuid != 0)
				targetBuilding = BuildingManager.getBuilding(uuid);

			if (targetBuilding == null) {
				throwbackError(player, "Unable to find building.");
				return;
			}

			Blueprint blueprint = targetBuilding.getBlueprint();

			if (blueprint == null) {
				throwbackError(player, "Attempt to rank building without blueprint" + targetBuilding.getObjectUUID());
				return;
			}

			if (targetRank > blueprint.getMaxRank()) {
				throwbackError(player, "Attempt to set Invalid Rank" + targetBuilding.getObjectUUID());
				return;
			}

			// Set the current targetRank
			int lastMeshID = targetBuilding.getMeshUUID();
			targetBuilding.setRank(targetRank);

			if (lastMeshID != targetBuilding.getMeshUUID())
				targetBuilding.refresh(true);
			else
				targetBuilding.refresh(false);

			ChatManager.chatSayInfo(player, "Rank set for building with ID " + targetBuilding.getObjectUUID() + " to rank " + targetRank);
		}



	}

	@Override
	protected String _getHelpString() {
		return "Sets the Rank of either the targets object or the object specified by ID.";
	}

	@Override
	protected String _getUsageString() {
		return "' /setrank ID rank' || ' /setrank rank' || ' /rank ID rank' || ' /rank rank'";
	}

}
