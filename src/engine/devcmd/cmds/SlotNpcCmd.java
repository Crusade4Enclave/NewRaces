// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum.BuildingGroup;
import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.Contract;
import engine.objects.PlayerCharacter;
import engine.util.StringUtils;
import org.pmw.tinylog.Logger;

/**
 * Summary: Game designer utility command to add or 
 *      remove building slot access for contracts
 */
 
public class SlotNpcCmd extends AbstractDevCmd {
      
	public SlotNpcCmd() {
        super("slotnpc");
    }

        
        // AbstractDevCmd Overridden methods
        
	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target)  {

          Contract contractObject;
          BuildingGroup buildingGroup;
          
          long slotBitvalue;
          String outString;
         
            if (target.getObjectType() != GameObjectType.NPC) {
                throwbackInfo(pc, "NpcSlot: target must be an NPC");
                return;
            }

            // Get the contract from the npc
            contractObject = getTargetAsNPC(pc).getContract();
            
            // User requests list of current groups.
            
            if (args[0].toUpperCase().equals("LIST")) {

                outString = "Current: " + contractObject.getAllowedBuildings();
                
                throwbackInfo(pc, outString);
                return;
            }

            if(validateUserInput(args) == false) {
                this.sendUsage(pc);
		 return;
            }
                     
         // Extract the building group flag from user input
            
         buildingGroup = BuildingGroup.valueOf(args[0].toUpperCase());
            
         switch (args[1].toUpperCase()) {
             
             case "ON":
                 contractObject.getAllowedBuildings().add(buildingGroup);
                 
                 if (!DbManager.ContractQueries.updateAllowedBuildings(contractObject, contractObject.getAllowedBuildings().toLong())){
                	 Logger.error( "Failed to update Database for Contract Allowed buildings");
                	 ChatManager.chatSystemError(pc, "Failed to update Database for Contract Allowed buildings. " +
                	 		"Contact A CCR, oh wait, you are a CCR. You're Fubared.");
                	 return;
                 }

                 throwbackInfo(pc, "SlotNpc " + buildingGroup.name() + " added to npc");
                 break;
             case "OFF":
                contractObject.getAllowedBuildings().remove(buildingGroup);
                 if (!DbManager.ContractQueries.updateAllowedBuildings(contractObject, contractObject.getAllowedBuildings().toLong())){
                	 Logger.error( "Failed to update Database for Contract Allowed buildings");
                	 ChatManager.chatSystemError(pc, "Failed to update Database for Contract Allowed buildings. " +
                	 		"Contact A CCR, oh wait, you are a CCR. You're Fubared.");
                	 return;
                 }

                 throwbackInfo(pc, "SlotNpc " + buildingGroup.name() + " removed from npc");
                 break;
         }
            
        }

	@Override
	protected String _getHelpString() {
        return "Sets a building slot on a targeted npc";
	}

	@Override
	protected String _getUsageString() {
		String usage = "/npcslot [BuildingType] on-off \n";
                
                for (BuildingGroup group:BuildingGroup.values()) {
                    usage += group.name() + ' ';
                }
                
                usage = StringUtils.wordWrap(usage, 30);

		return usage;
	}

        // Class methods
        
        private static boolean validateUserInput(String[] userInput) {

        int stringIndex;
        BuildingGroup testGroup;
        
        testGroup = BuildingGroup.FORGE;
        
        String commandSet = "onoff";
        
        // incorrect number of arguments test
        
        if (userInput.length > 2)
         return false;    
  
            
        // Test of toggle argument
        
        stringIndex = commandSet.indexOf(userInput[1].toLowerCase());
                
        if (stringIndex == -1)
         return false;
        
        // Validate we have a corrent building group name
        
        for (BuildingGroup group:BuildingGroup.values()) {
            if (group.name().equals(userInput[0].toUpperCase()))
                return true;
                    }
        return false;
        }
        
}
