package engine.ai;
import engine.Enum;
import engine.InterestManagement.HeightMap;
import engine.InterestManagement.WorldGrid;
import engine.exception.SerializationException;
import engine.gameManager.*;
import engine.job.JobContainer;
import engine.job.JobScheduler;
import engine.jobs.UpgradeNPCJob;
import engine.math.Vector3fImmutable;
import engine.net.ByteBufferWriter;
import engine.net.Dispatch;
import engine.net.DispatchMessage;
import engine.net.client.ClientConnection;
import engine.net.client.msg.ErrorPopupMsg;
import engine.net.client.msg.ManageCityAssetsMsg;
import engine.net.client.msg.PetMsg;
import engine.net.client.msg.PlaceAssetMsg;
import engine.net.client.msg.chat.ChatSystemMsg;
import engine.objects.*;
import engine.powers.EffectsBase;
import engine.server.MBServerStatics;
import org.joda.time.DateTime;
import org.pmw.tinylog.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import static engine.net.client.msg.ErrorPopupMsg.sendErrorPopup;

public class StaticMobActions {
    public static void dismissNecroPet(Mob mob, boolean updateOwner) {

        mob.state = MobileFSM.STATE.Disabled;

        mob.combatTarget = null;
        mob.hasLoot = false;

        if (mob.parentZone != null)
            mob.parentZone.zoneMobSet.remove(mob);

        try {
            mob.clearEffects();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        mob.playerAgroMap.clear();
        WorldGrid.RemoveWorldObject(mob);

        DbManager.removeFromCache(mob);

        // YEAH BONUS CODE!  THANKS UNNAMED ASSHOLE!
        //WorldServer.removeObject(this);
        //WorldGrid.INSTANCE.removeWorldObject(this);
        //owner.getPet().disableIntelligence();

        PlayerCharacter petOwner = mob.getOwner();

        if (petOwner != null) {
            setOwner(mob,null);
            petOwner.setPet(null);

            if (!updateOwner)
                return;
            PetMsg petMsg = new PetMsg(5, null);
            Dispatch dispatch = Dispatch.borrow(petOwner, petMsg);
            DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
        }
    }
    public static void dismiss(Mob mob) {

        if (mob.isPet()) {

            if (mob.isSummonedPet()) { //delete summoned pet

                WorldGrid.RemoveWorldObject(mob);
                DbManager.removeFromCache(mob);
                if (mob.getObjectType() == Enum.GameObjectType.Mob) {
                    mob.state = MobileFSM.STATE.Disabled;
                    if ((mob).parentZone != null)
                        (mob).parentZone.zoneMobSet.remove(mob);
                }

            } else { //revert charmed pet
                mob.setMob();
                mob.setCombatTarget(null);
            }
            //clear owner
            PlayerCharacter owner = mob.getOwner();

            //close pet window
            if (owner != null) {
                Mob pet = owner.getPet();
                PetMsg pm = new PetMsg(5, null);
                Dispatch dispatch = Dispatch.borrow(owner, pm);
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.SECONDARY);

                if (pet != null && pet.getObjectUUID() == mob.getObjectUUID())
                    owner.setPet(null);

                if (mob.getObjectType().equals(Enum.GameObjectType.Mob))
                    setOwner(mob,null);
            }


        }
    }
    public static void processRedeedMob(Mob mob, ClientConnection origin) {

        // Member variable declaration
        PlayerCharacter player;
        Contract contract;
        CharacterItemManager itemMan;
        ItemBase itemBase;
        Item item;

        mob.lock.writeLock().lock();

        try {

            player = SessionManager.getPlayerCharacter(origin);
            itemMan = player.getCharItemManager();


            contract = mob.contract;

            if (!player.getCharItemManager().hasRoomInventory((short) 1)) {
                ErrorPopupMsg.sendErrorPopup(player, 21);
                return;
            }


            if (!mob.building.getHirelings().containsKey(mob))
                return;

            if (!StaticMobActions.remove(mob, mob.building)) {
                PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
                return;
            }

            mob.building.getHirelings().remove(mob);

            itemBase = ItemBase.getItemBase(contract.getContractID());

            if (itemBase == null) {
                Logger.error("Could not find Contract for npc: " + mob.getObjectUUID());
                return;
            }

            boolean itemWorked = false;

            item = new Item(itemBase, player.getObjectUUID(), Enum.OwnerType.PlayerCharacter, (byte) ((byte) mob.getRank() - 1), (byte) ((byte) mob.getRank() - 1),
                    (short) 1, (short) 1, true, false, Enum.ItemContainerType.INVENTORY, (byte) 0,
                    new ArrayList<>(), "");
            item.setNumOfItems(1);
            item.containerType = Enum.ItemContainerType.INVENTORY;

            try {
                item = DbManager.ItemQueries.ADD_ITEM(item);
                itemWorked = true;
            } catch (Exception e) {
                Logger.error(e);
            }
            if (itemWorked) {
                itemMan.addItemToInventory(item);
                itemMan.updateInventory();
            }

            ManageCityAssetsMsg mca = new ManageCityAssetsMsg();
            mca.actionType = NPC.SVR_CLOSE_WINDOW;
            mca.setTargetType(mob.building.getObjectType().ordinal());
            mca.setTargetID(mob.building.getObjectUUID());
            origin.sendMsg(mca);


        } catch (Exception e) {
            Logger.error(e);
        } finally {
            mob.lock.writeLock().unlock();
        }

    }
    public static void processUpgradeMob(Mob mob, PlayerCharacter player) {

        mob.lock.writeLock().lock();

        try {

            mob.building = mob.building;

            // Cannot upgrade an npc not within a building

            if (mob.building == null)
                return;

            // Cannot upgrade an npc at max rank

            if (mob.getRank() == 7)
                return;

            // Cannot upgrade an npc who is currently ranking

            if (mob.upgradeDateTime != null)
                return;

            int rankCost = getUpgradeCost(mob);

            // SEND NOT ENOUGH GOLD ERROR

            if (rankCost > mob.building.getStrongboxValue()) {
                sendErrorPopup(player, 127);
                return;
            }

            try {

                if (!mob.building.transferGold(-rankCost, false)) {
                    return;
                }

                DateTime dateToUpgrade = DateTime.now().plusHours(getUpgradeTime(mob));
                setUpgradeDateTime(mob, dateToUpgrade);

                // Schedule upgrade job

                submitUpgradeJob(mob);

            } catch (Exception e) {
                PlaceAssetMsg.sendPlaceAssetError(player.getClientConnection(), 1, "A Serious error has occurred. Please post details for to ensure transaction integrity");
            }

        } catch (Exception e) {
            Logger.error(e);
        } finally {
            mob.lock.writeLock().unlock();
        }
    }
    public static DateTime getUpgradeDateTime(Mob mob) {

        mob.lock.readLock().lock();

        try {
            return mob.upgradeDateTime;
        } finally {
            mob.lock.readLock().unlock();
        }
    }
    public static void submitUpgradeJob(Mob mob) {

        JobContainer jc;

        if (getUpgradeDateTime(mob) == null) {
            Logger.error("Failed to get Upgrade Date");
            return;
        }

        // Submit upgrade job for future date or current instant

        if (getUpgradeDateTime(mob).isAfter(DateTime.now()))
            jc = JobScheduler.getInstance().scheduleJob(new UpgradeNPCJob(mob),
                    getUpgradeDateTime(mob).getMillis());
        else
            JobScheduler.getInstance().scheduleJob(new UpgradeNPCJob(mob), 0);

    }
    public static int getUpgradeTime(Mob mob) {

        if (mob.getRank() < 7)
            return (mob.getRank() * 8);

        return 0;
    }
    public static int getUpgradeCost(Mob mob) {

        int upgradeCost;

        upgradeCost = Integer.MAX_VALUE;

        if (mob.getRank() < 7)
            return (mob.getRank() * 100650) + 21450;

        return upgradeCost;

    }
    public static void __serializeForClientMsg(Mob mob, ByteBufferWriter writer) throws SerializationException {}
    public static void serializeMobForClientMsgOtherPlayer(Mob mob,ByteBufferWriter writer, boolean hideAsciiLastName) throws SerializationException {
        StaticMobActions.serializeForClientMsgOtherPlayer(mob,writer);
    }
    public static void serializeForClientMsgOtherPlayer(Mob mob, ByteBufferWriter writer)throws SerializationException {
        writer.putInt(0);
        writer.putInt(0);

        int tid = (mob.mobBase != null) ? mob.mobBase.getLoadID() : 0;
        int classID = MobBase.GetClassType(mob.mobBase.getObjectUUID());
        if (mob.isPet()) {
            writer.putInt(2);
            writer.putInt(3);
            writer.putInt(0);
            writer.putInt(2522);
            writer.putInt(Enum.GameObjectType.NPCClassRune.ordinal());
            writer.putInt(mob.currentID);
        } else if (tid == 100570) { //kur'adar
            writer.putInt(3);
            serializeRune(mob,writer, 3, Enum.GameObjectType.NPCClassRuneTwo.ordinal(), 2518); //warrior class
            serializeRune(mob,writer, 5, Enum.GameObjectType.NPCClassRuneThree.ordinal(), 252621); //guard rune
        } else if (tid == 100962 || tid == 100965) { //Spydraxxx the Mighty, Denigo Tantric
            writer.putInt(2);
            serializeRune(mob,writer, 5, Enum.GameObjectType.NPCClassRuneTwo.ordinal(), 252621); //guard rune
        }else if (mob.contract != null || mob.isPlayerGuard){
            writer.putInt(3);
            serializeRune(mob,writer, 3, Enum.GameObjectType.NPCClassRuneTwo.ordinal(),MobBase.GetClassType(mob.mobBase.getObjectUUID())); //warrior class
            serializeRune(mob,writer, 5, Enum.GameObjectType.NPCClassRuneThree.ordinal(), 252621); //guard rune
        }else {

            writer.putInt(1);
        }

        //Generate Race Rune
        writer.putInt(1);
        writer.putInt(0);

        if (mob.mobBase != null)
            writer.putInt(mob.mobBase.getLoadID());
        else
            writer.putInt(mob.loadID);

        writer.putInt(mob.getObjectType().ordinal());
        writer.putInt(mob.currentID);

        //Send Stats
        writer.putInt(5);
        writer.putInt(0x8AC3C0E6); //Str
        writer.putInt(0);
        writer.putInt(0xACB82E33); //Dex
        writer.putInt(0);
        writer.putInt(0xB15DC77E); //Con
        writer.putInt(0);
        writer.putInt(0xE07B3336); //Int
        writer.putInt(0);
        writer.putInt(0xFF665EC3); //Spi
        writer.putInt(0);

        if (!mob.nameOverride.isEmpty()){
            writer.putString(mob.nameOverride);
            writer.putInt(0);
        } else {
            writer.putString(mob.firstName);
            writer.putString(mob.lastName);

        }


        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);

