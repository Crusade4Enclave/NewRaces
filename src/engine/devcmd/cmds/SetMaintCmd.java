// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.gameManager.MaintenanceManager;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

import java.time.LocalDateTime;

public class SetMaintCmd extends AbstractDevCmd {

	public SetMaintCmd() {
        super("setMaint");
        this.addCmdString("setmaint");
    }

	@Override
	protected void _doCmd(PlayerCharacter player, String[] words,
			AbstractGameObject target) {

		if (!target.getObjectType().equals(Enum.GameObjectType.Building)) {
			ChatManager.chatSayInfo(player, "Target is not a valid building");
			return;
		}

		Building targetBuilding = (Building)target;

		if (targetBuilding.getProtectionState().equals(Enum.ProtectionState.NPC)) {
			ChatManager.chatSayInfo(player, "Target is not a valid building");
			return;
		}

		MaintenanceManager.setMaintDateTime(targetBuilding, LocalDateTime.now().minusDays(1).withHour(13).withMinute(0).withSecond(0).withNano(0));
		ChatManager.chatSayInfo(player, "Maint will run for UUID: " + targetBuilding.getObjectUUID());
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
