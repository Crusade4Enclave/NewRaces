// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.gameManager.PowersManager;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.powers.PowersBase;

import java.util.ArrayList;

/**
 *
 * @author Eighty
 *
 */
public class ApplyStatModCmd extends AbstractDevCmd {

	public ApplyStatModCmd() {
        super("applystatmod");
    }

	private static int cnt = 0;

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		if(args.length < 1) {
			//		if(args.length < 2) {
			this.sendUsage(pcSender);
			return;
		}

		if(!(target instanceof AbstractCharacter)) {
			this.sendHelp(pcSender);
			return;
		}

        this.setTarget(pcSender); //for logging

		int spellID;
		int powerAction = 0;
		if (args[0].toLowerCase().contains("all")){

			int amount = 0;
			if (args.length == 1) {
				amount = ApplyStatModCmd.cnt;
				ApplyStatModCmd.cnt++;
			} else {
				amount = Integer.valueOf(args[1]);
				ApplyStatModCmd.cnt = amount+1;
			}


			ArrayList<PowersBase> pbList = new ArrayList<>();
			pbList.add(PowersManager.getPowerByToken(429047968));
			pbList.add(PowersManager.getPowerByToken(429768864));
			pbList.add(PowersManager.getPowerByToken(428458144));
			pbList.add(PowersManager.getPowerByToken(428677994));
			pbList.add(PowersManager.getPowerByToken(431874079));
			pbList.add(PowersManager.getPowerByToken(431081336));





			for (PowersBase pb:pbList){
				if(amount <= 0) {
					if (pb.getToken() ==428677994)
						powerAction = 1;
					PowersManager.removeEffect(pcSender, pb.getActions().get(powerAction), false, false);
					continue;
				} else if(amount > 9999 || amount < 21) {
					ChatManager.chatSystemInfo(pcSender, "Amount must be between 21 and 9999 inclusive.");
					return;
				}

				if (pb.getToken() ==428677994){
					PowersManager.removeEffect(pcSender, pb.getActions().get(powerAction), false, false);
					PowersManager.runPowerAction(pcSender, pcSender, Vector3fImmutable.ZERO, pb.getActions().get(powerAction), amount - 20, pb);
				}
				if (pb.getToken() ==428677994)
					powerAction = 1;
				PowersManager.removeEffect(pcSender, pb.getActions().get(powerAction), false, false);
				PowersManager.runPowerAction(pcSender, pcSender, Vector3fImmutable.ZERO, pb.getActions().get(powerAction), amount - 20, pb);
			}
			return;
		}
		if(args[0].toLowerCase().contains("con")) {
			spellID = 429047968;	//Blessing of Health
		} else if(args[0].toLowerCase().contains("str")) {
			spellID = 429768864;	//Blessing of Might
		} else if(args[0].toLowerCase().contains("dex")) {
			spellID = 428458144;	//Blessing of Dexterity
		} else if(args[0].toLowerCase().contains("int")) {
			spellID = 428677994;	//Bard Spi - TODO
			powerAction = 1;
		} else if(args[0].toLowerCase().contains("spi")) {
			spellID = 428677994;	//Bard Spi
		} else{
			ChatManager.chatSystemInfo(pcSender, "No valid stat found.");
			return;
		}

		PowersBase pb = PowersManager.getPowerByToken(spellID);

		int amount = 0;
		if (args.length == 1) {
			amount = ApplyStatModCmd.cnt;
			ApplyStatModCmd.cnt++;
		} else {
			amount = Integer.valueOf(args[1]);
			ApplyStatModCmd.cnt = amount+1;
		}
		//		int amount = Integer.valueOf(args[1]);
		if(amount <= 0) {
			PowersManager.removeEffect(pcSender, pb.getActions().get(powerAction), false, false);
			return;
		} else if(amount > 9999 || amount < 21) {
			ChatManager.chatSystemInfo(pcSender, "Amount must be between 21 and 9999 inclusive.");
			return;
		}

		PowersManager.removeEffect(pcSender, pb.getActions().get(powerAction), false, false);
		PowersManager.runPowerAction(pcSender, pcSender, Vector3fImmutable.ZERO, pb.getActions().get(powerAction), amount - 20, pb);
	}

	@Override
	protected String _getUsageString() {
        return "' /applystatmod <stat> [trains]'";
	}

	@Override
	protected String _getHelpString() {
        return "You must be targeting a player!";
	}

}
