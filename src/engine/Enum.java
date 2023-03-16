// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine;

import ch.claude_martin.enumbitset.EnumBitSetHelper;
import engine.gameManager.BuildingManager;
import engine.gameManager.PowersManager;
import engine.gameManager.ZoneManager;
import engine.math.Vector2f;
import engine.math.Vector3fImmutable;
import engine.objects.*;
import engine.powers.EffectsBase;
import org.pmw.tinylog.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/*
 * MagicBane engine enumeration class.
 *
 * All enumerations accessed by multiple
 * classes should be defined here to keep
 * the imports consolidated.
 */

public class Enum {

	public enum MobRaceType {

		Aelfborn(436353765),
		All(80289),
		Animal(-1674072607),
		Aracoix(-1764716937),
		Celestial(-317458791),
		Centaur(775630999),
		Construct(-513218610),
		CSR(52803),
		Dragon(-1731031452),
		Dwarf(71831236),
		Elf(70053),
		Giant(90574087),
		Goblin(-1732836921),
		Grave(75107943),
		HalfGiant(251196434),
		Human(79806088),
		Infernal(-654077031),
		Insect(-1407990295),
		Irekei(-1770742167),
		Minotaur(-949570680),
		Monster(258519513),
		NecroPet(618137151),
		NPC(35374),
		Pet(88208),
		Plant(90574256),
		Rat(88082),
		Reptile(-591705981),
		Shade(74648883),
		Siege(74620179),
		SiegeEngineer(-839969219),
		Summoned(-656950110),
		Troll(82261620),
		Undead(-1942775307),
		Nephilim(-592098572),
		Vampire(-524731385);

		int token;

		private static HashMap<Integer, MobRaceType> _mobRaceTypeByToken = new HashMap<>();

		MobRaceType(int token) {
			this.token = token;
		}

		public static MobRaceType getRaceTypebyToken(int token) {
			return _mobRaceTypeByToken.get(token);
		}

		public static void initRaceTypeTables() {

			for (MobRaceType raceType : MobRaceType.values()) {
				_mobRaceTypeByToken.put(raceType.token, raceType);
			}
		}

	}

	public enum MobFlagType implements EnumBitSetHelper<MobFlagType> {
		AGGRESSIVE,
		CANROAM,
		CALLSFORHELP,
		RESPONDSTOCALLSFORHELP,
		HUMANOID,
		UNDEAD,
		BEAST,
		DRAGON,
		RAT,
		SENTINEL,
	}

	public enum AggroType implements EnumBitSetHelper<AggroType> {

		// Used for MobBase NoAggro types
		// *** WARNING: ENUM IS FRAGILE AS
		// ORDINALS STORED IN DB.

		AELF,
		ARACOIX,
		CENTAUR,
		DWARF,
		ELF,
		HALFGIANT,
		HUMAN,
		IREKEI,
		MINO,
		NEPH,
		SHADE,
		VAMP,
		ARCHON,
		REPTILE;

	}

	public enum CharacterSex {
		MALE,
		FEMALE,
		FUZZY,
		OTHER;
	}

	public enum RaceType {

		// RaceRuneID / AggroType, isFemale

		AELFMALE(2000, AggroType.AELF, RunSpeed.STANDARD, CharacterSex.MALE,1.05f),
		AELFFEMALE(2001, AggroType.AELF, RunSpeed.STANDARD, CharacterSex.FEMALE,1.05f),
		ARACOIXMALE(2002, AggroType.ARACOIX, RunSpeed.STANDARD, CharacterSex.MALE,1),
		ARACOIXFEMALE(2003, AggroType.ARACOIX, RunSpeed.STANDARD, CharacterSex.FEMALE,1),
		CENTAURMALE(2004, AggroType.CENTAUR, RunSpeed.CENTAUR, CharacterSex.MALE,1.2f),
		CENTAURFEMALE(2005, AggroType.CENTAUR, RunSpeed.CENTAUR, CharacterSex.FEMALE, 1.2f),
		DWARFMALE(2006, AggroType.DWARF, RunSpeed.STANDARD, CharacterSex.MALE,0.80000001f),
		ELFMALE(2008, AggroType.ELF, RunSpeed.STANDARD, CharacterSex.MALE, 1.4f),
		ELFFEMALE(2009, AggroType.ELF, RunSpeed.STANDARD, CharacterSex.FEMALE,1.1f),
		HALFGIANTMALE(2010, AggroType.HALFGIANT, RunSpeed.STANDARD, CharacterSex.MALE, 1.15f),
		HUMANMALE(2011, AggroType.HUMAN, RunSpeed.STANDARD, CharacterSex.MALE,1),
		HUMANFEMALE(2012, AggroType.HUMAN, RunSpeed.STANDARD, CharacterSex.FEMALE,1),
		IREKEIMALE(2013, AggroType.IREKEI, RunSpeed.STANDARD, CharacterSex.MALE,1.1f),
		IREKEIFEMALE(2014, AggroType.IREKEI, RunSpeed.STANDARD, CharacterSex.FEMALE,1.1f),
		SHADEMALE(2015, AggroType.SHADE, RunSpeed.STANDARD, CharacterSex.MALE,1),
		SHADEFEMALE(2016, AggroType.SHADE, RunSpeed.STANDARD, CharacterSex.FEMALE,1),
		MINOMALE(2017, AggroType.MINO, RunSpeed.MINOTAUR, CharacterSex.MALE,1.3f),
		ARCHONMALE(2018, AggroType.ARCHON, RunSpeed.STANDARD, CharacterSex.MALE,1),
		HALEGIANTOLDMALE(2019, AggroType.HALFGIANT, RunSpeed.STANDARD, CharacterSex.MALE,1.15f),
		CSRFEMALE(2020, AggroType.ARCHON, RunSpeed.STANDARD, CharacterSex.FEMALE,0.66000003f),
		CSRMALE(2021, AggroType.ARCHON, RunSpeed.STANDARD, CharacterSex.MALE,1),
		NEPHMALE(2025, AggroType.NEPH, RunSpeed.STANDARD, CharacterSex.MALE,1.1f),
		NEPHFEMALE(2026, AggroType.NEPH, RunSpeed.STANDARD, CharacterSex.FEMALE,1.1f),
		HALFGIANTFEMALE(2027, AggroType.HALFGIANT, RunSpeed.STANDARD, CharacterSex.FEMALE,1.15f),
		VAMPMALE(2028, AggroType.VAMP, RunSpeed.STANDARD, CharacterSex.MALE, 1),
		VAMPFEMALE(2029, AggroType.VAMP, RunSpeed.STANDARD, CharacterSex.FEMALE,1),
		REPTILE(2030, AggroType.REPTILE, RunSpeed.STANDARD, CharacterSex.OTHER,1);

		@SuppressWarnings("unchecked")

		private static HashMap<Integer, 
		RaceType> _raceTypeByID = new HashMap<>();

		int runeID;
		private AggroType aggroType;
		private CharacterSex characterSex;
		private RunSpeed runSpeed;
		private float scaleHeight;

		RaceType(int runeID, AggroType aggroType, RunSpeed runspeed, CharacterSex characterSex, float scaleHeight) {
			this.runeID = runeID;
			this.aggroType = aggroType;
			this.runSpeed = runspeed;
			this.characterSex = characterSex;
			this.scaleHeight = scaleHeight;
		}

		public int getRuneID() {
			return this.runeID;
		}

		public static RaceType getRaceTypebyRuneID(int runeID) {
			return _raceTypeByID.get(runeID);
		}
		
		public float getScaleHeight(){
			return this.scaleHeight;
		}

		public static void initRaceTypeTables() {

			for (RaceType raceType : RaceType.values()) {
				_raceTypeByID.put(raceType.runeID, raceType);
			}
		}

		public AggroType getAggroType() {
			return aggroType;
		}

		public RunSpeed getRunSpeed() {
			return runSpeed;
		}

		public CharacterSex getCharacterSex() {
			return characterSex;
		}
	}

	public enum RunSpeed {

		SENTINEL(0, 0, 0, 0, 0, 0, 0),
		STANDARD(6.1900001f, 13.97f, 4.2199998f, 13.97f, 6.3299999f, 18.379999f, 6.5f),
		CENTAUR(6.1900001f, 16.940001f, 5.5500002f, 16.940001f, 6.3299999f, 18.379999f, 6.5f),
		MINOTAUR(6.6300001f, 15.95f, 4.2199998f, 15.95f, 6.3299999f, 18.379999f, 6.5f);

		private float walkStandard;
		private float walkCombat;
		private float runStandard;
		private float runCombat;
		private float swim;
		private float flyRun;
		private float flyWalk;

		RunSpeed(float walkStandard, float runStandard, float walkCombat, float runCombat, float flyWalk, float flyRun, float swim) {
			this.walkStandard = walkStandard;
			this.walkCombat = walkCombat;
			this.runStandard = runStandard;
			this.runCombat = runCombat;
			this.swim = swim;
			this.flyRun = flyRun;
			this.flyWalk = flyWalk;
		}


		public float getWalkStandard() {
			return walkStandard;
		}

		public float getWalkCombat() {
			return walkCombat;
		}

		public float getRunStandard() {
			return runStandard;
		}

		public float getRunCombat() {
			return runCombat;
		}

		public float getSwim() {
			return swim;
		}


		public float getFlyRun() {
			return flyRun;
		}


		public float getFlyWalk() {
			return flyWalk;
		}

	}

	public enum FriendListType {

		VIEWHERALDRY(1),
		ADDHERALDRY(4),
		REMOVEHERALDRY(6),
		DEALTHS(7),
		KILLS(9),
		VIEWCONDEMN(11),
		ADDCONDEMN(14),
		REMOVECONDEMN(15),
		TOGGLEACTIVE(17),
		REVERSEKOS(19),
		VIEWFRIENDS(25),
		TOITEM(23),
		ADDFRIEND(28),
		REMOVEFRIEND(30);

		private final int listType;

		FriendListType(int listType) {
			this.listType = listType;
		}

		public int getListType() {
			return this.listType;
		}

