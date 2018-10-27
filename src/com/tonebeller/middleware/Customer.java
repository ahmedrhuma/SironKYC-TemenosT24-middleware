package com.tonebeller.middleware;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Customer class
 * all props required
 */
public class Customer {
	private String client,
			Id,
			clientSchema,
			globalSchema,
			risk;

	private int formId;
	public String branchCode = "", KycUser = "";
	public boolean isNew = false;

	Customer(Config config) throws Exception{
		if (new String(config.getClient()).isEmpty()){
			throw new Exception("Client is not defined!");
		}
		this.client = config.getClient();
		JSONObject schemas = (JSONObject)config.json.get("schemas");
		this.clientSchema = schemas.getString(this.client);
		this.globalSchema = schemas.getString("global");
		this.formId = config.json.getInt("formId");
		try {
			this.loadCustomerId().load();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to laod customer Id" + e.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Load formId, CustomerId, BranchCode in single query
	 * Load everything in single IO
	 * @return
	 * @throws Exception 
	 */
	Customer load() throws Exception{
		String sql = "SELECT"
				+ " P.ERFASSER AS KycUser, SUBSTRING(K.KYC_SP_32, 1, 4) AS BranchCode, RISIKO AS Risk"
				+ " FROM "+ this.clientSchema +".KYCCUST AS K, "+ this.clientSchema +".PRESULT AS P WHERE"
				+ " K.INSTITUTSNR='"+ this.client +"' AND K.KUNDNR='"+ this.Id +"' AND K.HISTBIS='9999'"
				+ " AND P.INSTITUTSNR='"+ this.client +"' AND P.KUNDNR = '"+ this.Id +"' AND P.HISTBIS = '9999'";
		
		System.out.println("2- Load data from KYC\n");
		JSONObject result = Database.getInstance().query(sql).getResult();
		if (result == null){
			throw new Exception("Could not load customer");
		}
		System.out.println("* Information Loaded *\n");
		this.KycUser = result.getString("KycUser");
		this.branchCode = (String) result.get("BranchCode");
		this.risk = (String) result.get("Risk");
		return this;
	}

	/**
	 * Load customer data
	 * @return Customer
	 */
	Customer loadData(){
		
		return this;
	}
	/**
	 * get customer risk
	 * @return integer risk number
	 */
	String getRisk(){
		return this.risk;
	}
	
	/**
	 * get form Id
	 * @return integer form Id
	 */
	int getFormId(){
		return this.formId;
	}
	
	/**
	 * get branch code
	 * @return string branch code
	 */
	String getBranchCode(){
		return this.branchCode;
	}
	
	/**
	 * get KYC user
	 * @return string KYC User
	 */
	String getKycUser(){
		return this.KycUser;
	}
	
	/**
	 * get client Schema
	 * @return string client Schema
	 */
	String getClientSchema(){
		return this.clientSchema;
	}
	
	/**
	 * get global Schema
	 * @return string global Schema
	 */
	String getGlobalSchema(){
		return this.globalSchema;
	}
	
	/**
	 * get customer Id
	 * @return string customerId
	 */
	String getId(){
		return this.Id;
	}
	
	/**
	 * Get customer Id from the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Customer loadCustomerId() throws FileNotFoundException{
		String customerId = null;
		BufferedReader in = null;
		System.out.println("1- Load customer Id: ");
		try {
			in = new BufferedReader(new FileReader(Config.getInstance().getWorkingDirectory() + '/' + Config.getInstance().json.getString("customerIdFile")));
			String line;
			while ((line = in.readLine()) != null) {
				customerId = line.substring(8, 24);
				customerId = customerId.trim();
				this.Id = customerId;
				System.out.println("* Customer Id " + this.Id);
				this.isNew = this.Id.startsWith("KYC");
			}
		}
		catch(IOException e){
			System.out.println("\n== Error while loading customer Id ==\n");
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (JSONException e) {
			System.out.println("\n== Error while loading customer Id JSON error ==\n");
			System.out.println(e.getMessage());
		}
		finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return this;
	}
}
