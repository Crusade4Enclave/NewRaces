// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.jobs;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.job.AbstractScheduleJob;
import engine.objects.AbstractGameObject;
import engine.objects.Building;
import engine.objects.PlayerCharacter;

public class DatabaseUpdateJob extends AbstractScheduleJob {

	private final AbstractGameObject ago;
	private final String type;

	public DatabaseUpdateJob(AbstractGameObject ago, String type) {
		super();
		this.ago = ago;
		this.type = type;
	}

	@Override
	protected void doJob() {
		if (this.ago == null)
			return;
		ago.removeDatabaseJob(this.type, false);
                
		if (ago.getObjectType().equals(GameObjectType.PlayerCharacter)) {
                    
                    PlayerCharacter pc = (PlayerCharacter) ago;
                        
                    switch (this.type) {
                        case "Skills":
                            pc.updateSkillsAndPowersToDatabase();
                            break;
                        case "Stats":
                            DbManager.PlayerCharacterQueries.UPDATE_CHARACTER_STATS(pc);
                            break;
                        case "EXP":
                            DbManager.PlayerCharacterQueries.UPDATE_CHARACTER_EXPERIENCE(pc);
                            break;
                    }

		}
		else if (ago instanceof Building) {
			Building b = (Building) ago;
			if (this.type.equals("health"))
				DbManager.BuildingQueries.UPDATE_BUILDING_HEALTH(b.getObjectUUID(), (int)(b.getHealth()));
		}

	}

	@Override
	protected void _cancelJob() {
	}
}

