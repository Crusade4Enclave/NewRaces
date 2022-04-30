// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com





// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.jobs;

import engine.job.AbstractJob;
import engine.objects.Mine;
import org.pmw.tinylog.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MineActiveJob extends AbstractJob {

	public MineActiveJob() {
		super();
	}

	@Override
	protected void doJob() {
		ArrayList<Mine> mines = Mine.getMines();
		LocalDateTime now = LocalDateTime.now();

		for (Mine mine : mines) {
			try {
				
				if (mine.getOwningGuild() == null){
					mine.handleStartMineWindow();
					Mine.setLastChange(System.currentTimeMillis());
					continue;
				}
					
				//handle claimed mines
                LocalDateTime mineWindow =  mine.openDate.withMinute(0).withSecond(0).withNano(0);
				if (mineWindow != null && now.plusMinutes(1).isAfter(mineWindow))
					if (!mine.getIsActive()) {
						Logger.info("activating mine. " + mineWindow.getHour() + " , " + now.getHour());
						mine.handleStartMineWindow();
						Mine.setLastChange(System.currentTimeMillis());
					
					}else{
						if (mine.handleEndMineWindow()){
							Logger.info("Deactivating mine. " + mineWindow.getHour() + " , " + now.getHour());
							Mine.setLastChange(System.currentTimeMillis());
						}
							
					}
			}catch (Exception e) {
				Logger.error( "mineID: " + mine.getObjectUUID() + e);
			}
		}
	}
}
