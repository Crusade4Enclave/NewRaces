// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.powers.effectmodifiers;

import engine.jobs.AbstractEffectJob;
import engine.objects.AbstractCharacter;
import engine.objects.AbstractWorldObject;
import engine.objects.Building;
import engine.objects.Item;
import engine.powers.EffectsBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemNameEffectModifier extends AbstractEffectModifier {

	String name = "";

	public ItemNameEffectModifier(ResultSet rs) throws SQLException {
		super(rs);

		//We're going to add effect names to a lookup map for ./makeitem
		int ID = rs.getInt("ID");
		switch (ID) { //don't add these ID's to the name list. They're duplicates
			case 4259: return;
			case 4210: return;
			case 4: return;
			case 97: return;
			case 610: return;
			case 4442: return;
			case 5106: return;
			case 4637: return;
			case 2271: return;
			case 587: return;
			case 600: return;
			case 3191: return;
			case 3589: return;
			case 3950: return;
			case 3499: return;
			case 4925: return;
			case 15: return;
			case 5101: return;
			case 2418: return;
			case 183: return;
			case 373: return;
			case 1893: return;
			case 3127: return;
			case 1232: return;
			case 4522: return;
			case 4817: return;
			case 2833: return;
			case 4469: return;
			case 2122: return;
			case 3057: return;
			case 3070: return;
			case 191: return;
			case 3117: return;
			case 3702: return;
			case 1619: return;
			case 2584: return;
			case 414: return;
			case 2078: return;
			case 4844: return;
			case 2275: return;
		}

		String namePre = rs.getString("string1");
		String nameSuf = rs.getString("string2");
		String n = (namePre.isEmpty()) ? nameSuf : namePre;
		this.name = n;
		n = n.toLowerCase();
		n = n.replace(" ", "_");
		String IDString = rs.getString("IDString");
		IDString = IDString.substring(0, IDString.length() - 1);
		EffectsBase.addItemEffectsByName(n, IDString);
	}

	public String getName() {
		return this.name;
	}

	@Override
	protected void _applyEffectModifier(AbstractCharacter source, AbstractWorldObject awo, int trains, AbstractEffectJob effect) {

	}

	@Override
	public void applyBonus(AbstractCharacter ac, int trains) {

	}

	@Override
	public void applyBonus(Item item, int trains) {}
	@Override
	public void applyBonus(Building building, int trains) {}
}
