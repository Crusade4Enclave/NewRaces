// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.*;
import engine.gameManager.BuildingManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector3fImmutable;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Open manage city asset window
 */
public class ManageCityAssetsMsg extends ClientNetMsg {

	//messageType
	//C->S 2: S->C: 0, 3, 4, 6
	//C->S 15: S->C: 15
	//C->S 14: S->C: 14
	//C->S ?: S->C: 10, 11, 16

	//C->S	2 = manage this asset
	//		20 = manage entire city

	//S->C,	0 = error message
	//		3 = manage asset
	//		4 = no access / building info

	public int actionType;
	private int targetID;
	private int targetType;
	private int targetType1;
	private int targetType2;
	private int targetType3;
	private int targetID1;
	private int targetID2;
	private int targetID3;
	public String assetName;
	private String AssetName1;
	public String CityName;
	private int rank;
	private int symbol;
	public int upgradeCost;
	private int unknown04;
	private int unknown05;
	private int unknown06;
	private int unknown07;
	private int unknown14;
	private int unknown15;
	private int unknown16;
	private int unknown17;
	private int unknown54;
	private int preName01;

	private byte UnkByte03;
	private byte UnkByte04;
	private int strongbox;

	private int baneHour;
	private PlayerCharacter assetManager;
	private Building asset;
	public byte labelProtected;
	public byte labelSiege;
	public byte labelCeaseFire;
	public byte buttonTransfer;
	public byte buttonDestroy;
	public byte buttonAbandon;
	public byte buttonUpgrade;

	/**
	 * This is the general purpose constructor
	 */
	public ManageCityAssetsMsg() {
		super(Protocol.MANAGECITYASSETS);
		this.actionType = 0;
		this.targetType = 0;
		this.targetID = 0;
		this.preName01 = 0;
		this.assetName = "";
		this.CityName = "";
		this.rank = 0;
		this.symbol = 0;
		this.unknown04 = 0;
		this.unknown06 = 0;
		this.unknown07 = 0;
		this.unknown14 = 0;
		this.unknown15 = 0;
		this.unknown16 = 0;
		this.unknown17 = 0;

		this.strongbox = 0;

		this.targetType1 = 0;
		this.targetType2 = 0;
		this.targetType3 = 0;

		this.targetID1 = 0;
		this.targetID2 = 0;
		this.targetID3 = 0;
		this.UnkByte03 = 0;
		this.UnkByte04 = 0;
		this.AssetName1 = "";
		this.unknown54 = 0;
		this.strongbox = 0;
		this.upgradeCost = 0;

		this.labelProtected = 0;
		this.labelSiege = 0;
		this.labelCeaseFire = 0;
		this.buttonTransfer = 0;
		this.buttonDestroy = 0;
		this.buttonAbandon = 0;
		this.buttonUpgrade = 0;

	}

