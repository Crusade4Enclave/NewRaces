package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;

/**
 * @author 
 * Summary: Devcmd to toggle logging of mysql statements
 *   
 */
 
public class SqlDebugCmd extends AbstractDevCmd {

    // Instance variables
    
             
	public SqlDebugCmd() {
        super("sqldebug");
    }

        
        // AbstractDevCmd Overridden methods
        
	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target) {
            
            Boolean debugState = false;

            if(validateUserInput(args) == false) {
                this.sendUsage(pc);
		 return;
            }
            
        // Arguments have been validated use argument to set debug state
            
            switch (args[0]) {
                case "on":
                  debugState = true;
                  break;
                case "off":
                  debugState = false;
                  break;
                default:
                  break;
            }
            
            MBServerStatics.DB_ENABLE_QUERY_OUTPUT = debugState;
         
         // Send results to user
         throwbackInfo(pc, "SQL debug state: " + debugState.toString());
        }

	@Override
	protected String _getHelpString() {
        return "Toggles sending SQL statements to log";
	}

	@Override
	protected String _getUsageString() {
        return "/sqldebug on|off";
	}

        // Class methods
        
        private static boolean validateUserInput(String[] userInput) {

        int stringIndex;
        String commandSet = "onoff";
        
        // incorrect number of arguments test
        
        if (userInput.length != 1)
         return false;    

        // Validate arguments
        
        stringIndex = commandSet.indexOf(userInput[0].toLowerCase());

            return stringIndex != -1;
        }
        
  
}
