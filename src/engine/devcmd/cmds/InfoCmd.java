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
import engine.Enum.TargetColor;
import engine.devcmd.AbstractDevCmd;
import engine.gameManager.BuildingManager;
import engine.gameManager.SessionManager;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.util.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author
 *
 */
public class InfoCmd extends AbstractDevCmd {

	public InfoCmd() {
		super("info");
	}

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] words,
			AbstractGameObject target) {
		// Arg Count Check
		if (words.length != 1) {
			this.sendUsage(pc);
			return;
		}
		if (pc == null) {
			return;
		}

		String newline = "\r\n ";

		try {
			int targetID = Integer.parseInt(words[0]);
			Building b = BuildingManager.getBuilding(targetID);
			if (b == null)
				throwbackError(pc, "Building with ID " + targetID
						+ " not found");
			else
				target = b;
		} catch (Exception e) {
		}

		if (target == null) {
			throwbackError(pc, "Target is unknown or of an invalid type."
					+ newline + "Type ID: 0x"
					+ pc.getLastTargetType().toString()
					+ "   Table ID: " + pc.getLastTargetID());
			return;
		}


		GameObjectType objType = target.getObjectType();
		int objectUUID = target.getObjectUUID();
		String output;

		output = "Target Information:" + newline;
		output += StringUtils.addWS("UUID: " + objectUUID, 20);
		output += newline;
		output += "Type: " + target.getClass().getSimpleName();
		output += " [0x" + objType.toString() + ']';

		if (target instanceof AbstractWorldObject) {
			AbstractWorldObject targetAWO = (AbstractWorldObject) target;
			Vector3fImmutable targetLoc = targetAWO.getLoc();
			output += newline;
			output += StringUtils.addWS("Lat: " + targetLoc.x, 20);
			output += "Lon: " + -targetLoc.z;
			output += newline;
			output += StringUtils.addWS("Alt: " + targetLoc.y, 20);
			output += newline;
			output += "Rot: " + targetAWO.getRot().y;
			output += newline;
			double radian = 0;
			
			
			if  (targetAWO.getBounds() != null && targetAWO.getBounds().getQuaternion() != null)
				radian = targetAWO.getBounds().getQuaternion().angleY;
			int degrees = (int) Math.toDegrees(radian);

			
			output += "Degrees: " + degrees;
			output += newline;
		}

		switch (objType) {
		case Building:
			Building targetBuilding = (Building) target;
			output += StringUtils.addWS("Lac: "
					+ targetBuilding.getw(), 20);
			output += "Blueprint : ";
			output += targetBuilding.getBlueprintUUID();
			output += newline;

			output += " MeshUUID : ";
			output += targetBuilding.getMeshUUID();
			output += newline;

			if (targetBuilding.getBlueprintUUID() != 0)
				output += ' ' + targetBuilding.getBlueprint().getName();
			
			output += newline;
			output += targetBuilding.getBlueprint() != null ? targetBuilding.getBlueprint().getBuildingGroup().name(): " no building group";

			output += newline;
			output += "EffectFlags: " + targetBuilding.getEffectFlags();
			output += newline;
			output += StringUtils.addWS("rank: " + targetBuilding.getRank(),
					20);
			output += "HP: " + targetBuilding.getHealth() + '/'
					+ targetBuilding.getMaxHitPoints();
			output += newline;
			output += "Scale: (" + targetBuilding.getMeshScale().getX();
			output += ", " + targetBuilding.getMeshScale().getY();
			output += ", " + targetBuilding.getMeshScale().getZ() + ')';
			output += newline;
			output += "Owner UID: " + targetBuilding.getOwnerUUID();
			output += (targetBuilding.isOwnerIsNPC() ? " (NPC)" : " (PC)");
			output += newline;
			output += "ProtectionState: " + targetBuilding.getProtectionState().name();
			output += newline;

			if (targetBuilding.getUpgradeDateTime() != null) {
				output += targetBuilding.getUpgradeDateTime().toString();
				output += newline;
			}

			Guild guild = targetBuilding.getGuild();
			Guild nation = null;
			String guildId = "-1";
			String nationId = "-1";
			String gTag = "";
			String nTag = "";

			if (guild != null) {
				int id = guild.getObjectUUID();

				if (id == 0) {
					guildId = id + " [" + guild.hashCode() + ']';
				} else
					guildId = Integer.toString(id);

				gTag = guild.getGuildTag().summarySentence();
				nation = guild.getNation();

				if (nation != null) {
					id = nation.getObjectUUID();
					if (id == 0) {
						nationId = id + " [" + nation.hashCode() + ']';
					} else {
						nationId = Integer.toString(id);
					}
					nTag = nation.getGuildTag().summarySentence();
				}
			}
			output += StringUtils.addWS("Guild UID: " + guildId, 20);

			if (gTag.length() > 0)
				output += "Guild crest: " + gTag;

			output += newline;
			output += StringUtils.addWS("Nation UID: " + nationId, 20);

			if (nTag.length() > 0) {
				output += "Nation crest: " + nTag;
			}

			output+= newline;


			if (targetBuilding.getBlueprint() != null){

				if(targetBuilding.getBlueprint().getBuildingGroup() == BuildingGroup.MINE){
					Mine mine = Mine.getMineFromTower(targetBuilding.getObjectUUID());

					if (mine != null){
						output+= newline;
						output+= "Mine active: " + mine.getIsActive();
						output+= newline;
						output+= "Mine Type: "+mine.getMineType().name;
						output+= newline;
						output+= "Expansion : " + mine.isExpansion();
						output+= newline;
						output+= "Production type: " +mine.getProduction().name();

						output+= newline;
                        output+= "Open Date: "+ ( mine.openDate).toString();

						output+= newline;
                        output+= "Open Date: "+ (mine.openDate).toString();
					}
				}
				output += newline;

				if (targetBuilding.maintDateTime != null){
					output += targetBuilding.maintDateTime.toString();
					output+= newline;
				}
			}

			output += "Reserve : " + targetBuilding.reserve;
			output+= newline;
			output += "Strongbox : " + targetBuilding.getStrongboxValue();
			output+= newline;

			// List hirelings

			if (targetBuilding.getHirelings().isEmpty() == false) {

				output += newline;
				output += "Hirelings List: name / slot / floor";

				BuildingModelBase buildingModelBase = BuildingModelBase.getModelBase(targetBuilding.getMeshUUID());

				for (AbstractCharacter npc : targetBuilding.getHirelings().keySet()) {

					if (npc.getObjectType() != GameObjectType.NPC)
						continue;
					output += newline + npc.getName() + " slot " + targetBuilding.getHirelings().get(npc);
					output += newline + "location " + npc.getLoc();
				}
			}

			ArrayList<BuildingRegions> tempList = BuildingRegions._staticRegions.get(targetBuilding.getMeshUUID());
			output+= newline;
			output+= "Building Regions: Size - " + tempList.size();
			output+= newline;
			output+= "Building Regions from Bounds: Size - " + targetBuilding.getBounds().getRegions().size();
			output+= newline;

			for (Regions regions: targetBuilding.getBounds().getRegions()){
				//TODO ADD REGION INFO
			}

			break;
		case PlayerCharacter:
			output += newline;
			PlayerCharacter targetPC = (PlayerCharacter) target;
			output += StringUtils.addWS("Name: " + targetPC.getName(), 20);
			output += newline;
			output += "InSession : " + SessionManager.getPlayerCharacterByID(target.getObjectUUID())  != null ? " true " : " false";
			output += newline;
			output += "RaceType: " + targetPC.getRace().getRaceType().name();
			output += newline;
			output += "Race: " + targetPC.getRace().getName();
			output += newline;
			output += "Safe:" + targetPC.inSafeZone();
			output+= newline;
			output+= "Experience : " + targetPC.getExp();
            output += newline;
            output += "OverFlowExperience : " + targetPC.getOverFlowEXP();
            output += newline;
            output += StringUtils.addWS("Level: "
                    + targetPC.getLevel() + " (" +
                    TargetColor.getCon(targetPC, pc).toString() + ')', 20);

            Account acpc = SessionManager.getAccount(pc);
            Account ac = SessionManager.getAccount(targetPC);

            if (acpc != null && ac != null) {
                output += "Account ID: " + ac.getObjectUUID();
                output += newline;
                output += "Access Level: " + ac.status.name();
            } else
                output += "Account ID: UNKNOWN";

            output += newline;
            output += "Inventory Weight:" + (targetPC.getCharItemManager().getInventoryWeight() + targetPC.getCharItemManager().getEquipWeight());
            output += newline;
            output += "Max Inventory Weight:" + ((int) targetPC.statStrBase * 3);
			output += newline;
			output += "ALTITUDE :"+ targetPC.getAltitude();
			output += newline;
			output += "BuildingID :"+ targetPC.getInBuildingID();
			output += newline;
			output += "inBuilding :"+ targetPC.getInBuilding();
			output += newline;
			output += "inFloor :"+ targetPC.getInFloorID();
			output += newline;

			BaseClass baseClass = targetPC.getBaseClass();

			if (baseClass != null)
				output += StringUtils.addWS("Class: " + baseClass.getName(), 20);
			 else
				output += StringUtils.addWS("", 20);

			PromotionClass promotionClass = targetPC.getPromotionClass();
			if (promotionClass != null) {
				output += "Pro. Class: " + promotionClass.getName();
			} else {
				output += "Pro. Class: ";
			}

			output += newline;
			output += "====Guild Info====";
			output += newline;
			
			 if (targetPC.getGuild() != null){
				output +=  "Name: " + targetPC.getGuild().getName();
				output += newline;
				output +=  "State: " + targetPC.getGuild().getGuildState();
				output += newline;
				output += "Realms Owned:" +targetPC.getGuild().getRealmsOwned();
				output += newline;
				output +=  "====Nation====";
				output += newline;
				output +=  "Nation Name: " + targetPC.getGuild().getNation().getName();
				output += newline;
				output +=  "Nation State: " + targetPC.getGuild().getNation().getGuildState();
				output += newline;
				output += "Realms Owned:" +targetPC.getGuild().getNation().getRealmsOwned();
				output += newline;
				output += "Guild Rank:" +(GuildStatusController.getRank(targetPC.getGuildStatus()) + targetPC.getGuild().getRealmsOwnedFlag());
			}

			output += newline;
			output += "Movement State: " + targetPC.getMovementState().name();
			output += newline;
			output += "Movement Speed: " + targetPC.getSpeed();

			output += "Altitude : " + targetPC.getLoc().y;

			output += "Swimming : " + targetPC.isSwimming();
			output += newline;
			output += "isMoving : " + targetPC.isMoving();

			break;

		case NPC:
			NPC targetNPC = (NPC) target;
			output += "databaseID: " + targetNPC.getDBID() + newline;
			output += "Name: " + targetNPC.getName();
			output += newline;
			output += StringUtils.addWS("Level: " + targetNPC.getLevel(), 20);
			MobBase mobBase = targetNPC.getMobBase();

			if (mobBase != null)
				output += "RaceID: " + mobBase.getObjectUUID();
			else
				output += "RaceID: " + targetNPC.getLoadID();

			output += newline;
			output += "Flags: " + targetNPC.getMobBase().getFlags().toString();
			output += newline;
			output += "Spawn: (" + targetNPC.getBindLoc().getX();
			output += ", " + targetNPC.getBindLoc().getY();
			output += ", " + targetNPC.getBindLoc().getZ() + ')';
			output += newline;
			output += "ContractID: "  + targetNPC.getContractID();
			output += newline;
			output += "InventorySet: " + targetNPC.getContract().inventorySet;
			output += newline;
			output += targetNPC.getContract().getAllowedBuildings().toString();
			output += newline;
			output += "Extra Rune: " + targetNPC.getContract().getExtraRune();

			output += newline;
			output += "isTrainer: " + targetNPC.getContract().isTrainer();
			output += newline;
			output += "Buy Cost: "  + targetNPC.getBuyPercent();
			output += "\tSell Cost: " + targetNPC.getSellPercent();
			output += newline;
			output += "fromInit: " + targetNPC.isStatic();
			output += newline;
			if (mobBase != null) {
				output += newline;
				output += "Slottable: " + targetNPC.getContract().getAllowedBuildings().toString();
				output += newline;
				output += "Fidelity ID: " + targetNPC.getFidalityID();
				output += newline;
				output += "EquipSet: " + targetNPC.getEquipmentSetID();
				output += newline;
				output += "Parent Zone LoadNum : " + targetNPC.getParentZone().getLoadNum();

			}
			
			if (targetNPC.getRegion() != null){
				output += newline;
				output += "BuildingID : " + targetNPC.getRegion().parentBuildingID;
				output += "building level : " + targetNPC.getRegion().level;
				output += "building room : " + targetNPC.getRegion().room;
			}else{
				output += newline;
				output += "No building found.";
			}
				
			
			
			break;

		case Mob:
			Mob targetMob = (Mob) target;
			output += "databaseID: " + targetMob.getDBID() + newline;
			output += "Name: " + targetMob.getName();
			output += newline;
			output += StringUtils.addWS("Level: " + targetMob.getLevel(), 20);
			mobBase = targetMob.getMobBase();
			if (mobBase != null)
				output += "RaceID: " + mobBase.getObjectUUID();
			else
				output += "RaceID: " + targetMob.getLoadID();
			output += newline;
			output += "NoAggro: " + mobBase.getNoAggro().toString();
			output += newline;
			output += "Spawn: (" + targetMob.getBindLoc().getX();
			output += ", " + targetMob.getBindLoc().getY();
			output += ", " + targetMob.getBindLoc().getZ() + ')';
			output += newline;
			if (targetMob.isPet()) {
				output += "isPet: true";
				output+= newline;
				if (targetMob.isSummonedPet())
					output += "isSummonedPet: true";
				else output += "isSummonedPet: false";
				PlayerCharacter owner = targetMob.getOwner();
				if (owner != null)
					output += "     owner: " + owner.getObjectUUID();
				output += newline;
				output += "assist: " + targetMob.assist() + "   resting: " + targetMob.isSit();
				output += newline;
			}
			if (targetMob.getMobBase() != null) {
				output += "Mobbase: " + targetMob.getMobBase().getObjectUUID();
				output += newline;
				output += "Flags: " + targetMob.getMobBase().getFlags().toString();
				output += newline;

			}
			if (targetMob.isMob()) {
				output += "SpawnRadius: " + targetMob.getSpawnRadius();
				output += newline;
				output += "Spawn Timer: " + targetMob.getSpawnTimeAsString();
				output += newline;
			}
			output += StringUtils.addWS("isAlive: "
					+ targetMob.isAlive(), 20);
			output += newline;
			output += "Mob State: " +targetMob.getState().name();

			output += newline;
			output += "Speed : " + targetMob.getSpeed();
			output += newline;
			output += "Fidelity ID: " + targetMob.getFidalityID();
			output += newline;
			output += "EquipSet: " + targetMob.getEquipmentSetID();
			output += newline;
			output += "Parent Zone LoadNum : " + targetMob.getParentZone().getLoadNum();
			output += newline;
			output += "isMoving : " + targetMob.isMoving();
			break;
		case Item:  //intentional passthrough
		case MobLoot:
			Item item = (Item) target;
			ItemBase itemBase = item.getItemBase();
			output += StringUtils.addWS("ItemBase: " + itemBase.getUUID(), 20);
			output += "Weight: " + itemBase.getWeight();
			output += newline;
			DecimalFormat df = new DecimalFormat("###,###,###,###,##0");
			output += StringUtils.addWS("Qty: "
					+ df.format(item.getNumOfItems()), 20);
			output += "Charges: " + item.getChargesRemaining()
			+ '/' + item.getChargesMax();
			output += newline;
			output += "Name: " + itemBase.getName();
			output += newline;
			output += item.getContainerInfo();

			throwbackInfo(pc, output);

			output = "Effects:" + newline;
			ConcurrentHashMap<String, Effect> effects = item.getEffects();
			for (String name : effects.keySet()) {
				Effect eff = effects.get(name);
				output+= eff.getEffectsBase().getIDString();
				output+= newline;
			//	output += eff.getEffectToken() + (eff.bakedInStat() ? " (baked in)" : "") + newline;
			}

			break;
		}

		throwbackInfo(pc, output);
	}

	@Override
	protected String _getHelpString() {
		return "Gets information on an Object.";
	}

	@Override
	protected String _getUsageString() {
		return "' /info targetID'";
	}

}
