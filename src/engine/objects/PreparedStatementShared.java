// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com


package engine.objects;

import engine.gameManager.DbManager;
import engine.job.JobScheduler;
import engine.jobs.BasicScheduledJob;
import engine.server.MBServerStatics;
import org.pmw.tinylog.Logger;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe sharing implementation of {@link PreparedStatement}.
 * <p>
 * All of the methods from the PreparedStatement interface simply check to see
 * that the PreparedStatement is active, and call the corresponding method on
 * that PreparedStatement.
 * 
 * @author Burfo
 * @see PreparedStatement
 * 
 **/
public class PreparedStatementShared implements PreparedStatement  {
	private static final ConcurrentHashMap<Integer, LinkedList<PreparedStatement>> statementList = new ConcurrentHashMap<>(MBServerStatics.CHM_INIT_CAP, MBServerStatics.CHM_LOAD, MBServerStatics.CHM_THREAD_LOW);
	private static final ArrayList<PreparedStatementShared> statementListDelegated = new ArrayList<>();
	private static final String ExceptionMessage = "PreparedStatementShared object " + "was accessed after being released.";
	private static boolean debuggingIsOn;
	
	private PreparedStatement ps = null;
	private int sqlHash;
	private String sql;
	private long delegatedTime;
	
	//debugging variables
	private StackTraceElement[] stackTrace;
	private DebugParam[] variables;
	private String filteredSql;

	private class DebugParam {
	    private Object debugObject;
	    private boolean valueAssigned;

	    public DebugParam(Object debugObject){
	        this.debugObject = debugObject;
	        valueAssigned = true;
	    }

	    public Object getDebugObject(){
	        return debugObject;
	    }

	    public boolean isValueAssigned(){
	        return valueAssigned;
	    }
        }
        
        @Override
            public boolean isCloseOnCompletion() {
		return true;
	}
        @Override
            public void closeOnCompletion() {
		Logger.warn( "Prepared Statement Closed");
	}
	
	/**
	 * Generates a new PreparedStatementShared based on the specified sql.
	 * 
	 * @param sql
	 *            Query string to generate the PreparedStatement
	 * @throws SQLException
	 **/
	public PreparedStatementShared(String sql) throws SQLException {
		this.sqlHash = sql.hashCode();
		this.sql = sql;
		this.delegatedTime = System.currentTimeMillis();
		this.ps = getFromPool(sql, sqlHash);
		if (this.ps == null) {
			this.ps = createNew(sql, sqlHash);
		}
		
		
		
		if (debuggingIsOn) {
			//see if there are any '?' in the statement that are not bind variables
	        //and filter them out.
	        boolean isString = false;
	        char[] sqlString = this.sql.toCharArray();
	        for (int i = 0; i < sqlString.length; i++){
	            if (sqlString[i] == '\'')
	                isString = !isString;
	            //substitute the ? with an unprintable character if is in a string
	            if (sqlString[i] == '?' && isString)
	                sqlString[i] = '\u0007';
	        }
	        this.filteredSql = new String(sqlString);
	        
	        //find out how many variables are present in statement.
	        int count = 0;
	        int index = -1;
	        while ((index = filteredSql.indexOf('?',index+1)) != -1){
	            count++;
	        }
	        
	        //create variables array with size equal to count of variables
	        this.variables = new DebugParam[count];
	        
	        this.stackTrace = Thread.currentThread().getStackTrace();
	        
		} else {
			this.stackTrace = null;
			this.variables = null;
			this.filteredSql = null;
		}
		
		synchronized (statementListDelegated) {
			statementListDelegated.add(this);
		}

	}

