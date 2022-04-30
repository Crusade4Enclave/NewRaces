// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

import engine.Enum.GameObjectType;
import engine.gameManager.DbManager;
import engine.objects.AbstractGameObject;
import engine.objects.RuneBaseEffect;

import java.util.ArrayList;
import java.util.HashMap;

public class dbRuneBaseEffectHandler extends dbHandlerBase {

	public dbRuneBaseEffectHandler() {
		this.localClass = RuneBaseEffect.class;
		this.localObjectType = engine.Enum.GameObjectType.valueOf(this.localClass.getSimpleName());
	}

	public ArrayList<RuneBaseEffect> GET_EFFECTS_FOR_RUNEBASE(int id) {
		prepareCallable("SELECT * FROM `static_rune_baseeffect` WHERE `runeID`=?");
		setInt(1, id);
		return getObjectList();
	}

	public RuneBaseEffect GET_RUNEBASE_EFFECT(int id) {

		if (id == 0)
			return null;
		RuneBaseEffect runeBaseEffect = (RuneBaseEffect) DbManager.getFromCache(GameObjectType.RuneBaseEffect, id);
		if (runeBaseEffect != null)
			return runeBaseEffect;
		prepareCallable("SELECT * FROM `static_rune_baseeffect` WHERE `ID` = ?");
		setInt(1, id);
		return (RuneBaseEffect) getObjectSingle(id);
	}

	public ArrayList<RuneBaseEffect> GET_ALL_RUNEBASE_EFFECTS(){
		prepareCallable("SELECT * FROM `static_rune_baseeffect`;");
		return  getObjectList();
	}

	//This calls from cache only. Call this AFTER caching all runebase effects;
	public HashMap<Integer, ArrayList<RuneBaseEffect>> LOAD_BASEEFFECTS_FOR_RUNEBASE() {
		HashMap<Integer, ArrayList<RuneBaseEffect>> runeBaseEffectSet;
		runeBaseEffectSet = new HashMap<>();


		for (AbstractGameObject runeBaseEffect:DbManager.getList(GameObjectType.RuneBaseEffect)){

			int runeBaseID = ((RuneBaseEffect)runeBaseEffect).getRuneBaseID();
			if (runeBaseEffectSet.get(runeBaseID) == null){
				ArrayList<RuneBaseEffect> runeBaseEffectList = new ArrayList<>();
				runeBaseEffectList.add((RuneBaseEffect)runeBaseEffect);
				runeBaseEffectSet.put(runeBaseID, runeBaseEffectList);
			}
			else{
				ArrayList<RuneBaseEffect>runeBaseEffectList = runeBaseEffectSet.get(runeBaseID);
				runeBaseEffectList.add((RuneBaseEffect)runeBaseEffect);
				runeBaseEffectSet.put(runeBaseID, runeBaseEffectList);
			}
		}
		return runeBaseEffectSet;
	}

}