		public static FriendListType getListTypeByID(int listType) {

			FriendListType outType = null;

			for (FriendListType friendListType : FriendListType.values()) {
				if (friendListType.listType == listType)
					outType = friendListType;
			}
			return outType;
		}
	}

	public enum DispatchChannel {
		PRIMARY(0),
		SECONDARY(1);

		private final int channelID;

		DispatchChannel(int channelID) {
			this.channelID = channelID;
		}

		public int getChannelID() {
			return this.channelID;
		}

	}

	public enum PvpHistoryType {
		KILLS,
		DEATHS;
	}

	public enum ChatMessageType {
		ERROR,
		INFO,
		MOTD;
	}

	public enum DataRecordType {
		PVP,
		CHARACTER,
		BANE,
		GUILD,
		CITY,
		ZONE,
		REALM,
		MINE;
	}

	public enum RecordEventType {
		CREATE,  // Shared with city/guild
		DISBAND,
		DESTROY, // City events
		CAPTURE,
		TRANSFER,
		PENDING,
		DEFEND,
		LOST; // Realm event
	}

	public enum CharterType {
		FEUDAL(-600065291, 5060000),
		MERCANTILE(-15978914, 5060400),
		BELLIGERENT(762228431, 5060800);

		private int charterID;
		private int meshID;

		CharterType(int charterID, int meshID) {
			this.charterID = charterID;
			this.meshID = meshID;
		}

		public int getMeshID() {
			return meshID;
		}

		public static CharterType getCharterTypeByID(int charterID) {
			CharterType outType = null;

			for (CharterType charterType : CharterType.values()) {
				if (charterType.charterID == charterID)
					outType = charterType;
			}
			return outType;
		}
	}


	public enum ChatChannelType {
		SYSTEM(1),
		FLASH(2),
		COMMANDER(3),
		NATION(5),
		LEADER(6),
		SHOUT(7),
		INFO(10),
		GUILD(12),
		INNERCOUNCIL(13),
		GROUP(14),
		CITY(15),
		SAY(16),
		EMOTE(17),
		TELL(19),
		COMBAT(20);

		private final int channelID;

		ChatChannelType(int channelID) {
			this.channelID = channelID;
		}

		public int getChannelID() {
			return this.channelID;
		}
	}

	public enum OwnerType {
		Npc,
		PlayerCharacter,
		Account,
		Mob;
	}

	public enum SiegePhase {
		ERRANT,
		CHALLENGE,
		STANDOFF,
		WAR,
		CEASEFIRE;
	}

	public enum SiegeResult {
		PENDING,
		DEFEND,
		DESTROY,
		CAPTURE;
	}

	public enum RealmType {

		SEAFLOOR(0, 0x000000),
		JOTUNHEIM(131, 0x006cff),
		BOGLANDS(184, 0x00b4ff),
		ESTRAGOTH(213, 0x00ff90),
		RENNONVALE(232, 0X00ffea),
		VOLGAARD(56, 0x1e00ff),
		VOSTRAGOTH(108, 0x245fae),
		NARROWS(61, 0x2f20a0),
		FENMARCH(170, 0x3fb5ab),
		MAELSTROM(63, 0x503e3e),
		FARRICH(185, 0x52cd98),
		TYRRANTHMINOR(96, 0x606060),
		GREYSWATHE(88, 0x6c419d),
		SUNSANVIL(64, 0x7800ff),
		THERRONMARCH(206, 0x7bcdef),
		DYVRENGISLE(119, 0x826b9c),
		KINGSLUND(60, 0x871a94),
		OUTERISLES(29, 0xa01313),
		KAELENSFJORD(165, 0xa0c04a),
		VARMADAI(95, 0xa16d1b),
		WESTERMOORE(73, 0xaa3374),
		OBLIVION(171, 0xababab),
		SUDRAGOTH(196, 0xbaff00),
		SKAARTHOL(183, 0xcfc57f),
		KHALURAM(71, 0xe400ff),
		VARSHADDUR(132, 0xf2845d),
		FORBIDDENISLE(18, 0xff0000),
		PIRATEISLES(48, 0xff008a),
		SWATHMOORE(66, 0xff4200),
		ESSENGLUND(130, 0xff9c00),
		RELGOTH(177, 0xffde00);

		private final int realmID;
		private final Color color;
		private static final HashMap<Integer, Integer> _rgbToIDMap = new HashMap<>();

		RealmType(int realmID, int colorRGB) {

			this.realmID = realmID;
			this.color = new Color(colorRGB);

		}

		public void addToColorMap() {
			_rgbToIDMap.put(this.color.getRGB(), this.realmID);
		}

		public static int getRealmIDByRGB(int realmRGB) {

			return _rgbToIDMap.get(realmRGB);

		}

		public int getRealmID() {
			return realmID;
		}

		public static RealmType getRealmTypeByUUID(int realmUUID) {
			RealmType returnType = RealmType.SEAFLOOR;

			for (RealmType realmType : RealmType.values()) {

				if (realmType.realmID == realmUUID)
					returnType = realmType;
			}
			return returnType;
		}
	}

	public enum TaxType {
		PROFIT,
		WEEKLY,
		NONE;

	}

	public enum Ruins {

		ESTRAGOTH(569),
		KARFELL(570),
		MORELAN(571),
		REGARS(572),
		HALLOS(573),
		WESTERMORE(574),
		EYWAN(575),
		CAER(576);

		private final int zoneUUID;

		Ruins(int uuid) {
			this.zoneUUID = uuid;
		}

		public int getZoneUUID() {
			return this.zoneUUID;
		}

		public Vector3fImmutable getLocation() {

			Zone ruinZone;
			Vector3fImmutable spawnLocation;

			ruinZone = ZoneManager.getZoneByUUID(this.zoneUUID);
			spawnLocation = Vector3fImmutable.getRandomPointOnCircle(ruinZone.getLoc(), 30);

			return spawnLocation;
		}

		public static Ruins getRandomRuin() {

			Ruins ruins;

			ruins = Ruins.values()[ThreadLocalRandom.current()
					.nextInt(Ruins.values().length)];

			return ruins;
		}

	}

	public enum Guards {

		HumanArcher(13.97f, 13.97f, 6.19f, 4.2199998f, 18.38f, 6.33f, 6.5f),
		HumanGuard(13.97f, 13.97f, 6.19f, 4.2199998f, 18.38f, 6.33f, 6.5f),
		HumanMage(13.97f, 13.97f, 6.19f, 4.2199998f, 18.38f, 6.33f, 6.5f),
		UndeadArcher(14.67f, 14.67f, 6.5f, 4.44f, 18.38f, 6.33f, 6.5f),
		UndeadGuard(14.67f, 14.67f, 6.5f, 4.44f, 18.38f, 6.33f, 6.5f),
		UndeadMage(14.67f, 14.67f, 6.5f, 4.44f, 18.38f, 6.33f, 6.5f);

		private final float runSpeed;
		private final float runCombatSpeed;
		private final float walkSpeed;
		private final float walkCombatSpeed;
		private final float fly;
		private final float flyWalk;
		private final float swim;

		Guards(float runSpeed, float runCombatSpeed, float walkSpeed, float walkCombatSpeed, float fly, float flyWalk, float swim) {
			this.runSpeed = runSpeed;
			this.runCombatSpeed = runCombatSpeed;
			this.walkSpeed = walkSpeed;
			this.walkCombatSpeed = walkCombatSpeed;
			this.fly = fly;
			this.flyWalk = flyWalk;
			this.swim = swim;
		}

		public float getRunSpeed() {
			return runSpeed;
		}

		public float getRunCombatSpeed() {
			return runCombatSpeed;
		}

		public float getWalkSpeed() {
			return walkSpeed;
		}

		public float getWalkCombatSpeed() {
			return walkCombatSpeed;
		}

		public float getFly() {
			return fly;
		}

		public float getSwim() {
			return swim;
		}

		public float getFlyWalk() {
			return flyWalk;
		}
	}

	public enum RunegateType {

		EARTH(6f, 19.5f, 128, 33213),
		AIR(-6f, 19.5f, 256, 33170),
		FIRE(15f, 7.5f, 512, 49612),
		WATER(-15f, 8.5f, 1024, 53073),
		SPIRIT(0, 10.5f, 2048, 33127),
		CHAOS(22f, 3.5f, 8192, 58093),
		OBLIV(0f, 42f, 16384, 60198),
		MERCHANT(-22f, 4.5f, 4096, 60245),
		FORBID(0.0f, 0.0f, 0, 54617);

		private final Vector2f offset;
		private final int bitFlag;
		private final int buildingUUID;

		RunegateType(float offsetX, float offsetY, int bitFlag,
					 int buildingUUID) {

			this.offset = new Vector2f(offsetX, offsetY);
			this.bitFlag = bitFlag;
			this.buildingUUID = buildingUUID;
		}

		public Vector2f getOffset() {
			return this.offset;
		}

		public int getEffectFlag() {
			return this.bitFlag;
		}

		public int getGateUUID() {
			return this.buildingUUID;
		}

		public Building getGateBuilding() {

			return BuildingManager.getBuilding(this.buildingUUID);
		}

		public static RunegateType getGateTypeFromUUID(int uuid) {

			RunegateType outType = RunegateType.AIR;

			for (RunegateType gateType : RunegateType.values()) {

				if (gateType.buildingUUID == uuid) {
					outType = gateType;
					return outType;
				}

			}

			return outType;
		}

	}

	// Enum for ItemBase flags

	public enum ItemType {
		DECORATION(0),
		WEAPON(1),
		ARMOR(2),
		HAIR(3),
		GOLD(4),
		RUNE(5),
		SCROLL(5),
		BOOK(6),
		COMMANDROD(7),
		POTION(8),
		TEARS(8),
		KEY(9),
		GUILDCHARTER(10),
		JEWELRY(13),
		WINE(16),
		ALEJUG(17),
		DEED(19),
		CONTRACT(20),
		PET(21),
		FURNITURE(25),
		BEDROLL(26),
		FARMABLE(27),
		WATERBUCKET(30),
		GIFT(31),
		OFFERING(33),
		RESOURCE(34),
		REALMCHARTER(35);

