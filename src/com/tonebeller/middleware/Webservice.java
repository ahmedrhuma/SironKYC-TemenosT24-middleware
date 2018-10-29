package com.tonebeller.middleware;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Webservice {

  private String cookies;
  
  Config config;
  
  int responceCode;
  
  String responseBody;

private String location;

   Webservice() throws Exception {
	
	this.config = Config.getInstance();
	
	this.cookies = null;
	
	this.doLogin().doMethods().doKYC();
  }
  
  private Webservice doLogin(){
//		login
	  System.out.println("\n===DO LOGIN===\n");
	JSONObject t24;
	try {
		this.cookies = null;
		t24 = this.config.json.getJSONObject("t24");
		JSONArray login = t24.getJSONArray("login");
		
		String page, postParams = new String();
		
		for(int i=0; i<login.length();i++){
			JSONObject params;
			try {
				params = login.getJSONObject(i).getJSONObject("contents");
			}
			catch(Exception e){
				params = new JSONObject();
			}
			switch(login.getJSONObject(i).getString("type")){
			case "GETFORM": 
				page = this.GetPageContent(login.getJSONObject(i).getString("url"));
				postParams = this.getFormParams(page, params);
				break;
			case "POST":
				this.sendPost(login.getJSONObject(i), postParams);
			}
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return this;
  }
  
  private Webservice doKYC(){
	String kycWsPass;
	try {
		this.cookies = null;
		System.out.println("\n\n ==== KYC RENAME === \n\n");
		System.out.println(this.responseBody);
		Document document = Jsoup.parse(this.responseBody);
		Elements td = document.getElementsByClass("message");
		System.out.println("\n\n === " + td.text());
		Pattern p = Pattern.compile("(Txn Complete: )([0-9]+)([0-9A-Za-z :,]+)");
		Matcher m = p.matcher(td.text());
		if (m.find()) {
	        String kycWsUser = this.config.json.getJSONObject("kyc").getString("username");
			kycWsPass = this.config.json.getJSONObject("kyc").getString("password");
			this.config.setNewId(m.group(2));
			String request = new String("<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\"><Body><kycRenameCustId xmlns=\"http://server.ws.sironkyc.tonbeller.com\"><li><pass xmlns=\"http://data.ws.sironkyc.tonbeller.com/xsd\">" + kycWsPass + "</pass><userName xmlns=\"http://data.ws.sironkyc.tonbeller.com/xsd\">" + kycWsUser + "</userName></li><oldCustId>" + this.config.getCustomer().getId() + "</oldCustId><newCustId>" + this.config.getNewId() + "</newCustId></kycRenameCustId></Body></Envelope>");
			System.out.println("\n\n" + request + "\n\n");
			JSONObject req = new JSONObject();
			System.out.println("\n===\n" + req);
			JSONObject headers = new JSONObject();
			headers.put("Content-Type", "text/xml; charset=\"UTF-8\"");
			headers.put("SOAPAction", "urn:kycRenameCustId");
			headers.put("Host", this.config.json.getJSONObject("kyc").getString("host"));
			headers.put("Content-Length", request.length() + "");
			req.put("headers", headers);
			req.put("contents", new JSONObject());
			req.put("url", this.config.json.getJSONObject("kyc").getString("url"));
			this.sendPost(req, request);
	    }
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return this;
  }
  
  private Webservice doMethods(){
	  System.out.println("\n===STARTING METHODS===\n");
	try {
		JSONArray methods = this.config.json.getJSONObject("t24").getJSONArray("methods");
		for(int a=0;a<methods.length();a++){
			System.out.println("Method NO. " + a + 1);
			JSONObject m = methods.getJSONObject(a);
			this.sendRequest(m);
	  }
	} catch (Exception e) {
		System.out.println("Error Performing method" + e.getMessage());
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return this;
  }
  
  /**
	 * send request
	 * @param requestNumber
	 * @return
	 */
	Webservice sendRequest(JSONObject req){
		try {
			JSONObject result = Database.getInstance().query(req.getString("mapping")).getResult();
			JSONObject allResult;
			try {
				allResult = this.merge(result, req.getJSONObject("contents"));
			}
			catch(Exception e){
				allResult = result;
			}
			System.out.println("ALl result: " + allResult);
			String resultText = this.parseObject(allResult);
			this.sendPost(req, resultText);
//			this.sendPost(req, "toggle=&command=globusCommand&requestType=OFS.APPLICATION&ofsOperation=PROCESS&ofsFunction=I&ofsMessage=&GTSControl=&routineName=&routineArgs=&cfNavOperation=&unlock=&activeTab=tab1&SaveChangesText=Changes+Not+Saved&contextenqdisplay=NO&RecordDelText=Are+you+sure+you+want+to+delete+the+transaction%3F&expansionHistory=&version=%2CINPUT&previousVersion=&application=CUSTOMER&existRecPaste=&decrypt=&addGroupTab=&deleteGroupTab=&expandableTab=&tabEnri=&product=ST&name=&operation=&windowName=INPUTTER_CUSTOMER_INPUT_I_F3_1540738959644&toolbarTarget=&overridesAccepted=&overridesApproved=&overrideUnApproved=&focus=fieldName%3AINDUSTRY&ContractStatus=CHG&windowUnqRef=&editVersion=&confirmVersion=&previewVersion=&enqname=&enqaction=&dropfield=&previousEnqs=&previousEnqTitles=&clientStyleSheet=&windowSizes=0%3A0%3A800%3A500&changedFields=fieldName%3ATITLE+fieldName%3AGIVEN.NAMES+fieldName%3AFAMILY.NAME+fieldName%3AGENDER&newCommands=&screenMode=I&lockArgs=&RecordRead=1&PastedDeal=&allowResize=YES&companyId=GB0010001&company=Model+Bank+R16&user=INPUTTER&userRole=&transSign=&skin=default&today=22-APR-2016&release=R16&compScreen=COMPOSITE.SCREEN_INPUTTER_405922892402&reqTabid=&compTargets=menu_menu405922892401%7C&attribframes=&EnqParentWindow=&timing=1935-79-0-1856-63&pwprocessid=&language=GB&languages=GB%2CFR%2CDE%2CES&savechanges=YES&staticId=&lockDateTime=&popupDropDown=true&allowcalendar=&allowdropdowns=&allowcontext=&nextStage=&maximize=&showStatusInfo=true&languageUndefined=Language+Code+Not+Defined&expandMultiString=Expand+Multi+Value&deleteMultiString=Delete+Value&expandSubString=Expand+Sub+Value&clientExpansion=true&showReleaseInfo=NO&singleLoadingIcon=no&WS_parentWindow=&WS_parent=&WS_parentFormId=&WS_dropfield=&WS_doResize=no&WS_initState=CUSTOMER%2CINPUT+I+F3&WS_PauseTime=&WS_multiPane=false&WS_showTabs=false&WS_replaceAll=&WS_parentComposite=&WS_delMsgDisplayed=&enableTabIndex=NO&tabIndexForInfoIcon=NO&tabIndexForDropFields=NO&tabIndexForExpansionIcons=NO&transactionId=190188&fieldName%3ATITLE=MR&fieldName%3AGIVEN.NAMES=GIVEN+NAME&fieldName%3AFAMILY.NAME=FAMLY+NAME&fieldName%3ANAME.1%3A1=FULL+NAME&fieldName%3ANAME.2%3A1=&fieldName%3ASHORT.NAME%3A1=MINNAME+NAME&fieldName%3AMNEMONIC=KYC1533922409193&fieldName%3AGENDER=MALE&radio%3AmainTab%3AGENDER=MALE&fieldName%3AMARITAL.STATUS=&fieldName%3AACCOUNT.OFFICER=&fieldName%3AOTHER.OFFICER%3A1=&fieldName%3ASECTOR=1001&fieldName%3AINDUSTRY=&fieldName%3ATARGET=&fieldName%3ACUSTOMER.STATUS=&fieldName%3ANATIONALITY=&fieldName%3ARESIDENCE=&fieldName%3ACUSTOMER.TYPE=&fieldName%3ACUSTOMER.RATING%3A1=&fieldName%3ADATE.OF.BIRTH=&fieldName%3ALANGUAGE=1&fieldName%3ASTREET%3A1=&fieldName%3AADDRESS%3A1%3A1=&fieldName%3ATOWN.COUNTRY%3A1=&fieldName%3APOST.CODE%3A1=&fieldName%3ACOUNTRY%3A1=&fieldName%3APHONE.1%3A1=&fieldName%3ASMS.1%3A1=&fieldName%3AEMAIL.1%3A1=&fieldName%3AOFF.PHONE%3A1=&fieldName%3AFAX.1%3A1=&fieldName%3ASECURE.MESSAGE=&fieldName%3ALEGAL.ID%3A1=&fieldName%3ALEGAL.DOC.NAME%3A1=&fieldName%3ALEGAL.HOLDER.NAME%3A1=&fieldName%3ALEGAL.ISS.AUTH%3A1=&fieldName%3ALEGAL.ISS.DATE%3A1=&fieldName%3ALEGAL.EXP.DATE%3A1=&fieldName%3ARELATION.CODE%3A1=&fieldName%3AREL.CUSTOMER%3A1=&fieldName%3AREVERS.REL.CODE%3A1=&fieldName%3AREL.DELIV.OPT%3A1%3A1=&fieldName%3AROLE%3A1%3A1=&fieldName%3AROLE.MORE.INFO%3A1%3A1=&fieldName%3AROLE.NOTES%3A1%3A1=&fieldName%3ACUSTOMER.LIABILITY=&fieldName%3APREVIOUS.NAME%3A1=&fieldName%3ACHANGE.DATE%3A1=&fieldName%3ACHANGE.REASON%3A1=&fieldName%3ACUSTOMER.SINCE=&fieldName%3ANO.OF.DEPENDENTS=&fieldName%3AOTHER.NATIONALITY%3A1=&fieldName%3ASPOKEN.LANGUAGE%3A1=&fieldName%3AINTERESTS%3A1=&fieldName%3AFURTHER.DETAILS=&fieldName%3APASTIMES=&fieldName%3AEMPLOYMENT.STATUS%3A1=&fieldName%3AOCCUPATION%3A1=&fieldName%3AJOB.TITLE%3A1=&fieldName%3AEMPLOYERS.NAME%3A1=&fieldName%3AEMPLOYERS.ADD%3A1%3A1=&fieldName%3AEMPLOYERS.BUSS%3A1=&fieldName%3AEMPLOYMENT.START%3A1=&fieldName%3ACUSTOMER.CURRENCY%3A1=&fieldName%3ASALARY%3A1=&fieldName%3AANNUAL.BONUS%3A1=&fieldName%3ASALARY.DATE.FREQ%3A1=&fieldName%3ANET.MONTHLY.IN=&fieldName%3ANET.MONTHLY.OUT=&fieldName%3ARESIDENCE.STATUS%3A1=&fieldName%3ARESIDENCE.TYPE%3A1=&fieldName%3ARESIDENCE.SINCE%3A1=&fieldName%3ARESIDENCE.VALUE%3A1=&fieldName%3AMORTGAGE.AMT%3A1=&fieldName%3ACOMM.TYPE%3A1=&fieldName%3APREF.CHANNEL%3A1=&fieldName%3ACONFID.TXT=&radio%3Atab7%3ACONFID.TXT=&fieldName%3AINTERNET.BANKING.SERVICE=&radio%3Atab7%3AINTERNET.BANKING.SERVICE=&fieldName%3AMOBILE.BANKING.SERVICE=&radio%3Atab7%3AMOBILE.BANKING.SERVICE=&fieldName%3ACONTACT.DATE=&fieldName%3AINTRODUCER=&fieldName%3AKYC.RELATIONSHIP=&fieldName%3AKYC.COMPLETE=&radio%3Atab8%3AKYC.COMPLETE=&fieldName%3ALAST.KYC.REVIEW.DATE=&fieldName%3AAUTO.NEXT.KYC.REVIEW.DATE=&fieldName%3AMANUAL.NEXT.KYC.REVIEW.DATE=&fieldName%3ALAST.SUIT.REVIEW.DATE=&fieldName%3AAUTO.NEXT.SUIT.REVIEW.DATE=&fieldName%3AMANUAL.NEXT.SUIT.REVIEW.DATE=&fieldName%3ARISK.ASSET.TYPE%3A1=&fieldName%3ARISK.LEVEL%3A1=&fieldName%3ARISK.TOLERANCE%3A1=&fieldName%3ARISK.FROM.DATE%3A1=&fieldName%3AAML.CHECK=&radio%3Atab8%3AAML.CHECK=&fieldName%3AAML.RESULT=&fieldName%3ALAST.AML.RESULT.DATE=&fieldName%3ACALC.RISK.CLASS=&fieldName%3AMANUAL.RISK.CLASS=&fieldName%3AOVERRIDE.REASON=&fieldName%3ACOMPANY.BOOK=&fieldName%3AISSUE.CHEQUES=&fieldName%3AALLOW.BULK.PROCESS=&fieldName%3AVIS.TYPE%3A1=&fieldName%3AVIS.COMMENT%3A1=&fieldName%3AVIS.INTERNAL.REVIEW%3A1=&fieldName%3AFORMER.VIS.TYPE%3A1=&fieldName%3ATAX.ID%3A1=&fieldName%3ANO.UPDATE.CRM=&radio%3Atab9%3ANO.UPDATE.CRM=&fieldName%3ACR.PROFILE.TYPE%3A1=&fieldName%3ACR.PROFILE%3A1=&fieldName%3ACR.USER.PROFILE.TYPE%3A1=&fieldName%3ACR.CALC.PROFILE%3A1=&fieldName%3ACR.USER.PROFILE%3A1=&fieldName%3ACR.CALC.RESET.DATE%3A1=&fieldName%3AREPORT.TEMPLATE=&radio%3Atab10%3AREPORT.TEMPLATE=&fieldName%3AHOLDINGS.PIVOT%3A1=&fieldName%3AOVERRIDE%3A1=&fieldName%3ARECORD.STATUS=&fieldName%3ACURR.NO=&fieldName%3AINPUTTER%3A1=&fieldName%3AAUTHORISER=&fieldName%3ADATE.TIME%3A1=&fieldName%3ACO.CODE=&fieldName%3ADEPT.CODE=&fieldName%3AAUDITOR.CODE=&fieldName%3AAUDIT.DATE.TIME=");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this;
	}
	
	private void sendGet(JSONObject req, String postParams) throws Exception {
		System.out.println("\n\n ====== GET Request === \n\n");
		URL obj = new URL(req.getString("url"));
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("GET");
		conn.setInstanceFollowRedirects(false);  
		JSONObject headers = req.getJSONObject("headers");
		Iterator<?> keys = headers.keys();
		while( keys.hasNext() ) {
		    String key = (String)keys.next();
		    conn.setRequestProperty(key, headers.getString(key));
		}
		System.out.println("Cookie: " + this.cookies);
		if (this.cookies != null) conn.addRequestProperty("Cookie", this.getCookies());
		conn.setRequestProperty("Content-Length", postParams != null ? Integer.toString(postParams.length()) : "0");

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		if (postParams != null){
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();			
		}
		

		int responseCode = conn.getResponseCode();
		this.setCookies(conn.getHeaderFields().get("Set-Cookie"));
		System.out.println("\nSending 'POST' request to URL : " + req.getString("url"));
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);
		System.out.println("REsponse Cookies: + " + conn.getHeaderFields());

		BufferedReader in = 
	             new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		System.out.println("\n===Cookies==\n" + this.cookies);
		 
		 this.responceCode = responseCode;
		 this.responseBody = response.toString();
		 this.setLocation(conn.getHeaderFields().get("Location"));
	  }
  
  private void sendPost(JSONObject req, String postParams) throws Exception {
  System.out.println("\n\n ====== POST Request === \n\n");
	URL obj = new URL(req.getString("url"));
	HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

	// Acts like a browser
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setInstanceFollowRedirects(false);  
	JSONObject headers = req.getJSONObject("headers");
	Iterator<?> keys = headers.keys();
	while( keys.hasNext() ) {
	    String key = (String)keys.next();
	    conn.setRequestProperty(key, headers.getString(key));
	}
	System.out.println("\n===Cookie: " + this.cookies);
	if (this.cookies != null) conn.addRequestProperty("Cookie", this.getCookies());
	conn.setRequestProperty("Content-Length", postParams != null ? Integer.toString(postParams.length()) : "0");
	
	Map<String, List<String>> hdrs = conn.getRequestProperties();
	
	System.out.println(hdrs);

	conn.setDoOutput(true);
	conn.setDoInput(true);

	// Send post request
	DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	wr.writeBytes(postParams);
	wr.flush();
	wr.close();
	

	int responseCode = conn.getResponseCode();
	this.setCookies(conn.getHeaderFields().get("Set-Cookie"));
	this.setLocation(conn.getHeaderFields().get("Location"));
	System.out.println("\nSending 'POST' request to URL : " + req.getString("url"));
	System.out.println("Post parameters : " + postParams);
	System.out.println("Response Code : " + responseCode);
	System.out.println("REsponse Cookies: + " + conn.getHeaderFields());

	BufferedReader in = 
             new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();
	 
	 this.responceCode = responseCode;
	 this.responseBody = response.toString();
  }
  
  private Webservice setLocation(List<String> list){
	  System.out.println("\n===SET LOCATION ==\n" + list);
	  if (list != null){
		  System.out.println("Forward to Location" + this.location);		  
		  this.location = list.get(0);
	  }
	  return this;
  }
  
  private String GetPageContent(JSONObject req) throws Exception {
	  System.out.println("\n\n ====== GET PAGE CONTENT GET Request === \n\n");
		URL obj = new URL(req.getString("url"));
		HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		JSONObject headers = req.getJSONObject("headers");
		Iterator<?> keys = headers.keys();
		while( keys.hasNext() ) {
		    String key = (String)keys.next();
		    conn.setRequestProperty(key, headers.getString(key));
		}
		if (this.cookies != null) conn.addRequestProperty("Cookie", this.getCookies());
		System.out.println("Cookie: " + this.cookies);
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + req.getString("url"));
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = 
	            new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		this.setCookies(conn.getHeaderFields().get("Set-Cookie"));

		return response.toString();

	  }

  private String GetPageContent(String url) throws Exception {
	  System.out.println("\n\n ====== GET PAGE CONTENT GET Request === \n\n");
	URL obj = new URL(url);
	HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

	// default is GET
	conn.setRequestMethod("GET");

	conn.setUseCaches(false);

	// act like a browser
	conn.setRequestProperty("Accept",
		"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	if (this.cookies != null) conn.addRequestProperty("Cookie", this.getCookies());
	int responseCode = conn.getResponseCode();
	System.out.println("\nSending 'GET' request to URL : " + url);
	System.out.println("Response Code : " + responseCode);

	BufferedReader in = 
            new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();

	// Get the response cookies
	
	this.setCookies(conn.getHeaderFields().get("Set-Cookie"));

	return response.toString();

  }

  public String getFormParams(String html, JSONObject contents)
		throws UnsupportedEncodingException, JSONException {

	System.out.println("Extracting form's data...");

	Document doc = Jsoup.parse(html);

	// Google form id
	Elements formContainer = doc.getElementsByTag("form");
	Element loginform = formContainer.first();
	Elements inputElements = loginform.getElementsByTag("input");
	List<String> paramList = new ArrayList<String>();
	for (Element inputElement : inputElements) {
		String key = inputElement.attr("name");
		String value = inputElement.attr("value");
		if (key.isEmpty()) continue;

		if (contents != null && contents.length() > 0 && this.isExist(key, contents)) value = contents.getString(key);
		paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
	}

	// build parameters list
	StringBuilder result = new StringBuilder();
	for (String param : paramList) {
		if (result.length() == 0) {
			result.append(param);
		} else {
			result.append("&" + param);
		}
	}
	return result.toString();
  }

  public String getCookies() {
	return this.cookies;
  }
  
  private boolean isExist (String key, JSONObject contents){
	  Iterator<String> keys = contents.keys();
	  while( keys.hasNext() ){
	     String k = (String)keys.next();
	     if (k.equals(key)) return true;
	  }
	return false;
  }

  public void setCookies(List<String> cookies) {
	  if (cookies != null){
		  for( String cookie : cookies){
			  List<HttpCookie> k = HttpCookie.parse(cookie);
			  for(HttpCookie cook : k){
				  if (this.cookies != null && !this.cookies.isEmpty()) this.cookies = new String(this.cookies + ";");
				  this.cookies = new String((this.cookies != null ? this.cookies : "") + cook.getName() + '=' + cook.getValue());
			  }
		  }
	  }
  }
  
  private String parseObject(JSONObject result){
	  try {
		  StringBuilder resultText = new StringBuilder();
			Iterator<?> keys = result.keys();
			while( keys.hasNext() ) {
				String key = (String)keys.next();
				if (resultText.length() == 0) {
					resultText.append(key.trim() + "=" + result.getString(key));
				} else {
					resultText.append("&" + key.trim() + "=" + result.getString(key));
				}
			}
			return resultText.toString();
	  }
	  catch(Exception e){
		  e.printStackTrace();
	  }
	  return "";
  }
  
  private JSONObject merge(JSONObject Obj1, JSONObject Obj2){
	  JSONObject merged = new JSONObject();
	  JSONObject[] objs = new JSONObject[] { Obj1, Obj2 };
	  try {
		  for (JSONObject obj : objs) {
		      Iterator it = obj.keys();
		      while (it.hasNext()) {
		          String key = (String)it.next();
					merged.put(key, obj.get(key));
		      }
		  }
	  } catch (JSONException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }
	  return merged;
  }

}