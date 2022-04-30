// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum.DispatchChannel;
import engine.devcmd.AbstractDevCmd;
import engine.net.DispatchMessage;
import engine.net.client.msg.TargetedActionMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;


public class SetStaminaCmd extends AbstractDevCmd {

	public SetStaminaCmd() {
        super("setStamina");
        this.addCmdString("stamina");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		float amount = 0.0f;
		try {
			amount = Float.parseFloat(words[0]);
			pc.setStamina(amount, pc);
			this.setTarget(pc); //for logging

			// Update all surrounding clients.
			TargetedActionMsg cmm = new TargetedActionMsg(pc);
			DispatchMessage.dispatchMsgToInterestArea(pc, cmm, DispatchChannel.PRIMARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);

		} catch (NumberFormatException e) {
			this.throwbackError(pc, "Supplied data: " + words[0]
					+ " failed to parse to a Float.");
		} catch (Exception e) {
			this.throwbackError(pc,
					"An unknown exception occurred while attempting to setStamina to "
							+ words[0]);
		}
	}

	@Override
	protected String _getHelpString() {
		return "Sets your character's Stamina to 'amount'";
	}

	@Override
	protected String _getUsageString() {
		return "' /setStamina amount'";
	}

}
