// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com



package engine.objects;

import engine.Enum.GameObjectType;
import engine.Enum.TransactionType;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;



public class Transaction  implements Comparable<Transaction> {

	private final int warehouseUUID;
	private final int targetUUID;
	private final Resource resource;
	private final DateTime date;
	private final int amount;
	private GameObjectType targetType;
	private final TransactionType transactionType;
	
	
	 
	public Transaction(ResultSet rs) throws SQLException {
		this.warehouseUUID = rs.getInt("warehouseUID");
		this.targetUUID = rs.getInt("targetUID");
		this.targetType = GameObjectType.valueOf(rs.getString("targetType"));
		this.transactionType = TransactionType.valueOf(rs.getString("type").toUpperCase());
		this.resource = Resource.valueOf(rs.getString("resource").toUpperCase());
		this.amount = rs.getInt("amount");
                
		Date sqlDateTime = rs.getTimestamp("date");
                
		if (sqlDateTime != null)
			this.date = new DateTime(sqlDateTime);
		else
			this.date = DateTime.now();

	}

	
	public Transaction(int warehouseUUID,GameObjectType targetType, int targetUUID, TransactionType transactionType, Resource resource, int amount,
			DateTime date) {
		this.warehouseUUID = warehouseUUID;
		this.targetUUID = targetUUID;
		this.resource = resource;
		this.date = date;
		this.amount = amount;
		this.targetType = targetType;
		this.transactionType = transactionType;
	}


	public int getWarehouseUUID() {
		return warehouseUUID;
	}


	public int getTargetUUID() {
		return targetUUID;
	}


	public Resource getResource() {
		return resource;
	}


	public DateTime getDate() {
		return date;
	}


	public int getAmount() {
		return amount;
	}


	public TransactionType getTransactionType() {
		return transactionType;
	}


	@Override
	public int compareTo(Transaction arg0) {
		// TODO Auto-generated method stub
		return 0;
	}


	public GameObjectType getTargetType() {
		return targetType;
	}


	public void setTargetType(GameObjectType targetType) {
		this.targetType = targetType;
	}
	
}