		private final int _value;
		private final static HashMap<Integer, ItemType> _typeLookup = new HashMap<>();

		ItemType(int value) {
			this._value = value;
		}

		public static ItemType getByValue(int value) {

			ItemType outType = ItemType.DECORATION;

			if (_typeLookup.isEmpty()) {

				for (ItemType itemType : ItemType.values()) {
					_typeLookup.put(itemType._value, itemType);
				}
			}

			if (_typeLookup.containsKey(value))
				outType = _typeLookup.get(value);

			return outType;
		}

		/**
		 * @return the _value
		 */
		public int getValue() {
			return _value;
		}

	}
	// Enum to derive effects for active spires from blueprintUUID

	public enum SpireType {

		WATCHFUL(1800100, (1 << 23), -1139520957),
		GROUNDING(1800400, (1 << 24), -1733819072),
		BINDING(1800700, (1 << 25), -1971545187),
		WARDING(1801000, (1 << 26), 2122002462),
		GUILEFUL(1801300, (1 << 27), -1378972677),
		BALEFUL(1801600, -1, 1323012132),
		ARCANE(1801900, (1 << 30), 1323888676),
		WOUNDING(1802200, (1 << 10), 1357392095),
		WEARYING(1802500, (1 << 10), 1350838495),
		CONFUSING(1802800, (1 << 10), 1358702815),
		CHILLING(1803100, (1 << 1), 1332155165),
		SEARING(1803400, (1 << 2), -1401744610),
		THUNDERING(1803700, (1 << 3), -443544829),
		UNHOLY(1804000, (1 << 4), 1330320167),
		BEFUDDLING(1804300, (1 << 5), 1489317547),
		WRATHFUL(1804600, (1 << 6), 165160210),
		SPITEFUL(1804900, (1 << 7), 1238906779),
		ENFEEBLING(1805200, (1 << 8), -908578401),
		CONFOUNDING(1805500, (1 << 9), 165165842),
		DISTRACTING(1805800, (1 << 10), 1238906697),
		WOLFPACK(1806100, (1 << 4), 416932375);

		private final int blueprintUUID;
		private final int effectFlag;
		private final int token;

		SpireType(int blueprint, int flag, int token) {
			this.blueprintUUID = blueprint;
			this.effectFlag = flag;
			this.token = token;
		}

		public int getBlueprintUUID() {
			return blueprintUUID;
		}

		public int getEffectFlag() {
			return effectFlag;
		}

		public int getToken() {
			return token;
		}

		public EffectsBase getEffectBase() {
			return PowersManager.getEffectByToken(token);
		}

		public static SpireType getByBlueprintUUID(int uuid) {

			SpireType outType = SpireType.GROUNDING;

			for (SpireType spireType : SpireType.values()) {

				if (spireType.blueprintUUID == uuid) {
					outType = spireType;
					return outType;
				}

			}

			return outType;
		}

	}

	public enum TransactionType {
		MAINTENANCE(43),
		WITHDRAWL(80),
		DEPOSIT(82),
		MINE(81),
		MIGRATION(83),
		PLAYERREWARD(84),
		TAXRESOURCE(85),
		TAXRESOURCEDEPOSIT(86);

		private final int ID;

		TransactionType(int ID) {
			this.ID = ID;
		}

		public int getID() {
			return ID;
		}
	}

	public enum TargetColor {

		White,
		Green,
		Cyan,
		Blue,
		Yellow,
		Orange,
		Red;

		public static TargetColor getCon(AbstractCharacter source,
										 AbstractCharacter target) {
			return getCon(source.getLevel(), target.getLevel());
		}

		public static TargetColor getCon(short sourceLevel, short targetLevel) {
			if (targetLevel > (sourceLevel + 2))
				return Red;
			else if (targetLevel == (sourceLevel + 2))
				return Orange;
			else if (targetLevel == (sourceLevel + 1))
				return Yellow;

			short lowestBlue = (short) (sourceLevel - (((sourceLevel / 5)) + 2));

			if (lowestBlue <= targetLevel)
				return Blue;
			else if (lowestBlue - 1 <= targetLevel)
				return Cyan;
			else if (lowestBlue - 2 <= targetLevel)
				return Green;
			return White;
		}
	}

	public enum DamageType {
		None,
		Crush,
		Slash,
		Siege,
		Pierce,
		Magic,
		Bleed,
		Poison,
		Mental,
		Holy,
		Unholy,
		Lightning,
		Fire,
		Cold,
		Healing,
		Acid,
		Disease,
		Unknown,
		// these added for immunities
		Attack,
		Powers,
		Combat,
		Spires,
		Snare,
		Stun,
		Blind,
		Root,
		Fear,
		Charm,
		PowerBlock,
		DeBuff,
		Powerblock,
		Steel,
		Drain;
		public static DamageType GetDamageType(String modName){
			DamageType damageType;
			if (modName.isEmpty())
				return DamageType.None;
			
			try{
				 damageType = DamageType.valueOf(modName.replace(",", ""));
			}catch(Exception e){
				Logger.error(e);
				return DamageType.None;
			}
			return damageType;
		}
	}
	
	
	public enum SourceType {
		None,
		Abjuration,
		Acid,
		AntiSiege,
		Archery,
		Axe,
		Bardsong,
		Beastcraft,
		Benediction,
		BladeWeaving,
		Bleed,
		Blind,
		Block,
		Bloodcraft,
		Bow,
		Buff,
		Channeling,
		Charm,
		Cold,
		COLD,
		Constitution,
		Corruption,
		Crossbow,
		Crush,
		Dagger,
		DaggerMastery,
		DeBuff,
		Dexterity,
		Disease,
		Dodge,
		Dragon,
		Drain,
		Earth,
		Effect,
		Exorcism,
		Fear,
		Fire,
		FIRE,
		Fly,
		Giant,
		GreatAxeMastery,
		GreatSwordMastery,
		Hammer,
		Heal,
		Healing,
		Holy,
		HOLY,
		ImmuneToAttack,
		ImmuneToPowers,
		Intelligence,
		Invisible,
		Lightning,
		LIGHTNING,
		Liturgy,
		Magic,
		MAGIC,
		Mental,
		MENTAL,
		NatureLore,
		Necromancy,
		Parry,
		Pierce,
		Poison,
		POISON,
		PoleArm,
		Powerblock,
		Rat,
		ResistDeBuff,
		Restoration,
		Root,
		Shadowmastery,
		Siege,
		Slash,
		Snare,
		Sorcery,
		Spear,
		SpearMastery,
		Spirit,
		Staff,
		Stormcalling,
		Strength,
		Stun,
		Summon,
		Sword,
		SwordMastery,
		Thaumaturgy,
		Theurgy,
		Transform,
		UnarmedCombat,
		UnarmedCombatMastery,
		Unholy,
		UNHOLY,
		Unknown,
		Warding,
		Warlockry,
		WayoftheGaana,
		WearArmorHeavy,
		WearArmorLight,
		WearArmorMedium,
		Wereform,
		Athletics,
		AxeMastery,
		Bargaining,
		BladeMastery,
		FlameCalling,
		GreatHammerMastery,
		HammerMastery,
		Leadership,
		PoleArmMastery,
		Running,
		StaffMastery,
		Throwing,
		Toughness,
		WayoftheWolf,
		WayoftheRat,
		WayoftheBear,
		Orthanatos,
		SunDancing,
		//Power categories.
		AE,
		AEDAMAGE,
		BEHAVIOR,
		BLESSING,
		BOONCLASS,
		BOONRACE,
		BREAKFLY,
		BUFF,
		CHANT,
		DAMAGE,
		DEBUFF,
		DISPEL,
		FLIGHT,
		GROUPBUFF,
		GROUPHEAL,
		HEAL,
		INVIS,
		MOVE,
		RECALL,
		SPECIAL,
		SPIREDISABLE,
		SPIREPROOFTELEPORT,
		STANCE,
		STUN,
		SUMMON,
		TELEPORT,
		THIEF,
		TRACK,
		TRANSFORM,
		VAMPDRAIN,
		WEAPON,
		Wizardry;
		public static SourceType GetSourceType(String modName){
			SourceType returnMod;
			if(modName.isEmpty())
				return SourceType.None;
			
			try{
				 returnMod = SourceType.valueOf(modName.replace(",", ""));
			}catch(Exception e){
				Logger.error(modName);
				Logger.error(e);
				return SourceType.None;
			}
			return returnMod;
		}
	}
	
	public enum EffectSourceType{
		None,
		AttackSpeedBuff,
		Bleeding,
		Blind,
		Buff,
		Chant,
		Charm,
		Cold,
		Combat,
		ConstitutionBuff,
		Crush,
		DamageShield,
		DeathShroud,
		DeBuff,
		Disease,
		Drain,
		Earth,
		Effect,
		Fear,
		Fire,
		Flight,
		Fortitude,
		Heal,
		Holy,
		Invisibility,
		Invulnerability,
		Lightning,
		Magic,
		Mental,
		Multielement,
		PetBuff,
		Pierce,
		Poison,
		Powerblock,
		RecoveryManaBuff,
		ResistDeBuff,
		Root,
		Siege,
		SiegeBuff,
		SiegeDamage,
		Silence,
		Slash,
		Snare,
		Stance,
		Stun,
		Summon,
		Transform,
		Unholy,
		Wereform,
		WereformATRBuff,
		WereformConBuff,
		WereformDexBuff,
		WereformHPRecBuff,
		WereformMoveBuff,
		WereformPhysResBuff,
		WereformSPRecBuff,
		WereformStrBuff;
		
		public static EffectSourceType GetEffectSourceType(String modName){
			EffectSourceType returnMod;
			if(modName.isEmpty())
				return EffectSourceType.None;
			
			try{
				 returnMod = EffectSourceType.valueOf(modName.replace(",", ""));
			}catch(Exception e){
				Logger.error(e);
				return EffectSourceType.None;
			}
			return returnMod;
		}
	}
	
