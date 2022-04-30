// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers;

import engine.Enum;
import engine.Enum.GameObjectType;
import engine.math.Vector3fImmutable;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.PlayerCharacter;

import java.util.HashSet;


//This package creates a list of AbstractWorldObjects
//sorted by range from a specified point.
public class RangeBasedAwo implements Comparable<RangeBasedAwo> {

	private float range;
	private AbstractWorldObject awo;

	public RangeBasedAwo(Float range, AbstractWorldObject awo) {
		super();
		this.range = range;
		this.awo = awo;
	}

	public static HashSet<RangeBasedAwo> createList(HashSet<AbstractWorldObject> awolist, Vector3fImmutable searchLoc) {
		HashSet<RangeBasedAwo> rbal = new HashSet<>();
		for (AbstractWorldObject awo: awolist) {
			RangeBasedAwo rba = new RangeBasedAwo(searchLoc.distance(awo.getLoc()), awo);
			rbal.add(rba);
		}
		return rbal;
	}

	@Override
	public int compareTo(RangeBasedAwo obj) throws ClassCastException {
		return (int)(this.range - obj.range);
	}

	public static HashSet<AbstractWorldObject> getSortedList(HashSet<AbstractWorldObject> awolist, Vector3fImmutable searchLoc, int maxPlayers, int maxMobs) {
		int playerCnt = 0;
		int mobCnt = 0;
		int maxCnt = (maxPlayers > maxMobs) ? maxPlayers : maxMobs;
		HashSet<RangeBasedAwo> rbal = RangeBasedAwo.createList(awolist, searchLoc);
		awolist = new HashSet<>();
		for (RangeBasedAwo rba : rbal) {
			if (awolist.size() >= maxCnt)
				return awolist;
			AbstractWorldObject awo = rba.awo;

			if (awo.getObjectType().equals(Enum.GameObjectType.PlayerCharacter)) {
				if (playerCnt < maxPlayers) {
					awolist.add(awo);
					playerCnt++;
				}
			} else if (awo.getObjectType().equals(Enum.GameObjectType.Mob)) {
				if (mobCnt < maxMobs) {
					awolist.add(awo);
					mobCnt++;
				}
			}
		}
		return awolist;
	}

	public static HashSet<AbstractCharacter> getTrackList(HashSet<AbstractWorldObject> awolist, PlayerCharacter pc, int max) {
		Vector3fImmutable searchLoc = pc.getLoc();
		int cnt = 0;
		HashSet<RangeBasedAwo> rbal = RangeBasedAwo.createList(awolist, searchLoc);
		HashSet<AbstractCharacter> aclist = new HashSet<>();
		for (RangeBasedAwo rba : rbal) {
			if (aclist.size() >= max)
				return aclist;
			AbstractWorldObject awo = rba.awo;
			
			if (awo.getObjectType().equals(GameObjectType.PlayerCharacter))
				if (((PlayerCharacter)awo).isCSR())
					continue;
					
			if (AbstractWorldObject.IsAbstractCharacter(awo) && !(pc.equals(awo))) {
				aclist.add((AbstractCharacter)awo);
				cnt++;
			}
		}
		return aclist;
	}

	public float getRange() {
		return this.range;
	}

	public AbstractWorldObject getAwo() {
		return this.awo;
	}
}
