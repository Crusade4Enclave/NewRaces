// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.Enum.ModType;
import engine.Enum.SourceType;
import engine.gameManager.DbManager;
import engine.gameManager.PowersManager;
import engine.jobs.AbstractEffectJob;
import engine.objects.*;
import engine.powers.EffectsBase;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;


public abstract class AbstractEffectModifier {

	protected EffectsBase parent;
	protected int UUID;
	protected String IDString;
	protected String effectType;
	protected float minMod;
	protected float maxMod;
	protected float percentMod;
	protected float ramp;
	protected boolean useRampAdd;
	protected String type;
	public SourceType sourceType;
	
	protected String string1;
	protected String string2;
	public ModType modType;

	public AbstractEffectModifier(ResultSet rs) throws SQLException {

		this.UUID = rs.getInt("ID");
		this.IDString = rs.getString("IDString");
		this.effectType = rs.getString("modType");
		this.modType = ModType.GetModType(this.effectType);		
		this.type = rs.getString("type").replace("\"", "");
			this.sourceType = SourceType.GetSourceType(this.type.replace(" ", "").replace("-", ""));
			this.minMod = rs.getFloat("minMod");
		this.maxMod = rs.getFloat("maxMod");
		this.percentMod = rs.getFloat("percentMod");
		this.ramp = rs.getFloat("ramp");
		this.useRampAdd = (rs.getInt("useRampAdd") == 1) ? true : false;
		
		this.string1 = rs.getString("string1");
		this.string2 = rs.getString("string2");
	}

