package engine.net.client.handlers;

import engine.Enum;
import engine.Enum.BuildingGroup;
import engine.Enum.DispatchChannel;
import engine.Enum.ItemType;
import engine.InterestManagement.RealmMap;
import engine.InterestManagement.WorldGrid;
import engine.exception.MsgSendException;
import engine.gameManager.*;
import engine.math.Bounds;
import engine.math.Vector3fImmutable;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.*;
import engine.objects.*;
import engine.powers.PowersBase;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
 * @Author:
 * @Summary: Processes application protocol message which actives
 * items such as charters and deeds in the character's inventory
 */
public class ObjectActionMsgHandler extends AbstractClientMsgHandler {

	// Reentrant lock for dropping banes

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public ObjectActionMsgHandler() {
		super(ObjectActionMsg.class);
	}

	@Override
	protected boolean _handleNetMsg(ClientNetMsg baseMsg, ClientConnection origin) throws MsgSendException {

		// Member variable declaration
		ObjectActionMsg msg;
		PlayerCharacter player;
		CharacterItemManager itemMan;
		ArrayList<Long> comps;
		Dispatch dispatch;
		boolean waterbucketBypass = false;

		// Member variable assignment
		msg = (ObjectActionMsg) baseMsg;
		player = SessionManager.getPlayerCharacter(origin);

		if (player == null) {
			return true;
		}

		itemMan = player.getCharItemManager();

		if (itemMan == null) {
			return true;
		}

		comps = msg.getTargetCompID();

		if (comps.isEmpty()) {
			return true;
		}

		long comp = comps.get(0);

		if (((int) comp) != 0) {
			Item item = Item.getFromCache((int) comp);

			if (item == null) {
				return true;
			}

			//dupe check
			if (!item.validForInventory(origin, player, itemMan)) {
				return true;
			}

			ItemBase ib = item.getItemBase();

			if (ib == null) {
				return true;
			}

			if (itemMan.doesCharOwnThisItem(item.getObjectUUID())) {

				if (ib.isConsumable() || ib.getType() == ItemType.FARMABLE) {

					int uuid = ib.getUUID();
					int type = ib.getType().getValue();

					switch (type) {
					case 27: //Mithril repair
						break;
					case 10: //charters
						//don't think they're handled here?
						break;
					case 19: //buildings
						//Call add building screen here, ib.getUseID() get's building ID

						//if inside player city, center loc on tol. otherwise center on player.
						Vector3fImmutable loc = player.getLoc();
						Zone zone = ZoneManager.findSmallestZone(player.getLoc());

						if (zone != null) {
							if (zone.isPlayerCity()) {
								loc = zone.getLoc();
							}
						}

						PlaceAssetMsg pam = new PlaceAssetMsg();
						pam.setActionType(2);
						pam.setContractID(item.getObjectUUID());
						pam.setX(loc.getX() + 64); //offset grid from tol
						pam.setY(loc.getY());
						pam.setZ(loc.getZ() + 64); //offset grid from tol
						pam.addPlacementInfo(ib.getUseID());

						dispatch = Dispatch.borrow(player, pam);
						DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

						//itemMan.consume(item); //temporary fix for dupe.. TODO Make Item Unusable after This message is sent.
						break;
					case 25: //furniture
						//Call add furniture screen here. ib.getUseID() get's furniture ID
						break;
					case 33:
						long shrineCompID = comps.get(1);
						Building shrineBuilding = BuildingManager.getBuilding((int)shrineCompID);
						if (shrineBuilding == null) {
							return true;
						}
						if (shrineBuilding.getBlueprint() != null && shrineBuilding.getBlueprint().getBuildingGroup() != engine.Enum.BuildingGroup.SHRINE) {
							return true;
						}

						if (shrineBuilding.getRank() == -1) {
							return true;
						}
						Shrine shrine = Shrine.shrinesByBuildingUUID.get(shrineBuilding.getObjectUUID());

						if (shrine == null) {
							return true;
						}

						if (shrine.addFavor(player, item)) {
							shrineBuilding.addEffectBit(1000000 << 2);
							shrineBuilding.updateEffects();
							shrineBuilding.removeEffectBit(1000000 << 2);
						}
						break;

					case 35:
						int charterType = 0;
						switch (uuid) {
						case 910020:
							charterType = 762228431;
							break;
						case 910021:
							charterType = -15978914;
							break;
						case 910022:
							charterType = -600065291;
							break;
						}
						if (claimRealm(player, charterType) == true) {
							itemMan.consume(item);
						}
						break;
					case 7: //rod of command
						long compID = comps.get(1);

						int objectType = AbstractWorldObject.extractTypeID(compID).ordinal();
						Mob toCommand;
						if (objectType == engine.Enum.GameObjectType.Mob.ordinal()) {
							toCommand = Mob.getFromCache((int)compID);
						} //Only Command Mob Types.
						else {
							return true;
						}

						if (toCommand == null) {
							return true;
						}

						if (!toCommand.isSiege())
							return true;

						if (player.commandSiegeMinion(toCommand)) {
							itemMan.consume(item);
						}
						break;
						//ANNIVERSERY GIFT
					case 31:


						if (ib.getUUID() == 971012){
							int random = ThreadLocalRandom.current().nextInt(ItemBase.AnniverseryGifts.size());
							int annyID = ItemBase.AnniverseryGifts.get(random);

							ItemBase annyIB = ItemBase.getItemBase(annyID);
							if (annyIB != null){
								Item gift = MobLoot.createItemForPlayer(player, annyIB);
								if (gift != null){
									itemMan.addItemToInventory(gift);
									itemMan.consume(item);
								}
							}
							break;
						}

						LootTable.CreateGamblerItem(item, player);


						break;

					case 30: //water bucket
					case 8: //potions, tears of saedron

					case 5: //runes, petition, warrant, scrolls
						if (uuid > 3000 && uuid < 3050) { //Discipline Runes
							if (ApplyRuneMsg.applyRune(uuid, origin, player)) {
								itemMan.consume(item);
							}
							break;
						} else if (uuid > 249999 && uuid < 250123) { //stat and mastery runes
							if (ApplyRuneMsg.applyRune(uuid, origin, player)) {
								itemMan.consume(item);
							}
							break;
						} else if (uuid > 250114 && uuid < 250123) { //mastery runes
							if (ApplyRuneMsg.applyRune(uuid, origin, player)) {
								itemMan.consume(item);
							}
							break;
						} else if (uuid > 252122 && uuid < 252128) { //mastery runes
							if (ApplyRuneMsg.applyRune(uuid, origin, player)) {
								itemMan.consume(item);
							}
							break;
						} else if (uuid > 680069 && uuid < 680074) //Handle Charter, Deed, Petition, Warrant here
						{
							break;
						} else if (uuid > 910010 && uuid < 910019) {

							int rank = uuid - 910010;

							if (rank < 1 || rank > 8) {
								ChatManager.chatSystemError(player, "Invalid Rank for bane scroll!");
								return true;
							}
							// Only one banestone at a time
							lock.writeLock().lock();

							try {
								if (Bane.summonBanestone(player, origin, rank) == true)
									itemMan.consume(item);
							} finally {
								lock.writeLock().unlock();
							}
							break;
						} else if (uuid == 910010) { //tears of saedron
							if (comps.size() > 1) {
								removeRune(player, origin, comps.get(1).intValue());
							}
							break;
						}

						else if (item.getChargesRemaining() > 0) {
							ArrayList<Long> tarList = msg.getTargetCompID();
							AbstractWorldObject target = player;
							if (tarList.size() > 1) {
								long tarID = tarList.get(1);
								if (tarID != 0) {
									AbstractGameObject tarAgo = AbstractGameObject.getFromTypeAndID(tarID);
									if (tarAgo != null && tarAgo instanceof AbstractWorldObject) {
										target = (AbstractWorldObject) tarAgo;
									}
								}
							}

							// Bypass for waterbuckets

							// test character targeted

							if (ib.getUUID() == 910005) {

								// test for valid target type
								if (target.getObjectType() == Enum.GameObjectType.PlayerCharacter)
									waterbucketBypass = true;
								else {
									// test distance to structure
									Building targetBuilding = (Building) target;
									Bounds testBounds = Bounds.borrow();
									testBounds.setBounds(player.getLoc(), 25);

									if (Bounds.collide(targetBuilding.getBounds(), testBounds, .1f) == false) {
										ChatManager.chatSystemError(player, "Not in range of structura for to heal!");
										return true;
									}
								}

								// Send piss bucket animation

								VisualUpdateMessage vum = new VisualUpdateMessage(player, 16323);
								vum.configure();
								DispatchMessage.sendToAllInRange(player, vum);
							}

							if (waterbucketBypass == false)
								PowersManager.applyPower(player, target, Vector3fImmutable.ZERO, ib.getUseID(), ib.getUseAmount(), true);

							itemMan.consume(item);
						} else //just remove the item at this point
							itemMan.consume(item);

						dispatch = Dispatch.borrow(player, msg);
						DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
						player.cancelOnSpell();
						break;
					default: //shouldn't be here, consume item
						dispatch = Dispatch.borrow(player, msg);
						DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);
						// itemMan.consume(item);
					}
				}
			} else {
				// TODO log item does not belong to player
				// System.out.println("Item does not belong to player");
				// Cleanup duped item here
			}
		}

