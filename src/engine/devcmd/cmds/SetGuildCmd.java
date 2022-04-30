package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.Guild;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

/**
 *
 * @author 
 * Dev command to set the guild of targeted npc.
 * Argument is a valid guild UID
 */
public class SetGuildCmd extends AbstractDevCmd {

        
	public SetGuildCmd() {
        super("setguild");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {

        NPC targetNPC;
        Guild targetGuild;
        
        if(validateUserInput(pcSender, target, args) == false) {
           this.sendUsage(pcSender);
           return;
            }
                        
        // Valid arguments, attempt to set guild of NPC.
        
        targetNPC = getTargetAsNPC(pcSender);
        targetGuild = Guild.getGuild(Integer.parseInt(args[0]));
        
        DbManager.NPCQueries.SET_PROPERTY(targetNPC, "npc_guildID", args[0]);
        targetNPC.setGuild(targetGuild);
        
        // Refresh loaded game object
        
        WorldGrid.updateObject(targetNPC, pcSender);
        
	}

	@Override
	protected String _getUsageString() {
        return "' /setguild [UID]";
	}

	@Override
	protected String _getHelpString() {
        return "Assigns NPC to a given guild";
	}
        
        private boolean validateUserInput(PlayerCharacter pcSender, AbstractGameObject currTarget, String[] userInput) {
        
        Guild tempGuild;
        
        // Incorrect number of arguments
            
        if (userInput.length != 1)
         return false;    

        // No target
        
         if (currTarget == null) {
           throwbackError(pcSender, "Requires an NPC be targeted");
           return false;
         }
         
        // Target must be an NPC
         
         if (currTarget.getObjectType() != GameObjectType.NPC) {
             throwbackError(pcSender, "Invalid object. Must be an NPC");
            return false; 
         }
         
        // Argument must parse as a int.
         
         try {
         Integer.parseInt(userInput[0]); }
        catch (NumberFormatException | NullPointerException e) {
         return false;
        }
         
         // Argument must return a valid guild
         
         tempGuild = Guild.getGuild(Integer.parseInt(userInput[0]));
         
         if (tempGuild == null) {
             throwbackError(pcSender, "Invalid Guild UID");
              return false;
         }
          
             
        return true;
}

}
