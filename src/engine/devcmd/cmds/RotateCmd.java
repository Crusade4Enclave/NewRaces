// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.InterestManagement.WorldGrid;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.ChatManager;
import engine.gameManager.DbManager;
import engine.math.Vector3f;
import engine.math.Vector3fImmutable;
import engine.objects.*;

public class RotateCmd extends AbstractDevCmd {

	public RotateCmd() {
        super("rotate");
        this.addCmdString("rot");
    }

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		if (target == null && (words.length != 2) ) {
			this.sendUsage(pc);
			return;
		}


		if (words.length == 3){
			try{

			}catch(Exception e){

			}
		}


		float rot;
		if (target != null && words.length == 1) {

			try {
				if (words[0].equalsIgnoreCase("face")){
					this.rotateFace(pc, target);
					return;
				}

				rot = Float.parseFloat(words[0]);
			} catch (NumberFormatException e) {
				throwbackError(pc, "Supplied rotation " + words[0]
						+ " failed to parse to a Float");
				return;
			} catch (Exception e) {
				throwbackError(pc,
						"Invalid Rotate Command.  Need Rotation specified.");
				return;
			}

			Vector3f rotation = new Vector3f(0f, rot, 0f);

			if (target instanceof Building)
				rotateBuilding(pc, (Building) target, rotation, rot,false);
			else if (target instanceof NPC)
				rotateNPC(pc, (NPC) target, rotation,false);
			else if (target instanceof Mob)
				rotateMob(pc, (Mob) target, rotation,false);
			else
				throwbackError(pc, "Target " + target.getObjectType()
				+ " is not a valid object type");
		} else {

			int id = 0;
			if (words.length == 2) {
				try {
					id = Integer.parseInt(words[0]);

					if (words[1].equalsIgnoreCase("face")){

						Building b;
						if (id != 0)
							b = BuildingManager.getBuilding(id);
						else
							b = getTargetAsBuilding(pc);
						if (b != null) {
							rotateFace(pc, b);
							return;
						}

						// building failed, try npc
						NPC npc;
						if (id != 0)
							npc = NPC.getNPC(id);
						else
							npc = getTargetAsNPC(pc);
						if (npc != null) {
							rotateFace(pc, npc);
							return;
						}

						// NPC failed, try mob
						Mob mob;
						if (id != 0)
							mob = Mob.getMob(id);
						else
							mob = getTargetAsMob(pc);
						if (mob != null) {
							rotateFace(pc, mob);
							return;
						}
						throwbackError(pc, "Nothing found to rotate.");
						return;
					}
					rot = Float.parseFloat(words[1]);
				} catch (NumberFormatException e) {
					throwbackError(pc, "Supplied arguments " + words[0] + ' '
							+ words[1] + " failed to parse");
					return;
				} catch (Exception e) {
					throwbackError(pc,
							"Invalid Rotate Command. Need Rotation specified.");
					return; // NaN
				}
			} else {
				try {
					rot = Float.parseFloat(words[0]);
				} catch (NumberFormatException e) {
					throwbackError(pc, "Supplied rotation " + words[0]
							+ " failed to parse to a Float");
					return;
				} catch (Exception e) {
					throwbackError(pc,
							"Invalid Rotate Command. Need Rotation specified.");
					return; // NaN
				}
			}

			Vector3f rotation = new Vector3f(0f, rot, 0f);

			Building b;
			if (id != 0)
				b = BuildingManager.getBuilding(id);
			else
				b = getTargetAsBuilding(pc);
			if (b != null) {
				rotateBuilding(pc, b, rotation, rot,false);
				return;
			}

			// building failed, try npc
			NPC npc;
			if (id != 0)
				npc = NPC.getNPC(id);
			else
				npc = getTargetAsNPC(pc);
			if (npc != null) {
				rotateNPC(pc, npc, rotation,false);
				return;
			}

			// NPC failed, try mob
			Mob mob;
			if (id != 0)
				mob = Mob.getMob(id);
			else
				mob = getTargetAsMob(pc);
			if (mob != null) {
				rotateMob(pc, mob, rotation,false);
				return;
			}
			throwbackError(pc, "Nothing found to rotate.");
		}
	}

	@Override
	protected String _getHelpString() {
        return "Rotates targeted or specified object";
	}

	@Override
	protected String _getUsageString() {
        return "' /rotate [objectID] rotation' || ' /rot [objectID] rotation'";
	}

	private void rotateBuilding(PlayerCharacter pc, Building building, Vector3f rot, float orig, boolean faceDirection) {
		if (!faceDirection)
			rot.set(0.0f, (float)Math.sin(Math.toRadians(orig)/2), 0.0f);
		building.setRot(rot);
		building.setw( (float) Math.abs(Math.cos(Math.toRadians(orig)/2)) );
		building.getBounds().setBounds(building);
		WorldGrid.updateObject(building, pc);
		DbManager.BuildingQueries.SET_PROPERTY(building, "rotY", building.getRot().getY());
		DbManager.BuildingQueries.SET_PROPERTY(building, "w", building.getw());
		ChatManager.chatSayInfo(pc,
				"Building with ID " + building.getObjectUUID() + " rotated");
	}

	private void rotateNPC(PlayerCharacter pc, NPC npc, Vector3f rot,boolean faceDirection) {
		npc.setRot(rot);
		DbManager.NPCQueries.SET_PROPERTY(npc, "npc_rotation", rot.y);
		WorldGrid.updateObject(npc, pc);
		//no rotation for npc's in db currently
		ChatManager.chatSayInfo(pc,
				"NPC with ID " + npc.getObjectUUID() + " rotated");
	}

	private void rotateMob(PlayerCharacter pc, Mob mob, Vector3f rot,boolean faceDirection) {
		mob.setRot(rot);
		DbManager.MobQueries.SET_PROPERTY(mob, "mob_rotation", rot.y);
		WorldGrid.updateObject(mob, pc);
		//no rotation for mobs's in db currently
		ChatManager.chatSayInfo(pc,
				"Mob with ID " + mob.getObjectUUID() + " rotated");
	}

	private void rotateFace(PlayerCharacter pc, AbstractGameObject target){
		AbstractWorldObject awo = (AbstractWorldObject)target;
		if (awo == null)
			return;
		Vector3fImmutable buildingLoc = awo.getLoc();
		Vector3fImmutable playerLoc = pc.getLoc();

		Vector3fImmutable faceDirection = playerLoc.subtract2D(buildingLoc);

		float rotangle = faceDirection.getRotation();

		float rot = (float) Math.toDegrees(rotangle);

		if (rot > 180)
			rot*=-1;

		Vector3f buildingrotation = new Vector3f(0f, rot, 0f);
		Vector3f rotation = new Vector3f(0f, rotangle, 0f);
		if (target instanceof Building)
			rotateBuilding(pc, (Building) target, buildingrotation, rot,false);
		else if (target instanceof NPC)
			rotateNPC(pc, (NPC) target, rotation,true);
		else if (target instanceof Mob)
			rotateMob(pc, (Mob) target, rotation,true);
		else
			throwbackError(pc, "Target " + target.getObjectType()
			+ " is not a valid object type");

	}
}