	public enum StackType {
		None,
		AggRangeDeBuff,
		ArcheryPrecisionBuff,
		AttackDebuff,
		AttackSpeedBuff,
		AttackSpeedDeBuff,
		AttackValueBuff,
		AttackValueDebuff,
		AttrCONBuff,
		AttrCONDebuff,
		AttrDEXBuff,
		AttrINTBuff,
		AttrSPRBuff,
		AttrSTRBuff,
		Bleeding,
		Blindness,
		BluntResistanceDebuff,
		BMHealing,
		Charm,
		ClassBoon,
		Confusion,
		DamageAbsorber,
		DamageDebuff,
		DamageModifierBuff,
		DamageShield,
		DeathShroud,
		DefenseBuff,
		DefenseBuffGroup,
		DefenseDebuff,
		DetectInvis,
		DrainImmunity,
		ElementalDeBuff,
		EnchantWeapon,
		Fear,
		Flight,
		Frenzy,
		GroupHeal,
		HealingBuff,
		HealOverTime,
		HealResBuff,
		HealthPotion,
		IgnoreStack,
		Invisible,
		ManaPotion,
		MangonelFire,
		MeleeDamageDeBuff,
		MeleeDeBuff,
		MoveBuff,
		MoveDebuff,
		NoFear,
		NoPassiveDefense,
		NoPowerBlock,
		NoPowerInhibitor,
		NoRecall,
		NoSnare,
		NoStun,
		NoTrack,
		PassiveDefense,
		PersAttrSPRBuff,
		PetBuff,
		PierceResistanceDebuff,
		PoisonBuchinine,
		PoisonGalpa,
		PoisonGorgonsVenom,
		PoisonMagusbane,
		PoisonPellegorn,
		PowerBlock,
		PowerCostBuff,
		PowerDamageModifierBuff,
		PowerInhibitor,
		PrecisionBuff,
		Protection,
		RaceBoon,
		RecoveryHealthBuff,
		RecoveryHealthDeBuff,
		RecoveryManaBuff,
		RecoveryManaDeBuff,
		RecoveryStaminaBuff,
		RecoveryStaminaDeBuff,
		ResistanceBuff,
		ResistanceDeBuff,
		ResistanceDebuff,
		Root,
		SafeMode,
		SelfOneAttrBuff,
		SelfThreeAttrBuff,
		SelfTwoAttrBuff,
		SiegeDebuff,
		SiegeWeaponBuff,
		Silence,
		SkillDebuff,
		SlashResistanceDebuff,
		Snare,
		StackableAttrCONBuff,
		StackableAttrDEXBuff,
		StackableAttrSTRBuff,
		StackableDefenseBuff,
		StackableRecoveryHealthBuff,
		StackableRecoveryStaminaBuff,
		StaminaPotion,
		StanceA,
		StanceB,
		Stun,
		Track,
		Transform,
		WeaponMove;
		public static StackType GetStackType(String modName){
			StackType stackType;
			if (modName.isEmpty())
				return StackType.None;
			
			try{
				 stackType = StackType.valueOf(modName.replace(",", ""));
			}catch(Exception e){
				Logger.error(modName);
				Logger.error(e);
				return StackType.None;
			}
			return stackType;
		}
	}
	
	public enum ModType {
		None,
		AdjustAboveDmgCap,
		Ambidexterity,
		AnimOverride,
		ArmorPiercing,
		AttackDelay,
		Attr,
		BlackMantle,
		BladeTrails,
		Block,
		BlockedPowerType,
		CannotAttack,
		CannotCast,
		CannotMove,
		CannotTrack,
		Charmed,
		ConstrainedAmbidexterity,
		DamageCap,
		DamageShield,
		DCV,
		Dodge,
		DR,
		Durability,
		ExclusiveDamageCap,
		Fade,
		Fly,
		Health,
		HealthFull,
		HealthRecoverRate,
		IgnoreDamageCap,
		IgnorePassiveDefense,
		ImmuneTo,
		ImmuneToAttack,
		ImmuneToPowers,
		Invisible,
		ItemName,
		Mana,
		ManaFull,
		ManaRecoverRate,
		MaxDamage,
		MeleeDamageModifier,
		MinDamage,
		NoMod,
		OCV,
		Parry,
		PassiveDefense,
		PowerCost,
		PowerCostHealth,
		PowerDamageModifier,
		ProtectionFrom,
		Resistance,
		ScaleHeight,
		ScaleWidth,
		ScanRange,
		SeeInvisible,
		Silenced,
		Skill,
		Slay,
		Speed,
		SpireBlock,
		Stamina,
		StaminaFull,
		StaminaRecoverRate,
		Stunned,
		Value,
		WeaponProc,
		WeaponRange,
		WeaponSpeed;
		
		public static ModType GetModType(String modName){
			ModType modType;
			if (modName.isEmpty())
				return ModType.None;
			
			try{
				 modType = ModType.valueOf(modName.replace(",", ""));
			}catch(Exception e){
				Logger.error(e);
				return ModType.None;
			}
			return modType;
		}
	}
	public enum MovementState {

		IDLE,
		SITTING,
		RUNNING,
		FLYING,
		SWIMMING;
	}

	public enum DoorState {

		OPEN,
		CLOSED,
		LOCKED,
		UNLOCKED;
	}

	// Used with stored procedure GET_UID_ENUM() for
	// type tests against objects not yet loaded into the game.
	public enum DbObjectType {

		INVALID,
		ACCOUNT,
		BUILDING,
		CHARACTER,
		CITY,
		CONTAINER,
		GUILD,
		ITEM,
		MINE,
		MOB,
		NPC,
		SHRINE,
		WORLDSERVER,
		ZONE,
		WAREHOUSE;
	}

	;

	/**
	 * Enumeration of Building Protection Status stored in the database as a
	 * mysql enumfield. WARNING: This enumeration is fragile. Do not rename. Do
	 * not reorder.
	 */
	public enum ProtectionState {

		NONE,
		PROTECTED,
		UNDERSIEGE,
		CEASEFIRE,
		CONTRACT,
		DESTROYED,
		PENDING,
		NPC;
	}

	;

	public enum CharacterSkills {

		Archery((1L << 1), -529201545, 20),
		Athletics((1L << 2), -327713877, 15),
		AxeMastery((1L << 3), 1103042709, 20),
		Axe((1L << 4), 73505, 1),
		Bardsong((1L << 5), 454246953, 10),
		Bargaining((1L << 6), 372927577, 10),
		Beastcraft((1L << 7), 56772766, 10),
		Benediction((1L << 8), 1464998706, 1),
		BladeMastery((1L << 9), -59908956, 20),
		BladeWeaving((1L << 10), -1839362429, 20),
		Block((1L << 11), 76592546, 3),
		Bow((1L << 12), 87490, 1),
		Channeling((1L << 13), -1899060872, 20),
		Crossbow((1L << 14), 1092138184, 1),
		DaggerMastery((1L << 15), -1549224741, 20),
		Dagger((1L << 16), -1603103740, 1),
		Dodge((1L << 17), 74619332, 5),
		FlameCalling((1L << 18), -1839578206, 20),
		GreatAxeMastery((1L << 19), 1427003458, 20),
		GreatHammerMastery((1L << 20), -309659310, 20),
		GreatSwordMastery((1L << 21), 2054956946, 20),
		HammerMastery((1L << 22), -1548903209, 20),
		Hammer((1L << 23), -1602765816, 1),
		Leadership((1L << 24), 1618560984, 20),
		Liturgy((1L << 25), -888415974, 10),
		NatureLore((1L << 26), -1911171474, 10),
		Parry((1L << 27), 95961104, 5),
		PoleArmMastery((1L << 28), -1432303709, 20),
		PoleArm((1L << 29), -1037845588, 1),
		Restoration((1L << 30), -504697054, 1),
		Running((1L << 31), 1488335491, 10),
		Shadowmastery((1L << 32), 1389222957, 10),
		Sorcery((1L << 33), -529481275, 1),
		SpearMastery((1L << 34), -48279755, 20),
		Spear((1L << 35), 83992115, 1),
		StaffMastery((1L << 36), -61022283, 20),
		Staff((1L << 37), 71438003, 1),
		Stormcalling((1L << 38), -532064061, 10),
		SwordMastery((1L << 39), -59316267, 20),
		Sword((1L << 40), 73938643, 1),
		Thaumaturgy((1L << 41), -2020131447, 10),
		Theurgy((1L << 42), -888431326, 10),
		Throwing((1L << 43), 391562015, 20),
		Toughness((1L << 44), -660435875, 10),
		UnarmedCombatMastery((1L << 45), 1692733771, 20),
		UnarmedCombat((1L << 46), -1094332856, 1),
		Warding((1L << 47), 1488142342, 1),
		Warlockry((1L << 48), 1121393557, 10),
		WayoftheGaana((1L << 49), -1954832975, 10),
		WearArmorHeavy((1L << 50), 1112121635, 15),
		WearArmorLight((1L << 51), 38031547, 1),
		WearArmorMedium((1L << 52), 468015203, 5),
		Wizardry((1L << 53), 218227659, 10),
		Corruption((1L << 54), -1519268706, 10),
		Abjuration((1L << 55), -2029900484, 10),
		WayoftheWolf((1L << 56), 1668913067, 20),
		WayoftheRat((1L << 57), -2114353637, 20),
		WayoftheBear((1L << 58), -906390863, 20),
		Orthanatos((1L << 59), -666929185, 20),
		Bloodcraft((1L << 60), 40661438, 10),
		Exorcism((1L << 61), 1444427097, 10),
		Necromancy((1L << 62), -556571154, 10),
		SunDancing((1L << 63), 22329752, 20);

		private long flag;
		private int token;
		private int reqLvl;

		CharacterSkills(long flag, int token, int reqLvl) {
			this.flag = flag;
			this.token = token;
			this.reqLvl = reqLvl;
		}

		public long getFlag() {
			return flag;
		}

		public int getReqLvl() {
			return this.reqLvl;
		}

		public void setFlag(long flag) {
			this.flag = flag;
		}

		public int getToken() {
			return token;
		}

		public void setToken(int token) {
			this.token = token;
		}

		public static CharacterSkills GetCharacterSkillByToken(int token) {
			for (CharacterSkills skill : CharacterSkills.values()) {
				if (skill.token == token)
					return skill;
			}

			Logger.info("Returned No Skill for token " + token + ". Defaulting to Axe");
			return CharacterSkills.Axe;
		}
	}

