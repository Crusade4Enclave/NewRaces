package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.devcmd.AbstractDevCmd;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

public class ShowOffsetCmd extends AbstractDevCmd {
      
	public ShowOffsetCmd() {
        super("showoffset");
    }

        // AbstractDevCmd Overridden methods
        
	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target)  {

       
          Building targetBuilding;
          String outString;
          Vector3fImmutable offset;
          
          String newline = "\r\n ";
          targetBuilding = (Building)target;
         
            if (targetBuilding.getObjectType() != GameObjectType.Building) {
                throwbackInfo(pc, "showgate: target must be an Building");
                return;
            }
  
           offset = pc.getLoc().subtract(targetBuilding.getLoc());
           
           outString = "Location: " + pc.getLoc().x + "x " + pc.getLoc().z + 'y';
           outString += newline;     
           outString += "Offset: " + offset.x + "x " + offset.y + 'y';
           throwbackInfo(pc, outString);
        }

	@Override
	protected String _getHelpString() {
        return "Shows offset to current building";
	}

	@Override
	protected String _getUsageString() {


        return "Shows offset to current building";
	}

 
        
}
