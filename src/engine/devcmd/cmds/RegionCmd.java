// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

import java.lang.reflect.Field;

public class RegionCmd extends AbstractDevCmd {

	public RegionCmd() {
        super("region");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {


	if (pc.getRegion() == null){
		this.throwbackInfo(pc, "No Region Found.");
		return;
	}
	
	
	  String newLine = System.getProperty("line.separator");
	  String result = "";
	  result+=(pc.getRegion().getClass().getSimpleName());
	    result+=( " {" );
	    result+=(newLine);
	 Field[] fields = pc.getRegion().getClass().getDeclaredFields();

	  //print field names paired with their values
	  for ( Field field : fields  ) {
		  field.setAccessible(true);
	    result+=(" ");
	    try {
	    	
	    	if(field.getName().contains("Furniture"))
	    		continue;
	      result+=( field.getName());
	      result+=(": ");
	      //requires access to private field:
	      result+=( field.get(pc.getRegion()).toString());
	    } catch ( IllegalAccessException ex ) {
	      System.out.println(ex);
	    }
	    result.trim();
	    result+=(newLine);
	  }
	  result+=("}");
	
	this.throwbackInfo(pc, result.toString());


	}

	@Override
	protected String _getHelpString() {
		return "Temporarily Changes SubRace";
	}

	@Override
	protected String _getUsageString() {
		return "' /setBuildingCollidables add/remove 'add creates a collision line.' needs 4 integers. startX, endX, startY, endY";

	}

}
