// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.ZoneManager;
import engine.math.FastMath;
import engine.net.client.msg.HotzoneChangeMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import engine.objects.Zone;
import engine.server.world.WorldServer;

/**
 * ./hotzone                      <- display the current hotzone & time remaining
 * ./hotzone random               <- change hotzone to random new zone
 * ./hotzone name of a macrozone  <- change hotzone to the zone name provided
 *
 */
public class HotzoneCmd extends AbstractDevCmd {

    public HotzoneCmd() {
        super("hotzone");
    }

    @Override
    protected void _doCmd(PlayerCharacter pc, String[] words,
                          AbstractGameObject target) {

        StringBuilder data = new StringBuilder();
        for (String s : words) {
            data.append(s);
            data.append(' ');
        }
        String input = data.toString().trim();

        if (input.length() == 0) {
            throwbackInfo(pc, "Current hotzone: " + hotzoneInfo());
            return;
        }

        Zone zone;

        if (input.equalsIgnoreCase("random")) {
            throwbackInfo(pc, "Previous hotzone: " + hotzoneInfo());
            ZoneManager.generateAndSetRandomHotzone();
            zone = ZoneManager.getHotZone();
        } else {
            zone = ZoneManager.findMacroZoneByName(input);

            if (zone == null) {
                throwbackError(pc, "Cannot find a macrozone with that name.");
                return;
            }

            if (zone == ZoneManager.getHotZone()) {
                throwbackInfo(pc, "That macrozone is already the Hotzone.");
                return;
            }

            if (ZoneManager.validHotZone(zone) == false) {
                throwbackError(pc, "That macrozone cannot be set as the Hotzone.");
                return;
            }

            throwbackInfo(pc, "Previous hotzone: " + hotzoneInfo());
            ZoneManager.setHotZone(zone);
        }

        throwbackInfo(pc, "New hotzone: " + hotzoneInfo());
        HotzoneChangeMsg hcm = new HotzoneChangeMsg(zone.getObjectType().ordinal(), zone.getObjectUUID());
        WorldServer.setLastHZChange(System.currentTimeMillis());
    }

    @Override
    protected String _getHelpString() {
        return "Use no arguments to see the current hotzone.  Specify a macrozone name to change the hotzone, or \"random\" to change it randomly.";
    }

    @Override
    protected String _getUsageString() {
        return "'./hotzone [random | <macroZoneName>]";
    }

    private static String hotzoneInfo() {
        final int hotzoneTimeLeft = FastMath.secondsUntilNextHour();
        final Zone hotzone = ZoneManager.getHotZone();
        String hotzoneInfo;

        if (hotzone == null) {
            hotzoneInfo = "none";
        } else {
            int hr = hotzoneTimeLeft/3600;
            int rem = hotzoneTimeLeft%3600;
            int mn = rem/60;
            int sec = rem%60;
            hotzoneInfo = hotzone.getName() +
                    " (" + (hr<10 ? "0" : "") + hr + ':' +
                    (mn<10 ? "0" : "") + mn + ':' +
                    (sec<10 ? "0" : "") + sec +
                    " remaining)";
        }
        return hotzoneInfo;
    }

}
