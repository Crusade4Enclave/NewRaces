// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class GetMemoryCmd extends AbstractDevCmd {

	public GetMemoryCmd() {
        super("getmemory");
    }

	@Override
	protected void _doCmd(PlayerCharacter pcSender, String[] words,
			AbstractGameObject target) {
		if (pcSender == null) return;

		String hSize = getMemoryOutput(Runtime.getRuntime().totalMemory());
		String mhSize = getMemoryOutput(Runtime.getRuntime().maxMemory());
		String fhSize = getMemoryOutput(Runtime.getRuntime().freeMemory());

		String out = "Heap Size: " + hSize + ", Max Heap Size: " + mhSize + ", Free Heap Size: " + fhSize;
		throwbackInfo(pcSender, out);
	}

	public static String getMemoryOutput(long memory) {
		String out = "";
		if (memory > 1073741824)
			return (memory / 1073741824) + "GB";
		else if (memory > 1048576)
			return (memory / 1048576) + "MB";
		else if (memory > 1024)
			return (memory / 1048576) + "KB";
		else
			return memory + "B";
	}

	@Override
	protected String _getUsageString() {
		return "' /getmemory'";
	}

	@Override
	protected String _getHelpString() {
		return "lists memory usage";
	}

}
