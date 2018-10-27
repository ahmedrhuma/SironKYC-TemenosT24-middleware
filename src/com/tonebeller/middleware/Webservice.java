package com.tonebeller.middleware;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class Webservice {
	public int count = 0,
			responseCode;
	
	private JSONArray methods;

	private JsonNode response;
	
	List<String> session;
	
	BasicCookieStore cookies;
	
	Webservice(){
//		Unirest.setHttpClient();
		Config config = Config.getInstance();
		this.cookies = new BasicCookieStore();
		this.performLogin();
		try {
			JSONArray methods = (JSONArray)config.json.getJSONArray("methods");
			this.count = methods.length();
			this.methods = methods;	
		}
		catch(JSONException e){
			System.out.println("Unable to initiate webservice class");
		}
	}
	
	/**
	 * parse all methods
	 */
	Webservice parseMethods(int[] exec){
		for(int a=0;a<this.count;a++){
			System.out.println("Hola" + a);
			if (exec.length == 0)this.sendRequest(a);
		}
		return this;
	}
	
	Webservice parseMethods(){
		int[] a = new int[0];
		this.parseMethods(a);
		return this;
	}
	
	/**
	 * send request
	 * @param requestNumber
	 * @return
	 */
	Webservice sendRequest(int requestNumber){
		System.out.println("Performing request NO. " + requestNumber);
		try {
			JSONObject req = this.methods.getJSONObject(requestNumber);
			JSONObject result = Database.getInstance().query(req.getString("mapping")).getResult();
			switch(req.getString("type").toLowerCase()){
			case "soap":
//				perform soap request
				break;
			case "post": 
				this.sendPost(req, result);
				if (this.responseCode != 200){
					System.out.println("\nFailed on requesting web service NO. " + requestNumber);
					System.exit(-1);
				}
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * perform login
	 * @return Webservice
	 */
	Webservice performLogin(){
		System.out.println("3- Performing login to T24");
		Config config = Config.getInstance();
		try {
			// get configuration
			JSONObject t24 = config.json.getJSONObject("t24");
			JSONArray login = (JSONArray)t24.getJSONArray("login");
			for(int i=0; i<login.length();i++) this.sendRequest(login.getJSONObject(i));
		} catch (JSONException e) {
			System.out.println("Failed to load T24 login properties" + e.getMessage());
		}
		return this;
	}
	
	Webservice sendRequest(JSONObject request){
		try {
			switch(request.getString("type").toUpperCase().trim()){
				case "POST":
					this.sendPost(request);
				break;
				case "GET": 
					this.sendGet(request);
				break;
			}
		} catch (JSONException e) {
			System.out.println("Unable to detect request type: + ");
		}
		return this;
	}
	
	private Webservice sendPost(JSONObject req, JSONObject body){
		try {
			System.out.println("Performing POST request to: " + req.getString("url"));
			CloseableHttpClient httpclient = HttpClients.custom()
			        .setDefaultCookieStore(this.cookies)
			        .build();
//			HttpClient httpClient = HttpClients.createDefault();
			HttpRequestWithBody request = Unirest.post(req.getString("url"));

		 // add headers
		    JSONObject headers = req.getJSONObject("headers");
			Iterator<?> keys = headers.keys();
			while( keys.hasNext() ) {
			    String key = (String)keys.next();
			    request.header(key, headers.getString(key));
			}
			
			JSONObject contents = req.getJSONObject("contents");
			request = this.encodeURL(contents, request);
			request = this.encodeURL(body, request);
			
			//Send request
			HttpResponse<JsonNode> jsonResponse = request.asJson();
			
			this.responseCode = jsonResponse.getStatus();
			this.response = jsonResponse.getBody();
		} catch (JSONException | UnirestException e) {
			System.out.println("Failed to send POST request, error: " + e.getMessage());
		}
		return this;
	}
	
	private Webservice sendGet(JSONObject req){
		try {
			System.out.println("\nPerforming GET request to: " + req.getString("url"));
			GetRequest request = Unirest.get(req.getString("url"));

		 // add headers
		    JSONObject headers = req.getJSONObject("headers");
			Iterator<?> keys = headers.keys();
			while( keys.hasNext() ) {
			    String key = (String)keys.next();
			    request.header(key, headers.getString(key));
			}
			
			//Send request
			HttpResponse<JsonNode> jsonResponse = request.asJson();
			
			this.responseCode = jsonResponse.getStatus();
			this.response = jsonResponse.getBody();
		} catch (JSONException | UnirestException e) {
			System.out.println("Failed to send POST request, error: " + e.getMessage());
		}
		return this;
	}
	
	private Webservice sendPost(JSONObject req){
		JSONObject a = new JSONObject();
		return this.sendPost(req, a);
	}
	
	JsonNode getResult(){
		return this.response;
	}
	
	private HttpRequestWithBody encodeURL(JSONObject json, HttpRequestWithBody request) {
        Iterator<?> keys = json.keys();
		while( keys.hasNext() ) {
		    String key = (String)keys.next();
		    try {
				request.field(key, json.getString(key));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return request;
    }
}