		return true;
	}

	private static boolean claimRealm(PlayerCharacter player, int charterUUID) {

		Guild guild;
		Realm realm;
		City city;
		Building tol;
		float hPMod;
		Warehouse warehouse;
		boolean hasResources = true;
		int resourceValue;

		if (GuildStatusController.isGuildLeader(player.getGuildStatus()) == false) {
			ErrorPopupMsg.sendErrorPopup(player, 176); // Only guild leaders can claim a territory
			return false;
		}

		guild = player.getGuild();
		city = guild.getOwnedCity();

		if (city == null) {
			ErrorPopupMsg.sendErrorPopup(player, 179); // Only landed guilds may claim a territory
			return false;
		}

		if (city.isLocationOnCityGrid(player.getLoc()) == false) {
			ErrorPopupMsg.sendErrorPopup(player, 186); // Your tree is not inside a territory!
			return false;
		}

		tol = city.getTOL();

		if (tol.getRank() != 7) {
			ErrorPopupMsg.sendErrorPopup(player, 181); // Your tree must be rank 7 before claiming a territory
			return false;
		}

		realm = RealmMap.getRealmForCity(city);

		if (realm.getCanBeClaimed() == false) {
			ErrorPopupMsg.sendErrorPopup(player, 180); // This territory cannot be ruled by anyone
			return false;
		}

		if (realm.isRuled() == true) {
			ErrorPopupMsg.sendErrorPopup(player, 178); // This territory is already claimed
			return false;
		}

		if (!Realm.HasAllBlessings(player)) {
			ErrorPopupMsg.sendErrorPopup(player, 185); // You must seek the blessing of the three sages before you can rule
			return false;
		}

		// Must have the required resources in warehouse to claim realm

		warehouse = city.getWarehouse();

		if (warehouse == null) {
			ErrorPopupMsg.sendErrorPopup(player, 188);  // You must have a warehouse to become a capital
			return false;
		}

		resourceValue = warehouse.getResources().get(Warehouse.goldIB);

		if (resourceValue < 5000000)
			hasResources = false;

		resourceValue = warehouse.getResources().get(Warehouse.stoneIB);

		if (resourceValue < 8000)
			hasResources = false;

		resourceValue = warehouse.getResources().get(Warehouse.lumberIB);

		if (resourceValue < 8000)
			hasResources = false;

		resourceValue = warehouse.getResources().get(Warehouse.galvorIB);

		if (resourceValue < 15)
			hasResources = false;

		resourceValue = warehouse.getResources().get(Warehouse.wormwoodIB);

		if (resourceValue < 15)
			hasResources = false;

		if (hasResources == false) {
			ErrorPopupMsg.sendErrorPopup(player, 184);  // Insufficient gold or resources to upgrade to capital
			return false;
		}

		// Remove resources from warehouse before claiming realm

		resourceValue = warehouse.getResources().get(Warehouse.goldIB);

		if (DbManager.WarehouseQueries.updateGold(warehouse, resourceValue - 5000000) == true) {
			warehouse.getResources().put(Warehouse.goldIB, resourceValue - 5000000);
			warehouse.AddTransactionToWarehouse(engine.Enum.GameObjectType.Building, tol.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.GOLD, 5000000);
		} else {
			Logger.error("gold update failed for warehouse of UUID:" + warehouse.getObjectUUID());
			return false;
		}

		resourceValue = warehouse.getResources().get(Warehouse.stoneIB);

		if (DbManager.WarehouseQueries.updateStone(warehouse, resourceValue - 8000) == true) {
			warehouse.getResources().put(Warehouse.stoneIB, resourceValue - 8000);
			warehouse.AddTransactionToWarehouse(engine.Enum.GameObjectType.Building, tol.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.STONE, 8000);
		} else {
			Logger.error( "stone update failed for warehouse of UUID:" + warehouse.getObjectUUID());
			return false;
		}

		resourceValue = warehouse.getResources().get(Warehouse.lumberIB);

		if (DbManager.WarehouseQueries.updateLumber(warehouse, resourceValue - 8000) == true) {
			warehouse.getResources().put(Warehouse.lumberIB, resourceValue - 8000);
			warehouse.AddTransactionToWarehouse(engine.Enum.GameObjectType.Building, tol.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.LUMBER, 8000);
		} else {
			Logger.error("lumber update failed for warehouse of UUID:" + warehouse.getObjectUUID());
			return false;
		}

		resourceValue = warehouse.getResources().get(Warehouse.galvorIB);

		if (DbManager.WarehouseQueries.updateGalvor(warehouse, resourceValue - 15) == true) {
			warehouse.getResources().put(Warehouse.galvorIB, resourceValue - 15);
			warehouse.AddTransactionToWarehouse(engine.Enum.GameObjectType.Building, tol.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.GALVOR, 15);
		} else {
			Logger.error("galvor update failed for warehouse of UUID:" + warehouse.getObjectUUID());
			return false;
		}

		resourceValue = warehouse.getResources().get(Warehouse.wormwoodIB);

		if (DbManager.WarehouseQueries.updateWormwood(warehouse, resourceValue - 15) == true) {
			warehouse.getResources().put(Warehouse.wormwoodIB, resourceValue - 15);
			warehouse.AddTransactionToWarehouse(engine.Enum.GameObjectType.Building, tol.getObjectUUID(), Enum.TransactionType.WITHDRAWL, Resource.WORMWOOD, 15);
		} else {
			Logger.error("wormwood update failed for warehouse of UUID:" + warehouse.getObjectUUID());
			return false;
		}

		realm.claimRealmForCity(city, charterUUID);

		tol.setRank(8);
		WorldGrid.updateObject(tol);

		for (Building building : city.getParent().zoneBuildingSet) {

			if (building.getBlueprintUUID() != 0) {

				// TOL Health set through regular linear equation
				if (building.getBlueprint().getBuildingGroup() == BuildingGroup.TOL) {
					continue;
				}

				hPMod = (building.getMaxHitPoints() * Realm.getRealmHealthMod(city));
				building.setMaxHitPoints(building.getMaxHitPoints() + hPMod);
			}
		}

		if (!guild.getNation().equals(guild)) {
			guild.getNation().setRealmsOwned(guild.getNation().getRealmsOwned() + 1);
			GuildManager.updateAllGuildTags(guild.getNation());
		}

		guild.setRealmsOwned(guild.getRealmsOwned() + 1);
		GuildManager.updateAllGuildTags(guild);

		removeAllBlessings(player);

		return true;

	}

	private static void removeAllBlessings(PlayerCharacter player) {

		PowersBase[] powers = new PowersBase[3];

		powers[0] = PowersManager.getPowerByIDString("BLS-POWER");
		powers[1] = PowersManager.getPowerByIDString("BLS-FORTUNE");
		powers[2] = PowersManager.getPowerByIDString("BLS-WISDOM");

		for (PowersBase power : powers) {
			PowersManager.removeEffect(player, power.getActions().get(0), true, false);
		}

	}
	// Handle activation of tears of seadron: Removes rune from player.

	private static void removeRune(PlayerCharacter pc, ClientConnection origin, int runeID) {

		if (pc == null || origin == null) {
			return;
		}

		//remove only if rune is discipline
		if (runeID < 3001 || runeID > 3048) {
			return;
		}

		//see if pc has rune
		ArrayList<CharacterRune> runes = pc.getRunes();

		if (runes == null)
			return;

		CharacterRune found = pc.getRune(runeID);

		if (found == null)
			return;

		//TODO see if player needs to refine skills or powers first
		//attempt remove rune from player

		if (!CharacterRune.removeRune(pc, runeID))
			return;

		//update client with removed rune.
		ApplyRuneMsg arm = new ApplyRuneMsg(pc.getObjectType().ordinal(), pc.getObjectUUID(), runeID);
		Dispatch dispatch = Dispatch.borrow(pc, arm);
		DispatchMessage.dispatchMsgDispatch(dispatch, DispatchChannel.SECONDARY);
	}

}
