// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum.GameObjectType;
import engine.Enum.MinionType;
import engine.Enum.ProtectionState;
import engine.ai.StaticMobActions;
import engine.gameManager.PowersManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;
import engine.powers.EffectsBase;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Order NPC
 */
public class ManageNPCMsg extends ClientNetMsg {

	private int targetType;
	private int targetID;
	private int unknown03;
	private int unknown04;
	private int unknown05;
	private int unknown06;

	private int unknown07;
	private int unknown08;
	private int unknown09;
	private int unknown10;
	private int unknown11;
	private int buyNormal;
	private int buyGuild;
	private int buyNation;
	private int sellNormal;
	private int sellGuild;
	private int sellNation;

	private String CityName;
	private String OwnerName;
	private String GuildName;
	private int unknown12;

	private int unknown13;
	private int unknown14;
	private int unknown15;
	private int unknown16;
	private int unknown17;
	private int unknown18;

	private int messageType;

	private int unknown19; //Arraylist motto length?
	private String motto; //motto Length 60 max?

	private int unknown01;

	private int buildingID;
	private int unknown20;
	private int unknown21;
	private int unknown22;
	private int unknown23;
	private int unknown24;
	private int unknown25;
	private int unknown26;
	private int unknown28;
	private int unknown30;
	private int unknown31;
	private int unknown32;
	private int unknown33;
	private int unknown34;
	private int unknown35;
	private int unknown36;
	private int unknown37;
	private int unknown38;
	private int unknown39;
	private int unknown40;
	private int unknown41;
	private int unknown42;
	private int unknown43;
	private int unknown44;
	private int unknown45;
	private int unknown46;
	private int unknown47;
	private int unknown48;
	private int unknown49;
	private int unknown50;
	private int unknown51;
	private int unknown52;
	private int unknown53;
	private int unknown54;
	private int unknown55;
	private int unknown56;
	private int unknown57;
	private int unknown58;
	private int unknown59;
	private int unknown60;
	private int unknown61;
	private int unknown62;
	private int unknown63;
	private int unknown64;
	private int unknown65;
	private int unknown66;
	private int unknown67;
	private int unknown68;
	private int unknown69;
	private int unknown70;
	private int unknown71;
	private int unknown72;
	private int unknown73;
	private int unknown74;
	private int unknown75;
	private int unknown76;
	private int unknown77;
	private int unknown78;
	private int unknown79;
	private int unknown80;
	private int unknown81;
	private int unknown82;
	private int unknown83;

