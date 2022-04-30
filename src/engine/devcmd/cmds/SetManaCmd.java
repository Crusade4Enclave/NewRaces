package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.net.DispatchMessage;
import engine.net.client.msg.TargetedActionMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;

public class SetManaCmd extends AbstractDevCmd {

	public SetManaCmd() {
        super("setMana");
        this.addCmdString("mana");
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
			pc.setMana(amount, pc);
			this.setTarget(pc); //for logging

			//Update all surrounding clients. - NOT for Mana?
			TargetedActionMsg cmm = new TargetedActionMsg(pc);
			DispatchMessage.dispatchMsgToInterestArea(pc, cmm, engine.Enum.DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);

		} catch (NumberFormatException e) {
			this.throwbackError(pc, "Supplied data: " + words[0]
					+ " failed to parse to a Float.");
		} catch (Exception e) {
			this.throwbackError(pc,
					"An unknown exception occurred while attempting to setMana to "
							+ words[0]);
		}
	}

	@Override
	protected String _getHelpString() {
		return "Sets your character's Mana to 'amount'";
	}

	@Override
	protected String _getUsageString() {
		return "' /setMana amount'";
	}

}
