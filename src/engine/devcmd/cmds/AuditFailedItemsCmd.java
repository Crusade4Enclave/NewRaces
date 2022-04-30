// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.Enum.ModType;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.PowersManager;
import engine.net.ItemProductionManager;
import engine.objects.*;
import engine.powers.effectmodifiers.AbstractEffectModifier;
import engine.powers.poweractions.AbstractPowerAction;
import org.pmw.tinylog.Logger;

public class AuditFailedItemsCmd extends AbstractDevCmd {

	public AuditFailedItemsCmd() {
		super("faileditems");
	}

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
	
		
      	   
      	   if (ItemProductionManager.FailedItems.isEmpty())
      		   return;
      	   
      	   Logger.info("Auditing Item production Failed Items");
      	   
      	   String newLine = "\r\n";
  		   String auditFailedItem = "Failed Item Name | Prefix | Suffix | NPC | Contract  | Player | ";
  		   
      	   for (ProducedItem failedItem: ItemProductionManager.FailedItems){
      		   
      		   String npcName = "";
      		   String playerName ="";
      		   String contractName = "";
      		   
      		   String prefix = "";
      		   String suffix = "";
      		   String itemName = "";
      		   NPC npc = NPC.getFromCache(failedItem.getNpcUID());
      		   
      		   if (npc == null){
      			   npcName = "null";
      			   contractName = "null";
      		   }else{
      			   npcName = npc.getName();
      			   if (npc.getContract() != null)
      				   contractName = npc.getContract().getName();
      		   }
      		   
      		   PlayerCharacter roller = PlayerCharacter.getFromCache(failedItem.getPlayerID());
      		   
      		   if (roller == null)
      			   playerName = "null";
      		   else
      			   playerName = roller.getName();
      		   
      		   ItemBase ib = ItemBase.getItemBase(failedItem.getItemBaseID());
      		   
      		   if (ib != null){
      			   itemName = ib.getName();
      		   }
      		   
      		   if (failedItem.isRandom() == false){
      			   if (failedItem.getPrefix().isEmpty() == false){
       				  AbstractPowerAction pa = PowersManager.getPowerActionByIDString(failedItem.getPrefix());
       				  if (pa != null){
       					  for (AbstractEffectModifier aem : pa.getEffectsBase().getModifiers()){
       						  if (aem.modType.equals(ModType.ItemName)){
       							  prefix = aem.getString1();
       							  break;
       						  }
       					  }
       				  }
       				  
       			   }
      			   
      			   if (failedItem.getSuffix().isEmpty() == false){
       				  AbstractPowerAction pa = PowersManager.getPowerActionByIDString(failedItem.getSuffix());
       				  if (pa != null){
       					  for (AbstractEffectModifier aem : pa.getEffectsBase().getModifiers()){
       						  if (aem.modType.equals(ModType.ItemName)){
       							  suffix = aem.getString1();
       							  break;
       						  }
       					  }
       				  }
       				  
       			   }
      			   
      		   }else{
      			   prefix = "random";
      		   }
      		   
      			   
      		   
      		   
      		   auditFailedItem += newLine;
      		   auditFailedItem += itemName + " | "+prefix + " | "+suffix + " | "+ failedItem.getNpcUID() + ":" +npcName + " | "+contractName + " | "+failedItem.getPlayerID() + ":" +playerName;

      	   }
      	   Logger.info(auditFailedItem);
      	 ItemProductionManager.FailedItems.clear();
         

	}


	@Override
	protected String _getUsageString() {
		return "' /bounds'";
	}

	@Override
	protected String _getHelpString() {
		return "Audits all the mobs in a zone.";

	}

}