	public ManageCityAssetsMsg(PlayerCharacter pc, Building asset) {
		super(Protocol.MANAGECITYASSETS);
		this.assetManager = pc;
		this.asset = asset;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public ManageCityAssetsMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.MANAGECITYASSETS, origin, reader);
	}

	public int getTargetID() {
		return targetID;
	}

	protected int getPowerOfTwoBufferSize() {
		return (20); // 2^10 == 1024
	}
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public void setTargetType3(int targetType3) {
		this.targetType3 = targetType3;
	}

	public void setTargetID3(int targetID3) {
		this.targetID3 = targetID3;
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
				 actionType = reader.getInt();
		targetType = reader.getInt();
		targetID = reader.getInt();
				 if (this.actionType == 20) {
			reader.getInt();
			this.baneHour = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();

		} else if (this.actionType == 5) { //rename building.
			reader.getInt();
			assetName = reader.getString();
			for (int i = 0; i < 5; i++)
				reader.getInt();
		} else if (this.actionType == 2) {
			reader.getInt();
			this.strongbox = reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			reader.getInt();
			

		}else{
			for (int i = 0; i < 6; i++)
				reader.getInt();
		}
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {
		writer.putInt(this.actionType);

		if (this.actionType == 2) {
			writer.putInt(asset.getObjectType().ordinal());
			writer.putInt(asset.getObjectUUID());
			writer.putInt(0);
			writer.putInt(asset.reserve);
			writer.putInt(0);
			return;
		}

		if (this.actionType == 13) {
			writer.putInt(asset.getObjectType().ordinal());
			writer.putInt(asset.getObjectUUID());
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(asset.getHirelings().size());
			for (AbstractCharacter hireling : asset.getHirelings().keySet()){
				if (!hireling.getObjectType().equals(GameObjectType.NPC))
					writer.putString(hireling.getName());
				else{
					NPC npc = (NPC)hireling;
					if (!npc.getNameOverride().isEmpty()){
						writer.putString(npc.getNameOverride());
					}else

						if (npc.getContract() != null) {
							if (npc.getContract().isTrainer()) {
								writer.putString(npc.getName() + ", " + npc.getContract().getName());
							} else {
								writer.putString(npc.getName() + " " + npc.getContract().getName());
							}
						} else {
							writer.putString(npc.getName());
						}
				}
			}
			
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
		}

		//Bane window

		if (this.actionType == 11) {
			writer.putInt(asset.getObjectType().ordinal());
			writer.putInt(asset.getObjectUUID());
			for (int a = 0;a<5;a++)
				writer.putInt(0);

			writer.putInt(asset.getHirelings().size());

			for (AbstractCharacter npcHire : asset.getHirelings().keySet()) {
				writer.putInt(npcHire.getObjectType().ordinal());
				writer.putInt(npcHire.getObjectUUID());
				if (npcHire.getObjectType() == GameObjectType.NPC)
					writer.putString(((NPC)npcHire).getContract().getName());
				else
					writer.putString("Guard Captain");
				writer.putString(npcHire.getName());
				writer.putInt(1);
				writer.putInt(Blueprint.getNpcMaintCost(npcHire.getRank()));
				if (npcHire.getObjectType() == GameObjectType.NPC)
					writer.putInt(((NPC)npcHire).getContract().getIconID()); // Was 60
				else if (npcHire.getObjectType() == GameObjectType.Mob){
					writer.putInt(((Mob)npcHire).contract.getIconID()); // Was 60
				}
				else
					writer.putInt(5);
				writer.put((byte) 0);
				writer.put((byte) 0);
				writer.put((byte) 1);
				writer.put((byte) 0);
				writer.put((byte) 0);
			}
			return;
		}

		if (this.actionType == 15) {
			writer.putInt(1);
			writer.putInt(1);
			City city = null;
			Zone playerZone = ZoneManager.findSmallestZone(assetManager.getLoc());
			Set<Building> buildings = ZoneManager.findSmallestZone(assetManager.getLoc()).zoneBuildingSet;
			
			Building tol = null;
			if (playerZone.getPlayerCityUUID() != 0)
				city = City.GetCityFromCache(playerZone.getPlayerCityUUID());
			
			if (city != null)
				tol = city.getTOL();
			
			

			writer.putInt(0); // 1 + String = custom message, cant control assets.
			writer.putInt(0);
			writer.putInt(0); //array

			writer.putInt(buildings.size());

			int i = 0;
			for (Building building: buildings){

				i++;
				writer.putString(building.getName()); //ARRAY
				writer.putInt(building.getObjectType().ordinal()); //?
				writer.putInt(building.getObjectUUID()); //?

				writer.putInt(4);
				writer.putInt(4);

				writer.put((byte)0);
				writer.put((byte)0);
				writer.put((byte)1);
				writer.put((byte)1);
				
				//max distance to bypass clientside check.
				float maxDistance = 2000;
				
				
				writer.putFloat(maxDistance);

				writer.putInt(0);
				writer.putInt(0);
				writer.putInt(0);

				if (building.getPatrolPoints() != null){
					writer.putInt(building.getPatrolPoints().size());
					for (Vector3fImmutable patrolPoint: building.getPatrolPoints()){
						writer.putVector3f(patrolPoint);
					}
				}else{
					writer.putInt(0);
				}
				writer.putInt(0); //Sentry Point
			
				if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK){
					writer.putInt(1); //Tab left Random Town? //Opens up 16 Bytes
					writer.putInt(4);
					writer.putInt(0);
					writer.putInt(0);
					writer.putInt(4);
				}else
					writer.putInt(0);
				writer.putInt(0); //array with size 32 bytes. // Adds information of building
			}
			writer.putInt(0); //ARRAY
			writer.putInt(0);
		}

		if (this.actionType == 18) {
			Zone zone = asset.getParentZone();

			if (zone == null)
				return;

			City banedCity = City.getCity(zone.getPlayerCityUUID());

			if (banedCity == null)
				return;

			Bane bane = banedCity.getBane();

			if (bane == null)
				return;

			Guild attackerGuild = bane.getOwner().getGuild();

			if (attackerGuild == null)
				return;

			writer.putInt(asset.getObjectType().ordinal());
			writer.putInt(asset.getObjectUUID());

			writer.putInt(0);
			writer.putString(attackerGuild.getName());
			writer.putString(Guild.GetGL(attackerGuild).getName());
			writer.putInt(bane.getSiegePhase().ordinal()); //1 challenge //2 standoff //3 war
			writer.put((byte) 0);

			if (!bane.isAccepted() && this.assetManager.getGuild() == banedCity.getGuild() && GuildStatusController.isInnerCouncil(this.assetManager.getGuildStatus()))
				writer.put((byte) 1); //canSetTime
			else
				writer.put((byte) 0);

			DateTime placedOn = bane.getLiveDate();

			if (placedOn == null)
				placedOn = new DateTime(DateTime.now());

			//set Calander to date of bane live.
			DateTime now = DateTime.now();
			DateTime defaultTime = new DateTime(bane.getPlacementDate());
			DateTime playerEnterWorldTime = new DateTime(this.assetManager.getTimeStamp("EnterWorld"));
			Period period = new Period(playerEnterWorldTime.getMillis(), now.getMillis());
			int hoursLoggedIn = period.getHours();
			hoursLoggedIn = hoursLoggedIn < 0 ? 0 : hoursLoggedIn;

			defaultTime = defaultTime.plusDays(2);
			defaultTime = defaultTime.hourOfDay().setCopy(22);
			defaultTime = defaultTime.minuteOfHour().setCopy(0);
			defaultTime = defaultTime.secondOfMinute().setCopy(0);

			long curTime = now.getMillis();
			long timeLeft = 0;

			if (bane.getLiveDate() != null)
				timeLeft = bane.getLiveDate().getMillis() - curTime;
			else
				timeLeft = defaultTime.getMillis() - curTime + 1000;

			//DO not touch these. They are static formula's until i get the correct converter for SB Time.

			writer.put((byte) placedOn.dayOfMonth().get());
			writer.put((byte) placedOn.monthOfYear().get());
			writer.putInt(placedOn.year().get() - 1900);
			writer.put((byte) 0);
			writer.put((byte) 0);
			writer.put((byte) 0);

			if (timeLeft < 0)
				writer.putInt(0);
			else
				writer.putInt((int) timeLeft / 1000); // Time remaing until bane/Seconds

			if (attackerGuild.getGuildState() == GuildState.Sworn)
				writer.putInt(4); //3 capture/errant,4 capture/sworn, 5 destroy/soveirgn.
			else
				writer.putInt(5);

			writer.put((byte) (16 - hoursLoggedIn)); // hour start
			writer.put((byte) (24 - hoursLoggedIn)); // hour end
			writer.put((byte) 2);
			writer.putString(banedCity.getCityName());
			writer.putString(Guild.GetGL(bane.getOwner().getGuild()) != null ? Guild.GetGL(bane.getOwner().getGuild()).getName() : "No Guild Leader");
			GuildTag._serializeForDisplay(attackerGuild.getGuildTag(),writer);
			GuildTag._serializeForDisplay(attackerGuild.getNation().getGuildTag(),writer);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
		}

		if (this.actionType == 3) {

			writer.putInt(targetType);
			writer.putInt(targetID);

			Guild nation = null;
			Building building = BuildingManager.getBuildingFromCache(targetID);
			Guild guild = building.getGuild();
			Zone zone = ZoneManager.findSmallestZone(building.getLoc());

			writer.putInt(0);//unknown  Might be to allow manager to open or not!
			writer.putString(building.getName());

			AbstractCharacter buildingOwner =  building.getOwner();

			if (buildingOwner == null)
				writer.putString("Morloch");
			else
				writer.putString(buildingOwner.getName());

			if (zone == null)
				writer.putString("Forlord");
			else
				writer.putString(zone.getName());

				writer.putString(building.getGuild().getName());

			writer.putInt(building.getRank());

			// Maintenance costs include resource if
			// this structure is an R8 tree

			if (building.getRank() == 8)
				writer.putInt(5); // Resources included
			else
				writer.putInt(1); // Gold only

			writer.putInt(2308551); //Gold
			if (building.getBlueprint() == null)
				writer.putInt(0);
			else
			writer.putInt(building.getBlueprint().getMaintCost(building.getRank())); //  maint cost

			if (building.getRank() == 8) {
				writer.putInt(74856115); // Stone
				writer.putInt(1500); //  maint cost
				writer.putInt(-1603256692); // Lumber
				writer.putInt(1500); //  maint cost
				writer.putInt(-1596311545); // Galvor
				writer.putInt(5); //  maint cost
				writer.putInt(1532478436); // Wormwood
				writer.putInt(5); //  maint cost
			}

			LocalDateTime maintDate = building.maintDateTime;

			if (maintDate == null)
				maintDate = LocalDateTime.now();
			writer.putLocalDateTime(LocalDateTime.now()); // current time

			// utc offset?
			writer.putInt((int)java.time.Duration.between(LocalDateTime.now(), maintDate).getSeconds()); // Seconds to maint date

			writer.putInt(building.getStrongboxValue());
			writer.putInt(building.reserve);//reserve Sets the buildings reserve display
			writer.putInt(0);//prosperity under maintenance (wtf is prosperity?)
			writer.putInt(10);
			writer.putFloat((float) .1);

			if (this.buttonUpgrade == 1) {
				if (building.getBlueprint() == null)
					this.upgradeCost = Integer.MAX_VALUE;
				else
				if (building.getRank() == building.getBlueprint().getMaxRank())
					this.upgradeCost = Integer.MAX_VALUE;
				else
					this.upgradeCost = building.getBlueprint().getRankCost(Math.min(building.getRank() + 1, 7));

				writer.putInt(this.upgradeCost);
			}
			else
				writer.putInt(0);

			LocalDateTime uc = LocalDateTime.now();

			if (building.getDateToUpgrade() != null)
				uc = building.getDateToUpgrade();

			long timeLeft = uc.atZone(ZoneId.systemDefault())
					.toInstant().toEpochMilli() - System.currentTimeMillis();
			long hour = timeLeft / 3600000;
			long noHour = timeLeft - (hour * 3600000);
			long minute = noHour / 60000;
			long noMinute = noHour - (minute * 60000);
			long second = noMinute / 1000;

			writer.put((byte) 0);//Has to do with repair time. A 1 here puts 23.9 hours in repair time A 2 here is 1.9 days
			writer.put((byte) 0);//unknown
			writer.putInt(0); //unknown

			if (LocalDateTime.now().isAfter(uc)) {
				writer.put((byte) 0);
				writer.put((byte) 0);
				writer.put((byte) 0);
			}
			else {
				writer.put((byte) (hour));
				writer.put((byte) minute);
				writer.put((byte) second);
			}

			if (timeLeft < 0)
				writer.putInt(0);
			else
				writer.putInt((int) timeLeft);

			writer.putInt((int) building.getCurrentHitpoints());
			writer.putInt((int) building.getMaxHitPoints());
			writer.putInt(BuildingManager.GetRepairCost(building));//sets the repair cost.
			writer.putInt(0);//unknown

			if (building.getBlueprint() == null)
				writer.putInt(0);
			else
			writer.putInt(building.getBlueprint().getSlotsForRank(building.getRank()));
			writer.put((byte) 1);//Has to do with removing update timer and putting in cost for upgrade

			writer.put(labelProtected); // 1 sets protection to invulnerable.
			writer.put(labelSiege);// 1 sets the protection under siege
			writer.put(labelCeaseFire); //0 with 1 set above sets to under siege // 1 with 1 set above sets protection status to under siege(cease fire)

			writer.put(buttonTransfer);// 1 enables the transfer asset button
			writer.put(buttonDestroy);// 1 enables the destroy asset button
			writer.put(buttonAbandon);// 1 here enables the abandon asset button
			writer.put(buttonUpgrade); //disable upgrade building

			if (building.getBlueprint() == null)
				writer.putInt(0);
			else
			writer.putInt(building.getBlueprint().getIcon()); //Symbol

			if (guild == null) {
				for (int i = 0; i < 3; i++)
					writer.putInt(16);
				for (int i = 0; i < 2; i++)
					writer.putInt(0);
			}
			else {
				GuildTag._serializeForDisplay(guild.getGuildTag(),writer);
				nation = guild.getNation();
			}

			if (nation == null) {
				for (int i = 0; i < 3; i++)
					writer.putInt(16);
				for (int i = 0; i < 2; i++)
					writer.putInt(0);
			}
			else {
				GuildTag._serializeForDisplay(nation.getGuildTag(),writer);
			}
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);//1 makes it so manage window does not open.

			if (!building.assetIsProtected() && !building.getProtectionState().equals(ProtectionState.PENDING)){
				writer.putInt(0);
			}
			else{
				writer.putInt(1); //kos on/off?
				writer.putInt(3); // was 3
				if (zone.getPlayerCityUUID() != 0 && asset.assetIsProtected()){
					writer.putInt(GameObjectType.Building.ordinal());
					writer.putInt(City.getCity(zone.getPlayerCityUUID()).getTOL().getObjectUUID());
				}
				else{
					writer.putInt(0);
					writer.putInt(0);
				}

				writer.putInt(0);
				writer.putInt(0);

				writer.putInt(targetType3);
				writer.putInt(targetID3);


				if (building.getProtectionState() == ProtectionState.PENDING)
					writer.put((byte)1); //Accept or decline.
				else
					writer.put((byte)0);

				if (building.taxType == TaxType.NONE)
					writer.put((byte)0); //? ??
				else if(building.taxDateTime != null)
					writer.put((byte)1);
				else
					writer.put((byte)0);

				writer.putString(""); //tree of life protection tax
				writer.putInt(0); //??
				writer.putInt(0); //??
				if (building.taxType == TaxType.NONE){
					writer.putInt(0);
					writer.putInt(0);
				}else if (building.taxType == TaxType.WEEKLY){
					writer.putInt(building.taxAmount);
					writer.putInt(0);
				}else{
					writer.putInt(0);
					writer.putInt(building.taxAmount);
				}


				writer.put(building.enforceKOS ? (byte)1:0); //enforceKOS
				writer.put((byte) 0); //?
				writer.putInt(1);
			}



			ConcurrentHashMap<AbstractCharacter, Integer> npcList = building.getHirelings();
			writer.putInt(npcList.size());
			if (npcList.size() > 0) {
				for (AbstractCharacter npcHire : npcList.keySet()) {
					writer.putInt(npcHire.getObjectType().ordinal());
					if (npcHire.getObjectType() == GameObjectType.Mob)
						writer.putInt(((Mob)npcHire).getDBID());
					else
						writer.putInt(npcHire.getObjectUUID());
					if (npcHire.getObjectType() == GameObjectType.NPC)
						writer.putString(((NPC)npcHire).getContract().getName());
					else
						writer.putString("Guard Captain");
					writer.putString(npcHire.getName());
					writer.putInt(npcHire.getRank());
					writer.putInt(Blueprint.getNpcMaintCost(npcHire.getRank()));
					if (npcHire.getObjectType() == GameObjectType.NPC)
						writer.putInt(((NPC)npcHire).getContract().getIconID()); // Was 60
					else  if (npcHire.getObjectType() == GameObjectType.Mob)
						writer.putInt(((Mob)npcHire).contract.getIconID()); // Was 60

					int contractID = 0;


					if (npcHire.getObjectType() == GameObjectType.Mob)
						contractID = ((Mob)npcHire).contract.getContractID();
					else if (npcHire.getObjectType() == GameObjectType.NPC)
						contractID = ((NPC)npcHire).getContract().getContractID();

					if (contractID ==830){
						writer.putInt(24580);
					}
					else	if (building.getBlueprint() != null && (building.getBlueprint().getBuildingGroup() == BuildingGroup.FORGE ||building.getBlueprint().getBuildingGroup() == BuildingGroup.MAGICSHOP||building.getBlueprint().getBuildingGroup() == BuildingGroup.TAILOR)){

						writer.put((byte)0);
						writer.put((byte)4);
						writer.put((byte)128);
						writer.put((byte)0);

					}else{
						writer.put((byte)0);
						if (building.getBlueprint() != null && building.getBlueprint().getBuildingGroup() == BuildingGroup.BARRACK)
							writer.put((byte)1);
						else
							writer.put((byte)0);
						writer.put((byte)0);
						writer.put((byte)0);
					}


					if (!npcHire.isAlive()){
						writer.put((byte) 1); // 1 SHOWs respawning
						writer.putInt(10); // Seconds in respawn.
						writer.putInt(20);
					}
					else
						writer.put((byte)0);

				}
			}
		}
		if (this.actionType == 4) {
			writer.putInt(targetType);
			writer.putInt(targetID);
			Building building = BuildingManager.getBuildingFromCache(targetID);

			writer.putInt(preName01);
			writer.putString(building.getName()); //assetName
			writer.putString(building.getOwnerName()); //ownerName
			writer.putString(building.getGuild().getName());//guild name
			writer.putString(building.getCityName()); //City Name
			writer.putInt(building.getRank());
			if (building.getBlueprint() == null)
				writer.putInt(0);
			else
				writer.putInt(building.getBlueprint().getIcon());

			//tags
			GuildTag._serializeForDisplay(building.getGuild().getGuildTag(), writer);
			GuildTag._serializeForDisplay(building.getGuild().getNation().getGuildTag(), writer);

			writer.putInt(unknown14);
			writer.putInt(unknown15);
			writer.putInt(unknown16);
			writer.putInt(unknown17);
			writer.putInt(0); // previously uninitialized unknown18
		}
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getSymbol() {
		return symbol;
	}

	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}

	public int getUnknown04() {
		return unknown04;
	}

	public void setUnknown04(int unknown04) {
		this.unknown04 = unknown04;
	}

	public int getUnknown05() {
		return unknown05;
	}

	public void setUnknown05(int unknown05) {
		this.unknown05 = unknown05;
	}

	public int getUnknown06() {
		return unknown06;
	}

	public void setUnknown06(int unknown06) {
		this.unknown06 = unknown06;
	}

	public void setUnknown07(int unknown07) {
		this.unknown07 = unknown07;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String AssetName) {
		this.assetName = AssetName;
	}

	public void setAssetName1(String AssetName1) {
		this.AssetName1 = AssetName1;
	}

	public String getCityName() {
		return CityName;
	}

	public void setUnknown54(int unknown54) {
		this.unknown54 = unknown54;
	}

	public int getStrongbox() {
		return strongbox;
	}

	public void setStrongbox(int strongbox) {
		this.strongbox = strongbox;
	}

	public int getBaneHour() {
		return baneHour;
	}

	public Building getAsset() {
		return asset;
	}

	public void setAsset(Building asset) {
		this.asset = asset;
	}

}

//Debug Info
//Run: Failed to make object TEMPLATE:135700 INSTANCE:1717987027141... (t=50.46) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:108760 INSTANCE:1717987027161... (t=50.46) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:108760 INSTANCE:1717987027177... (t=50.67) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:60040 INSTANCE:1717987027344... (t=50.87) (r=7/4/2011 11:56:39)
//C:\ArcanePrime\Main_Branch\Shadowbane\Source\ArcObjectLoader.cpp(466):ERROR: ArcObjectLoader::Run: Failed to make object TEMPLATE:3 INSTANCE:1717987027164... (t=50.88) (r=7/4/2011 11:56:39)

