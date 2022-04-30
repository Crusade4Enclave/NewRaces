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

/**
 *
 * @author Eighty
 *
 */
public class GetBankCmd extends AbstractDevCmd {

	public GetBankCmd() {
        super("getbank");
    }

	@Override
    protected void _doCmd(PlayerCharacter pcSender, String[] args,
			AbstractGameObject target) {
		if (pcSender == null) return;

		ClientConnection cc = SessionManager.getClientConnection(pcSender);
		if (cc == null) return;

		VendorDialogMsg.getBank(pcSender, null, cc);
		this.setTarget(pcSender); //for logging
	}

	@Override
	protected String _getUsageString() {
        return "' /getbank'";
	}

	@Override
	protected String _getHelpString() {
        return "Opens bank window";
	}

}
