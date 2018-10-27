package com.tonebeller.middleware;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Singleton class
 * System configuration object
 * sironkyc.properties needed parameters in (tbellerhome-web)
 * middleware.methods.define Define supported methods (use # as delimiter) e.g. method1#method2#method3#method4
 * middleware.schema.{CLIENT} client schema
 * middleware.shcema.gloabl Global schema
 * middleware.client.datadir working directory (online directory) 
 * middleware.methods.{METHOD_NAME} data mapping SQL statement
 * Strings that are replaced in SQL statements. (case-sensitive)
 * {CLIENT} replaced with client Id
 * {FORMID} replaced with form Id
 * {CLIENT_SCHEMA} replaced with client schema
 * {GLOBAL_SCHEMA} replaced with global schema
 * {CUSTOMER} replaced with customer Id
 * {BRANCH_CODE} replaced with branch code
 * {KYC_USER} replaced with KYC user
 * {RISK} replaced with risk value
 */
class Config {
	private static Config instance = null;
	public JSONObject json = new JSONObject();
	
	private Customer customer = null;
		
	// class props
	private String
		CLIENT,
		PATH,
		KYC_ROOT,
		SUB_FOLDER,
		WORK_DIRECTORY;
	private int RISK_HALT = 0;
	
	/**
	 * private constructor
	 * we don't need it to be called directly
	 * @throws JSONException 
	 */
	private Config(){
		// if property file is not loaded then load it
		if (this.KYC_ROOT != null){
			try {
				this.loadKycProperties();
			} catch (InvalidPathException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			
		}
	}
	
	// local setter
	Config setClient(String val){
		this.CLIENT = val;
		return this;
	}
	
	Config setPath(String path){
		this.PATH = path;
		return this;
	}
	
	// local setter
	Config setKYCRoot(String val){
		this.KYC_ROOT = val;
		return this;
	}
	
	// local setter
	Config setHalt(int val){
		this.RISK_HALT = val;
		return this;
	}
	
	// local getter
	String getClient(){
		return this.CLIENT;
	}
	
	// local getter
	String getWorkingDirectory(){
		return this.WORK_DIRECTORY;
	}
	
	// local getter
	String getKYCRoot(){
		return this.KYC_ROOT;
	}
	
	// local getter
	int getHalt(){
		return this.RISK_HALT;
	}
	
	/**
	 * Get new instance of the configuration object
	 * this method will only create one instance
	 * @return Object
	 */
	public static final Config getInstance(){
		if (instance == null) instance = new Config();
		return instance;
	}
	
	/**
	 * Load KYC properties
	 * @return Properties
	 * @throws IOException
	 * @throws JSONException 
	 */
	private void loadKycProperties() throws InvalidPathException, IOException {
		if (this.KYC_ROOT == null || this.KYC_ROOT.isEmpty()){
			throw new InvalidPathException("Unable to load KYC properties file, KYC_ROOT is not defined or not exists", "File not loaded");
		}
		try {
			File f = new File(this.PATH);
			if (f.exists()){
				BufferedReader br = new BufferedReader(new FileReader(this.PATH));
				try {
				    StringBuilder sb = new StringBuilder();
				    String line = br.readLine();
				    while (line != null) {
				        sb.append(line);
				        sb.append(System.lineSeparator());
				        line = br.readLine();
				    }
				    String everything = sb.toString();
				    this.json = new JSONObject(everything);
				    this.setHalt(this.json.getInt("riskHalt"));
				}
				finally {
				    br.close();
				}
			}
			else {
				System.out.println("\nUnable to find or load config file.\n");
				System.exit(-1);
			}
		}
		catch(JSONException e){
			System.out.println("\nError while loading config file.\n");
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * load properties
	 * @return Config
	 */
	Config load(){
		if (this.KYC_ROOT != null){
			try {
				this.loadKycProperties();
				this.WORK_DIRECTORY = this.KYC_ROOT + "/client" + '/' + this.CLIENT + "/online/" + this.SUB_FOLDER;
				System.out.println("*Config file loaded");
			} catch (InvalidPathException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			
		}
		return this;
	}
	
	/**
	 * set customer to be accessed in later classes
	 * @param c
	 * @return
	 */
	Config setCustomer(Customer c){
		this.customer = c;
		return this;
	}
	
	Customer getCustomer(){
		return this.customer;
	}
	
	/**
	 * Set sub folder in online folder
	 * @param v String sub folder name
	 * @return Customer
	 */
	Config setSubfolder(String v){
		this.SUB_FOLDER = v;
		return this;
	}
	
	
}