	private static PreparedStatement getFromPool(String sql, int sqlHash) throws SQLException {
		PreparedStatement ps = null;

		if (statementList.containsKey(sqlHash)) {
			LinkedList<PreparedStatement> list = statementList.get(sqlHash);
			if (list == null) { // Shouldn't happen b/c no keys are ever removed
				throw new AssertionError("list cannot be null.");
			}
			boolean success = false;
			synchronized (list) {
				do {
					ps = list.pollFirst();
					if (ps == null) {
						break;
					}
					if (ps.isClosed()) { // should rarely happen
						Logger.warn("A closed PreparedStatement was removed "
								+ "from AbstractGameObject statementList. " + "SQL: " + sql);
					} else {
						success = true;
					}
				} while (!success);
			}

			if (ps != null) {
				if (MBServerStatics.DB_DEBUGGING_ON_BY_DEFAULT) {
					Logger.info("Found cached PreparedStatement for SQL hash: " + sqlHash
							+ " SQL String: " + sql);
				}
			}
		}
		return ps;
	}

	private static PreparedStatement createNew(String sql, int sqlHash) throws SQLException {
		statementList.putIfAbsent(sqlHash, new LinkedList<>());
		return DbManager.prepareStatement(sql);
	}

	/**
	 * Releases the use of a PreparedStatementShared that was generated by
	 * {@link AbstractGameObject#prepareStatement}, making it available for use
	 * by another query.
	 * <p>
	 * Do not utilize or modify the object after calling this method.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * &#064;code
	 * PreparedStatementShared ps = prepareStatement(...);
	 * ps.executeUpdate();
	 * ps.release();
	 * ps = null;}
	 * </pre>
	 **/
	public void release() {
		if (this.ps == null) {
			return;
		} // nothing to release
		if (statementListDelegated.contains(this)) {
			synchronized (statementListDelegated) {
				statementListDelegated.remove(this);
			}
			try {
				if (this.ps.isClosed()) {
					return;
				}
				this.ps.clearParameters();
				this.variables = null;
			} catch (SQLException ignore) {
			}

			// add back to pool
			LinkedList<PreparedStatement> list = statementList.get(this.sqlHash);
			if (list == null) {
				return;
			}
			synchronized (list) {
				list.add(this.ps);
			}
		}
		// clear values from this object so caller cannot use it after it has
		// been released
		this.ps = null;
		this.sqlHash = 0;
		this.sql = "";
		this.delegatedTime = 0;
		this.stackTrace = null;
	}

