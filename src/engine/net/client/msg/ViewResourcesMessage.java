// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.net.client.msg;

import engine.Enum;
import engine.gameManager.DbManager;
import engine.net.AbstractConnection;
import engine.net.ByteBufferReader;
import engine.net.ByteBufferWriter;
import engine.net.client.Protocol;
import engine.objects.*;


public class ViewResourcesMessage extends ClientNetMsg {

	//resource hashes
	//0001240F
	//002339C7 (locked)
	//00263669
	//00270DC3
	//002D6DEF
	//047636B3 (locked)
	//047B0CC1
	//04AB3761
	//1AF5DB3A
	//47033237
	//4F8EFB0F
	//5B57C3E4
	//86A0AC24
	//9705591E
	//98378CB4
	//98D78D15
	//A0703E8C (locked)
	//A0DA3807
	//A1723A93
	//A26E59CF
	//D665C60F
	//E3D05AE3
	//ED13904D

	private Guild guild;
	private Building warehouseBuilding;
	private Warehouse warehouseObject;
	private PlayerCharacter player;
	private City city;

	/**
	 * This is the general purpose constructor.
	 */

	public ViewResourcesMessage(PlayerCharacter player) {
		super(Protocol.VIEWRESOURCES);
		this.guild = null;
		this.player = player;
	}

	/**
	 * This constructor is used by NetMsgFactory. It attempts to deserialize the ByteBuffer into a message. If a BufferUnderflow occurs (based on reading past the limit) then this constructor Throws that Exception to the caller.
	 */
	public ViewResourcesMessage(AbstractConnection origin, ByteBufferReader reader)
			 {
		super(Protocol.VIEWRESOURCES, origin, reader);
	}

	public boolean configure() {

		if (this.warehouseBuilding.getParentZone() == null)
			return false;

		this.city = (City) DbManager.getObject(Enum.GameObjectType.City, this.warehouseBuilding.getParentZone().getPlayerCityUUID());

		if (this.city == null)
			return false;

		this.warehouseObject = this.city.getWarehouse();

        return this.warehouseObject != null;
    }

	/**
	 * Serializes the subclass specific items to the supplied NetMsgWriter.
	 */
	@Override
	protected void _serialize(ByteBufferWriter writer) {

		writer.putInt(warehouseObject.getResources().size());

		for (ItemBase ib : (warehouseObject.getResources().keySet())){

			writer.putInt(ib.getHashID());
			writer.putInt((warehouseObject.getResources().get(ib)));


			if (warehouseObject.isResourceLocked(ib) == true)
				writer.put((byte)1);
			else
				writer.put((byte)0);
		}

		writer.putInt(warehouseObject.getResources().size());

		for (ItemBase ib : warehouseObject.getResources().keySet()){
			writer.putInt(ib.getHashID());
			writer.putInt(0); //available?
			writer.putInt(Warehouse.getMaxResources().get(ib.getUUID())); //max?
		}
		GuildTag._serializeForDisplay(guild.getGuildTag(),writer);

		// Serialize what tags?  Errant?

		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(16);
		writer.putInt(0);
		writer.putInt(0);

		if (GuildStatusController.isTaxCollector(player.getGuildStatus())){
			writer.putInt(1);
			writer.putString("Deposit");
			writer.putInt(-1760114543);
			writer.putInt(1);
			writer.put((byte)0);

		}else

			if (this.player.getGuild().equals(warehouseBuilding.getGuild()) &&  (GuildStatusController.isInnerCouncil(this.player.getGuildStatus()))){
				writer.putInt(4);
				writer.putString("Lock");
				writer.putInt(2393548);
				writer.putInt(1); //locked? on/off
				writer.put((byte)0);
				writer.putString("Deposit");
				writer.putInt(-1760114543);

				writer.putInt(1);
				writer.put((byte)0);
				writer.putString("Manage Mines");
				writer.putInt(-820683698);
				writer.putInt(1);
				writer.put((byte)0);
				writer.putString("Withdraw");
				writer.putInt(-530228289);
				writer.putInt(1);
				writer.put((byte)0);
			}else{
				writer.putInt(2);
				writer.putString("Lock");
				writer.putInt(2393548);
				writer.putInt(0); //locked? on/off
				writer.put((byte)0);
				writer.putString("Deposit");
				writer.putInt(-1760114543);
				writer.putInt(1);
				writer.put((byte)0);
			}

	}

	/**
	 * Deserializes the subclass specific items from the supplied NetMsgReader.
	 */
	@Override
	protected void _deserialize(ByteBufferReader reader)  {
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		reader.getInt();
		//		this.locX = reader.getFloat();
		//		this.locY = reader.getFloat();
		//		this.locZ = reader.getFloat();
		//		this.name = reader.getString();
		//		this.unknown01 = reader.getInt();
	}

	public void setGuild(Guild guild) {
		this.guild = guild;
	}

	public void setWarehouseBuilding(Building warehouseBuilding) {
		this.warehouseBuilding = warehouseBuilding;
	}
}
