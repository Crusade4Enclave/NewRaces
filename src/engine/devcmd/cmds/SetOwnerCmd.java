package engine.devcmd.cmds;

import engine.Enum.DbObjectType;
import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.DbManager;
import engine.objects.*;


/**
 *
 * @author 
 * Dev command to set the owner of targeted building.
 * Argument is a valid guild UID
 */
public class SetOwnerCmd extends AbstractDevCmd {

        Building _targetBuilding = null;
        DbObjectType _newOwnerType;
        AbstractCharacter outOwner;

	public SetOwnerCmd() {
        super("setowner");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
                    
        if(validateUserInput(pcSender, target, args) == false) {
           this.sendUsage(pcSender);
           return;
            }
                        
        // Valid arguments, attempt to set owner of Building.
        
        _targetBuilding = getTargetAsBuilding(pcSender);

        // if it's a tol change ownership of the city

        if (_targetBuilding.getBlueprint() != null &&
            _targetBuilding.getBlueprint().getBuildingGroup().equals(engine.Enum.BuildingGroup.TOL)) {

            City city = _targetBuilding.getCity();

            if (city != null) {
                city.claim(outOwner);
                return; }
        }
        _targetBuilding.setOwner(outOwner);
                
        DbManager.BuildingQueries.SET_PROPERTY(_targetBuilding, "ownerUUID", args[0]);
        
        _targetBuilding.refreshGuild();
        
	}

	@Override
	protected String _getUsageString() {
        return "' /setowner [UID]";
	}

	@Override
	protected String _getHelpString() {
        return "Assigns new owner to building";
	}
        
        private boolean validateUserInput(PlayerCharacter pcSender, AbstractGameObject currTarget, String[] userInput) {
        
        // Incorrect number of arguments
            
        if (userInput.length != 1)
         return false;    

        // No target
        
         if (currTarget == null) {
           throwbackError(pcSender, "Requires a Building to be targeted");
           return false;
         }
         
        // Target must be an Building
         
         if (currTarget.getObjectType() != GameObjectType.Building) {
             throwbackError(pcSender, "Invalid target. Must be a Building");
            return false; 
         }
         
        // Argument must parse to a long.
         
         try {
         Long.parseLong(userInput[0]); }
        catch (NumberFormatException | NullPointerException e) {
         return false;
        }
         
         // Argument must return a valid NPC or PlayerCharacter
         
         _newOwnerType = DbManager.BuildingQueries.GET_UID_ENUM(Long.parseLong(userInput[0]));

            switch (_newOwnerType) {
                case NPC:
                    outOwner = (AbstractCharacter)DbManager.getObject(GameObjectType.NPC, Integer.parseInt(userInput[0]));
                    break;
                case CHARACTER:
                    outOwner = (AbstractCharacter)DbManager.getObject(GameObjectType.PlayerCharacter, Integer.parseInt(userInput[0]));
                    break;
            }

         if (outOwner == null) {
             throwbackError(pcSender, "Invalid Owner UID");
             return false;
         }
             
        return true;
}

}