	/**
	 * Determines if the object is in a usable state.
	 * 
	 * @return True if the object is in a useable state.
	 **/
	public boolean isUsable() {
		if (ps == null) {
			return false;
		}
		try {
			if (ps.isClosed()) {
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	private String getTraceInfo() {
		if (stackTrace == null) {
			return "<no debug data>";
		}

		if (stackTrace.length > 3) {
			return stackTrace[3].getClassName() + '.' + stackTrace[3].getMethodName();
		} else if (stackTrace.length == 0) {
			return "<unavailable>";
		} else {
			return stackTrace[stackTrace.length - 1].getClassName() + '.' + stackTrace[stackTrace.length - 1].getMethodName();
		}
	}

	public static void submitPreparedStatementsCleaningJob() {
		JobScheduler.getInstance().scheduleJob(new BasicScheduledJob("cleanUnreleasedStatements", PreparedStatementShared.class), 1000 * 60 * 2); // 2
		// minutes
	}

	public static void cleanUnreleasedStatements() {
		long now = System.currentTimeMillis();
		long timeLimit = 120000; // 2 minutes

		synchronized (statementListDelegated) {
			Iterator<PreparedStatementShared> iterator = statementListDelegated.iterator();
			while (iterator.hasNext()) {
				PreparedStatementShared pss = iterator.next();
				if ((pss.delegatedTime + timeLimit) >= now) {
					continue;
				}
				iterator.remove();

				Logger.warn("Forcefully released after being held for > 2 minutes." + " SQL STRING: \""
						+ pss.sql + "\" METHOD: " + pss.getTraceInfo());
			}
		}

		submitPreparedStatementsCleaningJob(); // resubmit
	}

        @Override
	public boolean equals(Object obj) {
		if (ps == null || obj == null) {
			return false;
		}

		if (obj instanceof PreparedStatementShared) {
			return this.ps.equals(((PreparedStatementShared) obj).ps);
		}

		if (obj instanceof PreparedStatement) {
			return this.ps.equals(obj);
		}

		return false;
	}
	
	@Override
	public String toString(){
        if (!debuggingIsOn || variables == null) {
        	return "SQL: " + this.sql + " (enable DB debugging for more data)";
        }
		
        String out;
        
        out = "SQL: [" + this.sql + "] ";
        out += "VARIABLES[count=" + variables.length + "]: ";
        
        for (int i=0; i<variables.length; i++) {
        	out+= "[" + (i+1) + "]: "; 
        	DebugParam dp = variables[i];
        	if (dp == null || !dp.isValueAssigned()) {
        		out += "{MISSING} ";
        		continue;
        	}
        	Object dpObj = dp.getDebugObject();
        	out += dpObj.toString() + ' ';
        }
        return out;
    }

	public static void enableDebugging() {
		debuggingIsOn = true;
		Logger.info( "Database debugging has been enabled.");
	}
	
	public static void disableDebugging() {
		debuggingIsOn = false;
		Logger.info( "Database debugging has been disabled.");
	}
	
	@Override
	public void addBatch() throws SQLException {
		if (this.ps == null) {
			throw new SQLException();
		}
		this.ps.addBatch();

	}

	private void saveObject(int parameterIndex, Object obj) throws SQLException {
		if (!debuggingIsOn || this.variables == null) {
			return;
		}
		
		if (parameterIndex > variables.length){
			throw new SQLException("Parameter index of " + parameterIndex + 
					" exceeds actual parameter count of " + this.variables.length);
		}
		
		this.variables[parameterIndex-1] = new DebugParam(obj);
	}
	
	private void logExceptionAndRethrow(SQLException e) throws SQLException {
		Logger.error("SQL operation failed: (" +
				e.getMessage() + ") " + this.toString(), e);
		throw e;
	}
	
	@Override
	public void clearParameters() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}

		this.ps.clearParameters();
		for (int i=0; i<this.variables.length; i++)	{
			this.variables[i] = null;
		}
		
	}

	@Override
	public boolean execute() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		
		if(debuggingIsOn || MBServerStatics.ENABLE_EXECUTION_TIME_WARNING) {
			long startTime = System.currentTimeMillis();
			boolean rs = false;
			try {
				rs = this.ps.execute();
			} catch (SQLException e) {
				logExceptionAndRethrow(e);
			}
			if((startTime + MBServerStatics.DB_EXECUTION_WARNING_TIME_MS) < System.currentTimeMillis())
				Logger.warn("The following statement took " + (System.currentTimeMillis() - startTime)
						+ " millis to execute: " + this.sql);
			return rs;
		}
		
		return this.ps.execute();
	}

	@Override
	public ResultSet executeQuery() throws SQLException, SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		
		if(debuggingIsOn || MBServerStatics.ENABLE_QUERY_TIME_WARNING) {
			long startTime = System.currentTimeMillis();
			ResultSet rs = null;
			try {
				rs = this.ps.executeQuery();
			} catch (SQLException e) {
				logExceptionAndRethrow(e);
			}
			if((startTime + MBServerStatics.DB_QUERY_WARNING_TIME_MS) < System.currentTimeMillis())
				Logger.warn("The following query took " + (System.currentTimeMillis() - startTime)
						+ " millis to execute: " + this.sql);
			return rs;
		}
		