	public static ArrayList<AbstractEffectModifier> getAllEffectModifiers() {
		PreparedStatementShared ps = null;
		ArrayList<AbstractEffectModifier> out = new ArrayList<>();
		try {
			ps = new PreparedStatementShared("SELECT * FROM static_power_effectmod");
			ResultSet rs = ps.executeQuery();
		 String IDString;
			AbstractEffectModifier aem = null;
			while (rs.next()) {
				IDString = rs.getString("IDString");
				int token = DbManager.hasher.SBStringHash(IDString);

				EffectsBase eb = PowersManager.getEffectByIDString(IDString);

				ModType modifier = ModType.GetModType(rs.getString("modType"));

				//combine item prefix and suffix effect modifiers
				switch (modifier){
				case AdjustAboveDmgCap:
					aem = new AdjustAboveDmgCapEffectModifier(rs);
					break;
				case Ambidexterity:
					aem = new AmbidexterityEffectModifier(rs);
					break;
				case AnimOverride:
					break;
				case ArmorPiercing:
					aem = new ArmorPiercingEffectModifier(rs);
					break;
				case AttackDelay:
					aem = new AttackDelayEffectModifier(rs);
					break;
				case Attr:
					aem = new AttributeEffectModifier(rs);
					break;
				case BlackMantle:
					aem = new BlackMantleEffectModifier(rs);
					break;
				case BladeTrails:
					aem = new BladeTrailsEffectModifier(rs);
  					break;
				case Block:
					aem = new BlockEffectModifier(rs);
					break;
				case BlockedPowerType:
					aem = new BlockedPowerTypeEffectModifier(rs);
					break;
				case CannotAttack:
					aem = new CannotAttackEffectModifier(rs);
					break;
				case CannotCast:
					aem = new CannotCastEffectModifier(rs);
					break;
				case CannotMove:
					aem = new CannotMoveEffectModifier(rs);
					break;
				case CannotTrack:
					aem = new CannotTrackEffectModifier(rs);
					break;
				case Charmed:
					aem = new CharmedEffectModifier(rs);
					break;
				case ConstrainedAmbidexterity:
					aem = new ConstrainedAmbidexterityEffectModifier(rs);
					break;
				case DamageCap:
					aem = new DamageCapEffectModifier(rs);
					break;
				case DamageShield:
					aem = new DamageShieldEffectModifier(rs);
					break;
				case DCV:
					aem = new DCVEffectModifier(rs);
					break;
				case Dodge:
					aem = new DodgeEffectModifier(rs);
					break;
				case DR:
					aem = new DREffectModifier(rs);
					break;
				case Durability:
					aem = new DurabilityEffectModifier(rs);
					break;
				case ExclusiveDamageCap:
					aem = new ExclusiveDamageCapEffectModifier(rs);
					break;
				case Fade:
					aem = new FadeEffectModifier(rs);
					break;
				case Fly:
					aem = new FlyEffectModifier(rs);
					break;
				case Health:
					aem = new HealthEffectModifier(rs);
					break;
				case HealthFull:
					aem = new HealthFullEffectModifier(rs);
					break;
				case HealthRecoverRate:
					aem = new HealthRecoverRateEffectModifier(rs);
					break;
				case IgnoreDamageCap:
					aem = new IgnoreDamageCapEffectModifier(rs);
					break;
				case IgnorePassiveDefense:
					aem = new IgnorePassiveDefenseEffectModifier(rs);
					break;
				case ImmuneTo:
					aem = new ImmuneToEffectModifier(rs);
					break;
				case ImmuneToAttack:
					aem = new ImmuneToAttackEffectModifier(rs);
					break;
				case ImmuneToPowers:
					aem = new ImmuneToPowersEffectModifier(rs);
					break;
				case Invisible:
					aem = new InvisibleEffectModifier(rs);
					break;
				case ItemName:
					aem = new ItemNameEffectModifier(rs);
					if ((((ItemNameEffectModifier)aem).name.isEmpty()))
							break;
					if (eb != null)
						eb.setName((((ItemNameEffectModifier)aem).name));
					break;
				case Mana:
					aem = new ManaEffectModifier(rs);
					break;
				case ManaFull:
					aem = new ManaFullEffectModifier(rs);
					break;
				case ManaRecoverRate:
					aem = new ManaRecoverRateEffectModifier(rs);
					break;
				case MaxDamage:
					aem = new MaxDamageEffectModifier(rs);
					break;
				case MeleeDamageModifier:
					aem = new MeleeDamageEffectModifier(rs);
					break;
				case MinDamage:
					aem = new MinDamageEffectModifier(rs);
					break;
				case NoMod:
					aem = new NoModEffectModifier(rs);
					break;
				case OCV:
					aem = new OCVEffectModifier(rs);
					break;
				case Parry:
					aem = new ParryEffectModifier(rs);
					break;
				case PassiveDefense:
					aem = new PassiveDefenseEffectModifier(rs);
				case PowerCost:
					aem = new PowerCostEffectModifier(rs);
					break;
				case PowerCostHealth:
					aem = new PowerCostHealthEffectModifier(rs);
					break;
				case PowerDamageModifier:
					aem = new PowerDamageEffectModifier(rs);
					break;
				case ProtectionFrom:
					aem = new ProtectionFromEffectModifier(rs);
					break;
				case Resistance:
					aem = new ResistanceEffectModifier(rs);
					break;
				case ScaleHeight:
					aem = new ScaleHeightEffectModifier(rs);
					break;
				case ScaleWidth:
					aem = new ScaleWidthEffectModifier(rs);
					break;
				case ScanRange:
					aem = new ScanRangeEffectModifier(rs);
					break;
				case SeeInvisible:
					aem = new SeeInvisibleEffectModifier(rs);
					break;
				case Silenced:
					aem = new SilencedEffectModifier(rs);
					break;
				case Skill:
					aem = new SkillEffectModifier(rs);
					break;
				case Slay:
					aem = new SlayEffectModifier(rs);
					break;
				case Speed:
					aem = new SpeedEffectModifier(rs);
					break;
				case SpireBlock:
					aem = new SpireBlockEffectModifier(rs);
					break;
				case Stamina:
					aem = new StaminaEffectModifier(rs);
					break;
				case StaminaFull:
					aem = new StaminaFullEffectModifier(rs);
					break;
				case StaminaRecoverRate:
					aem = new StaminaRecoverRateEffectModifier(rs);
					break;
				case Stunned:
					aem = new StunnedEffectModifier(rs);
					break;
				case Value:
					aem = new ValueEffectModifier(rs);
					if (eb != null){
						ValueEffectModifier valueEffect = (ValueEffectModifier)aem;
						eb.setValue(valueEffect.minMod);
					}
					break;
				case WeaponProc:
					aem = new WeaponProcEffectModifier(rs);
					break;
				case WeaponRange:
					aem = new WeaponRangeEffectModifier(rs);
					break;
				case WeaponSpeed:
					aem = new WeaponSpeedEffectModifier(rs);
					break;
			
			}

			if (aem != null){
			
		
				if (EffectsBase.modifiersMap.containsKey(eb.getIDString()) == false)
					EffectsBase.modifiersMap.put(eb.getIDString(), new HashSet<>());
				EffectsBase.modifiersMap.get(eb.getIDString()).add(aem);
				
			}
			}
			rs.close();
		} catch (Exception e) {
			Logger.error( e);
		} finally {
			ps.release();
		}
		return out;
	}







	public int getUUID() {
		return this.UUID;
	}

	// public String getIDString() {
	// return this.IDString;
	// }

	public String getmodType() {
		return this.effectType;
	}

	public float getMinMod() {
		return this.minMod;
	}

	public float getMaxMod() {
		return this.maxMod;
	}

	public float getPercentMod() {
		return this.percentMod;
	}

	public float getRamp() {
		return this.ramp;
	}

	public String getType() {
		return this.type;
	}

	public String getString1() {
		return this.string1;
	}

	public String getString2() {
		return this.string2;
	}

	public EffectsBase getParent() {
		return this.parent;
	}

	public void setParent(EffectsBase value) {
		this.parent = value;
	}

	public void applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

		_applyEffectModifier(source, awo, trains, effect);
	}

	protected abstract void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect);

	public abstract void applyBonus(AbstractCharacter ac, int trains);
	public abstract void applyBonus(Item item, int trains);
	public abstract void applyBonus(Building building, int trains);
}
