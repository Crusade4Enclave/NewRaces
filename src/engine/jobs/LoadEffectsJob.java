// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.job.AbstractJob;
import engine.net.client.ClientConnection;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;

import java.util.ArrayList;

public class LoadEffectsJob extends AbstractJob {

	ArrayList<AbstractWorldObject> acsToLoad;
	ClientConnection originToSend;

	public LoadEffectsJob(ArrayList<AbstractWorldObject> acsToLoad, ClientConnection origin) {
		this.acsToLoad = acsToLoad;
		this.originToSend = origin;

	}

	@Override
	protected void doJob() {
		if (this.originToSend == null) {
			return;
		}

		for (AbstractWorldObject awo : this.acsToLoad) {

			if (AbstractWorldObject.IsAbstractCharacter(awo)) {
				AbstractCharacter acToLoad = (AbstractCharacter) awo;
				acToLoad.sendAllEffects(this.originToSend);

			}

		}

	}

}
