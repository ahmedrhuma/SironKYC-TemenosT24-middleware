package com.tonebeller.middleware;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Database connection
 * Singleton class
 */
public class Database {
	private static Database instance = null;
	
	private JSONObject result = null;
	
	private Connection connection = null;
	
	/**
	 * private constructor
	 * we don't need it to be called directly
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws JSONException 
	 */
	private Database() throws ClassNotFoundException, JSONException{
		// load driver
		Config config = Config.getInstance();
		JSONObject database = (JSONObject)config.json.get("database");
		Class.forName(database.getString("driver"));
		
		try {
			connection = DriverManager.getConnection(database.getString("connectionString"), database.getString("user"), database.getString("pass"));
			System.out.println("\n*Connected to database!*\n");
		}
		catch(SQLException e){
			System.out.println("\n---- Error while connecting to database ----\n");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Get new instance of the configuration object
	 * this method will only create one instance
	 * @return Object
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws JSONException 
	 */
	public static final Database getInstance() throws ClassNotFoundException, SQLException, JSONException{
		if (instance == null) instance = new Database();
		return instance;
	}
	
	Database query(String sql){
		Statement stmt = null;
		try {
			stmt = this.connection.createStatement();
	        ResultSet rs = stmt.executeQuery(this.parseSQL(sql));
	        System.out.println("Query: " + this.parseSQL(sql));
	        JSONObject values = new JSONObject();
	        while (rs.next()) {
	        	for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
	                values.put(rs.getMetaData().getColumnName(i), rs.getString(i));
	            }
	        }
	        this.result = values;
	    } catch (SQLException e ) {
	    	System.out.println("\n\n== error in database query ==\n\n");
			System.out.println(e.getMessage());
			this.result = null;
	    } catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("\n\n== Error while doing JSON object result from database: == \n" + e.getMessage() + "\n");
		}
		return this;
	}
	
	/**
	 * parse SQL statement and replace magic words
	 * @param sql
	 * @return
	 */
	String parseSQL(String sql){
		Config config = Config.getInstance();
		if (new String(sql).isEmpty() || config.getCustomer() == null) return sql;
		sql = sql.replace("{CLIENT}", config.getClientSchema());
		sql = sql.replace("{FORMID}", config.getCustomer().getFormId() + "");
		sql = sql.replace("{CLIENT_SCHEMA}", config.getCustomer().getClientSchema());
		sql = sql.replace("{GLOBAL_SCHEMA}", config.getCustomer().getGlobalSchema());
		sql = sql.replace("{CUSTOMER}", config.getCustomer().getId());
		sql = sql.replace("{BRANCH_CODE}", config.getCustomer().getBranchCode());
		sql = sql.replace("{KYC_USER}", config.getCustomer().getKycUser());
		sql = sql.replace("{RISK}", config.getCustomer().getRisk() + "");
		return sql;
	}
	
	JSONObject getResult(){
		return this.result;
	}
	
	/**
	 * close database connection
	 * @throws Exception
	 */
	void close() {
		try {
			if (connection != null) connection.close();
		} catch (SQLException se) {}
	}
}
