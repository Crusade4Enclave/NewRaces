// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


 package engine.powers;

import engine.Enum.DamageType;

public class DamageShield {

	private final DamageType damageType;
	private final float amount;
	private final boolean usePercent;

	public DamageShield(DamageType damageType, float amount, boolean usePercent) {
		super();
		this.damageType = damageType;
		this.amount = amount;
		this.usePercent = usePercent;
	}

	public DamageType getDamageType() {
		return this.damageType;
	}

	public float getAmount() {
		return this.amount;
	}

	public boolean usePercent() {
		return this.usePercent;
	}

	@Override
	public String toString() {
		return "ds.DamageType: " + this.damageType.name() + ", Amount: " + this.amount + ", UsePercent: " + this.usePercent;
	}
}
