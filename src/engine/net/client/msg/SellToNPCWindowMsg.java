// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;


import engine.exception.SerializationException;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;

import java.util.ArrayList;


/**
 * Sell to NPC window msg
 *
 * @author Eighty
 */
public class SellToNPCWindowMsg extends ClientNetMsg {

//Item Types:
//1: weapon
//2: armor/cloth/shield
//5: scrolls
//8: potions
//10: charters
//13: Jewelry


	private int npcType;
	private int npcID;
	private byte unknownByte01; //so far always 0x00
	private byte unknownByte02; //0: show only specified, 1: show all
	private ArrayList<Integer> itemTypes;
	private ArrayList<Integer> skillTokens;
	private ArrayList<Integer> unknownArray02;
	private int unknown01; //so far always 0
	private int unknown02; //so far always 0 on output
	private int unknown03; //so far always 10000
	private int unknown04; //so far always 0 on output
	private float unknown05; //suspect sell percentage, ex: 0.26 for 26%
	private int unknown06; //suspect gold available on vendor
	private int unknown07; //so far always 0

	/**
	 * This is the general purpose constructor
	 */
	public SellToNPCWindowMsg(int npcType, int npcID, byte unknownByte02,
							  ArrayList<Integer> itemTypes, ArrayList<Integer> skillTokens,
							  ArrayList<Integer> unknownArray02, float unknown05, int unknown06) {
		super(Protocol.SHOPINFO);
		this.npcType = npcType;
		this.npcID = npcID;
		this.unknownByte01 = (byte)0;
		this.unknownByte02 = unknownByte02;
		this.itemTypes = itemTypes;
		this.skillTokens = skillTokens;
		this.unknownArray02 = unknownArray02;
		this.unknown01 = 0;
		this.unknown02 = 0;
		this.unknown03 = 10000;
		this.unknown04 = 0;
		this.unknown05 = unknown05;
		this.unknown06 = unknown06;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public SellToNPCWindowMsg(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.SHOPINFO, origin, reader);
	}

	/**
	 * Deserializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)
			 {

		this.itemTypes = new ArrayList<>();
		this.skillTokens = new ArrayList<>();
		this.unknownArray02 = new ArrayList<>();

		this.npcType = reader.getInt();
		this.npcID = reader.getInt();
		this.unknownByte01 = reader.get();
		this.unknownByte02 = reader.get();
		int cnt = reader.getInt();
		for (int i=0; i<cnt; i++)
			this.itemTypes.add(reader.getInt());
		cnt = reader.getInt();
		for (int i=0; i<cnt; i++)
			this.skillTokens.add(reader.getInt());
		cnt = reader.getInt();
		for (int i=0; i<cnt; i++)
			this.unknownArray02.add(reader.getInt());
		this.unknown01 = reader.getInt();
		this.unknown02 = reader.getInt();
		this.unknown03 = reader.getInt();
		this.unknown04 = reader.getInt();
		this.unknown05 = reader.getFloat();
		this.unknown06 = reader.getInt();
		this.unknown07 = reader.getInt();
	}

	/**
	 * Serializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer)
			throws SerializationException {
		writer.putInt(this.npcType);
		writer.putInt(this.npcID);
		writer.put(this.unknownByte01);
		writer.put(this.unknownByte02);
		writer.putInt(this.itemTypes.size());
		for (Integer i : this.itemTypes)
			writer.putInt(i);
		writer.putInt(this.skillTokens.size());
		for (Integer i : this.skillTokens)
			writer.putInt(i);
		writer.putInt(this.unknownArray02.size());
		for (Integer i : this.unknownArray02)
			writer.putInt(i);
		writer.putInt(this.unknown01);
		writer.putInt(this.unknown02);
		writer.putInt(this.unknown03);
		writer.putInt(this.unknown04);
		writer.putFloat(this.unknown05);
		writer.putInt(this.unknown06);
		writer.putInt(this.unknown07);
	}

	public int getNPCType() {
		return this.npcType;
	}

	public int getNPCID() {
		return this.npcID;
	}

	public byte getUnknownByte01() {
		return this.unknownByte01;
	}

	public byte getUnknownByte02() {
		return this.unknownByte02;
	}

	public ArrayList<Integer> getItemTypes() {
		return this.itemTypes;
	}

	public ArrayList<Integer> getSkillTokens() {
		return this.skillTokens;
	}

	public ArrayList<Integer> getUnknownArray02() {
		return this.unknownArray02;
	}

	public int getUnknown01() {
		return unknown01;
	}

	public int getUnknown02() {
		return unknown02;
	}

	public int getUnknown03() {
		return unknown03;
	}

	public int getUnknown04() {
		return unknown04;
	}

	public float getUnknown05() {
		return unknown05;
	}

	public int getUnknown06() {
		return unknown06;
	}

	public int getUnknown07() {
		return unknown07;
	}

	public void setNPCType(int value) {
		this.npcType = value;
	}

	public void setNPCID(int value) {
		this.npcID = value;
	}

	public void setUnknownByte01(byte value) {
		this.unknownByte01 = value;
	}

	public void setUnknownByte02(byte value) {
		this.unknownByte02 = value;
	}

	public void setItemTypes(ArrayList<Integer> value) {
		this.itemTypes = value;
	}

	public void setskillTokens(ArrayList<Integer> value) {
		this.skillTokens = value;
	}

	public void setUnknownArray02(ArrayList<Integer> value) {
		this.unknownArray02 = value;
	}

	public void setUnknown01(int value) {
		this.unknown01 = value;
	}

	public void setUnknown02(int value) {
		this.unknown02 = value;
	}

	public void setUnknown03(int value) {
		this.unknown03 = value;
	}

	public void setUnknown04(int value) {
		this.unknown04 = value;
	}

	public void setUnknown05(float value) {
		this.unknown05 = value;
	}

	public void setUnknown06(int value) {
		this.unknown06 = value;
	}

	public void setUnknown07(int value) {
		this.unknown07 = value;
	}

	public void addItemType(int value) {
		this.itemTypes.add(value);
	}

	public void addSkillToken(int value) {
		this.skillTokens.add(value);
	}

	public void addUnknownArray02(int value) {
		this.unknownArray02.add(value);
	}

	public void setItemType(ArrayList<Integer> value) {
		this.itemTypes = value;
	}

	public void setSkillTokens(ArrayList<Integer> value) {
		this.skillTokens = value;
	}

	public void setUnknownArray(ArrayList<Integer> value) {
		this.unknownArray02 = value;
	}

	public void setupOutput() {
		this.unknownByte01 = (byte)0;
		this.unknownByte02 = (byte)0;
		this.unknown01 = 0;
		this.unknown02 = 0;
		this.unknown03 = 10000;
		this.unknown04 = 0;
		this.unknown07 = 0;
	}
}
