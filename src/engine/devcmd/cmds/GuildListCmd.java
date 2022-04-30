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


/**
 * @author 
 * Summary: Lists UID, Name and GL UID of either
 *          Player or NPC sovereign guilds
 */
 
public class GuildListCmd extends AbstractDevCmd {

    // Instance variables
    
    private int _guildType; // 0 = Player : 1 = NPC sovereign guilds
    private String outputStr = null;
    
	public GuildListCmd() {
        super("guildlist");
    }

        
        // AbstractDevCmd Overridden methods
        
	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target) {

            if(validateUserInput(args) == false) {
                this.sendUsage(pc);
		 return;
            }
            
            parseUserInput(args);
         
         // Execute stored procedure
            
            outputStr = DbManager.GuildQueries.GET_GUILD_LIST(_guildType);
            
         // Send results to user

            throwbackInfo(pc, outputStr);

        }

	@Override
	protected String _getHelpString() {
        return "Lists guild info for sovereign guilds";
	}

	@Override
	protected String _getUsageString() {
        return "/guildlist npc|player";
	}

        // Class methods
        
        private static boolean validateUserInput(String[] userInput) {

        int stringIndex;
        String commandSet = "npcplayer";
        
        // incorrect number of arguments test
        
        if (userInput.length != 1)
         return false;    

        // Test of game object type argument
        
        stringIndex = commandSet.indexOf(userInput[0].toLowerCase());

            return stringIndex != -1;
        }
        
        private void parseUserInput(String[] userInput) {
        
        // Build mask from user input
          
            switch (userInput[0].toLowerCase()) {
                case "npc":
                 _guildType = 1;
                    break;
                case "player":
                  _guildType = 0;
                    break;
                default:
                    break;
            }
            
        }
        
}
