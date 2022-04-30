// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.Enum.GameObjectType;
import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.MobBase;
import engine.objects.NPC;
import engine.objects.PlayerCharacter;

public class SetNpcEquipSetCmd extends AbstractDevCmd {

	public static int lastEquipSetID = 0;
	public SetNpcEquipSetCmd() {
        super("setEquipSet");
        this.addCmdString("equipset");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}

		if (target.getObjectType() != GameObjectType.NPC){
			this.sendUsage(pc);
			return;
		}

		NPC npc = (NPC)target;

		if (words[0].equalsIgnoreCase("next")){

			if (SetNpcEquipSetCmd.lastEquipSetID >= 1222)
				SetNpcEquipSetCmd.lastEquipSetID = 1;
			else
				SetNpcEquipSetCmd.lastEquipSetID++;

			boolean complete = false;

			while (complete == false){
				complete = NPC.UpdateEquipSetID(npc, SetNpcEquipSetCmd.lastEquipSetID);

				if (!complete){
					SetNpcEquipSetCmd.lastEquipSetID++;
					if (SetNpcEquipSetCmd.lastEquipSetID >= 1222)
						SetNpcEquipSetCmd.lastEquipSetID = 1;
				}
			}

			if (complete){
				npc.equip = MobBase.loadEquipmentSet(npc.getEquipmentSetID());
				WorldGrid.updateObject(npc);
				this.throwbackInfo(pc, "Equipment Set Changed to " + SetNpcEquipSetCmd.lastEquipSetID );
			}



			return;
		}

		if (words[0].equalsIgnoreCase("last")){

			if (SetNpcEquipSetCmd.lastEquipSetID <= 1)
				SetNpcEquipSetCmd.lastEquipSetID = 1222;
			else
				SetNpcEquipSetCmd.lastEquipSetID--;

			boolean complete = false;

			while (complete == false){
				complete = NPC.UpdateEquipSetID(npc, SetNpcEquipSetCmd.lastEquipSetID);

				if (!complete){
					SetNpcEquipSetCmd.lastEquipSetID--;
					if (SetNpcEquipSetCmd.lastEquipSetID <= 1)
						SetNpcEquipSetCmd.lastEquipSetID = 1222;
				}

			}

			if (complete){
				this.throwbackInfo(pc, "Equipment Set Changed to " + SetNpcEquipSetCmd.lastEquipSetID );
				npc.equip = MobBase.loadEquipmentSet(npc.getEquipmentSetID());
				WorldGrid.updateObject(npc);
			}



			return;
		}

		int equipSetID = 0;

		try{
			equipSetID = Integer.parseInt(words[0]);
		}catch(Exception e){
			this.throwbackError(pc, e.getMessage());
		}

		if (!NPC.UpdateEquipSetID(npc, equipSetID)){
			this.throwbackError(pc, "Unable to find Equipset for ID " + equipSetID );
			return;
		}

		SetNpcEquipSetCmd.lastEquipSetID = equipSetID;
		npc.equip = MobBase.loadEquipmentSet(npc.getEquipmentSetID());
		WorldGrid.updateObject(npc);

		this.throwbackInfo(pc, "Equipment Set Changed to " + equipSetID );


	}

	@Override
	protected String _getHelpString() {
		return "Sets slot position for an NPC to 'slot'";
	}

	@Override
	protected String _getUsageString() {
		return "' /changeslot slot'";
	}

}