		return this.ps.executeQuery();
	}

	@Override
	public int executeUpdate() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		
		if(debuggingIsOn || MBServerStatics.ENABLE_UPDATE_TIME_WARNING) {
			long startTime = System.currentTimeMillis();
			int rs = 0;
			try {
				rs = this.ps.executeUpdate();
			} catch (SQLException e) {
				logExceptionAndRethrow(e);
			}
			if((startTime + MBServerStatics.DB_UPDATE_WARNING_TIME_MS) < System.currentTimeMillis())
				Logger.warn("The following update took " + (System.currentTimeMillis() - startTime)
						+ " millis to execute: " + this.sql);
			return rs;
		}
		
		return this.ps.executeUpdate();
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getMetaData();
	}

	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getParameterMetaData();
	}

	@Override
	public void setArray(int parameterIndex, Array x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.saveObject(parameterIndex, x);
		this.ps.setArray(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<stream>"));
		this.ps.setAsciiStream(parameterIndex, x);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setAsciiStream(parameterIndex, x, length);
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setAsciiStream(parameterIndex, x, length);
	}

	@Override
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setBigDecimal(parameterIndex, x);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<stream>"));
		this.ps.setBinaryStream(parameterIndex, x);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setBinaryStream(parameterIndex, x, length);
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setBinaryStream(parameterIndex, x, length);
	}

	@Override
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setBlob(parameterIndex, x);
	}

	@Override
	public void setBlob(int parameterIndex, InputStream x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<blob stream>"));
		this.ps.setBlob(parameterIndex, x);
	}

	@Override
	public void setBlob(int parameterIndex, InputStream x, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"<blob stream length= " + length+ '>'));
		this.ps.setBlob(parameterIndex, x, length);
	}

	@Override
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, new Boolean(x));
		this.ps.setBoolean(parameterIndex, x);
	}

	@Override
	public void setByte(int parameterIndex, byte x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, new Byte(x));
		this.ps.setByte(parameterIndex, x);
	}

	@Override
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":"byte[] length="+x.length));
		this.ps.setBytes(parameterIndex, x);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream>"));
		this.ps.setCharacterStream(parameterIndex, reader);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setCharacterStream(parameterIndex, reader, length);
	}

	@Override
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setClob(parameterIndex, x);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream>"));
		this.ps.setClob(parameterIndex, reader);
	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setClob(parameterIndex, reader, length);
	}

	@Override
	public void setDate(int parameterIndex, Date x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex,x);
		this.ps.setDate(parameterIndex, x);
	}

	@Override
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex,x);
		this.ps.setDate(parameterIndex, x, cal);
	}

	@Override
	public void setDouble(int parameterIndex, double x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex,new Double(x));
		this.ps.setDouble(parameterIndex, x);
	}

	@Override
	public void setFloat(int parameterIndex, float x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex,new Float(x));
		this.ps.setFloat(parameterIndex, x);
	}

    /**
     * Sets the designated parameter to the given Java <code>int</code> value.  
     * The driver converts this
     * to an SQL <code>INTEGER</code> value when it sends it to the database.
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @param setZeroAsNull Converts an int value of 0 to an SQL NULL.
     *        Should be set TRUE on INSERTS and UPDATES for record pointers
     *        (e.g. GuildID) and FALSE for data elements (e.g. player's level).
     * @exception SQLException if parameterIndex does not correspond to a parameter
     * marker in the SQL statement; if a database access error occurs or 
     * this method is called on a closed <code>PreparedStatement</code>
     */
	public void setInt(int parameterIndex, int x, boolean setZeroAsNull) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		if (setZeroAsNull && x == 0) {
			this.ps.setNull(parameterIndex, java.sql.Types.INTEGER);
			saveObject(parameterIndex,"NULL");
			return;
		}
		saveObject(parameterIndex,new Integer(x));
		this.ps.setInt(parameterIndex, x);

	}
	
	@Override
	public void setInt(int parameterIndex, int x) throws SQLException {
		setInt(parameterIndex, x, false);
	}
	

	@Override
	public void setLong(int parameterIndex, long x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex,new Long(x));
		this.ps.setLong(parameterIndex, x);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (value==null?"NULL":"<stream>"));
		this.ps.setNCharacterStream(parameterIndex, value);
	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (value==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setNCharacterStream(parameterIndex, value, length);
	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (value==null?"NULL":"<stream>"));
		this.ps.setNClob(parameterIndex, value);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream>"));
		this.ps.setNClob(parameterIndex, reader);
	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (reader==null?"NULL":"<stream length= " + length+ '>'));
		this.ps.setNClob(parameterIndex, reader, length);
	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, value);
		this.ps.setNString(parameterIndex, value);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, "NULL");
		this.ps.setNull(parameterIndex, sqlType);
	}

	@Override
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, "NULL");
		this.ps.setNull(parameterIndex, sqlType, typeName);
	}

	@Override
	public void setObject(int parameterIndex, Object x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":x.getClass().getName()));
		this.ps.setObject(parameterIndex, x);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":x.getClass().getName()));
		this.ps.setObject(parameterIndex, x, targetSqlType);
	}

	@Override
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, (x==null?"NULL":x.getClass().getName()));
		this.ps.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
	}

	@Override
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setRef(parameterIndex, x);
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setRowId(parameterIndex, x);
	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, xmlObject);
		this.ps.setSQLXML(parameterIndex, xmlObject);
	}

	@Override
	public void setShort(int parameterIndex, short x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, new Short(x));
		this.ps.setShort(parameterIndex, x);
	}

	@Override
	public void setString(int parameterIndex, String x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setString(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setTime(parameterIndex, x);
	}

	@Override
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setTime(parameterIndex, x, cal);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setTimestamp(parameterIndex, x);
	}

	@Override
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setTimestamp(parameterIndex, x, cal);
	}

	@Override
	public void setURL(int parameterIndex, URL x) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		saveObject(parameterIndex, x);
		this.ps.setURL(parameterIndex, x);
	}

	@Override
	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("setUnicodeStream is unsupported");
		/*
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setUnicodeStream(parameterIndex, x, length);
		*/
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.addBatch(sql);
	}

	@Override
	public void cancel() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.cancel();
	}

	@Override
	public void clearBatch() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.clearBatch();
	}

	@Override
	public void clearWarnings() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.clearWarnings();
	}

	/**
	 * Redirected to the {@link #release} method.
	 * 
	 * @deprecated
	 **/
	@Override
	public void close() throws SQLException {
		this.release(); // redirect to release method
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.execute(sql);
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.execute(sql, autoGeneratedKeys);
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.execute(sql, columnIndexes);
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.execute(sql, columnNames);
	}

	@Override
	public int[] executeBatch() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.executeBatch();
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.executeQuery(sql);
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.executeUpdate(sql);
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.executeUpdate(sql, autoGeneratedKeys);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.executeUpdate(sql, columnIndexes);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.executeUpdate(sql, columnNames);
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getConnection();
	}

	@Override
	public int getFetchDirection() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getFetchDirection();
	}

	@Override
	public int getFetchSize() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getFetchSize();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getGeneratedKeys();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getMaxFieldSize();
	}

	@Override
	public int getMaxRows() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getMaxRows();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getMoreResults();
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getMoreResults(current);
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getQueryTimeout();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getResultSet();
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getResultSetConcurrency();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getResultSetHoldability();
	}

	@Override
	public int getResultSetType() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getResultSetType();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getUpdateCount();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.isClosed();
	}

	@Override
	public boolean isPoolable() throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.isPoolable();
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setCursorName(name);
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setEscapeProcessing(enable);
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setFetchDirection(direction);
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setFetchSize(rows);
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setMaxFieldSize(max);
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setMaxRows(max);
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setPoolable(poolable);
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		this.ps.setQueryTimeout(seconds);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.isWrapperFor(iface);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (this.ps == null) {
			throw new SQLException(ExceptionMessage);
		}
		return this.ps.unwrap(iface);
	}

}
