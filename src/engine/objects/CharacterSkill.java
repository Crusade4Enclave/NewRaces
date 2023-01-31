// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.objects;

import engine.Enum;
import engine.Enum.CharacterSkills;
import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.ai.StaticMobActions;
import engine.gameManager.DbManager;
import engine.net.ByteBufferWriter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CharacterSkill extends AbstractGameObject {

	private static final int[] maxTrains = {
			29, 29, 29, 29, 29, //0 to 4
			29, 32, 34, 36, 38, //5 to 9
			40, 42, 43, 45, 47, //10 to 14
			48, 49, 51, 52, 53, //15 to 19
			55, 56, 57, 58, 59, //20 to 24
			60, 62, 63, 64, 65, //25 to 29
			66, 67, 68, 68, 69, //30 to 34
			70, 71, 72, 73, 74, //35 to 39
			75, 76, 76, 77, 78, //40 to 44
			79, 80, 80, 81, 82, //45 to 49
			83, 83, 84, 85, 85, //50 to 54
			86, 87, 88, 88, 89, //55 to 59
			90, 90, 91, 92, 92, //60 to 64
			93, 94, 94, 95, 95, //65 to 69
			96, 97, 97, 98, 99, //70 to 74
			99, 100, 100, 101, 101, //75 to 79
			102, 103, 103, 104, 104, //80 to 84
			105, 105, 106, 106, 107, //85 to 89
			108, 109, 109, 110, 110, //90 to 94
			111, 112, 112, 113, 113, //95 to 99
			114, 115, 115, 116, 116, //100 to 104
			117, 118, 118, 119, 119, //105 to 109
			120, 121, 121, 122, 122, //110 to 114
			123, 124, 124, 125, 125, //115 to 119
			126, 127, 127, 128, 128, //120 to 124
			129, 130, 130, 131, 131, //125 to 129
			132, 133, 133, 134, 134, //130 to 134
			135, 136, 136, 137, 137, //135 to 139
			138, 139, 139, 140, 140, //140 to 144
			141, 142, 142, 143, 143, //145 to 149
			144, 145, 145, 146, 146, //150 to 154
			147, 148, 148, 149, 149, //155 to 159
			150, 151, 151, 152, 152, //160 to 164
			153, 154, 154, 155, 155, //165 to 169
			156, 157, 157, 158, 158, //170 to 174
			159, 160, 160, 161, 161, //175 to 179
			162, 163, 163, 164, 164, //180 to 184
			165, 166, 166, 167, 167, //185 to 189
			168}; //190

	private static final float[] baseSkillValues = {
			0.0f, 0.0f, 0.2f, 0.4f, 0.6f,  //0 to 4
			0.8f, 1.0f, 1.1666666f, 1.3333334f, 1.5f,  //5 to 9
			1.6666667f, 1.8333334f, 2.0f, 2.2f, 2.4f,  //10 to 14
			2.6f, 2.8f, 3.0f, 3.2f, 3.4f,  //15 to 19
			3.6f, 3.8f, 4.0f, 4.2f, 4.4f,  //20 to 24
			4.6f, 4.8f, 5.0f, 5.25f, 5.5f,  //25 to 29
			5.75f, 6.0f, 6.2f, 6.4f, 6.6f,  //30 to 34
			6.8f, 7.0f, 7.25f, 7.5f, 7.75f,  //35 to 39
			8.0f, 8.2f, 8.4f, 8.6f, 8.8f,  //40 to 44
			9.0f, 9.25f, 9.5f, 9.75f, 10.0f,  //45 to 49
			10.25f, 10.5f, 10.75f, 11.0f, 11.2f,  //50 to 54
			11.4f, 11.6f, 11.8f, 12.0f, 12.25f,  //55 to 59
			12.5f, 12.75f, 13.0f, 13.25f, 13.5f,  //60 to 64
			13.75f, 14.0f, 14.25f, 14.5f, 14.75f,  //65 to 69
			15.0f, 15.333333f, 15.666667f, 16.0f, 16.25f,  //70 to 74
			16.5f, 16.75f, 17.0f, 17.25f, 17.5f,  //75 to 79
			17.75f, 18.0f, 18.25f, 18.5f, 18.75f,  //80 to 84
			19.0f, 19.333334f, 19.666666f, 20.0f, 20.25f,  //85 to 89
			20.5f, 20.75f, 21.0f, 21.25f, 21.5f,  //90 to 94
			21.75f, 22.0f, 22.333334f, 22.666666f, 23.0f,  //95 to 99
			23.25f, 23.5f, 23.75f, 24.0f, 24.333334f,  //100 to 104
			24.666666f, 25.0f, 25.25f, 25.5f, 25.75f,  //105 to 109
			26.0f, 26.333334f, 26.666666f, 27.0f, 27.333334f,  //110 to 114
			27.666666f, 28.0f, 28.25f, 28.5f, 28.75f,  //115 to 119
			29.0f, 29.333334f, 29.666666f, 30.0f, 30.333334f,  //120 to 124
			30.666666f, 31.0f, 31.25f, 31.5f, 31.75f,  //125 to 129
			32.0f, 32.333332f, 32.666668f, 33.0f, 33.333332f,  //130 to 134
			33.666668f, 34.0f, 34.333332f, 34.666668f, 35.0f,  //135 to 139
			35.333332f, 35.666668f, 36.0f, 36.333332f, 36.666668f,  //140 to 144
			37.0f, 37.25f, 37.5f, 37.75f, 38.0f,  //145 to 149
			38.333332f, 38.666668f, 39.0f, 39.333332f, 39.666668f,  //150 to 154
			40.0f, 40.333332f, 40.666668f, 41.0f, 41.333332f,  //155 to 159
			41.666668f, 42.0f, 42.333332f, 42.666668f, 43.0f,  //160 to 164
			43.333332f, 43.666668f, 44.0f, 44.333332f, 44.666668f,  //165 to 169
			45.0f, 45.5f, 46.0f, 46.333332f, 46.666668f,  //170 to 174
			47.0f, 47.333332f, 47.666668f, 48.0f, 48.333332f,  //175 to 179
			48.666668f, 49.0f, 49.333332f, 49.666668f, 50.0f,  //180 to 184
			50.333332f, 50.666668f, 51.0f, 51.333332f, 51.666668f,  //185 to 189
			52.0f, 52.5f, 53.0f, 53.333332f, 53.666668f,  //190 to 194
			54.0f, 54.333332f, 54.666668f, 55.0f, 55.333332f,  //195 to 199
			55.666668f, 56.0f, 56.333332f, 56.666668f, 57.0f,  //200 to 204
			57.5f, 58.0f, 58.333332f, 58.666668f, 59.0f,  //205 to 209
			59.333332f, 59.666668f, 60.0f, 60.5f, 61.0f,  //210 to 214
			61.333332f, 61.666668f, 62.0f, 62.333332f, 62.666668f,  //215 to 219
			63.0f, 63.5f, 64.0f, 64.333336f, 64.666664f,  //220 to 224
			65.0f, 65.333336f, 65.666664f, 66.0f, 66.5f,  //225 to 229
			67.0f, 67.333336f, 67.666664f, 68.0f, 68.5f,  //230 to 234
			69.0f, 69.333336f, 69.666664f, 70.0f, 70.333336f,  //235 to 239
			70.666664f, 71.0f, 71.5f, 72.0f, 72.5f,  //240 to 244
			73.0f, 73.333336f, 73.666664f, 74.0f, 74.333336f,  //245 to 249
			74.666664f, 75.0f, 75.5f, 76.0f, 76.333336f,  //250 to 254
			76.666664f, 77.0f, 77.5f, 78.0f, 78.333336f,  //255 to 259
			78.666664f, 79.0f, 79.5f, 80.0f, 80.333336f,  //260 to 264
			80.666664f, 81.0f, 81.5f, 82.0f, 82.333336f,  //265 to 269
			82.666664f, 83.0f, 83.5f, 84.0f, 84.333336f,  //270 to 274
			84.666664f, 85.0f, 85.5f, 86.0f, 86.5f,  //275 to 279
			87.0f, 87.333336f, 87.666664f, 88.0f, 88.5f,  //280 to 284
			89.0f, 89.333336f, 89.666664f, 90.0f, 90.5f,  //285 to 289
			91.0f, 91.5f, 92.0f, 92.333336f, 92.666664f,  //290 to 294
			93.0f, 93.5f, 94.0f, 94.5f, 95.0f,  //295 to 299
			95.333336f, 95.666664f, 96.0f, 96.5f, 97.0f,  //300 to 304
			97.5f, 98.0f, 98.333336f, 98.666664f, 99.0f,  //305 to 309
			99.5f, 100.0f, 100.5f, 101.0f, 101.5f,  //310 to 314
			102.0f, 102.5f, 103.0f, 103.333336f, 103.666664f,  //315 to 319
			104.0f, 104.333336f, 104.666664f, 105.0f, 105.5f,  //320 to 324
			106.0f, 106.5f, 107.0f, 108.0f, 108.333336f,  //325 to 329
			108.666664f, 109.0f, 109.333336f, 109.666664f, 110.0f,  //330 to 334
			110.5f, 111.0f, 111.5f, 112.0f, 112.5f,  //335 to 339
			113.0f, 113.333336f, 113.666664f, 114.0f, 114.5f,  //340 to 344
			115.0f, 115.5f, 116.0f, 116.5f, 117.0f,  //345 to 349
			117.5f, 118.0f, 118.333336f, 118.666664f, 119.0f,  //350 to 354
			119.5f, 120.0f, 120.5f, 121.0f, 121.5f,  //355 to 359
			122.0f, 122.5f, 123.0f, 123.333336f, 123.666664f,  //360 to 364
			124.0f, 124.5f, 125.0f, 125.5f, 126.0f,  //365 to 369
			126.5f, 127.0f, 127.5f, 128.0f, 128.5f,  //370 to 374
			129.0f, 129.5f, 130.0f, 130.33333f, 130.66667f,  //375 to 379
			131.0f, 131.5f, 132.0f, 132.5f, 133.0f,  //380 to 384
			133.5f, 134.0f, 134.5f, 135.0f, 135.5f,  //385 to 389
			136.0f, 136.5f, 137.0f, 137.5f, 138.0f,  //390 to 394
			138.5f, 139.0f, 139.5f, 140.0f, 140.33333f,  //395 to 399
			140.66667f, 141.0f, 141.5f, 142.0f, 142.5f,  //400 to 404
			143.0f, 143.5f, 144.0f, 144.5f, 145.0f,  //405 to 409
			145.5f, 146.0f, 146.5f, 147.0f, 147.5f,  //410 to 414
			148.0f, 148.5f, 149.0f, 149.5f, 150.0f,  //415 to 419
			150.5f, 151.0f, 151.5f, 152.0f, 152.5f,  //420 to 424
			153.0f, 153.5f, 154.0f, 154.5f, 155.0f,  //425 to 429
			155.5f, 156.0f, 156.5f, 157.0f, 157.5f,  //430 to 434
			158.0f, 158.5f, 159.0f, 159.5f, 160.0f,  //435 to 439
			160.5f, 161.0f, 161.5f, 162.0f, 162.5f,  //440 to 444
			163.0f, 163.5f, 164.0f, 164.5f, 165.0f,  //445 to 449
			165.5f, 166.0f, 166.5f, 167.0f, 167.5f,  //450 to 454
			168.0f, 168.5f, 169.0f, 169.5f, 170.0f,  //455 to 459
			170.5f, 171.0f, 171.5f, 172.0f, 172.5f,  //460 to 464
			173.0f, 173.5f, 174.0f, 174.5f, 175.0f,  //465 to 469
			176.0f, 176.5f, 177.0f, 177.5f, 178.0f,  //470 to 474
			178.5f, 179.0f, 179.5f, 180.0f, 180.5f,  //475 to 479
			181.0f, 181.5f, 182.0f, 182.5f, 183.0f,  //480 to 484
			183.5f, 184.0f, 184.5f, 185.0f, 185.5f,  //485 to 489
			186.0f, 187.0f, 187.5f, 188.0f, 188.5f,  //490 to 494
			189.0f, 189.5f, 190.0f, 190.5f, 191.0f,  //495 to 499
			191.5f, 192.0f, 192.5f, 193.0f, 193.5f,  //500 to 504
			194.0f, 194.5f, 195.0f, 196.0f, 196.5f,  //505 to 509
			197.0f, 197.5f, 198.0f, 198.5f, 199.0f,  //510 to 514
			199.5f, 200.0f, 200.5f, 201.0f, 201.5f,  //515 to 519
			202.0f, 203.0f, 203.5f, 204.0f, 204.5f,  //520 to 524
			205.0f, 205.5f, 206.0f, 206.5f, 207.0f,  //525 to 529
			207.5f, 208.0f, 209.0f, 209.5f, 210.0f,  //530 to 534
			210.5f, 211.0f, 211.5f, 212.0f, 212.5f,  //535 to 539
			213.0f, 214.0f, 214.5f, 215.0f, 215.5f,  //540 to 544
			216.0f, 216.5f, 217.0f, 217.5f, 218.0f,  //545 to 549
			218.5f, 219.0f, 220.0f, 220.5f, 221.0f,  //550 to 554
			221.5f, 222.0f, 222.5f, 223.0f, 224.0f,  //555 to 559
			224.5f, 225.0f, 225.5f, 226.0f, 226.5f,  //560 to 564
			227.0f, 227.5f, 228.0f, 229.0f, 229.5f,  //565 to 569
			230.0f, 230.5f, 231.0f, 231.5f, 232.0f,  //570 to 574
			233.0f, 233.5f, 234.0f, 234.5f, 235.0f,  //575 to 579
			235.5f, 236.0f, 237.0f, 237.5f, 238.0f,  //580 to 584
			238.5f, 239.0f, 239.5f, 240.0f, 241.0f,  //585 to 589
			241.5f, 242.0f, 242.5f, 243.0f, 243.5f,  //590 to 594
			244.0f, 245.0f, 245.5f, 246.0f, 246.5f,  //595 to 599
			247.0f}; //600


	private SkillsBase skillsBase;
	private AtomicInteger numTrains = new AtomicInteger();

	private CharacterSkills skillType;

	//Skill% before trains and before any effects or item bonuses
	private float baseAmountBeforeMods;

	//Skill% after trains but before any effects or item bonuses
	private float modifiedAmountBeforeMods;
	private boolean isMobOwner = false;

	//Skill% before trains but after any effects or item bonuses
	private float baseAmount;

	//Skill% after trains and after any effects or item bonuses
	private float modifiedAmount;

	private int ownerUID;
	private boolean trained = false;
	private int requiredLevel = 0;

	/**
	 * No Table ID Constructor
	 */
	public CharacterSkill(SkillsBase skillsBase, PlayerCharacter pc) {
		super();
		this.skillsBase = skillsBase;
		this.numTrains.set(0);
		this.ownerUID = pc.getObjectUUID();
		calculateBaseAmount();
		calculateModifiedAmount();
		this.skillType = CharacterSkills.GetCharacterSkillByToken(this.skillsBase.getToken());
	}

	/**
	 * Normal Constructor
	 */
	public CharacterSkill(SkillsBase skillsBase, PlayerCharacter pc, int newUUID) {

		super(newUUID);
		this.skillsBase = skillsBase;
		this.numTrains.set(0);
		this.ownerUID = pc.getObjectUUID();
		this.trained = true;
		calculateBaseAmount();
		calculateModifiedAmount();
		this.skillType = CharacterSkills.GetCharacterSkillByToken(this.skillsBase.getToken());

	}

	/**
	 * ResultSet Constructor
	 */
	public CharacterSkill(ResultSet rs, PlayerCharacter pc) throws SQLException {
		super(rs);

		int skillsBaseID = rs.getInt("SkillsBaseID");
		this.skillsBase = DbManager.SkillsBaseQueries.GET_BASE(skillsBaseID);
		this.numTrains.set(rs.getShort("trains"));
		this.ownerUID = pc.getObjectUUID();
		calculateBaseAmount();
		calculateModifiedAmount();
		this.skillType = CharacterSkills.GetCharacterSkillByToken(this.skillsBase.getToken());
	}

	public CharacterSkill(SkillsBase sb, Mob mob, int trains) {
		super();
		this.skillsBase = sb;
		this.numTrains.set(trains);
		this.ownerUID = mob.getObjectUUID();
		this.isMobOwner = true;
		calculateMobBaseAmount();
		calculateModifiedAmount();
		this.skillType = CharacterSkills.GetCharacterSkillByToken(this.skillsBase.getToken());
	}

	public CharacterSkill(ResultSet rs) throws SQLException {
		super(rs);
		int skillsBaseID = rs.getInt("SkillsBaseID");
		this.skillsBase = DbManager.SkillsBaseQueries.GET_BASE(skillsBaseID);
		this.numTrains.set(rs.getShort("trains"));
		this.ownerUID = rs.getInt("CharacterID");
		//		this.owner = DbManager.PlayerCharacterQueries.GET_PLAYER_CHARACTER(rs.getInt("CharacterID"));
		calculateBaseAmount();
		calculateModifiedAmount();
		this.skillType = CharacterSkills.GetCharacterSkillByToken(this.skillsBase.getToken());
	}

	public static AbstractCharacter GetOwner(CharacterSkill cs){
		if (cs.ownerUID == 0)
			return null;
		if (cs.isMobOwner)
			return StaticMobActions.getFromCache(cs.ownerUID);
		else
			return PlayerCharacter.getFromCache(cs.ownerUID);
	}

	/*
	 * Getters
	 */

	public static float getATR(AbstractCharacter ac, String name) {
		if (ac == null)
			return 0f;
		float atr;
		ConcurrentHashMap<String, CharacterSkill> skills = ac.getSkills();
		CharacterSkill skill = skills.get(name);
		if (skill != null)
			atr = skill.getATR(ac);
		else {
			float mast = CharacterSkill.getQuickMastery(ac, name);
			atr = (((int)mast * 7) + (ac.getStatDexCurrent() / 2));
		}
		//apply effect mods
		PlayerBonuses bonus = ac.getBonuses();
		if (bonus == null)
			return atr;
		atr += bonus.getFloat(ModType.OCV, SourceType.None);
		float pos_Bonus = bonus.getFloatPercentPositive(ModType.OCV, SourceType.None);
		atr *=  (1 + pos_Bonus);
		//rUNES will already be applied
	//	atr *= (1 + ((float)bonus.getShort("rune.Attack") / 100)); //precise
		float neg_Bonus = bonus.getFloatPercentNegative(ModType.OCV, SourceType.None);
		atr *= (1 +neg_Bonus);
		return atr;
	}

	private float getATR(AbstractCharacter ac) {
		return (((int)this.modifiedAmount * 7) + (ac.getStatDexCurrent() / 2));
	}

	public synchronized boolean train(PlayerCharacter pc) {
		if (pc == null || this.skillsBase == null)
			return false;

		boolean running = false;

		//trying out a table lookup
		int intt = (int) pc.statIntBase;
		int maxTrains = 0;
		if (intt > 0 && intt < 191)
			maxTrains = CharacterSkill.maxTrains[intt];
		else
			maxTrains = (int)(33 + 1.25 * (int) pc.statIntBase - 0.005 * Math.pow((int) pc.statIntBase, 2));


		int oldTrains = this.numTrains.get();
		boolean succeeded = true;
		if (pc.getTrainsAvailable() <= 0)
			return false;
		if (oldTrains == maxTrains) //at gold, stop here
			return false;
		else if (oldTrains > maxTrains) //catch incase we somehow go over
			this.numTrains.set(maxTrains);
		else //add the train
			succeeded = this.numTrains.compareAndSet(oldTrains, oldTrains+1);

		if (this.numTrains.get() > maxTrains) { //double check not over max trains
			this.numTrains.set(maxTrains);
			succeeded = false;
		}

		if (succeeded) {
			this.trained = true;

			//subtract from trains available
			pc.modifyTrainsAvailable(-1);

			//update database
			pc.addDatabaseJob("Skills", MBServerStatics.THIRTY_SECONDS);

			//recalculate this skill
			calculateBaseAmount();
			calculateModifiedAmount();

			//see if any new skills or powers granted
			pc.calculateSkills();

			//reapply all bonuses
			pc.applyBonuses();

			//update cache if running trains change
			if (running)
				pc.incVer();

			return true;
		} else
			return false;
	}

	public float  getTrainingCost(PlayerCharacter pc, NPC trainer){
		int charLevel = pc.getLevel();
		int skillRank = this.getNumTrains() - 1 + this.requiredLevel;
		float baseCost = 15 * this.requiredLevel; //TODO GET BASE COSTS OF SKILLS.



		float sellPercent = -.20f;
		float cost;
		float const5;
		int const2 = 1;
		float const3 = 50;
		float const4 = const3 + const2;

		if (charLevel > 50)
			const5 = 50 / const4;
		else
			const5 = charLevel/const4;

		const5 = 1-const5;
		const5 = (float) (Math.log(const5) / Math.log(2) * .75f);
		float rounded5 = Math.round(const5);
		const5 = rounded5 - const5;

		const5 *= -1;

		const5 = (float) (Math.pow(2, const5) - 1);

		const5 +=1;
		const5 = Math.scalb(const5, (int) rounded5);
		const5 *= (charLevel - skillRank);
		const5 *= sellPercent;
		const5 = (float) (Math.log(const5) / Math.log(2) * 3);
		rounded5 = Math.round(const5);
		const5 = rounded5 - const5;
		const5 *= -1;
		const5 = (float) (Math.pow(2, const5) - 1);
		const5 +=1;


		const5 = Math.scalb(const5, (int) rounded5);
		const5 += 1;
		cost = const5 * (baseCost);


		if (Float.isNaN(cost))
			cost = baseCost;
		return cost;
	}

	//Call this to refine skills and recalculate everything for pc.
	public boolean refine(PlayerCharacter pc) {
		return refine(pc, true);
	}

	public boolean refine(PlayerCharacter pc, boolean recalculate) {
		if (pc == null || this.skillsBase == null)
			return false;

		int oldTrains = this.numTrains.get();
		boolean succeeded = true;
		if (this.getNumTrains() < 1)
			return false;
		succeeded = this.numTrains.compareAndSet(oldTrains, oldTrains-1);
		if (succeeded) {
			this.trained = true;

			//add to trains available
			pc.modifyTrainsAvailable(1);

			//update database
			pc.addDatabaseJob("Skills", MBServerStatics.THIRTY_SECONDS);

			if (recalculate) {
				//recalculate this skill
				calculateBaseAmount();
				calculateModifiedAmount();

				//see if any skills or powers removed
				pc.calculateSkills();

				//reapply all bonuses
				pc.applyBonuses();
			}

			return true;
		} else
			return false;
	}
	
	
	public boolean reset(PlayerCharacter pc, boolean recalculate) {
		if (pc == null || this.skillsBase == null)
			return false;

		int oldTrains = this.numTrains.get();
		boolean succeeded = true;
		if (this.getNumTrains() < 1)
			return false;
		succeeded = this.numTrains.compareAndSet(oldTrains, 0);
		if (succeeded) {
			this.trained = true;

			//add to trains available
			pc.modifyTrainsAvailable(oldTrains);

			//update database
			pc.addDatabaseJob("Skills", MBServerStatics.THIRTY_SECONDS);

			if (recalculate) {
				//recalculate this skill
				calculateBaseAmount();
				calculateModifiedAmount();

				//see if any skills or powers removed
				pc.calculateSkills();

				//reapply all bonuses
				pc.applyBonuses();
			}

			return true;
		} else
			return false;
	}


	/*
	 * Returns Skill Base for skill
	 */
	public SkillsBase getSkillsBase() {
		return this.skillsBase;
	}

	/*
	 * Returns number of trains in skill
	 */
	public int getNumTrains() {
		return this.numTrains.get();
	}



	/*
	 * Returns Skill% before trains added
	 */
	public float getBaseAmount() {
		return this.baseAmount;
	}

	/*
	 * Returns Skill% after trains added
	 */
	public float getModifiedAmount() {
		return this.modifiedAmount;
	}

	/*
	 * Returns Skill% before trains added, minus bonus from equip and effects
	 */
	public float getBaseAmountBeforeMods() {
		return this.baseAmountBeforeMods;
	}

	/*
	 * Returns Skill% after trains added, minus bonus from equip and effects
	 */
	public float getModifiedAmountBeforeMods() {
		return this.modifiedAmountBeforeMods;
	}

	public String getName() {
		if (this.skillsBase != null)
			return this.skillsBase.getName();
		return "";
	}

	public int getToken() {
		if (this.skillsBase != null)
			return this.skillsBase.getToken();
		return 0;
	}

	public boolean isTrained() {
		return trained;
	}

	public void syncTrains() {
		this.trained = false;
	}

	/*
	 * Serializing
	 */
	
	public static void serializeForClientMsg(CharacterSkill characterSkill, ByteBufferWriter writer) {
		if (characterSkill.skillsBase == null) {
			Logger.error( "SkillsBase not found for skill " + characterSkill.getObjectUUID());
			writer.putInt(0);
			writer.putInt(0);
		} else {
			writer.putInt(characterSkill.skillsBase.getToken());
			writer.putInt(characterSkill.numTrains.get());
		}
	}

	/**
	 * @ This updates all Base Skill Amouts for a player
	 * Convienence method
	 */
	public static void updateAllBaseAmounts(PlayerCharacter pc) {
		if (pc == null)
			return;
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		Iterator<String> it = skills.keySet().iterator();
		while(it.hasNext()) {
			String name = it.next();
			CharacterSkill cs = skills.get(name);
			if (cs != null)
				cs.calculateBaseAmount();
			// Logger.info("CharacterSkill", pc.getName() + ", skill: " +
			// cs.getSkillsBase().getName() + ", trains: " + cs.numTrains +
			// ", base: " + cs.baseAmount + ", mod: " + cs.modifiedAmount);
		}

		//Recalculate ATR, damage and defense
		//pc.calculateAtrDefenseDamage();

		//recalculate movement bonus
		//pc.calculateSpeedMod();
	}

	/**
	 * @ This updates all Modified skill Amounts for a player
	 * Convienence method
	 */
	public static void updateAllModifiedAmounts(PlayerCharacter pc) {
		if (pc == null)
			return;
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		Iterator<String> it = skills.keySet().iterator();
		while(it.hasNext()) {
			String name = it.next();
			CharacterSkill cs = skills.get(name);
			if (cs != null)
				cs.calculateModifiedAmount();

		}

		//Recalculate ATR, damage and defense
		//pc.calculateAtrDefenseDamage();
	}

	/**
	 * @ Calculates Base Skill Percentage
	 * Call this when stats change for a player
	 */
	public void calculateBaseAmount() {
		if (CharacterSkill.GetOwner(this) == null) {
			Logger.error("owner not found for owner uuid : " + this.ownerUID);
			this.baseAmount = 1;
			this.modifiedAmount = 1;
			return;
		}
		
		if (this.skillsBase == null) {
			Logger.error("SkillsBase not found for skill " + this.getObjectUUID());
			this.baseAmount = 1;
			this.modifiedAmount = 1;
			return;
		}

		//Get any rune bonus
		float bonus = 0f;
		//runes will already be calculated
		if (CharacterSkill.GetOwner(this).getBonuses() != null) {
			//Get bonuses from runes
			bonus = CharacterSkill.GetOwner(this).getBonuses().getSkillBonus(this.skillsBase.sourceType);
		}

		//Get Base skill for unmodified stats
		float base = 7f;
		float statMod = 0.5f;
		if (this.skillsBase.getStrMod() > 0)
            statMod += (float)this.skillsBase.getStrMod() * (float) (int) ((PlayerCharacter) CharacterSkill.GetOwner(this)).statStrBase / 100f;
		if (this.skillsBase.getDexMod() > 0)
			statMod += (float)this.skillsBase.getDexMod() * (float) (int) ((PlayerCharacter) CharacterSkill.GetOwner(this)).statDexBase / 100f;
		if (this.skillsBase.getConMod() > 0)
			statMod += (float)this.skillsBase.getConMod() * (float) (int) ((PlayerCharacter) CharacterSkill.GetOwner(this)).statConBase / 100f;
		if (this.skillsBase.getIntMod() > 0)
			statMod += (float)this.skillsBase.getIntMod() * (float) (int) ((PlayerCharacter) CharacterSkill.GetOwner(this)).statIntBase / 100f;
		if (this.skillsBase.getSpiMod() > 0)
			statMod += (float)this.skillsBase.getSpiMod() * (float) (int) ((PlayerCharacter) CharacterSkill.GetOwner(this)).statSpiBase / 100f;
		if (statMod < 1)
			statMod = 1f;
		else if (statMod > 600)
			statMod = 600f;
		base += CharacterSkill.baseSkillValues[(int)statMod];

		if (base + bonus < 1f)
			this.baseAmountBeforeMods = 1f;
		else
			this.baseAmountBeforeMods = base + bonus;
		this.modifiedAmountBeforeMods = Math.round(this.baseAmountBeforeMods + calculateAmountAfterTrains());
	}

	public void calculateMobBaseAmount() {
		if (CharacterSkill.GetOwner(this) == null) {
			Logger.error("owner not found for owner uuid : " + this.ownerUID);
			this.baseAmount = 1;
			this.modifiedAmount = 1;
			return;
		}
		
		if (this.skillsBase == null) {
			Logger.error("SkillsBase not found for skill " + this.getObjectUUID());
			this.baseAmount = 1;
			this.modifiedAmount = 1;
			return;
		}

		//Get any rune bonus
		float bonus = 0f;
		//TODO SKILLS RUNES
		
		if (CharacterSkill.GetOwner(this).getBonuses() != null) {
			//Get bonuses from runes
			bonus = CharacterSkill.GetOwner(this).getBonuses().getSkillBonus(this.skillsBase.sourceType);
		}

		//Get Base skill for unmodified stats
		float base = 7f;
		float statMod = 0.5f;
		if (this.skillsBase.getStrMod() > 0)
			statMod += (float)this.skillsBase.getStrMod() * (float)((Mob)CharacterSkill.GetOwner(this)).getMobBase().getMobBaseStats().getBaseStr() / 100f;
		if (this.skillsBase.getDexMod() > 0)
			statMod += (float)this.skillsBase.getDexMod() * (float)((Mob)CharacterSkill.GetOwner(this)).getMobBase().getMobBaseStats().getBaseDex() / 100f;
		if (this.skillsBase.getConMod() > 0)
			statMod += (float)this.skillsBase.getConMod() * (float)((Mob)CharacterSkill.GetOwner(this)).getMobBase().getMobBaseStats().getBaseCon() / 100f;
		if (this.skillsBase.getIntMod() > 0)
			statMod += (float)this.skillsBase.getIntMod() * (float)((Mob)CharacterSkill.GetOwner(this)).getMobBase().getMobBaseStats().getBaseInt() / 100f;
		if (this.skillsBase.getSpiMod() > 0)
			statMod += (float)this.skillsBase.getSpiMod() * (float)((Mob)CharacterSkill.GetOwner(this)).getMobBase().getMobBaseStats().getBaseSpi() / 100f;
		if (statMod < 1)
			statMod = 1f;
		else if (statMod > 600)
			statMod = 600f;
		base += CharacterSkill.baseSkillValues[(int)statMod];

		if (base + bonus < 1f)
			this.baseAmountBeforeMods = 1f;
		else
			this.baseAmountBeforeMods = base + bonus;
		this.modifiedAmountBeforeMods = (int)(this.baseAmountBeforeMods + calculateAmountAfterTrains());
	}

	public void calculateModifiedAmount() {
		if (CharacterSkill.GetOwner(this) == null || this.skillsBase == null) {
			Logger.error( "owner or SkillsBase not found for skill " + this.getObjectUUID());
			this.baseAmount = 1;
			this.modifiedAmount = 1;
			return;
		}

		//Get any rune bonus
		float bonus = 0f;
		if (CharacterSkill.GetOwner(this).getBonuses() != null) {
			//Get bonuses from runes
			bonus = CharacterSkill.GetOwner(this).getBonuses().getSkillBonus(this.skillsBase.sourceType);
		}

		//Get Base skill for modified stats
		//TODO this fomula needs verified
		float base = 7f;
		float statMod = 0.5f;
		if (this.skillsBase.getStrMod() > 0)
			statMod += (float)this.skillsBase.getStrMod() * (float)CharacterSkill.GetOwner(this).getStatStrCurrent() / 100f;
		if (this.skillsBase.getDexMod() > 0)
			statMod += (float)this.skillsBase.getDexMod() * (float)CharacterSkill.GetOwner(this).getStatDexCurrent() / 100f;
		if (this.skillsBase.getConMod() > 0)
			statMod += (float)this.skillsBase.getConMod() * (float)CharacterSkill.GetOwner(this).getStatConCurrent() / 100f;
		if (this.skillsBase.getIntMod() > 0)
			statMod += (float)this.skillsBase.getIntMod() * (float)CharacterSkill.GetOwner(this).getStatIntCurrent() / 100f;
		if (this.skillsBase.getSpiMod() > 0)
			statMod += (float)this.skillsBase.getSpiMod() * (float)CharacterSkill.GetOwner(this).getStatSpiCurrent() / 100f;
		if (statMod < 1)
			statMod = 1f;
		else if (statMod > 600)
			statMod = 600f;
		base += CharacterSkill.baseSkillValues[(int)statMod];
		SourceType sourceType = SourceType.GetSourceType(this.skillsBase.getNameNoSpace());

		//Get any rune, effect and item bonus
		
		if (CharacterSkill.GetOwner(this).getBonuses() != null) {
			//add bonuses from effects/items and runes
			base += bonus + CharacterSkill.GetOwner(this).getBonuses().getFloat(ModType.Skill, sourceType);
		}

		if (base < 1f)
			this.baseAmount = 1f;
		else
			this.baseAmount = base;

		float modAmount = this.baseAmount + calculateAmountAfterTrains();

		if (CharacterSkill.GetOwner(this).getBonuses() != null) {
			//Multiply any percent bonuses
			modAmount *= (1 + CharacterSkill.GetOwner(this).getBonuses().getFloatPercentAll(ModType.Skill, sourceType));
		}

		this.modifiedAmount = (int)(modAmount);
	}

	/**
	 * @ Calculates Modified Skill Percentage
	 * Call this when number of trains change for skill
	 */
	public float calculateAmountAfterTrains() {
		if (this.skillsBase == null) {
			Logger.error( "SkillsBase not found for skill " + this.getObjectUUID());
			this.modifiedAmount = this.baseAmount;
		}
		int amount;

		int trains = this.numTrains.get();
		if (trains < 10)
			amount = (trains * 2);
		else if (trains < 90)
			amount = 10 + trains;
		else if (trains < 134)
			amount = 100 + ((trains-90) / 2);
		else
			amount = 122 + ((trains-134) / 3);

		return amount;
	}

	public static int getTrainsAvailable(PlayerCharacter pc) {

		if (pc == null)
			return 0;
		if (pc.getRace() == null || pc.getBaseClass() == null) {
			Logger.error("Race or BaseClass not found for player " + pc.getObjectUUID());
			return 0;
		}
		int raceBonus = 0;
		int baseMod = 0;
		int promoMod = 6;
		int available = 0;

		//get racial bonus;
		if (pc.getRace().getRaceType().equals(Enum.RaceType.HUMANMALE) ||
				pc.getRace().getRaceType().equals(Enum.RaceType.HUMANFEMALE) )
			raceBonus = 1; //Human racial bonus;

		//get base class trains
		if (pc.getBaseClass().getObjectUUID() == 2500 || pc.getBaseClass().getObjectUUID() == 2502) {
			baseMod = 4; //Fighter or Rogue
		} else {
			baseMod = 5; //Healer or Mage
		}

		int level = pc.getLevel();
		if (level > 74)
			available = 62 + (49 * (promoMod + baseMod + raceBonus)) + (9 * (baseMod + raceBonus));
		else if (level > 69)
			available = ((level - 69) * 3) + 45 + (49 * (promoMod + baseMod + raceBonus)) + (9 * (baseMod + raceBonus));
		else if (level > 64)
			available = ((level - 64) * 4) + 25 + (49 * (promoMod + baseMod + raceBonus)) + (9 * (baseMod + raceBonus));
		else if (level > 59) //Between 60 and 65
			available = ((level - 59) * 5) + (49 * (promoMod + baseMod + raceBonus)) + (9 * (baseMod + raceBonus));
		else if (level > 10) //Between 11 and 59
			available = ((level - 10) * (promoMod + baseMod + raceBonus)) + (9 * (baseMod + raceBonus));
		//			available = (level - 59) + (49 * (promoMod + baseMod + raceBonus)) + (9 * (baseMod + raceBonus));
		else if (level == 10 && (pc.getPromotionClass() != null)) //10 but promoted
			available = (promoMod + baseMod + raceBonus) + (9 * (baseMod + raceBonus));
		else //not promoted
			available = (level-1) * (baseMod + raceBonus);

		//next subtract trains in any skills
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		Iterator<String> it = skills.keySet().iterator();
		while(it.hasNext()) {
			String name = it.next();
			CharacterSkill cs = skills.get(name);
			if (cs != null)
				available -= cs.numTrains.get();
		}

		//TODO subtract any trains from powers
		ConcurrentHashMap<Integer, CharacterPower> powers = pc.getPowers();
		for (CharacterPower power : powers.values()) {
			if (power != null)
				available -= power.getTrains();
		}

		if(MBServerStatics.BONUS_TRAINS_ENABLED) {
			available += 1000;
		}

		//		if (available < 0) {
		//TODO readd this error log after test
		//			Logger.error("CharacterSkill.getTrainsAvailable", "Number of trains available less then 0 for Player " + pc.getUUID());
		//			available = 0;
		//		}

		//return what's left
		return available;
	}

	/**
	 * @ Returns mastery base when mastery not granted
	 * to player. For calculating damage correctly
	 */
	public static float getQuickMastery(AbstractCharacter pc, String mastery) {
		SkillsBase sb = SkillsBase.getFromCache(mastery);
		if (sb == null) {
			sb = DbManager.SkillsBaseQueries.GET_BASE_BY_NAME(mastery);
			if (sb == null) {
				//Logger.error("CharacterSkill.getQuickMastery", "Unable to find skillsbase of name " + mastery);
				return 0f;
			}
		}

		float bonus = 0f;
		SourceType sourceType = SourceType.GetSourceType(sb.getNameNoSpace());
		if (pc.getBonuses() != null) {
			//Get bonuses from runes
			bonus = pc.getBonuses().getSkillBonus(sb.sourceType);
		}
		float base = 4.75f;
		base += (0.0025f * sb.getStrMod() * pc.getStatStrCurrent());
		base += (0.0025f * sb.getDexMod() * pc.getStatDexCurrent());
		base += (0.0025f * sb.getConMod() * pc.getStatConCurrent());
		base += (0.0025f * sb.getIntMod() * pc.getStatIntCurrent());
		base += (0.0025f * sb.getSpiMod() * pc.getStatSpiCurrent());
		return base + bonus;
	}

	/*
	 * This iterates through players runes and adds and removes skills as needed
	 * Don't Call this directly. Instead call pc.calculateSkills().
	 */
	public static void calculateSkills(PlayerCharacter pc) {
		if (pc == null)
			return;

		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();

		//First add skills that don't exist
		Race race = pc.getRace();
		if (race != null) {
			CharacterSkill.grantSkills(race.getSkillsGranted(), pc);
		} else
			Logger.error( "Failed to find Race for player " + pc.getObjectUUID());
		BaseClass bc = pc.getBaseClass();
		if (bc != null) {
			CharacterSkill.grantSkills(bc.getSkillsGranted(), pc);
		} else
			Logger.error( "Failed to find BaseClass for player " + pc.getObjectUUID());
		PromotionClass promo = pc.getPromotionClass();
		if (promo != null)
			CharacterSkill.grantSkills(promo.getSkillsGranted(), pc);
		ArrayList<CharacterRune> runes = pc.getRunes();
		if (runes != null) {
			for (CharacterRune rune : runes) {
				CharacterSkill.grantSkills(rune.getSkillsGranted(), pc);
			}
		} else
			Logger.error("Failed to find Runes list for player " + pc.getObjectUUID());

		//next remove any skills that no longer belong
		Iterator<CharacterSkill> it = skills.values().iterator();
		while(it.hasNext()) {
			CharacterSkill cs = it.next();
			if (cs == null)
				continue;
			SkillsBase sb = cs.skillsBase;
			if (sb == null) {
				DbManager.CharacterSkillQueries.DELETE_SKILL(cs.getObjectUUID());
				it.remove();
				continue;
			}
			boolean valid = false;
			if (CharacterSkill.skillAllowed(sb.getObjectUUID(), race.getSkillsGranted(), pc))
				continue;
			if (CharacterSkill.skillAllowed(sb.getObjectUUID(), bc.getSkillsGranted(), pc))
				continue;
			if (promo != null)
				if (CharacterSkill.skillAllowed(sb.getObjectUUID(), promo.getSkillsGranted(), pc))
					continue;
			for (CharacterRune rune : runes) {
				if (CharacterSkill.skillAllowed(sb.getObjectUUID(), rune.getSkillsGranted(), pc)) {
					valid = true;
					continue;
				}
			}
			//if skill doesn't belong to any runes, then remove it
			if (!valid) {
				DbManager.CharacterSkillQueries.DELETE_SKILL(cs.getObjectUUID());
				it.remove();
			}
		}
		CharacterSkill.updateAllBaseAmounts(pc);
		CharacterSkill.updateAllModifiedAmounts(pc);
		CharacterPower.calculatePowers(pc);
	}

	/*
	 *This grants skills for specific runes
	 */
	private static void grantSkills(ArrayList<SkillReq> skillsGranted, PlayerCharacter pc) {
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		if (skills == null)
			return;

		for (SkillReq skillreq : skillsGranted) {
			SkillsBase skillsBase = skillreq.getSkillsBase();

			//If player not high enough level for skill, then skip
			if (pc.getLevel() < skillreq.getLevel())
				continue;
			//If player doesn't have prereq skills high enough then skip
			boolean valid = true;
			for (byte prereqSkill : skillreq.getSkillReqs()) {
				SkillsBase sb = null;
				sb = DbManager.SkillsBaseQueries.GET_BASE(prereqSkill);
				if (sb != null) {
					if (skills.containsKey(sb.getName())) {
						if (validForWarrior(pc, skills.get(sb.getName()), skillreq)) {
							valid = true;
							break; //add if any prereq skills met
						} else if (skills.get(sb.getName()).modifiedAmountBeforeMods >= 80) {
							valid = true;
							break; //add if any prereq skills met
							// allow blade masters to use blade master without training sword above 80..
						} else if (skillsBase.getObjectUUID() == 9){
							valid = true;
							break;
						}

					}
				} else {
					Logger.error("Failed to find SkillsBase of ID " + prereqSkill);
				}
				valid = false;
			}
			// Throwing does not need axe,dagger, or hammer at 80%
			if (skillreq.getSkillID() == 43)
				valid = true;
			if (!valid)
				continue;

			//Skill valid for player. Add if don't already have
			if (skillsBase != null) {
				if (!skills.containsKey(skillsBase.getName())) {
					CharacterSkill newSkill = new CharacterSkill(skillsBase, pc);
					CharacterSkill cs = null;
					try {
						cs = DbManager.CharacterSkillQueries.ADD_SKILL(newSkill);
					} catch (Exception e) {
						cs = null;
					}
					if (cs != null){
						cs.requiredLevel = (int) skillreq.getLevel();
						skills.put(skillsBase.getName(), cs);
					}

					else
						Logger.error("Failed to add CharacterSkill to player " + pc.getObjectUUID());
				}
				else{
					CharacterSkill cs = skills.get(skillsBase.getName());
					if (cs != null && cs.requiredLevel == 0) {
						cs.requiredLevel = (int) skillreq.getLevel();
					}
				}

			} else
				Logger.error( "Failed to find SkillsBase for SkillReq " + skillreq.getObjectUUID());
		}
	}

	private static boolean validForWarrior(PlayerCharacter pc, CharacterSkill skill, SkillReq skillreq) {
		if (pc.getPromotionClass() == null || pc.getPromotionClass().getObjectUUID() != 2518 || skill == null || skillreq == null)
			return false; //not a warrior
		int sID = (skill.skillsBase != null) ? skill.skillsBase.getObjectUUID() : 0;
		switch (skillreq.getSkillID()) {
		case 3: //Axe mastery
		case 19: //Great axe mastery
			return (sID == 4) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 15: //Dagger mastery
			return (sID == 16) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 20: //Great hammer mastery
		case 22: //Hammer mastery
			return (sID == 23) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 28: //Polearm mastery
			return (sID == 29) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 21: //Great sword mastery
		case 39: //Sword mastery
			return (sID == 40) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 34: //Spear mastery
			return (sID == 35) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 36: //Staff mastery
			return (sID == 37) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 45: //Unarmed combat mastery
			return (sID == 46) ? (skill.modifiedAmountBeforeMods >= 50) : false;
		case 40:
			return true;
		case 9:
			return true;
		default:
		}
		return false;
	}

	/*
	 * This verifies if a skill is valid for a players rune
	 */
	private static boolean skillAllowed(int UUID, ArrayList<SkillReq> skillsGranted, PlayerCharacter pc) {
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		for (SkillReq skillreq : skillsGranted) {
			SkillsBase sb = skillreq.getSkillsBase();
			if (sb != null) {
				if (sb.getObjectUUID() == UUID) {
					if (skillreq.getLevel() <= pc.getLevel()) {
						SkillsBase sbp = null;
						if (skillreq.getSkillReqs().size() == 0)
							return true;
						for (byte prereqSkill : skillreq.getSkillReqs()) {
							sbp = DbManager.SkillsBaseQueries.GET_BASE(prereqSkill);
							if (sbp != null && skills.containsKey(sbp.getName())) {
								if (validForWarrior(pc, skills.get(sbp.getName()), skillreq)) {
									return true;
								} else if (skills.get(sbp.getName()).modifiedAmountBeforeMods >= 80)
									return true;
							}
						}

						if (skillreq.getSkillID() == 43)
							return true;
						if (skillreq.getSkillID() == 9)
							return true;
					}
				}
			} else
				Logger.error( "Failed to find SkillsBase for SkillReq " + skillreq.getObjectUUID());
		}
		return false;
	}

	//Print skills for player for debugging
	public static void printSkills(PlayerCharacter pc) {
		if (pc == null)
			return;
		ConcurrentHashMap<String, CharacterSkill> skills = pc.getSkills();
		String out = "Player: " + pc.getObjectUUID() + ", SkillCount: " + skills.size();
		Iterator<String> it = skills.keySet().iterator();
		while(it.hasNext()) {
			String name = it.next();
			out += ", " + name;
		}
		Logger.info( out);
	}

	public static int getMaxTrains(int intt) {
		if (intt > 0 && intt < 191)
			return CharacterSkill.maxTrains[intt];
		else
			return (int)(33 + 1.25 * intt - 0.005 * Math.pow(intt, 2));
	}

	public int getSkillPercentFromAttributes(){
		AbstractCharacter ac = CharacterSkill.GetOwner(this);

		if (ac == null)
			return 0;

		float statMod = 0;

		if (this.skillsBase.getStrMod() > 0){
			float strengthModPercent = (float)this.skillsBase.getStrMod() * .01f;
			strengthModPercent *= ac.getStatStrCurrent() * .01f + .6f;
			statMod += strengthModPercent;
		}

		if (this.skillsBase.getDexMod() > 0){
			float dexModPercent = (float)this.skillsBase.getDexMod() * .01f;
			dexModPercent *= ac.getStatDexCurrent() * .01f + .6f;
			statMod += dexModPercent;
		}
		if (this.skillsBase.getConMod() > 0){
			float conModPercent = (float)this.skillsBase.getConMod() * .01f;
			conModPercent *= ac.getStatConCurrent() * .01f + .6f;
			statMod += conModPercent;
		}
		if (this.skillsBase.getIntMod() > 0){
			float intModPercent = (float)this.skillsBase.getIntMod() * .01f;
			intModPercent *= ac.getStatIntCurrent() * .01f + .6f;
			statMod += intModPercent;
		}
		if (this.skillsBase.getSpiMod() > 0){
			float spiModPercent = (float)this.skillsBase.getSpiMod() * .01f;
			spiModPercent *= ac.getStatSpiCurrent() * .01f + .6f;
			statMod += spiModPercent;
		}

		statMod = (float) (Math.pow(statMod, 1.5f) * 15f);
		if (statMod < 1)
			statMod = 1f;
		else if (statMod > 600)
			statMod = 600f;




		return (int) statMod;
	}

	public int getSkillPercentFromTrains(){

		int trains = this.numTrains.get();
		if ( trains <= 10 )
			return 2 * trains;
		if ( trains <= 90 )
			return trains + 10;
		if ( trains  > 130 )
			return (int) (120 - ((trains - 130) * -0.33000001));
		return (int) (100 - ((trains - 90) * -0.5));

	}

	public int getTotalSkillPercet(){

		AbstractCharacter ac = CharacterSkill.GetOwner(this);

		if (ac == null)
			return 0;

		float bonus = 0f;
		SourceType sourceType = SourceType.GetSourceType(this.skillsBase.getNameNoSpace());
		if (CharacterSkill.GetOwner(this).getBonuses() != null) {
			//Get bonuses from runes
			bonus = CharacterSkill.GetOwner(this).getBonuses().getSkillBonus(this.skillsBase.sourceType);
		}
		return this.getSkillPercentFromTrains() + this.getSkillPercentFromAttributes();
	}

	@Override
	public void updateDatabase() {
		DbManager.CharacterSkillQueries.updateDatabase(this);
	}

	public int getRequiredLevel() {
		return requiredLevel;
	}

	public void setRequiredLevel(int requiredLevel) {
		this.requiredLevel = requiredLevel;
	}
}
