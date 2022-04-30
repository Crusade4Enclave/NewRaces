// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.db.handlers;

public class dbEffectsBaseHandler extends dbHandlerBase {

	public dbEffectsBaseHandler() {

	}



	public boolean CreateEffectBase(int token, String IDString,String name,int flags){
		prepareCallable("INSERT INTO `wpak_static_power_effectbase` (`token`,`IDString`,`name`,`flags`) VALUES (?,?,?,?)");
		setInt(1,token);
		setString(2,IDString);
		setString(3,name);
		setInt(4,flags);

		return (executeUpdate() > 0);
	}
	
	public boolean CreateEffectBaseRAW(String IDString,String type,String detail){
		prepareCallable("INSERT INTO `wpak_effect_effectbase_raw` (`token`,`IDString`,`name`,`flags`) VALUES (?,?,?,?)");
		setString(1,IDString);
		setString(2,type);
		setString(3,detail);

		return (executeUpdate() > 0);
	}

	public boolean CreateEffectSource(String IDString,String source){
		prepareCallable("INSERT INTO `wpak_static_power_sourcetype` (`IDString`,`source`) VALUES (?,?)");

		setString(1,IDString);
		setString(2,source);

		return (executeUpdate() > 0);
	}
	
	public boolean CreateEffectSourceRAW(String IDString,String type,String detail){
		prepareCallable("INSERT INTO `wpak_effect_source_raw` (`effectID`,`type`, `text`) VALUES (?,?,?)");

		setString(1,IDString);
		setString(2,type);
		setString(3,detail);

		return (executeUpdate() > 0);
	}

	public boolean CreateEffectCondition(String IDString,String powerOrEffect,String type,float amount,float ramp,byte useAddFormula,String damageType1,String damageType2,String damageType3){
		prepareCallable("INSERT INTO `wpak_static_power_failcondition` (`IDString`,`powerOrEffect`,`type`,`amount`,`ramp`,`useAddFormula`,`damageType1`,`damageType2`,`damageType3`) VALUES (?,?,?,?,?,?,?,?,?)");
		setString(1,IDString);
		setString(2,powerOrEffect);
		setString(3,type);
		setFloat(4,amount);
		setFloat(5,ramp);
		setByte(6,useAddFormula);
		setString(7,damageType1);
		setString(8,damageType2);
		setString(9,damageType3);

		return (executeUpdate() > 0);
	}
	
	public boolean CreateEffectConditionRAW(String IDString,String type,String detail){
		prepareCallable("INSERT INTO `wpak_effect_condition_raw` (`effectID`,`type`, `text`) VALUES (?,?,?)");
		setString(1,IDString);
		setString(2,type);
		setString(3,detail);
		return (executeUpdate() > 0);
	}

	public boolean CreateEffectMod(String IDString,String modType,float minMod,float maxMod,float percentMod,float ramp,byte useRampAdd,String type,String string1,String string2){
		prepareCallable("INSERT INTO `wpak_static_power_effectmod` (`IDString`,`modType`,`minMod`,`maxMod`,`percentMod`,`ramp`,`useRampAdd`,`type`,`string1`,`string2`) VALUES (?,?,?,?,?,?,?,?,?,?)");
		setString(1, IDString);
		setString(2, modType);
		setFloat(3, minMod);
		setFloat(4, maxMod);
		setFloat(5, percentMod);
		setFloat(6, ramp);
		setByte(7, useRampAdd);
		setString(8, type);
		setString(9, string1);
		setString(10, string2);

		return (executeUpdate() > 0);
	}
	
	public boolean CreateEffectModRAW(String IDString,String type,String detail){
		prepareCallable("INSERT INTO `wpak_effect_mod_raw` (`effectID`,`type`, `text`) VALUES (?,?,?)");
		setString(1,IDString);
		setString(2,type);
		setString(3,detail);

		return (executeUpdate() > 0);
	}
	

	public boolean CreatePowerPowerAction(String IDString,String type,String effectID,String effectID2,String deferredPowerID,float levelCap,float levelCapRamp,String damageType,int numIterations,String effectSourceToRemove,String trackFilter,int maxTrack,int mobID,int mobLevel,int simpleDamage,String transferFromType,String transferToType,float transferAmount,float transferRamp,float transferEfficiency,float transferEfficiencyRamp,int flags){
		prepareCallable("INSERT INTO `wpak_static_power_poweraction` (`IDString`,`type`,`effectID`,`effectID2`,`deferredPowerID`,`levelCap`,`levelCapRamp`,`damageType`,`numIterations`,`effectSourceToRemove`,`trackFilter`,`maxTrack`,`mobID`,`mobLevel`,`simpleDamage`,`transferFromType`,`transferToType`,`transferAmount`,`transferRamp`,`transferEfficiency`,`transferEfficiencyRamp`,`flags`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		setString(1,IDString);
		setString(2,type);
		setString(3,effectID);
		setString(4,effectID2);
		setString(5,deferredPowerID);
		setFloat(6,levelCap);
		setFloat(7,levelCapRamp);
		setString(8,damageType);
		setInt(9,numIterations);
		setString(10,effectSourceToRemove);
		setString(11,trackFilter);
		setInt(12,maxTrack);
		setInt(13,mobID);
		setInt(14,mobLevel);
		setInt(15,simpleDamage);
		setString(16,transferFromType);
		setString(17,transferToType);
		setFloat(18,transferAmount);
		setFloat(19,transferRamp);
		setFloat(20,transferEfficiency);
		setFloat(21,transferEfficiencyRamp);
		setInt(22,flags);

		return (executeUpdate() > 0);
	}
	
	public boolean CreatePowerPowerActionRAW(String IDString,String type,String detail){
		prepareCallable("INSERT INTO `wpak_effect_poweraction_raw` (`effectID`,`type`, `text`) VALUES (?,?,?)");

		setString(1,IDString);
		setString(2,type);
		setString(3,detail);

		return (executeUpdate() > 0);
	}

	public boolean ClearAllEffectBase(){
		prepareCallable("DELETE from `wpak_static_power_effectbase`");
		executeUpdate();

		prepareCallable(" DELETE from `wpak_static_power_sourcetype` ");
		executeUpdate();

		prepareCallable(" DELETE from `wpak_static_power_failcondition` WHERE `powerOrEffect` = ?");
		setString(1,"Effect");
		executeUpdate();

		prepareCallable(" DELETE from `wpak_static_power_effectmod` ");
		executeUpdate();

		return true;

	}

	public boolean ResetIncrement(){
		prepareCallable("ALTER TABLE `wpak_static_power_effectbase` AUTO_INCREMENT = 1");
		executeUpdate();

		prepareCallable("ALTER TABLE `wpak_static_power_sourcetype` AUTO_INCREMENT = 1");
		executeUpdate();

		prepareCallable("ALTER TABLE `wpak_static_power_failcondition` AUTO_INCREMENT = 1");
		executeUpdate();

		prepareCallable("ALTER TABLE `wpak_static_power_effectmod` AUTO_INCREMENT = 1");
		executeUpdate();


		return true;
	}

}
