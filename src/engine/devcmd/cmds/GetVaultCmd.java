// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.gameManager.SessionManager;
import engine.net.client.ClientConnection;
import engine.net.client.msg.VendorDialogMsg;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;

public class GetVaultCmd extends AbstractDevCmd {

	public GetVaultCmd() {
        super("getvault");
    }

	@Override
    protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		if (pcSender == null) return;

		ClientConnection cc = SessionManager.getClientConnection(pcSender);
		if (cc == null) return;

		VendorDialogMsg.getVault(pcSender, null, cc);
		this.setTarget(pcSender); //for logging
	}

	@Override
	protected String _getUsageString() {
        return "' /getvault'";
	}

	@Override
	protected String _getHelpString() {
        return "Opens account vault";
	}

}
