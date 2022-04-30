// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;

/**
 * @author Steve
 *
 */
public class SetAICmd extends AbstractDevCmd {

	public SetAICmd() {
        super("setAI");
        this.addCmdString("ai");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (words.length < 2){
			this.sendUsage(pc);
			return;
		}
			
		int amount;

		try{
			amount = Integer.valueOf(words[1]);
		}catch (NumberFormatException e) {
			this.throwbackError(pc, "Failed to parse amount");
			return;
		}
		
		switch(words[0]){
		case "angle" :
			float angle = Float.parseFloat(words[1]);
			
			MBServerStatics.AI_MAX_ANGLE = angle;
			break;
			case "aggrorange":
				MBServerStatics.AI_BASE_AGGRO_RANGE = amount;
				DbManager.MobBaseQueries.UPDATE_AI_DEFAULTS();
				this.throwbackInfo(pc, "Aggro Range is now set to " + amount);
				break;
			case "dropaggrorange":
				MBServerStatics.AI_DROP_AGGRO_RANGE = amount;
				DbManager.MobBaseQueries.UPDATE_AI_DEFAULTS();
				this.throwbackInfo(pc, "Drop Aggro Range is now set to " + amount);
				break;
			case "patroldivisor":
				MBServerStatics.AI_PATROL_DIVISOR = amount;
				DbManager.MobBaseQueries.UPDATE_AI_DEFAULTS();
				this.throwbackInfo(pc, "Patrol Chance is now set to " + amount);
				break;
			case "pulse":
				if (amount < 500){
					this.throwbackError(pc, "pulse amount must be greather than 500 to execute.");
					return;
				}
				MBServerStatics.AI_PULSE_MOB_THRESHOLD = amount;
				this.throwbackInfo(pc, "Pulse is now set to " + amount);
				break;
			case "sleepthread":
				if (amount < 500){
					this.throwbackError(pc, "sleep amount must be greather than 500 to execute.");
					return;
				}
				MBServerStatics.AI_THREAD_SLEEP = amount;
				this.throwbackInfo(pc, "Thread Sleep is now set to " + amount);
				break;
			case "recallrange":
				MBServerStatics.AI_RECALL_RANGE = amount;
				DbManager.MobBaseQueries.UPDATE_AI_DEFAULTS();
				this.throwbackInfo(pc, "Recall Range is now set to " + amount);
				break;
			case "powerdivisor":
				MBServerStatics.AI_POWER_DIVISOR = amount;
				DbManager.MobBaseQueries.UPDATE_AI_DEFAULTS();
				this.throwbackInfo(pc, "Power Divisor is now set to " + amount);
				break;
			case "losehate":
				MBServerStatics.PLAYER_HATE_DELIMITER = amount;
				break;
			case "hatemodcombat":
				MBServerStatics.PLAYER_COMBAT_HATE_MODIFIER = amount;
				default:
					this.throwbackError(pc, words[0] + " is not a valid AI Command.");
					break;
		}
	}

	@Override
	protected String _getHelpString() {
		String help = "Modifies Mob AI Statics. Commands:";
		help += "\n AGGRORANGE: Sets the range when a mob will aggro it's target. Aggro range is currently " + MBServerStatics.AI_BASE_AGGRO_RANGE;
		help += "\n DROPAGGRORANGE: Sets the range when a mob will drop aggro from it's target. Drop aggro range is currently " + MBServerStatics.AI_DROP_AGGRO_RANGE;
		help += "\n PATROLDIVISOR: Sets the Patrol Divisor for Mob AI. Setting this will give a 1/[amount] chance to parol the area. Patrol Chance is currently 1/" + MBServerStatics.AI_PATROL_DIVISOR;
		help += "\n PULSE: sets how often to run mob's AI. Measured in MS. Pulse is currently  " + MBServerStatics.AI_PULSE_MOB_THRESHOLD + "ms.";
		help += "\n SLEEPTHREAD: Sets how long to sleep the AI for ALL mobs.Thread sleep is currently " + MBServerStatics.AI_THREAD_SLEEP + "ms.";
		help += "\n RECALLRANGE: Sets the range of a mob to recall back to it's bind location. Recall range is currently " + MBServerStatics.AI_RECALL_RANGE;
		help += "\n POWERDIVISOR: Sets the Power Divisor for Mob AI.Setting this will give a 1/[amount] chance to use power on a player. Power Divisor is currently " + MBServerStatics.AI_POWER_DIVISOR;
		help += "\n LOSEHATE: Sets the amount per second to reduce hate amount for player while they are idle. Hate Delimiter is currently " + MBServerStatics.PLAYER_HATE_DELIMITER;
		help += "\n HATEMODCOMBAT: sets the modifier for Hate value for Combat. Hate Value is `Damage *[HATEMODCOMBAT]`.Hate Mod Combat is currently " + MBServerStatics.PLAYER_COMBAT_HATE_MODIFIER;

		return help;
	}

	@Override
	protected String _getUsageString() {
		String usage = "' /setai  `command` `amount` `";
		usage += '\n' + _getHelpString();
		return usage;
	}

}