	;

	public enum GuildHistoryType {
		JOIN(1),
		LEAVE(4),
		BANISHED(3),
		CREATE(7),
		DISBAND(5);
		private final int type;

		GuildHistoryType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}

	public enum SexType {
		NONE,
		MALE,
		FEMALE;
	}

	public enum ClassType {
		FIGHTER,
		HEALER,
		ROGUE,
		MAGE;
	}

	public enum PromoteType {
		Assassin(SexType.NONE),
		Barbarian(SexType.NONE),
		Bard(SexType.NONE),
		Channeler(SexType.NONE),
		Confessor(SexType.NONE),
		Crusader(SexType.NONE),
		Doomsayer(SexType.NONE),
		Druid(SexType.NONE),
		Fury(SexType.FEMALE),
		Huntress(SexType.FEMALE),
		Prelate(SexType.NONE),
		Priest(SexType.NONE),
		Ranger(SexType.NONE),
		Scout(SexType.NONE),
		Sentinel(SexType.NONE),
		Templar(SexType.NONE),
		Thief(SexType.NONE),
		Warlock(SexType.MALE),
		Warrior(SexType.NONE),
		Wizard(SexType.NONE),
		Nightstalker(SexType.NONE),
		Necromancer(SexType.NONE),;

		private SexType sexRestriction;

		PromoteType(SexType sexRestriction) {
			this.sexRestriction = sexRestriction;
		}

		public SexType getSexRestriction() {
			return sexRestriction;
		}
	}

	public enum ShrineType {

		Aelfborn(1701900, -75506007, true),
		Aracoix(1703100, -563708986, true),
		Centaur(1704000, 521645243, true),
		Dwarf(1708500, -2000467257, true),
		Elf(1703400, 1254603001, true),
		HalfGiant(1709100, 349844468, true),
		Human(1702200, 281172391, true),
		Irekei(1702800, -764988442, true),
		Minotaur(1704600, 549787579, true),
		Nephilim(1701000, -655183799, true),
		Shade(1700100, 1724071104, true),
		Assassin(1700400, 1989015892, false),
		Barbarian(1708800, 9157124, false),
		Bard(1704300, 80190554, false),
		Channeler(1702500, 5658278, false),
		Confessor(1707600, 1871658719, false),
		Crusader(1706700, -187454619, false),
		Doomsayer(1700700, -993659433, false),
		Druid(1701600, -926740122, false),
		Fury(1705500, 214401375, false),
		Huntress(1704900, 970312892, false),
		Prelate(1707000, -225200922, false),
		Priest(1705200, -535691898, false),
		Ranger(1701300, 604716986, false),
		Scout(1706100, -1497297486, false),
		Sentinel(1707300, -184898375, false),
		Templar(1707900, 826673315, false),
		Thief(1708200, 1757633920, false),
		Warlock(1706400, 1003385946, false),
		Warrior(1703700, 931048026, false),
		Wizard(1705800, 777115928, false),
		Nightstalker(1709400, 373174890, false),
		Necromancer(1709700, -319294505, false),
		Vampire(1710000, 1049274530, true);

		private final int blueprintUUID;
		private final int powerToken;
		private final ArrayList<Shrine> shrines = new ArrayList<>();
		private final boolean isRace;

		ShrineType(int blueprintUUID, int powerToken, boolean isRace) {
			this.blueprintUUID = blueprintUUID;
			this.powerToken = powerToken;
			this.isRace = isRace;

		}

		public int getBlueprintUUID() {
			return blueprintUUID;
		}

		public int getPowerToken() {
			return powerToken;
		}

		public ArrayList<Shrine> getShrinesCopy() {
			ArrayList<Shrine> copyShrines = new ArrayList<>();
			copyShrines.addAll(shrines);
			Collections.sort(copyShrines);
			return copyShrines;
		}

		public final void addShrineToServerList(Shrine shrine) {
			synchronized (shrines) {
				shrines.add(shrine);
			}
		}

		public final void RemoveShrineFromServerList(Shrine shrine) {
			synchronized (shrines) {
				shrines.remove(shrine);
			}
		}

		public boolean isRace() {
			return isRace;
		}
	}

	public enum GuildState {

		Errant(0),
		Sworn(4),
		Protectorate(6),
		Petitioner(2),
		Province(8),
		Nation(5),
		Sovereign(7);

		private final int stateID;

		GuildState(int stateID) {
			this.stateID = stateID;
		}

		public int getStateID() {
			return stateID;
		}

	}


	// Building group enumeration.
	// This is used to drive linear equations to calculate
	// structure hp, ranking times and such from within
	// the BuildingBlueprint class.
	//
	// It is also used as a bitvector flag in the npc
	// building slot mechanics.

	public enum BuildingGroup implements EnumBitSetHelper<BuildingGroup> {
		NONE(0,0),
		TOL(64f, 64f),
		BARRACK(32f, 64f),
		CHURCH(64f, 64f),
		FORGE(32f, 64f),
		SPIRE(16f, 16f),
		GENERICNOUPGRADE(16f, 16f),
		WALLSTRAIGHT(16f, 64),
		WALLCORNER(64f, 64f),
		SMALLGATE(64f, 64),
		ARTYTOWER(64f, 64),
		SIEGETENT(32f, 32f),
		BANESTONE(16f, 16f),
		MINE(16f, 16f),
		WAREHOUSE(32f, 32f),
		SHRINE(16f, 16f),
		RUNEGATE(64f, 64f),
		AMAZONHALL(64f, 64f),
		CATHEDRAL(64f, 64f),
		GREATHALL(64f, 64f),
		KEEP(64f, 64f),
		THIEFHALL(64f, 24f),
		TEMPLEHALL(64f, 64f),
		WIZARDHALL(64f, 64f),
		ELVENHALL(64f, 64f),
		ELVENSANCTUM(64f, 64f),
		IREKEIHALL(64f, 64f),
		FORESTHALL(64f, 64f),
		MAGICSHOP(32f, 32f),
		BULWARK(32f, 32f),
		SHACK(16f, 16f),
		INN(64f, 32f),
		TAILOR(32f, 32f),
		VILLA(64f, 32f),
		ESTATE(64f, 64f),
		FORTRESS(64f, 64f),
		CITADEL(64f, 64f),
		WALLSTRAIGHTTOWER(16f, 64),
		WALLSTAIRS(64,64);
		
		private final Vector2f extents;

		BuildingGroup(float extentX, float extentY) {
			this.extents = new Vector2f(extentX, extentY);
		}

		public Vector2f getExtents() {
			return extents;
		}

	}
	
	public enum UpdateType{
		ALL,
		MOVEMENT,
		REGEN,
		FLIGHT,
		LOCATION,
		MOVEMENTSTATE;
	}
	
	public enum ServerType{
		WORLDSERVER,
		LOGINSERVER,
		NONE;
	}
	
	public enum ChatChannel implements EnumBitSetHelper<ChatChannel> {
		System,
		Announce,
		Unknown,
		Commander,
		Address,
		Nation,
		Leader,
		Shout,
		Siege,
		Territory,
		Info,
		CSR,
		Guild,
		InnerCouncil,
		Group,
		City,
		Say,
		Emote,
		Social,
		Tell,
		Combat,
		Powers,
		Snoop,
		Debug,
		Global,
		Trade,
		PVP,
		Mine,
		Alert,
		Assassin,
		Barbarian,
		Bard,
		Channeler,
		Confessor,
		Crusader,
		Doomsayer,
		Druid,
		Fury,
		Huntress,
		Necromancer,
		Nightstalker,
		Prelate,
		Priest,
		Ranger,
		Scout,
		Sentinel,
		Templar,
		Thief,
		Warlock,
		Warrior,
		Wizard;

	}

	public enum AllianceType {
		RecommendedAlly,
		RecommendedEnemy,
		Ally,
		Enemy;
	}
	
	public enum FriendStatus {
		Available,
		Away,
		Busy;
	}
	
	public enum ProfitType {
		
		
		BuyNormal("buy_normal"),
		BuyGuild("buy_guild"),
		BuyNation("buy_nation"),
		SellNormal("sell_normal"),
		SellGuild("sell_guild"),
		SellNation("sell_nation");
		
		public String dbField;

		private ProfitType(String dbField) {
			this.dbField = dbField;
		}
	}

	public enum GameObjectType {

		/*
		 * These will be used as the 4 high bytes in the application protocol's
		 * long CompositeID field and when tracking an AbstractGameObject's type
		 * from within the code. The low 4 bytes will be used as the Object's
		 * UUID
		 */
		unknown,
		Account,
		AccountIP,
		ActiveEffect,
		ArmorBase,
		BaseClass,
		BeardStyle,
		BlockedIP,
		Building,
		BuildingLocation,
		BuildingModelBase,
		CharacterPower,
		CharacterPowers,
		CharacterRune,
		CharacterSkill,
		City,
		Contract,
		Corpse,
		CSSession,
		EffectsResourceCosts,
		EnchantmentBase,
		GenericItemBase,
		Group,
		Guild,
		GuildAllianceEnemy,
		GuildBanish,
		GuildCharacterKOS,
		GuildGuildKOS,
		GuildTableList,
		HairStyle,
		Item,
		ItemContainer,
		ItemEnchantment,
		JewelryBase,
		Kit,
		MenuOption,
		Mine,
		Mob,
		MobBase,
		MobEquipment,
		MobLoot,
		MobType,
		NPC,
		NPCClassRune,
		NPCClassRuneThree,
		NPCClassRuneTwo,
		NPCExtraRune,
		NPCRaceRune,
		NPCRune,
		NPCShopkeeperRune,
		NPCTrainerRune,
		Nation,
		PlayerCharacter,
		PlayerInfo,
		PowerGrant,
		PowerReq,
		PowersBase,
		PowersBaseAttribute,
		PromotionClass,
		Race,
		RuneBase,
		RuneBaseAttribute,
		RuneBaseEffect,
		SkillReq,
		SkillsBase,
		SkillsBaseAttribute,
		SpecialLoot,
		StrongBox,
		Trigger,
		ValidRaceBeardStyle,
		ValidRaceClassCombo,
		ValidRaceHairStyle,
		VendorDialog,
		Warehouse,
		WeaponBase,
		WorldServerInfo,
		WorldServerInfoSnapshot,
		Shrine,
		Zone,
		Transaction;
	}