        writer.put((byte) 0);
        writer.putInt(mob.getObjectType().ordinal());
        writer.putInt(mob.currentID);

        if (mob.mobBase != null) {
            writer.putFloat(mob.mobBase.getScale());
            writer.putFloat(mob.mobBase.getScale());
            writer.putFloat(mob.mobBase.getScale());
        } else {
            writer.putFloat(1.0f);
            writer.putFloat(1.0f);
            writer.putFloat(1.0f);
        }

        //Believe this is spawn loc, ignore for now
        writer.putVector3f(mob.getLoc());

        //Rotation
        writer.putFloat(mob.getRot().y);

        //Inventory Stuff
        writer.putInt(0);

        // get a copy of the equipped items.


        if (mob.equip != null){

            writer.putInt(mob.equip.size());

            for (MobEquipment me:mob.equip.values()){
                MobEquipment.serializeForClientMsg(me,writer);
            }
        }else{
            writer.putInt(0);
        }

        writer.putInt(mob.getRank());
        writer.putInt(mob.getLevel());
        writer.putInt(mob.getIsSittingAsInt()); //Standing
        writer.putInt(mob.getIsWalkingAsInt()); //Walking
        writer.putInt(mob.getIsCombatAsInt()); //Combat
        writer.putInt(2); //Unknown
        writer.putInt(1); //Unknown - Headlights?
        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);
        writer.put((byte) 0);
        writer.put((byte) 0);
        writer.put((byte) 0);
        writer.putInt(0);

        if (mob.contract != null && mob.npcOwner == null){
            writer.put((byte) 1);
            writer.putLong(0);
            writer.putLong(0);

            if (mob.contract != null)
                writer.putInt(mob.contract.getIconID());
            else
                writer.putInt(0); //npc icon ID

        } else
            writer.put((byte)0);


        if (mob.npcOwner != null){
            writer.put((byte) 1);
            writer.putInt(Enum.GameObjectType.PlayerCharacter.ordinal());
            writer.putInt(131117009);
            writer.putInt(mob.npcOwner.getObjectType().ordinal());
            writer.putInt(mob.npcOwner.getObjectUUID());
            writer.putInt(8);
        }else
            writer.put((byte)0);

        if (mob.isPet()) {

            writer.put((byte) 1);

            if (mob.getOwner() != null) {
                writer.putInt(mob.getOwner().getObjectType().ordinal());
                writer.putInt(mob.getOwner().getObjectUUID());
            } else {
                writer.putInt(0); //ownerType
                writer.putInt(0); //ownerID
            }
        } else {
            writer.put((byte) 0);
        }
        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);

        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);
        writer.putInt(0);

        if (!mob.isAlive() && !mob.isPet() && !mob.mobBase.isNecroPet() && !mob.isSiege && !mob.isPlayerGuard) {
            writer.putInt(0);
            writer.putInt(0);
        }

        writer.put((byte) 0);
        Guild._serializeForClientMsg(mob.getGuild(),writer);
        //		writer.putInt(0);
        //		writer.putInt(0);
        if (mob.mobBase != null && mob.mobBase.getObjectUUID() == 100570) {
            writer.putInt(2);
            writer.putInt(0x00008A2E);
            writer.putInt(0x1AB84003);
        } else if (mob.isSiege) {
            writer.putInt(1);
            writer.putInt(74620179);
        } else
            writer.putInt(0);

        //		writer.putInt(1);
        //		writer.putInt(0); //0xAC13C5E9 - alternate textures
        writer.putInt(0); //0xB8400300
        writer.putInt(0);

        //TODO Guard
        writer.put((byte) 0);
        //		writer.put((byte)0); //Is guard..

        writer.putFloat(mob.healthMax);
        writer.putFloat(mob.health.get());

        //TODO Peace Zone
        writer.put((byte) 1); //0=show tags, 1=don't

        //DON't LOAD EFFECTS FOR DEAD MOBS.

        if (!mob.isAlive())
            writer.putInt(0);
        else{
            int	indexPosition = writer.position();
            writer.putInt(0); //placeholder for item cnt
            int total = 0;

            //	Logger.info("",""+ mob.getEffects().size());
            for (Effect eff : mob.getEffects().values()) {
                if (eff.isStatic())
                    continue;
                if ( !eff.serializeForLoad(writer))
                    continue;
                ++total;
            }

            writer.putIntAt(total, indexPosition);
        }

        //        // Effects
        writer.put((byte) 0);
    }
    public static int randomGoldAmount( Mob mob) {

        // percentage chance to drop gold

        //R8 mobs have 100% gold drop.
        if (mob.getLevel() < 80)
            if ((ThreadLocalRandom.current().nextDouble() * 100d) > MBServerStatics.GOLD_DROP_PERCENTAGE_CHANCE)
                return 0;


        int level = (int) mob.getLevel();
        level = (level < 0) ? 0 : level;
        level = (level > 50) ? 50 : level;

        double minGold;
        double maxGold;

        if (mob.mobBase != null) {
            minGold = mob.mobBase.getMinGold();
            maxGold = mob.mobBase.getMaxGold();
        } else {
            minGold = MBServerStatics.GOLD_DROP_MINIMUM_PER_MOB_LEVEL[level];
            maxGold = MBServerStatics.GOLD_DROP_MAXIMUM_PER_MOB_LEVEL[level];
        }

        double gold = (ThreadLocalRandom.current().nextDouble() * (maxGold - minGold) + minGold);


        //server specific gold multiplier
        double goldMod = MBServerStatics.GOLD_RATE_MOD;
        gold *= goldMod;

        //modify for hotzone

        if (ZoneManager.inHotZone(mob.getLoc()))
            gold *= MBServerStatics.HOT_GOLD_RATE_MOD;

        gold *= MBServerStatics.GOLD_RATE_MOD;

        return (int) gold;
    }
    public static int nextStaticID() {
        int id = Mob.staticID;
        Mob.staticID++;
        return id;
    }
    public static Mob getMob(int id) {

        if (id == 0)
            return null;

        Mob mob  = (Mob) DbManager.getFromCache(Enum.GameObjectType.Mob, id);
        if (mob != null)
            return mob;
        return DbManager.MobQueries.GET_MOB(id);
    }
    public static Mob getFromCache(int id) {


        return (Mob) DbManager.getFromCache(Enum.GameObjectType.Mob, id);
    }
    public static Mob getFromCacheDBID(int id) {
        if (Mob.mobMapByDBID.containsKey(id)) {
            return Mob.mobMapByDBID.get(id);
        }
        return null;
    }
    public static int getBuildingSlot(Mob mob){
        int slot = -1;

        if (mob.building == null)
            return -1;



        BuildingModelBase buildingModel = BuildingModelBase.getModelBase(mob.building.getMeshUUID());

        if (buildingModel == null)
            return -1;


        if (mob.building.getHirelings().containsKey(mob))
            slot =  (mob.building.getHirelings().get(mob));


        if (buildingModel.getNPCLocation(slot) == null)
            return -1;


        return slot;
    }
    public static void HandleAssistedAggro(PlayerCharacter source, PlayerCharacter target) {

        HashSet<AbstractWorldObject> mobsInRange = WorldGrid.getObjectsInRangePartial(source, MBServerStatics.AI_DROP_AGGRO_RANGE, MBServerStatics.MASK_MOB);

        for (AbstractWorldObject awo : mobsInRange) {
            Mob mob = (Mob) awo;

            //Mob is not attacking anyone, skip.
            if (mob.getCombatTarget() == null)
                continue;

            //Mob not attacking target's target, let's not be failmu and skip this target.
            if (mob.getCombatTarget() != target)
                continue;

            //target is mob's combat target, LETS GO.
            if (source.getHateValue() > target.getHateValue()) {
                mob.setCombatTarget(source);
                MobileFSM.setAggro(mob, source.getObjectUUID());
            }
        }
    }
    public static void setUpgradeDateTime(Mob mob,DateTime upgradeDateTime) {

        if (!DbManager.MobQueries.updateUpgradeTime(mob, upgradeDateTime)){
            Logger.error("Failed to set upgradeTime for building " + mob.currentID);
            return;
        }
        mob.upgradeDateTime = upgradeDateTime;
    }
    public static Vector3fImmutable GetSpawnRadiusLocation(Mob mob){

        Vector3fImmutable returnLoc = Vector3fImmutable.ZERO;

        if (mob.fidalityID != 0 && mob.building != null){


            Vector3fImmutable spawnRadiusLoc = Vector3fImmutable.getRandomPointInCircle(mob.localLoc, mob.spawnRadius);

            Vector3fImmutable buildingWorldLoc = ZoneManager.convertLocalToWorld(mob.building, spawnRadiusLoc);

            return buildingWorldLoc;



        }else{

            boolean run = true;

            while(run){
                Vector3fImmutable localLoc = new Vector3fImmutable(mob.statLat + mob.parentZone.absX, mob.statAlt + mob.parentZone.absY, mob.statLon + mob.parentZone.absZ);
                Vector3fImmutable spawnRadiusLoc = Vector3fImmutable.getRandomPointInCircle(localLoc, mob.spawnRadius);

                //not a roaming mob, just return the random loc.
                if (mob.spawnRadius < 12000)
                    return spawnRadiusLoc;

                Zone spawnZone = ZoneManager.findSmallestZone(spawnRadiusLoc);
                //dont spawn roaming mobs in npc cities
                if (spawnZone.isNPCCity())
                    continue;

                //dont spawn roaming mobs in player cities.
                if (spawnZone.isPlayerCity())
                    continue;

                //don't spawn mobs in water.
                if (HeightMap.isLocUnderwater(spawnRadiusLoc))
                    continue;

                run = false;

                return spawnRadiusLoc;

            }

        }

        //shouldn't ever get here.

        return returnLoc;
    }
    public static void serializeRune(Mob mob, ByteBufferWriter writer, int type, int objectType, int runeID) {
        writer.putInt(type);
        writer.putInt(0);
        writer.putInt(runeID);
        writer.putInt(objectType);
        writer.putInt(mob.currentID);
    }
    public static void removeMinions(Mob mob) {

        for (Mob toRemove : mob.siegeMinionMap.keySet()) {

            toRemove.state = MobileFSM.STATE.Disabled;

            if (mob.isMoving()){

                mob.stopMovement(mob.getLoc());
                mob.state = MobileFSM.STATE.Disabled;

                if (toRemove.parentZone != null)
                    toRemove.parentZone.zoneMobSet.remove(toRemove);
            }

            try {
                toRemove.clearEffects();
            } catch(Exception e){
                Logger.error(e.getMessage());
            }

            if (toRemove.parentZone != null)
                toRemove.parentZone.zoneMobSet.remove(toRemove);

            WorldGrid.RemoveWorldObject(toRemove);
            WorldGrid.removeObject(toRemove);
            DbManager.removeFromCache(toRemove);

            PlayerCharacter petOwner = toRemove.getOwner();

            if (petOwner != null) {

                petOwner.setPet(null);
                setOwner(toRemove,null);

                PetMsg petMsg = new PetMsg(5, null);
                Dispatch dispatch = Dispatch.borrow(petOwner, petMsg);
                DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
            }
        }
    }
    public static void refresh(Mob mob) {
        if (mob.isAlive())
            WorldGrid.updateObject(mob);
    }
    public static void recalculateStats(Mob mob) {

        try {
            calculateModifiedStats(mob);
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }

        try {
            calculateAtrDefenseDamage(mob);
        } catch (Exception e) {
            Logger.error( mob.mobBase + " /" + e.getMessage());
        }
        try {
            calculateMaxHealthManaStamina(mob);
        } catch (Exception e) {
            Logger.error( e.getMessage());
        }

        Resists.calculateResists(mob);
    }
    public static void calculateMaxHealthManaStamina(Mob mob) {
        float h = 1f;
        float m = 0f;
        float s = 0f;

        h = mob.mobBase.getHealthMax();
        m = mob.statSpiCurrent;
        s = mob.statConCurrent;

        // Apply any bonuses from runes and effects
        if (mob.bonuses != null) {
            h += mob.bonuses.getFloat(Enum.ModType.HealthFull, Enum.SourceType.None);
            m += mob.bonuses.getFloat(Enum.ModType.ManaFull, Enum.SourceType.None);
            s += mob.bonuses.getFloat(Enum.ModType.StaminaFull, Enum.SourceType.None);

            //apply effects percent modifiers. DO THIS LAST!
            h *= (1 + mob.bonuses.getFloatPercentAll(Enum.ModType.HealthFull, Enum.SourceType.None));
            m *= (1 + mob.bonuses.getFloatPercentAll(Enum.ModType.ManaFull, Enum.SourceType.None));
            s *= (1 + mob.bonuses.getFloatPercentAll(Enum.ModType.StaminaFull, Enum.SourceType.None));
        }

        // Set max health, mana and stamina
        if (h > 0)
            mob.healthMax = h;
        else
            mob.healthMax = 1;

        if (m > -1)
            mob.manaMax = m;
        else
            mob.manaMax = 0;

        if (s > -1)
            mob.staminaMax = s;
        else
            mob.staminaMax = 0;

        // Update health, mana and stamina if needed
        if (mob.getHealth() > mob.healthMax)
            mob.setHealth(mob.healthMax);

        if (mob.mana.get() > mob.manaMax)
            mob.mana.set(mob.manaMax);

        if (mob.stamina.get() > mob.staminaMax)
            mob.stamina.set(mob.staminaMax);

    }
    public static void calculateAtrDefenseDamage(Mob mob) {

        if (mob.charItemManager == null || mob.equip == null) {
            Logger.error("Player " + mob.currentID + " missing skills or equipment");
            defaultAtrAndDamage(mob, true);
            defaultAtrAndDamage(mob, false);
            mob.defenseRating = 0;
            return;
        }

        try {
            calculateAtrDamageForWeapon(mob,
                    mob.equip.get(MBServerStatics.SLOT_MAINHAND), true, mob.equip.get(MBServerStatics.SLOT_OFFHAND));
        } catch (Exception e) {

            mob.atrHandOne = (short) mob.mobBase.getAttackRating();
            mob.minDamageHandOne = (short) mob.mobBase.getMinDmg();
            mob.maxDamageHandOne = (short) mob.mobBase.getMaxDmg();
            mob.rangeHandOne = 6.5f;
            mob.speedHandOne = 20;
            Logger.info("Mobbase ID " + mob.mobBase.getObjectUUID() + " returned an error. setting to default ATR and Damage." + e.getMessage());
        }

        try {
            calculateAtrDamageForWeapon(mob, mob.equip.get(MBServerStatics.SLOT_OFFHAND), false, mob.equip.get(MBServerStatics.SLOT_MAINHAND));

        } catch (Exception e) {

            mob.atrHandTwo = (short) mob.mobBase.getAttackRating();
            mob.minDamageHandTwo = (short) mob.mobBase.getMinDmg();
            mob.maxDamageHandTwo = (short) mob.mobBase.getMaxDmg();
            mob.rangeHandTwo = 6.5f;
            mob.speedHandTwo = 20;
            Logger.info( "Mobbase ID " + mob.mobBase.getObjectUUID() + " returned an error. setting to default ATR and Damage." + e.getMessage());
        }

        try {
            float defense = mob.mobBase.getDefenseRating();
            defense += getShieldDefense(mob, mob.equip.get(MBServerStatics.SLOT_OFFHAND));
            defense += getArmorDefense(mob, mob.equip.get(MBServerStatics.SLOT_HELMET));
            defense += getArmorDefense(mob, mob.equip.get(MBServerStatics.SLOT_CHEST));
            defense += getArmorDefense(mob, mob.equip.get(MBServerStatics.SLOT_ARMS));
            defense += getArmorDefense(mob, mob.equip.get(MBServerStatics.SLOT_GLOVES));
            defense += getArmorDefense(mob, mob.equip.get(MBServerStatics.SLOT_LEGGINGS));
            defense += getArmorDefense(mob, mob.equip.get(MBServerStatics.SLOT_FEET));
            defense += getWeaponDefense(mob, mob.equip);

            if (mob.bonuses != null) {
                // add any bonuses
                defense += (short) mob.bonuses.getFloat(Enum.ModType.DCV, Enum.SourceType.None);

                // Finally multiply any percent modifiers. DO THIS LAST!
                float pos_Bonus = 1 + mob.bonuses.getFloatPercentPositive(Enum.ModType.DCV, Enum.SourceType.None);



                defense = (short) (defense * pos_Bonus);

                //Lucky rune applies next

                float neg_Bonus = mob.bonuses.getFloatPercentNegative(Enum.ModType.DCV, Enum.SourceType.None);
                defense = (short) (defense *(1 + neg_Bonus));



            } else {
                // TODO add error log here
                Logger.error( "Error: missing bonuses");
            }

            defense = (defense < 1) ? 1 : defense;
            mob.defenseRating = (short) (defense + 0.5f);
        } catch (Exception e) {
            Logger.info("Mobbase ID " + mob.mobBase.getObjectUUID() + " returned an error. Setting to Default Defense." + e.getMessage());
            mob.defenseRating = (short) mob.mobBase.getDefense();
        }
        // calculate defense for equipment
    }
    public static float getWeaponDefense(Mob mob, HashMap<Integer, MobEquipment> equipped) {
        MobEquipment weapon = equipped.get(MBServerStatics.SLOT_MAINHAND);
        ItemBase wb = null;
        CharacterSkill skill, mastery;
        float val = 0;
        boolean unarmed = false;
        if (weapon == null) {
            weapon = equipped.get(MBServerStatics.SLOT_OFFHAND);

            if (weapon == null)
                unarmed = true;
            else
                wb = weapon.getItemBase();

        } else
            wb = weapon.getItemBase();

        if (wb == null)
            unarmed = true;

        if (unarmed) {
            skill = null;
            mastery = null;
        } else {
            skill = mob.skills.get(wb.getSkillRequired());
            mastery = mob.skills.get(wb.getMastery());
        }

        if (skill != null)
            val += (int) skill.getModifiedAmount() / 2f;

        if (mastery != null)
            val += (int) mastery.getModifiedAmount() / 2f;

        return val;
    }

    public static float getShieldDefense(Mob mob, MobEquipment shield) {

        if (shield == null)
            return 0;

        ItemBase ab = shield.getItemBase();

        if (ab == null || !ab.isShield())
            return 0;

        CharacterSkill blockSkill = mob.skills.get("Block");
        float skillMod;

        if (blockSkill == null) {
            skillMod = CharacterSkill.getQuickMastery(mob, "Block");

            if (skillMod == 0f)
                return 0;

        } else
            skillMod = blockSkill.getModifiedAmount();

        //			// Only fighters and healers can block
        //			if (mob.baseClass != null && (mob.baseClass.getUUID() == 2500 || mob.baseClass.getUUID() == 2501))
        //				mob.bonuses.setBool("Block", true);

        float def = ab.getDefense();
        //apply item defense bonuses
        // float val = ((float)ab.getDefense()) * (1 + (skillMod / 100));
        return (def * (1 + ((int) skillMod / 100f)));
    }

    public static float getArmorDefense(Mob mob, MobEquipment armor) {

        if (armor == null)
            return 0;

        ItemBase ib = armor.getItemBase();

        if (ib == null)
            return 0;

        if (!ib.getType().equals(Enum.ItemType.ARMOR))
            return 0;

        if (ib.getSkillRequired().isEmpty())
            return ib.getDefense();

        CharacterSkill armorSkill = mob.skills.get(ib.getSkillRequired());

        if (armorSkill == null)
            return ib.getDefense();

        float def = ib.getDefense();

        //apply item defense bonuses

        return (def * (1 + ((int) armorSkill.getModifiedAmount() / 50f)));
    }

    public static void calculateAtrDamageForWeapon(Mob mob, MobEquipment weapon, boolean mainHand, MobEquipment otherHand) {

        int baseStrength = 0;

        float skillPercentage, masteryPercentage;
        float mastDam;

        // make sure weapon exists
        boolean noWeapon = false;
        ItemBase wb = null;

        if (weapon == null)
            noWeapon = true;
        else {

            ItemBase ib = weapon.getItemBase();

            if (ib == null)
                noWeapon = true;
            else {

                if (ib.getType().equals(Enum.ItemType.WEAPON) == false) {
                    defaultAtrAndDamage(mob, mainHand);
                    return;
                } else
                    wb = ib;
            }
        }
        float min, max;
        float speed = 20f;
        boolean strBased = false;

        // get skill percentages and min and max damage for weapons

        if (noWeapon) {

            if (mainHand)
                mob.rangeHandOne = mob.mobBase.getAttackRange();
            else
                mob.rangeHandTwo = -1; // set to do not attack

            skillPercentage = getModifiedAmount(mob.skills.get("Unarmed Combat"));
            masteryPercentage = getModifiedAmount(mob.skills.get("Unarmed Combat Mastery"));

            if (masteryPercentage == 0f)
                mastDam = CharacterSkill.getQuickMastery(mob,  "Unarmed Combat Mastery");
            else
                mastDam = masteryPercentage;

            // TODO Correct these
            min = mob.mobBase.getMinDmg();
            max = mob.mobBase.getMaxDmg();
        } else {

            if (mainHand)
                mob.rangeHandOne = weapon.getItemBase().getRange() * (1 + (baseStrength / 600));
            else
                mob.rangeHandTwo = weapon.getItemBase().getRange() * (1 + (baseStrength / 600));

            skillPercentage = getModifiedAmount(mob.skills.get(wb.getSkillRequired()));
            masteryPercentage = getModifiedAmount(mob.skills.get(wb.getMastery()));

            if (masteryPercentage == 0f)
                mastDam = 0f;
            else
                mastDam = masteryPercentage;

            min = (float) wb.getMinDamage();
            max = (float) wb.getMaxDamage();
            strBased = wb.isStrBased();
        }

        // calculate atr
        float atr = mob.mobBase.getAttackRating();

        //atr += ((int) skillPercentage * 4f); //<-round down skill% -
        //atr += ((int) masteryPercentage * 3f);

        if (mob.statStrCurrent > mob.statDexCurrent)
            atr += mob.statStrCurrent / 2;
        else
            atr += mob.statDexCurrent / 2;

        // add in any bonuses to atr
        if (mob.bonuses != null) {
            // Add any base bonuses
            atr += mob.bonuses.getFloat(Enum.ModType.OCV, Enum.SourceType.None);

            // Finally use any multipliers. DO THIS LAST!
            float pos_Bonus = 1 + mob.bonuses.getFloatPercentPositive(Enum.ModType.OCV, Enum.SourceType.None);


            atr *= pos_Bonus;

            // next precise

//			atr *= (1 + ((float) mob.bonuses.getShort("rune.Attack") / 100));

            //and negative percent modifiers
            //TODO DO DEBUFFS AFTER?? wILL TEst when finished
            float neg_Bonus = mob.bonuses.getFloatPercentNegative(Enum.ModType.OCV, Enum.SourceType.None);



            atr *= (1 + neg_Bonus);
        }

        atr = (atr < 1) ? 1 : atr;

        // set atr
        if (mainHand)
            mob.atrHandOne = (short) (atr + 0.5f);
        else
            mob.atrHandTwo = (short) (atr + 0.5f);

        //calculate speed

        if (wb != null)
            speed = wb.getSpeed();
        else
            speed = 20f; //unarmed attack speed

        if (mob.bonuses != null && mob.bonuses.getFloat(Enum.ModType.AttackDelay, Enum.SourceType.None) != 0f) //add effects speed bonus
            speed *= (1 + mob.bonuses.getFloatPercentAll(Enum.ModType.AttackDelay, Enum.SourceType.None));

        if (speed < 10)
            speed = 10;

        //add min/max damage bonuses for weapon  **REMOVED

        //if duel wielding, cut damage by 30%
        // calculate damage
        float minDamage;
        float maxDamage;
        float pri = (strBased) ? (float) mob.statStrCurrent : (float) mob.statDexCurrent;
        float sec = (strBased) ? (float) mob.statDexCurrent : (float) mob.statStrCurrent;

        minDamage = (float) (min * ((0.0315f * Math.pow(pri, 0.75f)) + (0.042f * Math.pow(sec, 0.75f)) + (0.01f * ((int) skillPercentage + (int) mastDam))));
        maxDamage = (float) (max * ((0.0785f * Math.pow(pri, 0.75f)) + (0.016f * Math.pow(sec, 0.75f)) + (0.0075f * ((int) skillPercentage + (int) mastDam))));

        minDamage = (float) ((int) (minDamage + 0.5f)); //round to nearest decimal
        maxDamage = (float) ((int) (maxDamage + 0.5f)); //round to nearest decimal
        //	Logger.info("MobCalculateDamage", "Mob with ID "+ mob.getObjectUUID() +   " and MOBBASE with ID " + mob.getMobBaseID() + " returned " + minDamage + "/" + maxDamage + " modified Damage.");

        //add Base damage last.
        float minDamageMod = mob.mobBase.getDamageMin();
        float maxDamageMod = mob.mobBase.getDamageMax();

        minDamage += minDamageMod;
        maxDamage += maxDamageMod;

        // add in any bonuses to damage
        if (mob.bonuses != null) {
            // Add any base bonuses
            minDamage += mob.bonuses.getFloat(Enum.ModType.MinDamage, Enum.SourceType.None);
            maxDamage += mob.bonuses.getFloat(Enum.ModType.MaxDamage, Enum.SourceType.None);

            // Finally use any multipliers. DO THIS LAST!
            minDamage *= (1 + mob.bonuses.getFloatPercentAll(Enum.ModType.MinDamage, Enum.SourceType.None));
            maxDamage *= (1 + mob.bonuses.getFloatPercentAll(Enum.ModType.MaxDamage, Enum.SourceType.None));
        }

        // set damages
        if (mainHand) {
            mob.minDamageHandOne = (short) minDamage;
            mob.maxDamageHandOne = (short) maxDamage;
            mob.speedHandOne = 30;
        } else {
            mob.minDamageHandTwo = (short) minDamage;
            mob.maxDamageHandTwo = (short) maxDamage;
            mob.speedHandTwo = 30;
        }
    }
    public static float getModifiedAmount(CharacterSkill skill) {

        if (skill == null)
            return 0f;

        return skill.getModifiedAmount();
    }

    public static void defaultAtrAndDamage(Mob mob, boolean mainHand) {

        if (mainHand) {
            mob.atrHandOne = 0;
            mob.minDamageHandOne = 0;
            mob.maxDamageHandOne = 0;
            mob.rangeHandOne = -1;
            mob.speedHandOne = 20;
        } else {
            mob.atrHandTwo = 0;
            mob.minDamageHandTwo = 0;
            mob.maxDamageHandTwo = 0;
            mob.rangeHandTwo = -1;
            mob.speedHandTwo = 20;
        }
    }
    public static void calculateModifiedStats(Mob mob) {

        float strVal = mob.mobBase.getMobBaseStats().getBaseStr();
        float dexVal = mob.mobBase.getMobBaseStats().getBaseDex();
        float conVal = 0; // I believe this will desync the Mobs Health if we call it.
        float intVal = mob.mobBase.getMobBaseStats().getBaseInt();
        float spiVal = mob.mobBase.getMobBaseStats().getBaseSpi();

        // TODO modify for equipment
        if (mob.bonuses != null) {
            // modify for effects
            strVal += mob.bonuses.getFloat(Enum.ModType.Attr, Enum.SourceType.Strength);
            dexVal += mob.bonuses.getFloat(Enum.ModType.Attr, Enum.SourceType.Dexterity);
            conVal += mob.bonuses.getFloat(Enum.ModType.Attr, Enum.SourceType.Constitution);
            intVal += mob.bonuses.getFloat(Enum.ModType.Attr, Enum.SourceType.Intelligence);
            spiVal += mob.bonuses.getFloat(Enum.ModType.Attr, Enum.SourceType.Spirit);

            // apply dex penalty for armor
            // modify percent amounts. DO THIS LAST!
            strVal *= (1+mob.bonuses.getFloatPercentAll(Enum.ModType.Attr, Enum.SourceType.Strength));
            dexVal *= (1+mob.bonuses.getFloatPercentAll(Enum.ModType.Attr, Enum.SourceType.Dexterity));
            conVal *= (1+mob.bonuses.getFloatPercentAll(Enum.ModType.Attr, Enum.SourceType.Constitution));
            intVal *= (1+mob.bonuses.getFloatPercentAll(Enum.ModType.Attr, Enum.SourceType.Intelligence));
            spiVal *= (1+mob.bonuses.getFloatPercentAll(Enum.ModType.Attr, Enum.SourceType.Spirit));
        } else {
            // apply dex penalty for armor
        }

        // Set current stats
        mob.statStrCurrent = (strVal < 1) ? (short) 1 : (short) strVal;
        mob.statDexCurrent = (dexVal < 1) ? (short) 1 : (short) dexVal;
        mob.statConCurrent = (conVal < 1) ? (short) 1 : (short) conVal;
        mob.statIntCurrent = (intVal < 1) ? (short) 1 : (short) intVal;
        mob.statSpiCurrent = (spiVal < 1) ? (short) 1 : (short) spiVal;

    }	public static void respawn(Mob mob) {
        //Commenting out Mob ID rotation.

        mob.despawned = false;
        mob.playerAgroMap.clear();
        mob.setCombatTarget(null);
        mob.setHealth(mob.healthMax);
        mob.stamina.set(mob.staminaMax);
        mob.mana.set(mob.manaMax);
        mob.combat = false;
        mob.walkMode = true;
        mob.combatTarget = null;
        mob.isAlive.set(true);

        if (!mob.isSiege)
            mob.lastBindLoc = StaticMobActions.GetSpawnRadiusLocation(mob);
        else
            mob.lastBindLoc = mob.bindLoc;
        mob.bindLoc = mob.lastBindLoc;
        mob.setLoc(mob.lastBindLoc);
        mob.stopMovement(mob.lastBindLoc);
        StaticMobActions.initializeStaticEffects(mob);
        StaticMobActions.recalculateStats(mob);

        mob.setHealth(mob.healthMax);

        if (!mob.isSiege && !mob.isPlayerGuard && mob.contract == null)
            loadInventory(mob);

        //		LoadJob.getInstance();
        //		LoadJob.forceLoad(this);
    }
    public static void loadInventory(Mob mob) {

        if (!MBServerStatics.ENABLE_MOB_LOOT)
            return;

        mob.charItemManager.clearInventory();
        mob.charItemManager.clearEquip();

        if (mob.isPlayerGuard)
            return;

        int gold = StaticMobActions.randomGoldAmount(mob);

        if (gold > 0 && mob.mobBase.getLootTable() != 0) {
            addGoldToInventory(mob, gold);
        }

        //add random loot to mob
        ArrayList<MobLoot> alml = LootTable.getMobLoot(mob, mob.level, mob.mobBase.getLootTable(), false); //add hotzone check in later

        for (MobLoot ml : alml) {
            mob.charItemManager.addItemToInventory(ml);
        }

//send announcement if disc or godly rune
        for(Item it : mob.getInventory()) {
            ItemBase ib = it.getItemBase();
            if (ib.isDiscRune()) {
                //if disc rune send system message
                ChatSystemMsg chatMsg = new ChatSystemMsg(null, mob.getName() + " in " + mob.parentZone.getName() + " has found the " + ib.getName() +". Are you tough enough to take it?");
                chatMsg.setMessageType(10);
                chatMsg.setChannel(Enum.ChatChannelType.SYSTEM.getChannelID());
                DispatchMessage.dispatchMsgToAll(chatMsg);
            }
            if (ib.isStatRune() && ib.getName().toLowerCase().contains("of the gods")) {
                //godly rune send system message
                ChatSystemMsg chatMsg = new ChatSystemMsg(null, mob.getName() + " in " + mob.parentZone.getName() + " has found the " + ib.getName() +". Are you tough enough to take it?");
                chatMsg.setMessageType(10);
                chatMsg.setChannel(Enum.ChatChannelType.SYSTEM.getChannelID());
                DispatchMessage.dispatchMsgToAll(chatMsg);
                return;
            }
        }

        //add special loot to mob
    }
    private static void addGoldToInventory(Mob mob, int quantity) {
        MobLoot gold = new MobLoot(mob, quantity);
        mob.charItemManager.addItemToInventory(gold);
    }
    public static void killCleanup(Mob mob) {
        Dispatch dispatch;

        try {
            if (mob.isSiege) {
                mob.deathTime = System.currentTimeMillis();
                mob.state = MobileFSM.STATE.Dead;
                try {
                    mob.clearEffects();
                }catch(Exception e){
                    Logger.error( e.getMessage());
                }
                mob.combatTarget = null;
                mob.hasLoot = false;
                mob.playerAgroMap.clear();

                mob.timeToSpawnSiege = System.currentTimeMillis() + 60 * 15 * 1000;

                if (mob.isPet()) {

                    PlayerCharacter petOwner = mob.getOwner();

                    if (petOwner != null){
                        setOwner(mob,null);
                        petOwner.setPet(null);
                        PetMsg petMsg = new PetMsg(5, null);
                        dispatch = Dispatch.borrow(mob.getOwner(), petMsg);
                        DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
                    }
                }

            } else if (mob.isPet() || mob.mobBase.isNecroPet()) {
                mob.state = MobileFSM.STATE.Disabled;

                mob.combatTarget = null;
                mob.hasLoot = false;

                if (mob.parentZone != null)
                    mob.parentZone.zoneMobSet.remove(mob);

                try {
                    mob.clearEffects();
                }catch(Exception e){
                    Logger.error( e.getMessage());
                }
                mob.playerAgroMap.clear();
                WorldGrid.RemoveWorldObject(mob);

                DbManager.removeFromCache(mob);

                // YEAH BONUS CODE!  THANKS UNNAMED ASSHOLE!
                //WorldServer.removeObject(this);
                //WorldGrid.INSTANCE.removeWorldObject(this);
                //owner.getPet().disableIntelligence();

                PlayerCharacter petOwner = mob.getOwner();

                if (petOwner != null){
                    setOwner(mob,null);
                    petOwner.setPet(null);
                    PetMsg petMsg = new PetMsg(5, null);
                    dispatch = Dispatch.borrow(petOwner, petMsg);
                    DispatchMessage.dispatchMsgDispatch(dispatch, Enum.DispatchChannel.PRIMARY);
                }
            }  else {

                //cleanup effects

                mob.deathTime = System.currentTimeMillis();
                mob.state = MobileFSM.STATE.Dead;

                mob.playerAgroMap.clear();

                if (!mob.isPlayerGuard){

                    ArrayList<MobLoot> alml = LootTable.getMobLootDeath(mob, mob.getLevel(), mob.mobBase.getLootTable());

                    for (MobLoot ml : alml) {
                        mob.charItemManager.addItemToInventory(ml);
                    }

                    if (mob.equip != null){

                        for (MobEquipment me: mob.equip.values()){
                            if (me.getDropChance() == 0)
                                continue;

                            float chance = ThreadLocalRandom.current().nextFloat();

                            if (chance <= me.getDropChance()){
                                MobLoot ml = new MobLoot(mob, me.getItemBase(), false);
                                ml.setFidelityEquipID(me.getObjectUUID());
                                mob.charItemManager.addItemToInventory(ml);
                            }
                        }
                    }
                }

            }
            try {
                mob.clearEffects();
            }catch(Exception e){
                Logger.error( e.getMessage());
            }

            mob.combat = false;
            mob.walkMode = true;
            mob.combatTarget = null;

            mob.hasLoot = (mob.charItemManager.getInventoryCount() > 0) ? true : false;

        } catch (Exception e) {
            Logger.error(e);
        }
    }
    public static int getSpawnTime(Mob mob) {

        if (mob.spawnTime == 0)
            return MBServerStatics.RESPAWN_TIMER;
        else
            return mob.spawnTime * 1000;
    }
    public static String getSpawnTimeAsString(Mob mob) {
        if (mob.spawnTime == 0)
            return MBServerStatics.DEFAULT_SPAWN_TIME_MS / 1000 + " seconds (Default)";
        else
            return mob.spawnTime + " seconds";

    }
    public static ItemBase getWeaponItemBase(Mob mob, boolean mainHand) {

        if (mob.equipmentSetID != 0){

            if (mob.equip != null) {
                MobEquipment me = null;

                if (mainHand)
                    me = mob.equip.get(1); //mainHand
                else
                    me = mob.equip.get(2); //offHand

                if (me != null) {

                    ItemBase ib = me.getItemBase();

                    if (ib != null)
                        return ib;

                }
            }
        }
        MobBase mb = mob.mobBase;

        if (mb != null) {

            if (mob.equip != null) {

                MobEquipment me = null;

                if (mainHand)
                    me = mob.equip.get(1); //mainHand
                else
                    me = mob.equip.get(2); //offHand

                if (me != null) {

                    ItemBase ib = me.getItemBase();

                    if (ib != null)
                        return ib;
                }
            }
        }
        return null;
    }
    public static boolean remove(Mob mob,Building building) {

        // Remove npc from it's building
        mob.state = MobileFSM.STATE.Disabled;

        try {
            mob.clearEffects();
        }catch(Exception e){
            Logger.error(e.getMessage());
        }

        if (mob.parentZone != null)
            mob.parentZone.zoneMobSet.remove(mob);

        if (building != null) {
            building.getHirelings().remove(mob);
            StaticMobActions.removeMinions(mob);
        }

        // Delete npc from database

        if (DbManager.MobQueries.DELETE_MOB(mob) == 0)
            return false;

        // Remove npc from the simulation

        mob.removeFromCache();
        DbManager.removeFromCache(mob);
        WorldGrid.RemoveWorldObject(mob);
        WorldGrid.removeObject(mob);
        return true;
    }
    public static void handleDirectAggro(Mob mob, AbstractCharacter ac) {

        if (ac.getObjectType().equals(Enum.GameObjectType.PlayerCharacter) == false)
            return;

        PlayerCharacter player = (PlayerCharacter)ac;

        if (mob.getCombatTarget() == null) {
            MobileFSM.setAggro(mob, player.getObjectUUID());
            return;
        }

        if (player.getObjectUUID() == mob.getCombatTarget().getObjectUUID())
            return;

        if (mob.getCombatTarget().getObjectType() == Enum.GameObjectType.PlayerCharacter) {

            if (ac.getHateValue() > ((PlayerCharacter) mob.getCombatTarget()).getHateValue()) {
                mob.setCombatTarget(player);
                MobileFSM.setAggro(mob, player.getObjectUUID());
            }
        }
    }
    public static void setInBuildingLoc(Mob mobO,Building inBuilding, AbstractCharacter ac) {

        Mob mob = null;

        NPC npc = null;


        if (ac.getObjectType().equals(Enum.GameObjectType.Mob))
            mob = (Mob)ac;

        else if (ac.getObjectType().equals(Enum.GameObjectType.NPC))
            npc = (NPC)ac;

        // *** Refactor : Need to take a look at this, make sure
        // npc's are loaded in correct spots.

        BuildingModelBase buildingModel = BuildingModelBase.getModelBase(inBuilding.getMeshUUID());

        Vector3fImmutable slotLocation = Vector3fImmutable.ZERO;

        if (buildingModel != null){


            int putSlot = -1;
            BuildingLocation buildingLocation = null;

            //-1 slot means no slot available in building.

            if (npc != null){
                if (npc.getSiegeMinionMap().containsKey(mobO))
                    putSlot = npc.getSiegeMinionMap().get(mobO);
            }else if (mob != null)
                if (mob.siegeMinionMap.containsKey(mobO))
                    putSlot = mob.siegeMinionMap.get(mobO);

            int count = 0;

            for (BuildingLocation slotLoc: buildingModel.getLocations())
                if (slotLoc.getType() == 6)
                    count++;


            buildingLocation = buildingModel.getSlotLocation((count) - putSlot);

            if (buildingLocation != null){
                slotLocation = buildingLocation.getLoc();
            }

        }

        mobO.inBuildingLoc = slotLocation;

    }
    public static boolean canRespawn(Mob mob){
        return System.currentTimeMillis() > mob.despawnTime + 4000;
    }
    public static void setRelPos(Mob mob,Zone zone, float locX, float locY, float locZ) {

        //update mob zone map

        if (mob.parentZone != null)
            mob.parentZone.zoneMobSet.remove(mob);

        zone.zoneMobSet.add(mob);

        mob.statLat = locX;
        mob.statAlt = locY;
        mob.statLon = locZ;
        mob.parentZone = zone;
        mob.setBindLoc(new Vector3fImmutable(mob.statLat + zone.absX, mob.statAlt + zone.absY, mob.statLon + zone.absZ));
    }
    public static void despawn(Mob mob) {

        mob.despawned = true;

        //WorldServer.removeObject(this);
        WorldGrid.RemoveWorldObject(mob);
        mob.charItemManager.clearInventory();
        mob.despawnTime = System.currentTimeMillis();
        //		this.setLoc(Vector3fImmutable.ZERO);
    }
    public static void updateLocation(Mob mob){

        if (!mob.isMoving())
            return;

        if (mob.state == MobileFSM.STATE.Disabled)
            return;

        if ( mob.isAlive() == false || mob.getBonuses().getBool(Enum.ModType.Stunned, Enum.SourceType.None) || mob.getBonuses().getBool(Enum.ModType.CannotMove, Enum.SourceType.None)) {
            //Target is stunned or rooted. Don't move

            mob.stopMovement(mob.getMovementLoc());

            return;
        }

        Vector3fImmutable newLoc = mob.getMovementLoc();

        if (newLoc.equals(mob.getEndLoc())){
            mob.stopMovement(newLoc);
            return;
            //Next upda
        }

        mob.setLoc(newLoc);
        //Next update will be end Loc, lets stop him here.

    }
    public static void setOwner(Mob mob,PlayerCharacter value) {

        if (value == null)
            mob.ownerUID = 0;
        else
            mob.ownerUID = value.getObjectUUID();
    }
    public static void initializeStaticEffects(Mob mob) {

        EffectsBase eb = null;
        for (MobBaseEffects mbe : mob.mobBase.getRaceEffectsList()) {

            eb = PowersManager.getEffectByToken(mbe.getToken());

            if (eb == null) {
                Logger.info( "EffectsBase Null for Token " + mbe.getToken());
                continue;
            }

            //check to upgrade effects if needed.
            if (mob.effects.containsKey(Integer.toString(eb.getUUID()))) {
                if (mbe.getReqLvl() > (int) mob.level)
                    continue;

                Effect eff = mob.effects.get(Integer.toString(eb.getUUID()));

                if (eff == null)
                    continue;

                if (eff.getTrains() > mbe.getRank())
                    continue;

                //new effect is of a higher rank. remove old effect and apply new one.
                eff.cancelJob();
                mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
            } else {
                if (mbe.getReqLvl() > (int) mob.level)
                    continue;

                mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
            }
        }

        //Apply all rune effects.
        // Only Captains have contracts
        if (mob.contract != null || mob.isPlayerGuard){
            RuneBase guardRune = RuneBase.getRuneBase(252621);
            for (MobBaseEffects mbe : guardRune.getEffectsList()) {

                eb = PowersManager.getEffectByToken(mbe.getToken());

                if (eb == null) {
                    Logger.info( "EffectsBase Null for Token " + mbe.getToken());
                    continue;
                }

                //check to upgrade effects if needed.
                if (mob.effects.containsKey(Integer.toString(eb.getUUID()))) {

                    if (mbe.getReqLvl() > (int) mob.level)
                        continue;

                    Effect eff = mob.effects.get(Integer.toString(eb.getUUID()));

                    if (eff == null)
                        continue;

                    //Current effect is a higher rank, dont apply.
                    if (eff.getTrains() > mbe.getRank())
                        continue;

                    //new effect is of a higher rank. remove old effect and apply new one.
                    eff.cancelJob();
                    mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                } else {

                    if (mbe.getReqLvl() > (int) mob.level)
                        continue;

                    mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                }
            }

            RuneBase WarriorRune = RuneBase.getRuneBase(2518);
            for (MobBaseEffects mbe : WarriorRune.getEffectsList()) {

                eb = PowersManager.getEffectByToken(mbe.getToken());

                if (eb == null) {
                    Logger.info( "EffectsBase Null for Token " + mbe.getToken());
                    continue;
                }

                //check to upgrade effects if needed.
                if (mob.effects.containsKey(Integer.toString(eb.getUUID()))) {

                    if (mbe.getReqLvl() > (int) mob.level)
                        continue;

                    Effect eff = mob.effects.get(Integer.toString(eb.getUUID()));

                    if (eff == null)
                        continue;

                    //Current effect is a higher rank, dont apply.
                    if (eff.getTrains() > mbe.getRank())
                        continue;

                    //new effect is of a higher rank. remove old effect and apply new one.
                    eff.cancelJob();
                    mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                } else {

                    if (mbe.getReqLvl() > (int) mob.level)
                        continue;

                    mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                }
            }
        }

        if (mob.fidelityRunes != null){

            for (int fidelityRune : mob.fidelityRunes) {

                RuneBase rune = RuneBase.getRuneBase(fidelityRune);

                if (rune != null)
                    for (MobBaseEffects mbe : rune.getEffectsList()) {

                        eb = PowersManager.getEffectByToken(mbe.getToken());
                        if (eb == null) {
                            Logger.info("EffectsBase Null for Token " + mbe.getToken());
                            continue;
                        }

                        //check to upgrade effects if needed.
                        if (mob.effects.containsKey(Integer.toString(eb.getUUID()))) {
                            if (mbe.getReqLvl() > (int) mob.level)
                                continue;

                            Effect eff = mob.effects.get(Integer.toString(eb.getUUID()));

                            if (eff == null)
                                continue;

                            //Current effect is a higher rank, dont apply.
                            if (eff.getTrains() > mbe.getRank())
                                continue;

                            //new effect is of a higher rank. remove old effect and apply new one.
                            eff.cancelJob();
                            mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);

                        } else {

                            if (mbe.getReqLvl() > (int) mob.level)
                                continue;

                            mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                        }
                    }
            }
        }else
            for (RuneBase rune : mob.mobBase.getRunes()) {
                for (MobBaseEffects mbe : rune.getEffectsList()) {

                    eb = PowersManager.getEffectByToken(mbe.getToken());
                    if (eb == null) {
                        Logger.info( "EffectsBase Null for Token " + mbe.getToken());
                        continue;
                    }

                    //check to upgrade effects if needed.
                    if (mob.effects.containsKey(Integer.toString(eb.getUUID()))) {
                        if (mbe.getReqLvl() > (int) mob.level)
                            continue;

                        Effect eff = mob.effects.get(Integer.toString(eb.getUUID()));

                        if (eff == null)
                            continue;

                        //Current effect is a higher rank, dont apply.
                        if (eff.getTrains() > mbe.getRank())
                            continue;

                        //new effect is of a higher rank. remove old effect and apply new one.
                        eff.cancelJob();
                        mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                    } else {

                        if (mbe.getReqLvl() > (int) mob.level)
                            continue;

                        mob.addEffectNoTimer(Integer.toString(eb.getUUID()), eb, mbe.getRank(), true);
                    }
                }
            }
    }
    public static void initializeSkills(Mob mob) {

        if (mob.mobBase.getMobBaseStats() == null)
            return;

        long skillVector = mob.mobBase.getMobBaseStats().getSkillSet();
        int skillValue = mob.mobBase.getMobBaseStats().getSkillValue();

        if (mob.mobBase.getObjectUUID() >= 17233) {
            for (Enum.CharacterSkills cs : Enum.CharacterSkills.values()) {
                SkillsBase sb = DbManager.SkillsBaseQueries.GET_BASE_BY_TOKEN(cs.getToken());
                CharacterSkill css = new CharacterSkill(sb, mob, 50);
                mob.skills.put(sb.getName(), css);
            }
        } else {
            for (Enum.CharacterSkills cs : Enum.CharacterSkills.values()) {
                if ((skillVector & cs.getFlag()) != 0) {
                    SkillsBase sb = DbManager.SkillsBaseQueries.GET_BASE_BY_TOKEN(cs.getToken());
                    CharacterSkill css = new CharacterSkill(sb, mob, skillValue);
                    mob.skills.put(sb.getName(), css);
                }
            }
        }
    }
    public static void clearStatic(Mob mob) {

        if (mob.parentZone != null)
            mob.parentZone.zoneMobSet.remove(mob);

        mob.parentZone = null;
        mob.statLat = 0f;
        mob.statLon = 0f;
        mob.statAlt = 0f;
    }
    public static void initializeMob(Mob mob, boolean isPet, boolean isSiege, boolean isGuard) {

        if (mob.mobBase != null) {

            mob.gridObjectType = Enum.GridObjectType.DYNAMIC;
            mob.healthMax = mob.mobBase.getHealthMax();
            mob.manaMax = 0;
            mob.staminaMax = 0;
            mob.setHealth(mob.healthMax);
            mob.mana.set(mob.manaMax);
            mob.stamina.set(mob.staminaMax);

            if(!mob.nameOverride.isEmpty())
                mob.firstName = mob.nameOverride;
            else
                mob.firstName = mob.mobBase.getFirstName();
            if (isPet) {
                mob.setObjectTypeMask(MBServerStatics.MASK_PET | mob.mobBase.getTypeMasks());
                if (ConfigManager.serverType.equals(Enum.ServerType.LOGINSERVER))
                    mob.setLoc(mob.getLoc());
            }
            if (!isPet && mob.contract == null) {
                mob.level = (short) mob.mobBase.getLevel();
            }

        } else
            mob.level = 1;

        //add this npc to building
        if (mob.building != null && mob.loadID != 0 && mob.fidalityID == 0) {

            int maxSlots;
            maxSlots = mob.building.getBlueprint().getSlotsForRank(mob.building.getRank());

            for (int slot = 1; slot < maxSlots + 1; slot++) {
                if (!mob.building.getHirelings().containsValue(slot)) {
                    mob.building.getHirelings().put(mob, slot);
                    break;
                }
            }
        }

        //set bonuses
        mob.bonuses = new PlayerBonuses(mob);

        //TODO set these correctly later
        mob.rangeHandOne = 8;
        mob.rangeHandTwo = -1;
        mob.minDamageHandOne = 0;
        mob.maxDamageHandOne = 0;
        mob.minDamageHandTwo = 1;
        mob.maxDamageHandTwo = 4;
        mob.atrHandOne = 300;
        mob.atrHandOne = 300;
        mob.defenseRating = (short) mob.mobBase.getDefenseRating();
        mob.isActive = true;

        mob.charItemManager.load();

        //load AI for general mobs.

        if (isPet || isSiege || (isGuard && mob.contract == null))
            mob.currentID =  (--Mob.staticID);
        else
            mob.currentID = mob.dbID;

        if (!isPet && !isSiege && !mob.isPlayerGuard)
            StaticMobActions.loadInventory(mob);

        //store mobs by Database ID

        if (!isPet && !isSiege)
            Mob.mobMapByDBID.put(mob.dbID, mob);
    }
}
