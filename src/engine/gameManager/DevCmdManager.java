// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

import engine.Enum;
import engine.devcmd.AbstractDevCmd;
import engine.devcmd.cmds.*;
import engine.objects.AbstractGameObject;
import engine.objects.Account;
import engine.objects.PlayerCharacter;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public enum DevCmdManager {
		DEV_CMD_MANAGER;

	public static ConcurrentHashMap<String, AbstractDevCmd> devCmds;

	DevCmdManager() {
		init();
	}
	
	public static void init() {
		DevCmdManager.devCmds = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
		DevCmdManager.registerCommands();
	}

	/**
	 *
	 */
	private static void registerCommands() {
	
		// Player
		DevCmdManager.registerDevCmd(new DistanceCmd());;
		DevCmdManager.registerDevCmd(new HelpCmd());
		DevCmdManager.registerDevCmd(new GetZoneCmd());
		DevCmdManager.registerDevCmd(new GetZoneMobsCmd());
		DevCmdManager.registerDevCmd(new PrintBankCmd());
		DevCmdManager.registerDevCmd(new PrintEquipCmd());
		DevCmdManager.registerDevCmd(new PrintInventoryCmd());
		DevCmdManager.registerDevCmd(new PrintVaultCmd());
		DevCmdManager.registerDevCmd(new PrintStatsCmd());
		DevCmdManager.registerDevCmd(new PrintSkillsCmd());
		DevCmdManager.registerDevCmd(new PrintPowersCmd());
		DevCmdManager.registerDevCmd(new PrintBonusesCmd());
		DevCmdManager.registerDevCmd(new PrintResistsCmd());
		DevCmdManager.registerDevCmd(new PrintLocationCmd());
		DevCmdManager.registerDevCmd(new InfoCmd());
		DevCmdManager.registerDevCmd(new GetHeightCmd());

		// Tester
		DevCmdManager.registerDevCmd(new JumpCmd());
		DevCmdManager.registerDevCmd(new GotoCmd());
		DevCmdManager.registerDevCmd(new SummonCmd());
		DevCmdManager.registerDevCmd(new SetHealthCmd());
		DevCmdManager.registerDevCmd(new SetManaCmd());
		DevCmdManager.registerDevCmd(new SetStaminaCmd());
		DevCmdManager.registerDevCmd(new FindBuildingsCmd());
		DevCmdManager.registerDevCmd(new TeleportModeCmd());
		DevCmdManager.registerDevCmd(new SetLevelCmd());
		DevCmdManager.registerDevCmd(new SetBaseClassCmd());
		DevCmdManager.registerDevCmd(new SetPromotionClassCmd());
		DevCmdManager.registerDevCmd(new EffectCmd());
		DevCmdManager.registerDevCmd(new SetRuneCmd());
		DevCmdManager.registerDevCmd(new GetOffsetCmd());
		DevCmdManager.registerDevCmd(new DebugCmd());
		DevCmdManager.registerDevCmd(new AddGoldCmd());
		DevCmdManager.registerDevCmd(new ZoneInfoCmd());
		DevCmdManager.registerDevCmd(new DebugMeleeSyncCmd());
		DevCmdManager.registerDevCmd(new HotzoneCmd());
		DevCmdManager.registerDevCmd(new SetActivateMineCmd());
		// Dev
		DevCmdManager.registerDevCmd(new ApplyStatModCmd());
		DevCmdManager.registerDevCmd(new AddBuildingCmd());
		DevCmdManager.registerDevCmd(new AddNPCCmd());
		DevCmdManager.registerDevCmd(new AddMobCmd());
		DevCmdManager.registerDevCmd(new CopyMobCmd());
		DevCmdManager.registerDevCmd(new RemoveObjectCmd());
		DevCmdManager.registerDevCmd(new RotateCmd());
		DevCmdManager.registerDevCmd(new FlashMsgCmd());
		DevCmdManager.registerDevCmd(new SysMsgCmd());
		DevCmdManager.registerDevCmd(new GetBankCmd());
		DevCmdManager.registerDevCmd(new GetVaultCmd());
		DevCmdManager.registerDevCmd(new CombatMessageCmd());
		DevCmdManager.registerDevCmd(new RenameMobCmd());
		DevCmdManager.registerDevCmd(new RenameCmd());
		DevCmdManager.registerDevCmd(new CreateItemCmd());
		DevCmdManager.registerDevCmd(new GetMemoryCmd());
		DevCmdManager.registerDevCmd(new SetRankCmd());
		DevCmdManager.registerDevCmd(new MakeBaneCmd());
		DevCmdManager.registerDevCmd(new RemoveBaneCmd());
		DevCmdManager.registerDevCmd(new SetBaneActiveCmd());
		DevCmdManager.registerDevCmd(new SetAdminRuneCmd());
		DevCmdManager.registerDevCmd(new SetInvulCmd());
		DevCmdManager.registerDevCmd(new MakeItemCmd());
		DevCmdManager.registerDevCmd(new EnchantCmd());
		DevCmdManager.registerDevCmd(new SetSubRaceCmd());
		// Admin
		DevCmdManager.registerDevCmd(new GetCacheCountCmd());
		DevCmdManager.registerDevCmd(new GetRuneDropRateCmd());
		DevCmdManager.registerDevCmd(new DecachePlayerCmd());
		DevCmdManager.registerDevCmd(new SetRateCmd());
		DevCmdManager.registerDevCmd(new AuditMobsCmd());
		DevCmdManager.registerDevCmd(new ChangeNameCmd());
		DevCmdManager.registerDevCmd(new GuildListCmd());
		DevCmdManager.registerDevCmd(new SetGuildCmd());
		DevCmdManager.registerDevCmd(new SetOwnerCmd());
		DevCmdManager.registerDevCmd(new NetDebugCmd());
		DevCmdManager.registerDevCmd(new SqlDebugCmd());
		DevCmdManager.registerDevCmd(new PullCmd());
		DevCmdManager.registerDevCmd(new PurgeObjectsCmd());
		DevCmdManager.registerDevCmd(new SplatMobCmd());
		DevCmdManager.registerDevCmd(new SlotNpcCmd());
		DevCmdManager.registerDevCmd(new SetAICmd());
		DevCmdManager.registerDevCmd(new GateInfoCmd());
		DevCmdManager.registerDevCmd(new ShowOffsetCmd());
		DevCmdManager.registerDevCmd(new RealmInfoCmd());
		DevCmdManager.registerDevCmd(new RebootCmd());
		DevCmdManager.registerDevCmd(new AddMobPowerCmd());
		DevCmdManager.registerDevCmd(new AddMobRuneCmd());
		DevCmdManager.registerDevCmd(new SetMineTypeCmd());
		DevCmdManager.registerDevCmd(new SetMineExpansion());
		DevCmdManager.registerDevCmd(new SetForceRenameCityCmd());
		DevCmdManager.registerDevCmd(new GotoObj());
		DevCmdManager.registerDevCmd(new convertLoc());
		DevCmdManager.registerDevCmd(new GetMobBaseLoot());
		DevCmdManager.registerDevCmd(new MBDropCmd());
		DevCmdManager.registerDevCmd(new GetDisciplineLocCmd());
		DevCmdManager.registerDevCmd(new AuditHeightMapCmd());
		DevCmdManager.registerDevCmd(new UnloadFurnitureCmd());
		DevCmdManager.registerDevCmd(new SetNPCSlotCmd());
		DevCmdManager.registerDevCmd(new SetNpcEquipSetCmd());
		DevCmdManager.registerDevCmd(new SetBuildingAltitudeCmd());
		DevCmdManager.registerDevCmd(new ResetLevelCmd());
		DevCmdManager.registerDevCmd(new HeartbeatCmd());
		DevCmdManager.registerDevCmd(new SetNpcNameCmd());
		DevCmdManager.registerDevCmd(new SetNpcMobbaseCmd());
		DevCmdManager.registerDevCmd(new DespawnCmd());
		DevCmdManager.registerDevCmd(new BoundsCmd());
		DevCmdManager.registerDevCmd(new GotoBoundsCmd());
		DevCmdManager.registerDevCmd(new RegionCmd());
		DevCmdManager.registerDevCmd(new SetMaintCmd());
		DevCmdManager.registerDevCmd(new ApplyBonusCmd());
		DevCmdManager.registerDevCmd(new setOpenDateCmd());
		DevCmdManager.registerDevCmd(new AuditFailedItemsCmd());

	}

	private static void registerDevCmd(AbstractDevCmd cmd) {
		ArrayList<String> cmdStrings = cmd.getCmdStrings();
		for (String cmdString : cmdStrings) {
			DevCmdManager.devCmds.put(cmdString, cmd);
		}
	}

	public static AbstractDevCmd getDevCmd(String cmd) {
			String lowercase = cmd.toLowerCase();
			return DevCmdManager.devCmds.get(lowercase);
	}

	public static boolean handleDevCmd(PlayerCharacter pcSender, String cmd,
			String argString, AbstractGameObject target) {

		if (pcSender == null) {
			return false;
		}

		Account a = SessionManager.getAccount(pcSender);

		if (a == null) {
			return false;
		}

		AbstractDevCmd adc = DevCmdManager.getDevCmd(cmd);

		if (adc == null) {
			return false;
		}

		//kill any commands not available to everyone on production server
		//only admin level can run dev commands on production

		if (a.status.equals(Enum.AccountStatus.ADMIN) == false) {
			Logger.info("Account " + a.getUname() + "attempted to use dev command " + cmd);
			return false;
		}

		// TODO add a job here to separate calling thread form executing thread?
		// Log

		String accName = a.getUname();
		String pcName = pcSender.getCombinedName();
		String logString = pcName + '(' + accName
				+ ") '";
		logString += cmd + ' ' + argString + '\'';
		Logger.info( logString);

		// execute command;
		try {
			adc.doCmd(pcSender, argString, target);
		} catch (Exception e) {
			Logger.error(e.toString());
			e.printStackTrace();
		}

		return true;
	}

	public static String getCmdsForAccessLevel() {
		String out = "";

		for (Entry<String, AbstractDevCmd> e : DevCmdManager.devCmds.entrySet())
			out += e.getKey() + ", ";

		return out;
	}

}
