// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

public class LootRow {

	private int valueOne;
	private int valueTwo;
	private int valueThree;
	private String action;


	/**
	 * Generic Constructor
	 */
	public LootRow(int valueOne, int valueTwo, int valueThree, String action) {
		this.valueOne = valueOne;
		this.valueTwo = valueTwo;
		this.valueThree = valueThree;
		this.action = action;
	
	}

	public int getValueOne() {
		return this.valueOne;
	}

	public int getValueTwo() {
		return this.valueTwo;
	}

	public int getValueThree() {
		return this.valueThree;
	}

	public String getAction() {
		return this.action;
	}

	public void setValueOne(int value) {
		this.valueOne = value;
	}

	public void setValueTwo(int value) {
		this.valueTwo = value;
	}

	public void setValueThree(int value) {
		this.valueThree = value;
	}

	public void setAction(String value) {
		this.action = value;
	}

}
