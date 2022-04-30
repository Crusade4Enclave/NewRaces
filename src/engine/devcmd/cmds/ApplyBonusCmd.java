// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.PowerActionType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ChatManager;
import engine.gameManager.PowersManager;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.powers.ActionsBase;
import engine.powers.PowersBase;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.util.ThreadUtils;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.HashSet;

public class ApplyBonusCmd extends AbstractDevCmd {

	public ApplyBonusCmd() {
		super("applybonus");
	}

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		
		String action = words[0];
		
		PowerActionType actionType = null;
		
		HashMap<String,HashSet<String>> appliedMods = new HashMap<>();
		
		try{
			
			if (action.equals("all") == false)
		for (PowerActionType powerActionType : PowerActionType.values()){
			if (powerActionType.name().equalsIgnoreCase(action) == false)
				continue;
			actionType = powerActionType;
			break;
		}
			
		}catch(Exception e){
			this.throwbackError(pcSender, "Invalid power Action type for " + action);
			this.throwbackInfo(pcSender, "Valid Types : " + this.getActionTypes());
			return;
		}
		if (action.equals("all") == false)
		if (actionType == null){
			this.throwbackError(pcSender, "Invalid power Action type for " + action);
			this.throwbackInfo(pcSender, "Valid Types : " + this.getActionTypes());
			return;
		}
		
		
		for (PowersBase pb : PowersManager.powersBaseByIDString.values()){
			if (pb.getActions() == null || pb.getActions().isEmpty())
				continue;
			
			for (ActionsBase ab: pb.getActions()){
				if (ab.getPowerAction() == null)
					continue;
				if (action.equals("all") == false)
				if (ab.getPowerAction().getType().equalsIgnoreCase(action) == false)
					continue;
				String effect1 = "";
				String effect2 = "";
				ChatManager.chatSystemInfo(pcSender,"Applying Power " + pb.getName() + " : " +pb.getDescription());
				if (ab.getPowerAction().getEffectsBase() == null){
					
					try {
						PowersManager.runPowerAction(pcSender, pcSender, pcSender.getLoc(), ab, 1, pb);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ThreadUtils.sleep(500);
					continue;
				}

			
					if (ab.getPowerAction().getEffectsBase().getModifiers() == null || ab.getPowerAction().getEffectsBase().getModifiers().isEmpty()){
						try {
							PowersManager.runPowerAction(pcSender, pcSender, pcSender.getLoc(), ab, 1, pb);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
						
					boolean run = true;
					for (AbstractEffectModifier mod : ab.getPowerAction().getEffectsBase().getModifiers()){
						if (appliedMods.containsKey(mod.modType.name()) == false){
							appliedMods.put(mod.modType.name(), new HashSet<>());
						}
						
//						if (appliedMods.get(mod.modType.name()).contains(mod.sourceType.name())){
//							continue;
//						}
						
						appliedMods.get(mod.modType.name()).add(mod.sourceType.name());
						try{
							try {
								PowersManager.runPowerAction(pcSender, pcSender, pcSender.getLoc(), ab, 1, pb);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	
						}catch(Exception e){
							Logger.error(e);
						}
						break;
						
							
					
					
				}
			
			}
		}	
	}


	@Override
	protected String _getUsageString() {
		return "' /bounds'";
	}

	@Override
	protected String _getHelpString() {
		return "Audits all the mobs in a zone.";

	}
	
	private String getActionTypes(){
		String output = "";
		
		for (PowerActionType actionType : PowerActionType.values()){
			output += actionType.name() + " | ";
			
		}
		
		return output.substring(0, output.length() -3);
	}

}
