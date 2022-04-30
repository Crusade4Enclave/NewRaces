package engine.devcmd.cmds;

import engine.Enum.DispatchChannel;
import engine.InterestManagement.InterestManager;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.net.DispatchMessage;
import engine.net.client.msg.ApplyRuneMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.PromotionClass;
import engine.server.MBServerStatics;

public class SetPromotionClassCmd extends AbstractDevCmd {

	public SetPromotionClassCmd() {
        super("setPromotionClass");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		int classID = 0;
		try {
			classID = Integer.parseInt(words[0]);
		} catch (Exception e) {
			throwbackError(pc,
					"Invalid setPromotionClass Command. must specify an ID between 2504 and 2526.");
			return;
		}
		if (classID < 2504 || classID > 2526 || classID == 2522) {
			throwbackError(pc,
					"Invalid setPromotionClass Command. must specify an ID between 2504 and 2526.");
			return;
		}
		pc.setPromotionClass(classID);
		ChatManager.chatSayInfo(pc,
				"PromotionClass changed to " + classID);
		InterestManager.reloadCharacter(pc);
		this.setTarget(pc); //for logging


		// recalculate all bonuses/formulas/skills/powers
		pc.recalculate();

		// send the rune application to the clients

		PromotionClass promo = pc.getPromotionClass();
		if (promo != null) {
			ApplyRuneMsg arm = new ApplyRuneMsg(pc.getObjectType().ordinal(), pc.getObjectUUID(), promo.getObjectUUID(), promo.getObjectType().ordinal(), promo.getObjectUUID(), true);
			DispatchMessage.dispatchMsgToInterestArea(pc, arm, DispatchChannel.SECONDARY, MBServerStatics.CHARACTER_LOAD_RANGE, true, false);
		}

	}

	@Override
	protected String _getHelpString() {
		return "Sets your character's PromotionClass to 'ID'";
	}

	@Override
	protected String _getUsageString() {
		return "' /setPromotionClass id'";
	}

}
