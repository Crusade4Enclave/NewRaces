// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.exception.SerializationException;
import engine.gameManager.PowersManager;
import engine.net.ByteBufferWriter;
import engine.powers.EffectsBase;
import engine.powers.poweractions.AbstractPowerAction;
import org.pmw.tinylog.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class MobEquipment extends AbstractGameObject {

	private static AtomicInteger equipCounter = new AtomicInteger(0);
	private final ItemBase itemBase;
	private int slot;
	private int parentID;

	//effects
	private boolean enchanted;
	private boolean isID = false;
	private AbstractPowerAction prefix;
	private AbstractPowerAction suffix;
	private int pValue;
	private int sValue;
	private int magicValue;

	private float dropChance = 0;

	/**
	 * No Id Constructor
	 */
	public MobEquipment(ItemBase itemBase, int slot, int parentID) {
		super(MobEquipment.getNewID());
		this.itemBase = itemBase;
		this.slot = slot;
		this.parentID = parentID;
		this.enchanted = false;
		this.prefix = null;
		this.suffix = null;
		this.pValue = 0;
		this.sValue = 0;
		setMagicValue();
	}

	public MobEquipment(ItemBase itemBase, int slot, int parentID, String pIDString, String sIDString, int pValue, int sValue) {
		super(MobEquipment.getNewID());
		this.itemBase = itemBase;
		this.slot = slot;
		this.parentID = parentID;

		//add effects
		this.prefix = PowersManager.getPowerActionByIDString(pIDString);
		this.suffix = PowersManager.getPowerActionByIDString(sIDString);

		this.pValue = pValue;
		this.sValue = sValue;
		this.enchanted = this.prefix == null || this.suffix == null;
		setMagicValue();
	}

	/**
	 * ResultSet Constructor
	 */
	public MobEquipment(ResultSet rs) throws SQLException {
		super(MobEquipment.getNewID());
		int itemBaseID = rs.getInt("ItemID");
		this.itemBase = ItemBase.getItemBase(itemBaseID);
		this.slot = rs.getInt("slot");
		this.parentID = rs.getInt("mobID");
		setMagicValue();
	}


	public MobEquipment(int itemBaseID,float dropChance)  {
		super(MobEquipment.getNewID());
		this.itemBase = ItemBase.getItemBase(itemBaseID);

		if (this.itemBase != null)
			this.slot = this.itemBase.getValidSlot();
		else{
			Logger.error("Failed to find Itembase for ID : "  + itemBaseID);
			this.slot = 0;
		}

		this.dropChance = dropChance;

		this.parentID = 0;
		setMagicValue();
	}

	public ItemBase getItemBase() {
		return itemBase;
	}

	public int getSlot() {
		return this.slot;
	}

	public void setSlot(int value) {
		this.slot = value;
	}

	public static int getNewID() {
		return MobEquipment.equipCounter.incrementAndGet();
	}

	public static void serializeForVendor(MobEquipment mobEquipment,ByteBufferWriter writer, float percent) throws SerializationException {
		_serializeForClientMsg(mobEquipment,writer, false);
		int baseValue = mobEquipment.itemBase.getBaseValue() + mobEquipment.itemBase.getMagicValue();
		writer.putInt(mobEquipment.magicValue);
		writer.putInt(mobEquipment.magicValue);
	}

	
	public static void serializeForClientMsg(MobEquipment mobEquipment,ByteBufferWriter writer) throws SerializationException {
		_serializeForClientMsg(mobEquipment,writer, true);
	}

	public static void _serializeForClientMsg(MobEquipment mobEquipment,ByteBufferWriter writer, boolean useSlot) throws SerializationException {

		if (useSlot)
			writer.putInt(mobEquipment.slot);
		writer.putInt(0); // Pad
		writer.putInt(mobEquipment.itemBase.getUUID());
		writer.putInt(mobEquipment.getObjectType().ordinal());
		writer.putInt(mobEquipment.getObjectUUID());

		// Unknown statics
		for (int i = 0; i < 3; i++) {
			writer.putInt(0); // Pad
		}
		for (int i = 0; i < 4; i++) {
			writer.putInt(0x3F800000); // Static
		}
		for (int i = 0; i < 5; i++) {
			writer.putInt(0); // Pad
		}
		for (int i = 0; i < 2; i++) {
			writer.putInt(0xFFFFFFFF); // Static
		}

		writer.putInt(0);

		writer.put((byte) 1); // End Datablock byte
		writer.putInt(0); // Unknown. pad?
		writer.put((byte) 1); // End Datablock byte

		writer.putFloat(mobEquipment.itemBase.getDurability());
		writer.putFloat(mobEquipment.itemBase.getDurability());

		writer.put((byte) 1); // End Datablock byte

		writer.putInt(0); // Pad
		writer.putInt(0); // Pad

		writer.putInt(mobEquipment.itemBase.getBaseValue());
		writer.putInt(mobEquipment.magicValue);

		serializeEffects(mobEquipment,writer);

		writer.putInt(0x00000000);

		//name color, think mobEquipment is where mobEquipment goes
		if (mobEquipment.enchanted)
			if (mobEquipment.isID)
				writer.putInt(36);
			else
				writer.putInt(40);
		else
			writer.putInt(4);

		writer.putInt(0);
		writer.putInt(0); // Pad
		writer.putInt(1);
		writer.putShort((short) 0);
		writer.put((byte) 0);
	}

	public final void setMagicValue() {
		float value = 1;
		if (itemBase != null)
			value = itemBase.getBaseValue();
		if (this.prefix != null) {
			if (this.prefix.getEffectsBase() != null)
				value += this.prefix.getEffectsBase().getValue();
			if (this.prefix.getEffectsBase2() != null)
				value += this.prefix.getEffectsBase2().getValue();
		}
		if (this.suffix != null) {
			if (this.suffix.getEffectsBase() != null)
				value += this.suffix.getEffectsBase().getValue();
			if (this.suffix.getEffectsBase2() != null)
				value += this.suffix.getEffectsBase2().getValue();
		}

		if (itemBase != null)

			for (Integer token : itemBase.getBakedInStats().keySet()) {

				EffectsBase effect = PowersManager.getEffectByToken(token);

				AbstractPowerAction apa = PowersManager.getPowerActionByIDString(effect.getIDString());
				if (apa.getEffectsBase() != null)
					if (apa.getEffectsBase().getValue() > 0){
						//System.out.println(apa.getEffectsBase().getValue());
						value += apa.getEffectsBase().getValue();
					}


				if (apa.getEffectsBase2() != null)
					value += apa.getEffectsBase2().getValue();
			}

		this.magicValue = (int) value;
	}

	public int getMagicValue() {
		
		if (!this.isID) {
            return itemBase.getBaseValue();
        }
		return this.magicValue;
	}


	public static void serializeEffects(MobEquipment mobEquipment,ByteBufferWriter writer) {

		//skip sending effects if not IDed
		if (!mobEquipment.isID) {
			writer.putInt(0);
			return;
		}

		//handle effect count
		int cnt = 0;
		EffectsBase pre = null;
		EffectsBase suf = null;
		if (mobEquipment.prefix != null) {
			pre = PowersManager.getEffectByIDString(mobEquipment.prefix.getIDString());
			if (pre != null)
				cnt++;
		}
		if (mobEquipment.suffix != null) {
			suf = PowersManager.getEffectByIDString(mobEquipment.suffix.getIDString());
			if (suf != null)
				cnt++;
		}

		writer.putInt(cnt);

		//serialize prefix
		if (pre != null)
			serializeEffect(mobEquipment,writer, pre, mobEquipment.pValue);

		//serialize suffix
		if (suf != null)
			serializeEffect(mobEquipment,writer, suf, mobEquipment.sValue);
	}

	public static void serializeEffect(MobEquipment mobEquipment,ByteBufferWriter writer, EffectsBase eb, int rank) {
		String name;
		if (eb.isPrefix()) {
			if (mobEquipment.itemBase == null)
				name = eb.getName();
			else
				name = eb.getName() + ' ' + mobEquipment.itemBase.getName();
		}
		else if (eb.isSuffix()) {
			if (mobEquipment.itemBase == null)
				name = eb.getName();
			else
				name = mobEquipment.itemBase.getName() + ' ' + eb.getName();
		}
		else {
			if (mobEquipment.itemBase == null)
				name = "";
			else
				name = mobEquipment.itemBase.getName();
		}

		writer.putInt(eb.getToken());
		writer.putInt(rank);
		writer.putInt(1);
		writer.put((byte) 1);
		writer.putInt(mobEquipment.getObjectType().ordinal());
		writer.putInt(mobEquipment.getObjectUUID());
		writer.putString(name);
		writer.putFloat(-1000f);
	}

	public void setPrefix(String pIDString, int pValue) {
		AbstractPowerAction apa = PowersManager.getPowerActionByIDString(pIDString);
		if (apa != null) {
			this.prefix = apa;
			this.pValue = pValue;
		} else
			this.prefix = null;

		this.enchanted = this.prefix != null || this.suffix != null;

		setMagicValue();
	}

	public void setSuffix(String sIDString, int sValue) {
		AbstractPowerAction apa = PowersManager.getPowerActionByIDString(sIDString);
		if (apa != null) {
			this.suffix = apa;
			this.sValue = sValue;
		} else
			this.suffix = null;

		this.enchanted = this.prefix != null || this.suffix != null;

		setMagicValue();
	}

	public void setIsID(boolean value) {
		this.isID = value;
	}

	public boolean isID() {
		return this.isID;
	}

	public void transferEnchants(Item item) {
		if (this.prefix != null) {
			String IDString = this.prefix.getIDString();
			item.addPermanentEnchantment(IDString, this.pValue);
		}
		if (this.suffix != null) {
			String IDString = this.suffix.getIDString();
			item.addPermanentEnchantment(IDString, this.sValue);
		}
		if (this.isID)
			item.setIsID(true);
	}



	/*
	 * Database
	 */
	@Override
	public void updateDatabase() {
	}


	public void persistObject() {
		PreparedStatementShared ps = null;
		try {
			ps = prepareStatement("INSERT INTO static_npc_mobequipment (`mobID`, `slot`, `itemID`) VALUES (?, ?, ?)");
			ps.setInt(1, this.parentID, true);
			ps.setInt(2, this.slot);
			ps.setInt(3, this.itemBase.getUUID(), true);
			ps.executeUpdate();
		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
	}

	public static void removeObject(int UUID, int slot) {
		PreparedStatementShared ps = null;
		try {
			ps = prepareStatement("DELETE FROM `static_npc_mobequipment` WHERE `mobID`=? AND slot=?");
			ps.setInt(1, UUID);
			ps.setInt(2, slot);
			ps.executeUpdate();
		} catch (SQLException e) {
			Logger.error( e.toString());
		} finally {
			ps.release();
		}
	}

	public float getDropChance() {
		return dropChance;
	}

	public void setDropChance(float dropChance) {
		this.dropChance = dropChance;
	}
}