	/**
	 * This is the general purpose constructor
	 */
	public ManageNPCMsg(AbstractCharacter ac) {
		super(Protocol.MANAGENPC);
		this.targetType = ac.getObjectType().ordinal();
		this.targetID = ac.getObjectUUID();
		this.buyGuild = 26;  //TODO pull all these from the NPC object
		this.buyNation = 26;
		this.buyNormal = 26;
		this.sellGuild = 100;
		this.sellNation = 100;
		this.sellNormal = 100;
		this.messageType = 1; //This seems to be the "update Hireling window" value flag

		//Unknown defaults...
		this.unknown20 = 0;
		this.unknown21 = 0;
		this.unknown22 = 0;
		this.unknown23 = 0;
		this.unknown24 = 0;
		this.unknown25 = 0;
		this.unknown26 = 0;
		this.unknown28 = 0;
		this.unknown30 = 0;
		this.unknown31 = 0;
		this.unknown32 = 0;
		this.unknown33 = 0;
		this.unknown34 = 0;
		this.unknown35 = 0;
		this.unknown36 = 0;
		this.unknown37 = 1;//1
		this.unknown38 = 0;//0
		this.unknown39 = 0;//0
		this.unknown40 = 1;//1
		this.unknown41 = 0;//0
		this.unknown42 = 1;//1 [Toggles tree icon in protection slots]
		this.unknown43 = 0;//0
		this.unknown44 = 1;//1
		this.unknown45 = 0;//0
		this.unknown46 = 0;//0
		this.unknown47 = 0;//0
		this.unknown48 = 0;//0
		this.unknown49 = 0;//0
		this.unknown50 = 0;//0
		this.unknown51 = 0;//0
		this.unknown52 = 0;//0
		this.unknown53 = 0;//0
		this.unknown54 = 1;//1
		this.unknown55 = 0;//0
		this.unknown56 = 3;//3
		this.unknown57 = 3;//3
		this.unknown58 = 0;//0
		this.unknown59 = 5;//5
		this.unknown60 = 0;//0
		this.unknown61 = 0;//0
		this.unknown62 = 0;//0
		this.unknown63 = 64;//64
		this.unknown64 = 0;//0
		this.unknown65 = 0;//0
		this.unknown66 = 0;//0
		this.unknown67 = 0;//0
		this.unknown68 = 1;//1
		this.unknown69 = 1;//1
		this.unknown70 = 0;//0
		this.unknown71 = 1;//1
		this.unknown72 = 0;
		this.unknown73 = 0;
		this.unknown74 = 5;
		this.unknown75 = 1;
		this.unknown76 = 2;
		this.unknown77 = 15;
		this.unknown78 = 3;
		this.unknown79 = 18;
		this.unknown80 = 0;
		this.unknown81 = 0;
		this.unknown82 = 0;
		this.unknown83 = 0;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the
	 * ByteBuffer into a message. If a BufferUnderflow occurs (based on reading
	 * past the limit) then this constructor Throws that Exception to the
	 * caller.
	 */
	public ManageNPCMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.MANAGENPC, origin, reader);
	}

	@Override
	protected int getPowerOfTwoBufferSize() {
		return (19); // 2^10 == 1024
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {
		//TODO do we need to do anything here? Does the client ever send this message to the server?
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		Period upgradePeriod;
		int upgradePeriodInSeconds;

		try{
			
	
		writer.putInt(messageType); //1
		if (messageType == 5) {
			writer.putInt(unknown20);//0
			writer.putInt(targetType);
			writer.putInt(targetID);

			writer.putInt(GameObjectType.Building.ordinal());
			writer.putInt(buildingID);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);
			writer.putInt(0);

		} else if (messageType == 1) {
			NPC npc = null;
			Mob mobA = null;

			if (this.targetType == GameObjectType.NPC.ordinal()){

				npc = NPC.getFromCache(this.targetID);

				if (npc == null) {
					Logger.error("Missing NPC of ID " + this.targetID);
					return;
				}

				Contract contract = null;
				contract = npc.getContract();

				if (contract == null) {
					Logger.error("Missing contract for NPC " + this.targetID);
					return;
				}

				writer.putInt(0); //anything other than 0 seems to mess up the client
				writer.putInt(targetType);
				writer.putInt(targetID);
				writer.putInt(0); //static....
				writer.putInt(0);//static....
				writer.putInt(Blueprint.getNpcMaintCost(npc.getRank()));  // salary

				writer.putInt(npc.getUpgradeCost());

				if (npc.isRanking() && npc.getUpgradeDateTime().isAfter(DateTime.now()))
					upgradePeriod = new Period(DateTime.now(), npc.getUpgradeDateTime());
				else
					upgradePeriod = new Period(0);

				writer.put((byte) upgradePeriod.getDays());    //for timer
				writer.put((byte) unknown26);//unknown
				writer.putInt(100); //unknown

				writer.put((byte) upgradePeriod.getHours());    //for timer
				writer.put((byte) upgradePeriod.getMinutes());  //for timer
				writer.put((byte) upgradePeriod.getSeconds());  //for timer

				if (npc.isRanking() && npc.getUpgradeDateTime().isAfter(DateTime.now()))
					upgradePeriodInSeconds = Seconds.secondsBetween(DateTime.now(), npc.getUpgradeDateTime()).getSeconds();
				else
					upgradePeriodInSeconds = 0;

				writer.putInt(upgradePeriodInSeconds);

				writer.put((byte) 0);
				writer.put((byte) (npc.getRank() == 7 ? 0 : 1));  //0 will make the upgrade field show "N/A"
				writer.put((byte) 0);
				writer.put((byte) 0);
				writer.putInt(0);
				writer.putInt(10000);  //no idea...
				writer.put((byte) 0);
				writer.put((byte) 0);
				writer.put((byte) 0);
				writer.putInt(0);

				NPCProfits profit = NPC.GetNPCProfits(npc);
				
				if (profit == null)
					profit = NPCProfits.defaultProfits;
				//adding .000000001 to match client.
				int buyNormal = (int) ((profit.buyNormal + .000001f) * 100);
				int buyGuild = (int) ((profit.buyGuild + .000001f) *100);
				int buyNation = (int) ((profit.buyNation + .000001f) * 100);
				
				int sellNormal = (int) ((profit.sellNormal + .000001f) * 100);
				int sellGuild = (int) ((profit.sellGuild + .000001f) * 100);
				int sellNation = (int) ((profit.sellNation + .000001f) * 100);
				
				writer.putInt(buyNormal);
				writer.putInt(buyGuild);
				writer.putInt(buyNation);
				writer.putInt(sellNormal);
				writer.putInt(sellGuild);
				writer.putInt(sellNation);

				if (contract.isRuneMaster()) {
					writer.putInt(0); //vendor slots
					writer.putInt(0); //artillery slots

					//figure out number of protection slots based on building rank
					int runemasterSlots = (2 * npc.getRank()) + 6;

					writer.putInt( runemasterSlots);

					for (int i = 0; i < 13; i++) {
						writer.putInt(0); //statics
					}
					//some unknown list
					writer.putInt(4); //list count
					writer.putInt(17);
					writer.putInt(2);
					writer.putInt(12);
					writer.putInt(23);

					writer.putInt(0); //static
					writer.putInt(0); //static

					//TODO add runemaster list here

					ArrayList<Building> buildingList = npc.getProtectedBuildings();

					writer.putInt(buildingList.size());

					for (Building b : buildingList) {
						writer.putInt(3);
						writer.putInt(b.getObjectType().ordinal());
						writer.putInt(b.getObjectUUID());

						writer.putInt(npc.getParentZone().getObjectType().ordinal());
						writer.putInt(npc.getParentZone().getObjectUUID());

						writer.putLong(0); //TODO Identify what Comp this is suppose to be.
						if (b.getProtectionState() == ProtectionState.PENDING)
							writer.put((byte)1);
						else
							writer.put((byte)0);
						writer.put((byte)0);
						writer.putString(b.getName());
						writer.putInt(1);//what?
						writer.putInt(1);//what?
						//taxType = b.getTaxType()
						switch(b.taxType){
						case NONE:
							writer.putInt(0);
							writer.putInt(0);
							break;
						case WEEKLY:
							writer.putInt(b.taxAmount);
							writer.putInt(0);
							break;
						case PROFIT:
							writer.putInt(0);
							writer.putInt(b.taxAmount);
							break;

						}
                        writer.put(b.enforceKOS ? (byte)1:0); //ENFORCE KOS
						writer.put((byte)0); //??
						writer.putInt(1);
					}

					writer.putInt(0); //artillery captain list

				} else if (contract.isArtilleryCaptain()) {
					int slots = 1;
					if (contract.getContractID() == 839)
						slots = 3;


					writer.putInt(0); //vendor slots
					writer.putInt(slots); //artillery slots
					writer.putInt(0); //runemaster slots

					for (int i = 0; i < 13; i++) {
						writer.putInt(0); //statics
					}
					//some unknown list
					writer.putInt(1); //list count
					writer.putInt(16);

					writer.putInt(0); //static
					writer.putInt(0); //static
					writer.putInt(0); //runemaster list

					//artillery captain list
					ConcurrentHashMap<Mob, Integer> siegeMinions = npc.getSiegeMinionMap();
					writer.putInt(1 + siegeMinions.size());
					serializeBulwarkList(writer, 1); //Trebuchet
					//serializeBulwarkList(writer, 2); //Ballista

					if (siegeMinions != null && siegeMinions.size() > 0)

						for (Mob mob : siegeMinions.keySet()) {
							this.unknown83 = mob.getObjectUUID();
							writer.putInt(2);
							writer.putInt(mob.getObjectType().ordinal());
							writer.putInt(this.unknown83);
							writer.putInt(0);
							writer.putInt(10);
							writer.putInt(0);
							writer.putInt(1);
							writer.putInt(1);
							writer.put((byte) 0);
							long curTime = System.currentTimeMillis() / 1000;
							long upgradeTime = mob.timeToSpawnSiege / 1000;
							long timeLife = upgradeTime - curTime;

							writer.putInt(900);
							writer.putInt(900);
							writer.putInt((int) timeLife); //time remaining?
							writer.putInt(0);
							writer.put((byte)0);
							writer.putString(mob.getName());
							writer.put((byte) 0);
						}
					return;

				}else{

					if (Contract.NoSlots(npc.getContract()))
						writer.putInt(0);
					else
					writer.putInt(npc.getRank()); //vendor slots
					writer.putInt(0); //artilerist slots
					writer.putInt(0); //runemaster slots

					writer.putInt(1); //is this static?
					for (int i = 0; i < 4; i++) {
						writer.putInt(0); //statics
					}
					//Begin Item list for creation.
					writer.putInt(npc.getCanRoll().size());

					for (Integer ib : npc.getCanRoll()) {
						ItemBase item = ItemBase.getItemBase(ib);
						writer.put((byte) 1);
						writer.putInt(0);
						writer.putInt(ib); //itemID
						writer.putInt(item.getBaseValue());
						writer.putInt(600);
						writer.put((byte) 1);
						writer.put((byte) item.getModTable());
						writer.put((byte) item.getModTable());
						writer.put((byte) item.getModTable());
						writer.put((byte) item.getModTable());//EffectItemType
					}
					ArrayList<MobLoot> itemList = npc.getRolling();

					if (itemList.isEmpty())
						writer.putInt(0);
					else {
						if (itemList.size() < npc.getRank())
							writer.putInt(itemList.size());
						else
							writer.putInt(npc.getRank());
						for (Item i : itemList) {
							if (itemList.indexOf(i) >= npc.getRank())
								break;
							ItemBase ib = i.getItemBase();
							writer.put((byte) 0); // ? Unknown45
							writer.putInt(i.getObjectType().ordinal());
							writer.putInt(i.getObjectUUID());

							writer.putInt(0);
							writer.putInt(i.getItemBaseID());
							writer.putInt(ib.getBaseValue());
							long curTime = System.currentTimeMillis() / 1000;
							long upgradeTime = i.getDateToUpgrade() / 1000;
							long timeLife = i.getDateToUpgrade() - System.currentTimeMillis();

							timeLife /= 1000;
							writer.putInt((int) timeLife);
							writer.putInt(npc.getRollingTimeInSeconds(i.getItemBaseID()));
							writer.putInt(1);
							if (i.isComplete())
								writer.put((byte) 1);
							else
								writer.put((byte) 0);

							ArrayList<String> effectsList = i.getEffectNames();
							EffectsBase prefix = null;
							EffectsBase suffix = null;

							for (String effectName: effectsList){
								if (effectName.contains("PRE"))
									prefix = PowersManager.getEffectByIDString(effectName);
								if (effectName.contains("SUF"))
									suffix = PowersManager.getEffectByIDString(effectName);

							}

							if ((prefix == null && suffix == null))
								writer.putInt(0);
							else
								writer.putInt(-1497023830);
							if ((prefix != null && !i.isRandom()) || (prefix != null && i.isComplete()))
								writer.putInt(prefix.getToken());
							else
								writer.putInt(0);
							if ((suffix != null && !i.isRandom())  || (suffix != null && i.isComplete()))
								writer.putInt(suffix.getToken());
							else
								writer.putInt(0);
							writer.putString(i.getCustomName());
						}
					}

					writer.putInt(0);
					writer.putInt(0);
					writer.putInt(1);
					writer.putInt(0);
					writer.putInt(3);
					writer.putInt(3);
					writer.putInt(0);
					writer.putString("Repair items");
					writer.putString("percent");
					writer.putInt(npc.getRepairCost()); //cost for repair
					writer.putInt(0);
					//ArrayList<Integer> modSuffixList =
					ArrayList<Integer> modPrefixList = npc.getModTypeTable();
					Integer mod = modPrefixList.get(0);

					if (mod != 0) {
						writer.putInt(npc.getModTypeTable().size()); //Effects size
						for (Integer mtp : npc.getModTypeTable()) {

							Integer imt = modPrefixList.indexOf(mtp);
							writer.putInt(npc.getItemModTable().get(imt)); //?
							writer.putInt(0);
							writer.putInt(0);
							writer.putFloat(2);
							writer.putInt(0);
							writer.putInt(1);
							writer.putInt(2);
							writer.putInt(0);
							writer.putInt(1);
							writer.put(npc.getItemModTable().get(imt));
							writer.put(npc.getItemModTable().get(imt));
							writer.put(npc.getItemModTable().get(imt));
							writer.put(npc.getItemModTable().get(imt));//writer.putInt(-916801465); effectItemType
							writer.putInt(mtp); //prefix
							Integer mts = modPrefixList.indexOf(mtp);
							writer.putInt(npc.getModSuffixTable().get(mts)); //suffix
						}
					} else
						writer.putInt(0);
					ArrayList<Item> inventory = npc.getInventory();


					writer.putInt(inventory.size()); //placeholder for item cnt



					for (Item i : inventory) {

						Item.serializeForClientMsgWithoutSlot(i,writer);
					}


					writer.putInt(0);
					writer.putInt(5);
					writer.putInt(1);
					writer.putInt(2);
					writer.putInt(15);
					writer.putInt(3);
					writer.putInt(18);

					writer.putInt(0);
					writer.putInt(0);
					writer.putInt(0);
					writer.putInt(0);
				}

			}else if (this.targetType == GameObjectType.Mob.ordinal()){

				mobA = StaticMobActions.getFromCacheDBID(this.targetID);
				if (mobA == null) {
					Logger.error("Missing Mob of ID " + this.targetID);
					return;
				}

				if (mobA != null){
					Contract con = mobA.contract;
					if (con == null) {
						Logger.error("Missing contract for NPC " + this.targetID);
						return;
					}

					int maxSlots = 1;

					switch (mobA.getRank()){
					case 1:
					case 2:
						maxSlots = 1;
						break;
					case 3:
						maxSlots = 2;
						break;
					case 4:
					case 5:
						maxSlots = 3;
						break;
					case 6:
						maxSlots = 4;
						break;
					case 7:
						maxSlots = 5;
						break;
					default:
						maxSlots = 1;

					}
					writer.putInt(0); //anything other than 0 seems to mess up the client
					writer.putInt(targetType);
					writer.putInt(targetID);
					writer.putInt(0); //static....
					writer.putInt(0);//static....
					writer.putInt(Blueprint.getNpcMaintCost(mobA.getRank()));  // salary

					writer.putInt(StaticMobActions.getUpgradeCost(mobA));

					if (mobA.upgradeDateTime != null && mobA.upgradeDateTime.isAfter(DateTime.now()))
						upgradePeriod = new Period(DateTime.now(), mobA.upgradeDateTime);
					else
						upgradePeriod = new Period(0);

					writer.put((byte) upgradePeriod.getDays());    //for timer
					writer.put((byte) unknown26);//unknown
					writer.putInt(100); //unknown

					writer.put((byte) upgradePeriod.getHours());    //for timer
					writer.put((byte) upgradePeriod.getMinutes());  //for timer
					writer.put((byte) upgradePeriod.getSeconds());  //for timer

					if (mobA.upgradeDateTime != null && mobA.upgradeDateTime.isAfter(DateTime.now()))
						upgradePeriodInSeconds = Seconds.secondsBetween(DateTime.now(), mobA.upgradeDateTime).getSeconds();
					else
						upgradePeriodInSeconds = 0;

					writer.putInt(upgradePeriodInSeconds);


					writer.put((byte) 0);
					writer.put((byte) (mobA.getRank() == 7 ? 0 : 1));  //0 will make the upgrade field show "N/A"
					writer.put((byte) 0);
					writer.put((byte) 0);
					writer.putInt(0);
					writer.putInt(10000);  //no idea...
					writer.put((byte) 0);
					writer.put((byte) 0);
					writer.put((byte) 0);
					writer.putInt(0);


					NPCProfits profit = NPCProfits.defaultProfits;
					
					writer.putInt((int) (profit.buyNormal * 100));
					writer.putInt((int) (profit.buyGuild * 100));
					writer.putInt((int) (profit.buyNation * 100));
					writer.putInt((int) (profit.sellNormal * 100));
					writer.putInt((int) (profit.sellGuild * 100));
					writer.putInt((int) (profit.sellNation * 100));

					writer.putInt(0); //vendor slots
					writer.putInt(maxSlots); //artillery slots
					writer.putInt(0); //runemaster slots

					for (int i = 0; i < 13; i++) {
						writer.putInt(0); //statics
					}
					//some unknown list
					writer.putInt(1); //list count
					writer.putInt(16);

					writer.putInt(0); //static
					writer.putInt(0); //static
					writer.putInt(0); //runemaster list

					//artillery captain list
					ConcurrentHashMap<Mob, Integer> siegeMinions = mobA.siegeMinionMap;

				
						writer.putInt(siegeMinions.size() + 1);
							serializeGuardList(writer, mobA.contract.getContractID()); //Guard
				
					if (siegeMinions != null && siegeMinions.size() > 0)

						for (Mob mob : siegeMinions.keySet()) {
							this.unknown83 = mob.getObjectUUID();
							writer.putInt(2);
							writer.putInt(mob.getObjectType().ordinal());
							writer.putInt(this.unknown83);
							writer.putInt(0);
							writer.putInt(10);
							writer.putInt(0);
							writer.putInt(1);
							writer.putInt(1);
							writer.put((byte) 0);
							long curTime = System.currentTimeMillis() / 1000;
							long upgradeTime = mob.timeToSpawnSiege / 1000;
							long timeLife = upgradeTime - curTime;

							writer.putInt(900);
							writer.putInt(900);
							writer.putInt((int) timeLife); //time remaining?
							writer.putInt(0);
							writer.put((byte)0);
							writer.putString(mob.nameOverride.isEmpty() ? mob.getName() : mob.nameOverride);
							writer.put((byte) 0);
						}

				}


			}

		}
		
		}catch(Exception e){
			e.printStackTrace();
		}


	}



	//Serializes lists for Bulwarks
	private static void serializeBulwarkList(ByteBufferWriter writer, int minion) {
		int minionIndex;

		if (minion < 1 || minion > 3)
			minionIndex = 1;
		else
			minionIndex = minion;

		writer.putInt(0);
		for (int i = 0; i < 3; i++) {
			writer.putInt(0); //static
		}
		writer.putInt(9);
		writer.putInt(5);
		writer.putInt(9);
		writer.putInt(5);
		writer.put((byte) 0);

		writer.putInt((minion == 1) ? 900 : 600); //roll time
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0); //Array
		writer.put((byte) 0);

		if (minion == 1)
			writer.putString("Trebuchet");
		else if (minion == 2)
			writer.putString("Ballista");
		else
			writer.putString("Mangonel");
		writer.put((byte) 1);
		writer.putString("A weapon suited to laying siege");
	}

	private static void serializeGuardList(ByteBufferWriter writer, int minion) {

		writer.putInt(1);

		for (int i = 0; i < 3; i++)
			writer.putInt(0); //static

		writer.putInt(minion);
		writer.putInt(1);
		writer.putInt(minion);
		writer.putInt(1);
		writer.put((byte) 0);

		writer.putInt(600); //roll time
		writer.putInt(0);
		writer.putInt(0);
		writer.putInt(0); //Array
		writer.put((byte) 0);
		
		MinionType minionType = MinionType.ContractToMinionMap.get(minion);
		writer.putString(minionType != null ? minionType.getRace() + " " + minionType.getName() : "Minion Guard");
		writer.put((byte) 1);
		writer.putString("A Guard To Protect Your City.");
	}





	public String getCityName() {
		return CityName;
	}


	public void setUnknown07(int unknown07) {
		this.unknown07 = unknown07;
	}



	public void setMotto(String motto) {
		this.motto = motto;
	}

	public String getMotto() {
		return motto;
	}

	public void setUnknown01(int unknown01) {
		this.unknown01 = unknown01;
	}

	public int getUnknown01() {
		return unknown01;
	}

	public void setUnknown03(int unknown03) {
		this.unknown03 = unknown03;
	}

	public int getUnknown03() {
		return unknown03;
	}

	public void setUnknown04(int unknown04) {
		this.unknown04 = unknown04;
	}

	public int getUnknown04() {
		return unknown04;
	}

	public void setUnknown05(int unknown05) {
		this.unknown05 = unknown05;
	}

	public int getUnknown05() {
		return unknown05;
	}

	public void setUnknown06(int unknown06) {
		this.unknown06 = unknown06;
	}

	public int getUnknown06() {
		return unknown06;
	}

	public int getBuyNormal() {
		return buyNormal;
	}

	public void setBuyNormal(int buyNormal) {
		this.buyNormal = buyNormal;
	}

	public int getBuyGuild() {
		return buyGuild;
	}

	public void setBuyGuild(int buyGuild) {
		this.buyGuild = buyGuild;
	}

	public int getBuyNation() {
		return buyNation;
	}

	public void setBuyNation(int buyNation) {
		this.buyNation = buyNation;
	}

	public int getSellNormal() {
		return sellNormal;
	}

	public void setSellNormal(int sellNormal) {
		this.sellNormal = sellNormal;
	}

	public int getSellGuild() {
		return sellGuild;
	}

	public void setSellGuild(int sellGuild) {
		this.sellGuild = sellGuild;
	}

	public int getSellNation() {
		return sellNation;
	}

	public void setSellNation(int sellNation) {
		this.sellNation = sellNation;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public int getBuildingID() {
		return buildingID;
	}

	public void setBuildingID(int buildingID) {
		this.buildingID = buildingID;
	}

	public int getUnknown20() {
		return unknown20;
	}

	public void setUnknown20(int unknown20) {
		this.unknown20 = unknown20;
	}

	public int getUnknown21() {
		return unknown21;
	}

	public void setUnknown21(int unknown21) {
		this.unknown21 = unknown21;
	}

	public int getUnknown22() {
		return unknown22;
	}

	public void setUnknown22(int unknown22) {
		this.unknown22 = unknown22;
	}

	public int getUnknown23() {
		return unknown23;
	}

	public void setUnknown23(int unknown23) {
		this.unknown23 = unknown23;
	}

	public int getUnknown24() {
		return unknown24;
	}

	public void setUnknown24(int unknown24) {
		this.unknown24 = unknown24;
	}

	public int getUnknown25() {
		return unknown25;
	}

	public void setUnknown25(int unknown25) {
		this.unknown25 = unknown25;
	}

	public int getUnknown26() {
		return unknown26;
	}

	public void setUnknown26(int unknown26) {
		this.unknown26 = unknown26;
	}

	public int getUnknown28() {
		return unknown28;
	}

	public void setUnknown28(int unknown28) {
		this.unknown28 = unknown28;
	}

	public int getUnknown30() {
		return unknown30;
	}

	public void setUnknown30(int unknown30) {
		this.unknown30 = unknown30;
	}

	public int getUnknown31() {
		return unknown31;
	}

	public void setUnknown31(int unknown31) {
		this.unknown31 = unknown31;
	}

	public int getUnknown32() {
		return unknown32;
	}

	public void setUnknown32(int unknown32) {
		this.unknown32 = unknown32;
	}

	public int getUnknown33() {
		return unknown33;
	}

	public void setUnknown33(int unknown33) {
		this.unknown33 = unknown33;
	}

	public int getUnknown34() {
		return unknown34;
	}

	public void setUnknown34(int unknown34) {
		this.unknown34 = unknown34;
	}

	public int getUnknown35() {
		return unknown35;
	}

	public void setUnknown35(int unknown35) {
		this.unknown35 = unknown35;
	}

	public int getUnknown36() {
		return unknown36;
	}

	public void setUnknown36(int unknown36) {
		this.unknown36 = unknown36;
	}

	public int getUnknown37() {
		return unknown37;
	}

	public void setUnknown37(int unknown37) {
		this.unknown37 = unknown37;
	}

	public int getUnknown38() {
		return unknown38;
	}

	public void setUnknown38(int unknown38) {
		this.unknown38 = unknown38;
	}

	public int getUnknown39() {
		return unknown39;
	}

	public void setUnknown39(int unknown39) {
		this.unknown39 = unknown39;
	}

	public int getUnknown40() {
		return unknown40;
	}

	public void setUnknown40(int unknown40) {
		this.unknown40 = unknown40;
	}

	public int getUnknown41() {
		return unknown41;
	}

	public void setUnknown41(int unknown41) {
		this.unknown41 = unknown41;
	}

	public int getUnknown42() {
		return unknown42;
	}

	public void setUnknown42(int unknown42) {
		this.unknown42 = unknown42;
	}

	public int getUnknown44() {
		return unknown44;
	}

	public void setUnknown44(int unknown44) {
		this.unknown44 = unknown44;
	}

	public int getUnknown43() {
		return unknown43;
	}

	public void setUnknown43(int unknown43) {
		this.unknown43 = unknown43;
	}

	public int getUnknown45() {
		return unknown45;
	}

	public void setUnknown45(int unknown45) {
		this.unknown45 = unknown45;
	}

	public int getUnknown46() {
		return unknown46;
	}

	public void setUnknown46(int unknown46) {
		this.unknown46 = unknown46;
	}

	public int getUnknown47() {
		return unknown47;
	}

	public void setUnknown47(int unknown47) {
		this.unknown47 = unknown47;
	}

	public int getUnknown48() {
		return unknown48;
	}

	public void setUnknown48(int unknown48) {
		this.unknown48 = unknown48;
	}

	public int getUnknown49() {
		return unknown49;
	}

	public void setUnknown49(int unknown49) {
		this.unknown49 = unknown49;
	}

	public int getUnknown50() {
		return unknown50;
	}

	public void setUnknown50(int unknown50) {
		this.unknown50 = unknown50;
	}

	public int getUnknown51() {
		return unknown51;
	}

	public void setUnknown51(int unknown51) {
		this.unknown51 = unknown51;
	}

	public int getUnknown52() {
		return unknown52;
	}

	public void setUnknown52(int unknown52) {
		this.unknown52 = unknown52;
	}

	public int getUnknown53() {
		return unknown53;
	}

	public void setUnknown53(int unknown53) {
		this.unknown53 = unknown53;
	}

	public int getUnknown54() {
		return unknown54;
	}

	public void setUnknown54(int unknown54) {
		this.unknown54 = unknown54;
	}

	public int getUnknown55() {
		return unknown55;
	}

	public void setUnknown55(int unknown55) {
		this.unknown55 = unknown55;
	}

	public int getUnknown56() {
		return unknown56;
	}

	public void setUnknown56(int unknown56) {
		this.unknown56 = unknown56;
	}

	public int getUnknown57() {
		return unknown57;
	}

	public void setUnknown57(int unknown57) {
		this.unknown57 = unknown57;
	}

	public int getUnknown58() {
		return unknown58;
	}

	public void setUnknown58(int unknown58) {
		this.unknown58 = unknown58;
	}

	public int getUnknown59() {
		return unknown59;
	}

	public void setUnknown59(int unknown59) {
		this.unknown59 = unknown59;
	}

	public int getUnknown60() {
		return unknown60;
	}

	public void setUnknown60(int unknown60) {
		this.unknown60 = unknown60;
	}

	public int getUnknown61() {
		return unknown61;
	}

	public void setUnknown61(int unknown61) {
		this.unknown61 = unknown61;
	}

	public int getUnknown62() {
		return unknown62;
	}

	public void setUnknown62(int unknown62) {
		this.unknown62 = unknown62;
	}

	public int getUnknown63() {
		return unknown63;
	}

	public void setUnknown63(int unknown63) {
		this.unknown63 = unknown63;
	}

	public int getUnknown64() {
		return unknown64;
	}

	public void setUnknown64(int unknown64) {
		this.unknown64 = unknown64;
	}

	public int getUnknown65() {
		return unknown65;
	}

	public void setUnknown65(int unknown65) {
		this.unknown65 = unknown65;
	}

	public int getUnknown66() {
		return unknown66;
	}

	public void setUnknown66(int unknown66) {
		this.unknown66 = unknown66;
	}

	public int getUnknown67() {
		return unknown67;
	}

	public void setUnknown67(int unknown67) {
		this.unknown67 = unknown67;
	}

	public int getUnknown68() {
		return unknown68;
	}

	public void setUnknown68(int unknown68) {
		this.unknown68 = unknown68;
	}

	public int getUnknown69() {
		return unknown69;
	}

	public void setUnknown69(int unknown69) {
		this.unknown69 = unknown69;
	}

	public int getUnknown70() {
		return unknown70;
	}

	public void setUnknown70(int unknown70) {
		this.unknown70 = unknown70;
	}

	public int getUnknown71() {
		return unknown71;
	}

	public void setUnknown71(int unknown71) {
		this.unknown71 = unknown71;
	}

	public int getUnknown72() {
		return unknown72;
	}

	public void setUnknown72(int unknown72) {
		this.unknown72 = unknown72;
	}

	public int getUnknown73() {
		return unknown73;
	}

	public void setUnknown73(int unknown73) {
		this.unknown73 = unknown73;
	}

	public int getUnknown74() {
		return unknown74;
	}

	public void setUnknown74(int unknown74) {
		this.unknown74 = unknown74;
	}

	public int getUnknown75() {
		return unknown75;
	}

	public void setUnknown75(int unknown75) {
		this.unknown75 = unknown75;
	}

	public int getUnknown76() {
		return unknown76;
	}

	public void setUnknown76(int unknown76) {
		this.unknown76 = unknown76;
	}

	public int getUnknown77() {
		return unknown77;
	}

	public void setUnknown77(int unknown77) {
		this.unknown77 = unknown77;
	}

	public int getUnknown78() {
		return unknown78;
	}

	public void setUnknown78(int unknown78) {
		this.unknown78 = unknown78;
	}

	public int getUnknown79() {
		return unknown79;
	}

	public void setUnknown79(int unknown79) {
		this.unknown79 = unknown79;
	}

	public int getTargetType() {
		return targetType;
	}

	public void setTargetType(int targetType) {
		this.targetType = targetType;
	}

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}



}