	public enum ContainerType {
		BANK,
		INVENTORY,
		VAULT;
	}

	;

	public enum CompoundCurveType {
		DefaultFlat(0),
		DefaultSlope(1),
		DefaultSlopeDown(-1),
		SL0001Up(0.01),
		SL0003Up(0.03),
		SL0005Up(0.05),
		SL0006Up(0.06),
		SL0007Up(0.07),
		SL0008Up(0.08),
		SL0010Up(0.10),
		SL0011Up(0.11),
		SL0012Up(0.12),
		SL0013Up(0.13),
		SL0014Up(0.14),
		SL00143U(0.143),
		SL0015Up(0.15),
		SL0016Up(0.16),
		SL0019Up(0.19),
		SL0020Up(0.20),
		SL0021Up(0.21),
		SL0022Up(0.22),
		SL0023Up(0.23),
		SL0024Up(0.24),
		SL0025Up(0.25),
		SL0026Up(0.26),
		SL0028Up(0.28),
		SL0030Up(0.30),
		SL0031Up(0.31),
		SL0032Up(0.32),
		SL0033Up(0.33),
		SL0034Up(0.34),
		SL0035Up(0.35),
		SL0037Up(0.37),
		SL0038Up(0.38),
		SL0039Up(0.39),
		SL0040Up(0.40),
		SL0041Up(0.41),
		SL0042Up(0.42),
		SL0043Up(0.43),
		SL0044Up(0.44),
		SL0045Up(0.45),
		SL0046Up(0.46),
		SL0047Up(0.47),
		SL0048Up(0.48),
		SL0050Up(0.50),
		SL0051Up(0.51),
		SL0053Up(0.53),
		SL0054Up(0.54),
		SL0055Up(0.55),
		SL0056Up(0.56),
		SL0057Up(0.57),
		SL0058Up(0.58),
		SL0060Up(0.60),
		SL0061Up(0.61),
		SL0063Up(0.63),
		SL0064Up(0.64),
		SL0065Up(0.65),
		SL0066Up(0.66),
		SL0067Up(0.67),
		SL0068Up(0.68),
		SL0069Up(0.69),
		SL0070Up(0.70),
		SL0071Up(0.71),
		SL0073Up(0.73),
		SL0074Up(0.74),
		SL0075Up(0.75),
		SL0076Up(0.76),
		SL0077Up(0.77),
		SL0079Up(0.79),
		SL0080Up(0.80),
		SL0081Up(0.81),
		SL0082Up(0.82),
		SL0083Up(0.83),
		SL0084Up(0.84),
		SL0085Up(0.85),
		SL0087Up(0.87),
		SL0088Up(0.88),
		SL0089Up(0.89),
		SL0090Up(0.90),
		SL0092Up(0.92),
		SL0098Up(0.98),
		SL0100Up(1.00),
		SL0106Up(1.06),
		SL0109Up(1.09),
		SL0112Up(1.12),
		SL0113Up(1.13),
		SL0115Up(1.15),
		SL0116Up(1.16),
		SL0122Up(1.22),
		SL0123Up(1.23),
		SL0125Up(1.25),
		SL0128Up(1.28),
		SL0130Up(1.30),
		SL0135Up(1.35),
		SL0140Up(1.40),
		SL0143Up(1.43),
		SL0145Up(1.45),
		SL0150Up(1.50),
		SL0154Up(1.54),
		SL0163Up(1.63),
		SL0166Up(1.66),
		SL0175Up(1.75),
		SL0188Up(1.88),
		SL0190Up(1.90),
		SL0200Up(2.00),
		SL0222Up(2.22),
		SL0225Up(2.25),
		SL0235Up(2.35),
		SL0238Up(2.38),
		SL0250Up(2.50),
		SL0260Up(2.60),
		SL0263Up(2.63),
		SL0275Up(2.75),
		SL0280Up(2.80),
		SL0300Up(3.00),
		SL0308Up(3.08),
		SL0312Up(3.12),
		SL0350Up(3.50),
		SL0357Up(3.57),
		SL0360Up(3.60),
		SL0375Up(3.75),
		SL0380Up(3.80),
		SL0385Up(3.85),
		SL0400Up(4.00),
		SL0410Up(4.10),
		SL0429Up(4.29),
		SL0450Up(4.50),
		SL0460Up(4.60),
		SL0480Up(4.80),
		SL0500Up(5.00),
		SL0510Up(5.10),
		SL0550Up(5.50),
		SL0600Up(6.00),
		SL0643Up(6.43),
		SL0714Up(7.14),
		SL0750Up(7.50),
		SL0790Up(7.90),
		SL0800Up(8.00),
		SL0900Up(9.00),
		SL1000Up(10.00),
		SL1050Up(10.50),
		SL1100Up(11.00),
		SL1125Up(11.25),
		SL1200Up(12.00),
		SL1282Up(12.82),
		SL1300Up(13.00),
		SL1350Up(13.50),
		SL1400Up(14.00),
		SL1500Up(15.00),
		SL1579Up(15.79),
		SL2000Up(20.00),
		SL2100Up(21.00),
		SL2500Up(25.00),
		SL2521Up(25.21),
		SL3000Up(30.00),
		SL4000Up(40.00),
		SL5000Up(50.00),
		SL6000Up(60.00),
		SL7500Up(75.00),
		SL8000Up(80.00),
		SL12000Up(120.00),
		SL14000Up(140.00),
		SL30000Up(300.00),
		SL66600Up(666.00),
		SL71500Up(715.00),
		SL00003Down(-0.003),
		SL0001Down(-0.01),
		SL0003Down(-0.03),
		SL0004Down(-0.04),
		SL0005Down(-0.05),
		SL0006Down(-0.06),
		SL0007Down(-0.07),
		SL00075Down(-0.075),
		SL0008Down(-0.08),
		SL0009Down(-0.09),
		SL0010Down(-0.10),
		SL0011Down(-0.11),
		SL0012Down(-0.12),
		SL0013Down(-0.13),
		SL00125Down(-0.125),
		SL0014Down(-0.14),
		SL0015Down(-0.15),
		SL0016Down(-0.16),
		SL0017Down(-0.17),
		SL00175Down(-0.175),
		SL0018Down(-0.18),
		SL0019Down(-0.19),
		SL0020Down(-0.20),
		SL0023Down(-0.23),
		SL0024Down(-0.24),
		SL0025Down(-0.25),
		SL0027Down(-0.27),
		SL0028Down(-0.28),
		SL0029Down(-0.29),
		SL0030Down(-0.30),
		SL0032Down(-0.32),
		SL0033Down(-0.33),
		SL0035Down(-0.35),
		SL0038Down(-0.38),
		SL0040Down(-0.40),
		SL0044Down(-0.44),
		SL0045Down(-0.45),
		SL0050Down(-0.50),
		SL0055Down(-0.55),
		SL0060Down(-0.60),
		SL0062Down(-0.62),
		SL0063Down(-0.63),
		SL0064Down(-0.64),
		SL0066Down(-0.66),
		SL0069Down(-0.69),
		SL0071Down(-0.71),
		SL0075Down(-0.75),
		SL0077Down(-0.77),
		SL0079Down(-0.79),
		SL0080Down(-0.80),
		SL0090Down(-0.90),
		SL0100Down(-1.00),
		SL0113Down(-1.13),
		SL0120Down(-1.20),
		SL0125Down(-1.25),
		SL0128Down(-1.28),
		SL0130Down(-1.30),
		SL0135Down(-1.35),
		SL0150Down(-1.50),
		SL0175Down(-1.75),
		SL0188Down(-1.88),
		SL0200Down(-2.00),
		SL0225Down(-2.25),
		SL0250Down(-2.50),
		SL0263Down(-2.63),
		SL0300Down(-3.00),
		SL0357Down(-3.57),
		SL0385Down(-3.85),
		SL0429Down(-4.29),
		SL0450Down(-4.50),
		SL0500Down(-5.00),
		SL0550Down(-5.50),
		SL0600Down(-6.00),
		SL0643Down(-6.43),
		SL0714Down(-7.14),
		SL0750Down(-7.50),
		SL0790Down(-7.90),
		SL0800Down(-8.00),
		SL1000Down(-10.00),
		SL1050Down(-10.50),
		SL1200Down(-12.00),
		SL1350Down(-13.50),
		SL1500Down(-15.00),
		SL1579Down(-15.79),
		SL2000Down(-20.00),
		SL2400Down(-24.00),
		SL2500Down(-25.00),
		SL3000Down(-30.00),
		SL4500Down(-45.00),
		SL7500Down(-75.00),
		SIVL0005(0.005),
		SIVL0008(0.008),
		SIVL0009(0.009),
		SIVL0010(0.010),
		SIVL0012(0.012),
		SIVL0013(0.013),
		SIVL0014(0.014),
		SIVL0015(0.015),
		SIVL0016(0.016),
		SIVL0017(0.017),
		SIVL0019(0.019),
		SIVL0020(0.020),
		SIVL0021(0.021),
		SIVL0022(0.022),
		SIVL0023(0.023),
		SIVL0024(0.024),
		SIVL0025(0.025),
		SIVL0026(0.026),
		SIVL0027(0.027),
		SIVL0029(0.029),
		SIVL0030(0.030),
		SIVL0031(0.031),
		SIVL0032(0.032),
		SIVL0033(0.033),
		SIVL0034(0.034),
		SIVL0035(0.035),
		SIVL0036(0.036),
		SIVL0038(0.038),
		SIVL0040(0.040),
		SIVL0044(0.044),
		SIVL0046(0.046),
		SIVL0048(0.048),
		SIVL0055(0.055),
		SIVL0056(0.056),
		SIVL0057(0.057),
		SIVL0058(0.058),
		SIVL0060(0.060),
		SIVL0061(0.061),
		SIVL0066(0.066),
		SIVL0067(0.067),
		SIVL0075(0.075),
		SIVL0078(0.078),
		SIVL0130(0.130),
		SIVL0150(0.150),
		SIVL0205(0.205),
		SIVL0220(0.220),
		SIVL0243(0.243),
		SIVL0360(0.360);

