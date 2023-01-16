// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.gameManager;

/* This enumeration implements Magicbane's configuration data which
   is loaded from environment variables.
 */

import engine.Enum;
import engine.net.NetMsgHandler;
import engine.server.login.LoginServer;
import engine.server.world.WorldServer;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum ConfigManager {

    // Bind address can differ from public address
    // when running over a network bridge, etc.

    MB_PUBLIC_ADDR,
    MB_BIND_ADDR,

    // Database connection config

    MB_DATABASE_ADDRESS,
    MB_DATABASE_PORT,
    MB_DATABASE_NAME,
    MB_DATABASE_USER,
    MB_DATABASE_PASS,

    // Data warehouse remote connection

    MB_WAREHOUSE_ADDR,
    MB_WAREHOUSE_USER,
    MB_WAREHOUSE_PASS,

    // Login server config

    MB_LOGIN_PORT,
    MB_LOGIN_AUTOREG,
    MB_LOGIN_FNAME_REGEX,

    MB_MAJOR_VER,
    MB_MINOR_VER,

    // Worldserver configuration

    MB_WORLD_NAME,
    MB_WORLD_MAPID,
    MB_WORLD_PORT,
    MB_WORLD_ACCESS_LVL,
    MB_WORLD_UUID,
    MB_WORLD_WAREHOUSE_PUSH,
    MB_WORLD_MAINTENANCE,
    MB_WORLD_MAINTENANCE_HOUR,
    MB_WORLD_GREETING,
    MB_WORLD_KEYCLONE_MAX,

    // MagicBot configuration.

    MB_MAGICBOT_SERVERID,
    MB_MAGICBOT_BOTTOKEN,
    MB_MAGICBOT_ROLEID,
    MB_MAGICBOT_ANNOUNCE,
    MB_MAGICBOT_SEPTIC,
    MB_MAGICBOT_CHANGELOG,
    MB_MAGICBOT_POLITICAL,
    MB_MAGICBOT_GENERAL,
    MB_MAGICBOT_FORTOFIX,
    MB_MAGICBOT_RECRUIT,
    MB_MAGICBOT_ADMINLOG,
    MB_MAGICBOT_BOTVERSION,
    MB_MAGICBOT_GAMEVERSION;
    
    // Map to hold our config pulled in from the environment
    // We also use the config to point to the current message pump
    // and determine the server type at runtime.

    public static Map<String, String> configMap = new HashMap(System.getenv());
    public static Enum.ServerType serverType = Enum.ServerType.NONE;
    public static NetMsgHandler handler;
    public static WorldServer worldServer;
    public static LoginServer loginServer;
    public static Map<ConfigManager, Pattern> regex = new HashMap<>();

    // Called at bootstrap: ensures that all config values are loaded.

    public static boolean init() {

        Logger.info("ConfigManager: init()");

        for (ConfigManager configSetting : ConfigManager.values())
            if (configMap.containsKey(configSetting.name()))
                Logger.info(configSetting.name() + ":" + configSetting.getValue());
            else {
                Logger.error("Missing Config: " + configSetting.name());
                Logger.error("This codebase requires >= MagicBox v1.3");
                Logger.error("docker pull magicbane/magicbox:latest");
                return false;
            }

            // compile regex here

            regex.put(MB_LOGIN_FNAME_REGEX, Pattern.compile(MB_LOGIN_FNAME_REGEX.getValue()));

      return true;
    }

    // Get the value associated with this enumeration

    public  String getValue() {
      return configMap.get(this.name());
    }
    public  void  setValue(String value) { configMap.put(this.name(), value); }
}
