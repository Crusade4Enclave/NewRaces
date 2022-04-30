package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class ResetLevelCmd extends AbstractDevCmd {
      
	public ResetLevelCmd() {
        super("resetlevel");
    }

        // AbstractDevCmd Overridden methods
        
	@Override
	protected void _doCmd(PlayerCharacter player, String[] args,
			AbstractGameObject target)  {
       
          player.ResetLevel(Short.parseShort(args[0]));
        }

	@Override
	protected String _getHelpString() {
        return "Resets character level to `level`. All training points are reset. Player must relog for changes to update.";
	}

	@Override
	protected String _getUsageString() {


        return "/resetlevel <level>";
	}

 
        
}