		private final double value;

		private CompoundCurveType(double value) {

			this.value = value;
		}

		public double getValue() {
			return value;
		}
	}
	
	public enum PowerFailCondition{
		
		Attack,
		AttackSwing,
		Cast,
		CastSpell,
		EquipChange,
		Logout,
		Move,
		NewCharm,
		Sit,
		TakeDamage,
		TerritoryClaim,
		UnEquip;
	}
	
	public enum PowerSubType{
		Amount,
		Ramp,
		UseAddFormula,
		DamageType1,
		DamageType2,
		DamageType3,
		Cancel;
	}

	public enum PowerCategoryType {
		NONE,
		WEAPON,
		BUFF,
		DEBUFF,
		SPECIAL,
		DAMAGE,
		DISPEL,
		INVIS,
		STUN,
		TELEPORT,
		HEAL,
		VAMPDRAIN,
		BLESSING,
		BOONRACE,
		BOONCLASS,
		BEHAVIOR,
		CHANT,
		GROUPBUFF,
		MOVE,
		FLIGHT,
		GROUPHEAL,
		AEDAMAGE,
		BREAKFLY,
		AE,
		TRANSFORM,
		TRACK,
		SUMMON,
		STANCE,
		RECALL,
		SPIREPROOFTELEPORT,
		SPIREDISABLE,
		THIEF;
	}

	public enum PowerTargetType {

		SELF,
		PCMOBILE,
		PET,
		MOBILE,
		PC,
		WEAPON,
		GUILDLEADER,
		BUILDING,
		GROUP,
		ARMORWEAPONJEWELRY,
		CORPSE,
		JEWELRY,
		WEAPONARMOR,
		ARMOR,
		ITEM;
	}

	public enum objectMaskType {
		PLAYER,
		MOB,
		PET,
		CORPSE,
		BUILDING,
		UNDEAD,
		BEAST,
		HUMANOID,
		NPC,
		IAGENT,
		DRAGON,
		RAT,
		SIEGE,
		CITY,
		ZONE;

		public static EnumSet<objectMaskType> AGGRO = EnumSet.of(PLAYER, PET);
		public static EnumSet<objectMaskType> MOBILE = EnumSet.of(PLAYER, MOB, PET);
		public static EnumSet<objectMaskType> STATIC = EnumSet.of(CORPSE, BUILDING, NPC);

	}

	public enum ItemContainerType {
		NONE,
		INVENTORY,
		EQUIPPED,
		BANK,
		VAULT,
		FORGE,
		WAREHOUSE;
	}

	public enum ItemSlotType implements EnumBitSetHelper<ItemSlotType> {
		RHELD,
		LHELD,
		HELM,
		CHEST,
		SLEEVES,
		HANDS,
		RRING,
		LRING,
		AMULET,
		LEGS,
		FEET,
		CLOAK,
		SHIN,
		UPLEGS,
		UPARM,
		WINGS,
		BEARD,
		HAIR;
	}

	public enum CityBoundsType {

		GRID(512),
		ZONE(576),
		SIEGE(1040);

		public final float extents;

		CityBoundsType(float extents) {
		this.extents = extents;
		}
	}

