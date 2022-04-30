package engine.devcmd.cmds;

import engine.devcmd.AbstractDevCmd;
import engine.objects.AbstractGameObject;
import engine.objects.PlayerCharacter;
import org.pmw.tinylog.Logger;

/**
 * @author
 * Summary: Devcmd to reboot server
 *
 */

public class RebootCmd extends AbstractDevCmd {

	// Instance variables

	public RebootCmd() {
        super("reboot");
    }


	// AbstractDevCmd Overridden methods

	@Override
	protected void _doCmd(PlayerCharacter pc, String[] args,
			AbstractGameObject target) {

		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("./mbrestart.sh");
		} catch (java.io.IOException err) {
			Logger.info( err.getMessage());
		}

	}

	@Override
	protected String _getHelpString() {
        return "Reboot server";
	}

	@Override
	protected String _getUsageString() {
        return "./reboot";
	}

}