	public enum GuildType {
		NONE("None", new String[][] {{"None"}}, new String[] {"Thearchy", "Common Rule", "Theocracy", "Republic Rule"}),
		CATHEDRAL("Church of the All-Father", new String[][]{
			{"Acolyte","Acolyte"},
			{"Catechist"},
			{"Deacon", "Deaconess"},
			{"Priest", "Priestess"},
			{"High Priest", "High Priestess"},
			{"Bishop", "Bishop"},
			{"Lord Cardinal", "Lady Cardinal"},
			{"Patriarch", "Matriarch"}},
				new String[] {"Thearchy", "Common Rule", "Theocracy", "Republic Rule"}),
		MILITARY("Military", new String[][] {
			{"Recruit"},
			{"Footman"},
			{"Corporal"},
			{"Sergeant"},
			{"Lieutenant"},
			{"Captain"},
			{"General"},
			{"Lord Marshall","Lady Marshall"}},
				new String[]{"Autocracy", "Common Rule", "Council Rule", "Militocracy"}),
		TEMPLE("Temple of the Cleansing Flame", new String[][]{
			{"Aspirant"},
			{"Novice"},
			{"Initiate"},
			{"Inquisitor"},
			{"Jannisary"},
			{"Tribune"},
			{"Lictor"},
			{"Justiciar"},
			{"Pontifex","Pontifectrix"}},
				new String[] {"Despot Rule", "Common Rule", "Protectorship", "Republic Rule"}),
		BARBARIAN("Barbarian Clan", new String[][] {
			{"Barbarian"},
			{"Skald"},
			{"Raider"},
			{"Karl"},
			{"Jarl"},
			{"Chieftain"},
			{"Thane"}},
				new String[]{"Chiefdom", "Common Rule", "Council Rule", "Republic Rule"}),
		RANGER("Ranger's Brotherhood", new String[][] {
			{"Yeoman"},
			{"Pathfinder"},
			{"Tracker"},
			{"Seeker"},
			{"Protector"},
			{"Guardian"},
			{"Lord Protector","Lady Protector"}},
				new String[]{"Despot Rule", "Collectivism","Council Rule","Republic Rule"}),
		AMAZON("Amazon Temple", new String[][] {
			{"Amazon Thrall", "Amazon"},
			{"Amazon Slave", "Amazon Warrior"},
			{"Amazon Servant", "Amazon Chieftess"},
			{"Amazon Consort", "Amazon Princess"},
			{"Amazon Seneschal", "Majestrix"},
			{"Amazon Regent", "Imperatrix"}},
				new String[] {"Despot Rule", "Common Rule", "Gynarchy", "Gynocracy"}),
		NOBLE("Noble House", new String[][] {
			{"Serf"},
			{"Vassal"},
			{"Exultant"},
			{"Lord", "Lady"},
			{"Baron", "Baroness"},
			{"Count", "Countess"},
			{"Duke", "Duchess"},
			{"King", "Queen"},
			{"Emperor", "Empress"}},
				new String[] {"Monarchy", "Common Rule", "Feodality", "Republic"}),
		WIZARD("Wizard's Conclave", new String[][] {
			{"Apprentice"},
			{"Neophyte"},
			{"Adeptus Minor"},
			{"Adeptus Major"},
			{"Magus"},
			{"High Magus"},
			{"Archmagus"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Magocracy"}),
		MERCENARY("Mercenary Company", new String[][] {
			{"Soldier"},
			{"Man-at-Arms"},
			{"Veteran"},
			{"Myrmidon"},
			{"Captain"},
			{"Commander"},
			{"High Commander"},
			{"Warlord"}},
				new String[] {"Magistrature", "Mob Law", "Council Rule", "Republic Rule"}),
		THIEVES("Thieve's Den", new String[][] {
			{"Urchin"},
			{"Footpad"},
			{"Grifter"},
			{"Burglar"},
			{"Collector"},
			{"Naster Thief"},
			{"Treasurer"},
			{"Grandmaster Thief"},
			{"Grandfather"}},
				new String[] {"Despot Rule", "Common Rule", "Oligarchy", "Republic Rule"}),
		DWARF("Dwarf Hold", new String[][] {
			{"Citizen"},
			{"Master"},
			{"Councilor"},
			{"Thane"},
			{"Great Thane"},
			{"High Thane"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		HIGHCOURT("High Court", new String[][] {
			{"Eccekebe"},
			{"Saedulor"},
			{"Hodrimarth"},
			{"Mandrae"},
			{"Imaelin"},
			{"Thaelostor", "Thaelostril"},
			{"Dar Thaelostor", "Dar Thaelostril"},
			{"Aglaeron"},
			{"Ellestor", "Elestril"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		VIRAKT("Virakt", new String[][] {
			{"Jov'uus"},
			{"Urikhan"},
			{"Irkhan"},
			{"Khal'usht"},
			{"Arkhalar"},
			{"Khal'uvho"},
			{"Khar'uus"},
			{"Kryqh'khalin"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		BRIALIA("Coven of Brialia", new String[][] { // Unknown Rank names
			{"Devotee"},
			{"Initiated"},
			{"Witch of the First"},
			{"Witch of the Second"},
			{"Witch of the Third"},
			{"Elder"},
			{"Hierophant"},
			{"Witch King", "Witch Queen"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		UNHOLY("Unholy Legion", new String[][] { // Unknown Rank names
			{"Footman"},
			{"Fell Legionaire"},
			{"Fell Centurion"},
			{"Dark Captain"},
			{"Dark Commander"},
			{"Dark Master", "Dark Mistress"},
			{"Dread Master", "Dread Mistress"},
			{"Dread Lord", "Dread Lady"}},
				new String[] {"Despot Rule", "Despot Rule", "Council Rule", "Republic Rule"}),
		SCOURGE("Cult of the Scourge", new String[][] {
			{"Thrall"},
			{"Mudir"},
			{"Dark Brother", "Dark Sister"},
			{"Hand of the Dark"},
			{"Dark Father", "Dark Mother"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		PIRATE("Pirate Crew", new String[][] {
			{"Midshipman", "Midshipwoman"},
			{"Sailor"},
			{"Third Mat"},
			{"Second Mat"},
			{"First Mate"},
			{"Captain"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		HERALD("Academy of Heralds", new String[][] {
			{"Pupil"},
			{"Scribe"},
			{"Recorder"},
			{"Scrivener"},
			{"Chronicler"},
			{"Scholar"},
			{"Archivist"},
			{"Loremaster"}},
				new String[]{"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		CENTAUR("Centaur Cohort", new String[][] {
			{"Hoplite"},
			{"Peltast"},
			{"Myrmidon"},
			{"Myrmidon"},
			{"Cataphract"},
			{"Septenrion"},
			{"Praetorian"},
			{"Paragon"}},
				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"}),
		KHREE("Aracoix Kh'ree", new String[][] {
			{"Duriacor"},
			{"Exarch"},
			{"Tetrarch"},
			{"Dimarch"},
			{"Elnarch"},
			{"Illiarch"},
			{"Tellotharch"},
			{"Erentar"},
			{"Araceos"},
			{"Hierarch"}},

				new String[] {"Despot Rule", "Common Rule", "Council Rule", "Republic Rule"});

		GuildType(String name, String[][] ranks, String[] leadershipTypes) {
			this.name = name;
			this.ranks = ranks;
			this.leadershipTypes = leadershipTypes;
		}

		private final String name;
		private final String[][] ranks;	//Stored Rank#->Gender(M,F)
		private final String[] leadershipTypes;

		public String getCharterName() {
			return this.name;
		}

		public int getNumberOfRanks() {
			return ranks.length;
		}

		public String getRankForGender(int rank, boolean male) {
			if(ranks.length < rank) {
				return "";
			}

			if(ranks[rank].length != 1 && !male) {
				return ranks[rank][1];
			}
			return ranks[rank][0];
		}

		public String getLeadershipType(int i) {
			return leadershipTypes[i];
		}

		public static GuildType getGuildTypeFromCharter(ItemBase itemBase) {

			GuildType charterType;

			// Must be a valid charter object

			if(itemBase.getType().equals(ItemType.GUILDCHARTER) == false)
				return GuildType.NONE;	//No guild Type

			// No switches on long in java.  Cast to int
			// when refactor to long uuid's.  Loss won't matter
			// with values this small.

			switch (itemBase.getUUID()) {

			case 559:
				charterType = GuildType.CATHEDRAL;
				break;
			case 560:
				charterType = GuildType.MILITARY;
				break;
			case 561:
				charterType = GuildType.TEMPLE;
				break;
			case 562:
				charterType = GuildType.BARBARIAN;
				break;
			case 563:
				charterType = GuildType.RANGER;
				break;
			case 564:
				charterType = GuildType.AMAZON;
				break;
			case 565:
				charterType = GuildType.NOBLE;
				break;
			case 566:
				charterType = GuildType.WIZARD;
				break;
			case 567:
				charterType = GuildType.MERCENARY;
				break;
			case 568:
				charterType = GuildType.THIEVES;
				break;
			case 569:
				charterType = GuildType.DWARF;
				break;
			case 570:
				charterType = GuildType.HIGHCOURT;
				break;
			case 571:
				charterType = GuildType.VIRAKT;
				break;
			case 572:
				charterType = GuildType.SCOURGE;
				break;
			case 573:
				charterType = GuildType.KHREE;
				break;
			case 574:
				charterType = GuildType.CENTAUR;
				break;
			case 575:
				charterType = GuildType.UNHOLY;
				break;
			case 576:
				charterType = GuildType.PIRATE;
				break;
			case 577:
				charterType = GuildType.BRIALIA;
				break;

			default:
				charterType = GuildType.HERALD;
			}

			return charterType;
		}

		public static GuildType getGuildTypeFromInt(int i) {
			return GuildType.values()[i];
		}

	}

	public enum MinionClass {
		MELEE,
		ARCHER,
		MAGE;
	}
	
	public enum MinionType {
		AELFBORNGUARD(951,1637, MinionClass.MELEE, "Guard","Aelfborn"),
		AELFBORNMAGE(952, 1635, MinionClass.MAGE,"Adept","Aelfborn"),
		AMAZONGUARD(1500,1670, MinionClass.MELEE,"Guard","Amazon"),
		AMAZONMAGE(1502, 1638, MinionClass.MAGE,"Fury","Amazon"),
		ARACOIXGUARD(1600,1672,MinionClass.MELEE, "Guard","Aracoix"), //used guard captain equipset.
		ARACOIXMAGE(1602,885,MinionClass.MAGE,"Adept","Aracoix"),
		CENTAURGUARD(1650,1642, MinionClass.MELEE,"Guard","Centaur"),
		CENTAURMAGE(1652, 1640, MinionClass.MAGE,"Druid","Centaur"),
		DWARVENARCHER(845,1644, MinionClass.ARCHER, "Marksman","Dwarven"),
		DWARVENGUARD(1050,1666, MinionClass.MELEE,"Guard","Dwarven"),
		DWARVENMAGE(1052, 1643, MinionClass.MAGE,"War Priest","Dwarven"),
		ELFGUARD(1180,1671, MinionClass.MELEE,"Guard","Elven"), //old 1645
		ELFMAGE(1182, 1667, MinionClass.MAGE,"Adept","Elven"),
		FORESTGUARD(1550,1668, MinionClass.MELEE,"Guard","Forest"), //captain changed to guard equipset
		FORESTMAGE(1552, 436, MinionClass.MAGE,"Adept","Forest"),
		HOLYGUARD(1525,1658, MinionClass.MELEE,"Guard","Holy Church"),
		HOLYMAGE(1527, 1646, MinionClass.MAGE,"Prelate","Holy Church"),
		HUMANARCHER(846,1654,MinionClass.ARCHER, "Archer","Human"),
		HUMANGUARD(840,1665, MinionClass.MELEE, "Guard","Human"),
		HUMANMAGE(848, 1655, MinionClass.MAGE,"Adept","Human"),
		IREKEIGUARD(1350,1659, MinionClass.MELEE,"Guard","Irekei"),
		IREKEIMAGE(1352, 1660, MinionClass.MAGE,"Adept","Irekei"),
		MINOTAURARCHER(1701,0,MinionClass.ARCHER,"Archer","Minotaur"),
		MINOTAURGUARD(1700,1673,MinionClass.MELEE,"Guard","Minotaur"),
		NORTHMANGUARD(1250,1669, MinionClass.MELEE,"Guard","Northman"),
		NORTHMANMAGE(1252, 1650, MinionClass.MAGE,"Runecaster","Northman"),
		SHADEGUARD(1450,1662, MinionClass.MELEE,"Guard","Shade"),
		SHADEMAGE(1452, 1664, MinionClass.MAGE,"Adept","Shade"),
		TEMPLARGUARD(841,1564,MinionClass.MELEE,"Marksman","Templar"),
		TEMPLEGUARD(1575,1652, MinionClass.MELEE,"Guard","Temple"),
		TEMPLEMAGE(1577, 1656, MinionClass.MAGE,"Confessor","Temple"),
		UNDEADGUARD(980100,1674,MinionClass.MELEE,"Guard","Undead"),
		UNDEADMAGE(980102,1675,MinionClass.MAGE,"Adept","Undead");
		
		private final int captainContractID;
		private final int equipSetID;
		private final MinionClass minionClass;
		private final String name;
		private final String race;
		
		public static HashMap<Integer,MinionType> ContractToMinionMap = new HashMap<>();
		
		MinionType(int captainContractID, int equipSetID, MinionClass minionClass, String name, String race) {
			
			this.captainContractID = captainContractID;
			this.equipSetID = equipSetID;
			this.minionClass = minionClass;
			this.name = name;
			this.race = race;
			
		}
		
		public static void InitializeMinions(){
			
			for (MinionType minionType :MinionType.values())
			ContractToMinionMap.put(minionType.captainContractID, minionType);
		}

		public int getCaptainContractID() {
			return captainContractID;
		}

		public int getEquipSetID() {
			return equipSetID;
		}

		public MinionClass getMinionClass() {
			return minionClass;
		}

		public String getName() {
			return name;
		}

		public String getRace() {
			return race;
		}
		
	}
	
	public enum GridObjectType{
		STATIC,
		DYNAMIC;
	}

	public enum SupportMsgType {
	    NONE(0),
		PROTECT(1),
		UNPROTECT(3),
		VIEWUNPROTECTED(4),
		REMOVETAX(6),
		ACCEPTTAX(7),
		CONFIRMPROTECT(8);

		private final int type;
		public static HashMap<Integer, SupportMsgType> typeLookup = new HashMap<>();

		SupportMsgType(int messageType) {
			this.type = messageType;
		
		}

		public static void InitializeSupportMsgType(){
			
			for (SupportMsgType supportMsgType :SupportMsgType.values())
				typeLookup.put(supportMsgType.type, supportMsgType);
		}
	}

	public enum ResourceType implements EnumBitSetHelper<ResourceType> {

		STONE(1580000),
		TRUESTEEL(1580001),
		IRON(1580002),
		ADAMANT(1580003),
		LUMBER(1580004),
		OAK(1580005),
		BRONZEWOOD(1580006),
		MANDRAKE(1580007),
		COAL(1580008),
		AGATE(1580009),
		DIAMOND(1580010),
		ONYX(1580011),
		AZOTH(1580012),
		ORICHALK(1580013),
		ANTIMONY(1580014),
		SULFUR(1580015),
		QUICKSILVER(1580016),
		GALVOR(1580017),
		WORMWOOD(1580018),
		OBSIDIAN(1580019),
		BLOODSTONE(1580020),
		MITHRIL(1580021),
		GOLD(7);

		public static HashMap<Integer, ResourceType> resourceLookup = new HashMap<>();
		public int itemID;

		ResourceType(int itemID) {
			this.itemID = itemID;
		}

		public static void InitializeResourceTypes(){

			for (ResourceType resourceType :ResourceType.values())
				resourceLookup.put(resourceType.itemID, resourceType);
		}
	}
	
	public enum PowerActionType {
		ApplyEffect,
		ApplyEffects,
		Block,
		Charm,
		ClaimMine,
		ClearAggro,
		ClearNearbyAggro,
		Confusion,
		CreateMob,
		DamageOverTime,
		DeferredPower,
		DirectDamage,
		Invis,
		MobRecall,
		Peek,
		Recall,
		RemoveEffect,
		Resurrect,
		RunegateTeleport,
		SetItemFlag,
		SimpleDamage,
		SpireDisable,
		Steal,
		Summon,
		Teleport,
		Track,
		TransferStat,
		TransferStatOT,
		Transform,
		TreeChoke
	}

	public enum AccountStatus {
		BANNED,
		ACTIVE,
		ADMIN;
	}

}
