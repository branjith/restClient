import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


public class RestClient {

	public static HashSet<String> endpoints;
	public static HashSet<String> methods;
	public static HashSet<String> commands;
	public static HashSet<String> settings;

	DefaultHttpClient httpClient;
	String baseUrl;
	boolean debug;
	String fileLocation;
	String configFile;


	public RestClient(){
		httpClient = new DefaultHttpClient();
		settings = new HashSet<String>(Arrays.asList("seturl","listconfig","setdebug","setfiledir")); 
		methods = new HashSet<String>(Arrays.asList("POST","GET","PATCH","PUT","DELETE"));
		String pwd=System.getProperty("user.dir"),line=null;
		configFile="config.txt";
		File f1= new File(pwd+"/"+configFile);
		String url1=null,fileDir1=null;
		boolean debug1=false;
		try{
		if(f1.exists()){
			DataInputStream dis= new DataInputStream(new FileInputStream(f1));
			while((line=dis.readLine())!=null){
				if(line.startsWith("debug")){
					line=line.substring(line.indexOf("=")+1);
					if(line.equalsIgnoreCase("true"))
						debug1=true;
				}
				else if(line.startsWith("url")){
					url1=line.substring(line.indexOf("=")+1);
					if(url1.equalsIgnoreCase("null"))
						url1=null;
				}else if(line.startsWith("fileDir")){
					fileDir1=line.substring(line.indexOf("=")+1);
					if(fileDir1.equalsIgnoreCase("null"))
						fileDir1=null;
				} 
			}
			dis.close();
		}
		
		}catch(Exception e){
			e.printStackTrace();
		}
		debug=debug1;
		if(fileLocation==null)
			fileLocation=pwd+"/";
		if(url1==null){
			try{
				/*Scanner reader = new Scanner(System.in); 
				System.out.print("Please enter the REST URL: ");
				System.out.flush();
				url1=reader.nextLine().trim();*/
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		if(url1!=null&&!url1.endsWith("/"))
			url1=url1+"/";
		baseUrl=url1;
		writeConfig();
	}

	/*
	 * seturl <url>
	 * listconfig
	 * setloglevel <true false>
	 * <url> GET
	 */


	public static void main(String[] args) throws Exception{
		RestClient ob=new RestClient();
		try{
			String endpoint=args[0];
			String method=null;
			String JSONfile=null;
			JSONObject json=new JSONObject();
			if(settings.contains(endpoint.toLowerCase())){
				if(endpoint.equalsIgnoreCase("listconfig")){
					ob.getConfig();	
				} else if(endpoint.equalsIgnoreCase("seturl")){
					if(args[1]==null)
						ob.printUsage(args);
					else{
						ob.baseUrl=args[1];
						ob.writeConfig();
					}
				}else if(endpoint.equalsIgnoreCase("setdebug")){
					if(args[1]==null)
						ob.printUsage(args);
					else{
						if(args[1].equalsIgnoreCase("true"))
							ob.debug=true;
						else
							ob.debug=false;
						ob.writeConfig();
					}
				}else if(endpoint.equalsIgnoreCase("setfiledir")){
					if(args[1]==null)
						ob.printUsage(args);
					else{
						ob.fileLocation=args[1];
						ob.writeConfig();
					}
				}
			}else{
				if(endpoint.startsWith("http")){
					ob.baseUrl=endpoint;
					endpoint="";
				}					
				method=args[1];
				if(methods.contains(method)){
					if(method.equalsIgnoreCase("get")||method.equalsIgnoreCase("delete")){
						if(method.equalsIgnoreCase("get")){
							ob.getMethod(ob.baseUrl+endpoint);
						}else if(method.equalsIgnoreCase("delete")){
							ob.deleteMethod(ob.baseUrl+endpoint);
						}
					}
					else{
						JSONfile=args[2];
						StringBuffer sb=new StringBuffer();
						try{
							DataInputStream dis = new DataInputStream(new FileInputStream(new File(ob.fileLocation+JSONfile)));
							String line=null;
							while((line=dis.readLine())!=null){
								sb.append(line.trim());
							}
							json=new JSONObject(sb.toString());
						}catch(Exception e){
							System.out.println("bad json file");
							e.printStackTrace();
						}
						
						if(args.length>4)
							ob.replaceValues(json, args[3]);
						if(method.equalsIgnoreCase("post")){
							ob.postMethod(ob.baseUrl+endpoint, json.toString());
						}else if(method.equalsIgnoreCase("put")){
							ob.postMethod(ob.baseUrl+endpoint, json.toString());
						}else if(method.equalsIgnoreCase("patch")){
							ob.postMethod(ob.baseUrl+endpoint, json.toString());
						}
					}
				}
			}
		}catch(Exception e){
			ob.printUsage(args);
			e.printStackTrace();
		}
	}
	
	public void replaceValues(JSONObject json,String newValue){
		String[] strs=newValue.split("=");
		System.out.println("OLD "+json.get(strs[0]));
		json.put(strs[0], strs[1]);
		System.out.println("NEW "+json.get(strs[0]));
	}
	
	public void writeConfig(){
		try{
			FileWriter fw = new FileWriter(new File("config.txt"));
			//fw.write("<settings>"); /** todo */
			fw.write("debug="+debug);
			fw.write("\nurl="+baseUrl);
			fw.write("\nfileDir="+fileLocation);
			//fw.write("\n</settings>");
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}

	/**
	 * todo print the whole config file
	 */
	public void getConfig(){
		System.out.println(" --------- Configuration Details --------- ");
		System.out.println("  "+"URL: "+baseUrl);
		System.out.println("  "+"DEBUG Flag :"+debug);
		System.out.println("  "+"JSON File Directory : "+fileLocation);
		System.out.println(" ----------------------------------------- ");
	}

	public void printUsage(String args[])
	{
		System.out.println("invalid command");
		if(args!=null)
			for(int i=0;i<args.length;i++)
				System.out.print(args[i]+" ");
		System.out.println("\n---------------------------------------------------------");
		System.out.println("- sh restclient <url/endpoint> <method> <jsonFile>      -");
		System.out.println("- sh restclient http://abc/myEndpoint GET ---------------");
		System.out.println("- sh restclient myEndpoint GET --------------------------");
		System.out.println("- sh restclient endpnts POST jsonFile -------------------");
		System.out.println("- sh restclient endpnts/1 PUT jsonFile ------------------");
		System.out.println("- sh restclient seturl http://adc/ ----------------------");
		System.out.println("- sh restclient listconfig ------------------------------");
		System.out.println("- sh restclient setdebug <true/false> -------------------");
		System.out.println("- sh restclient endpoints/1 delete ---__-----------------");
		System.out.println("- sh restclient setFileDir <json file's directory loc> --");
		System.out.println("---------------------------------------------------------\n");
	}

	public void patchMethod(String url, String json)throws Exception{
		System.out.println("URL :"+url);
		prettyPrint(json);
		HttpPatch patchRequest = new HttpPatch(url);
		patchRequest.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(patchRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed : HTTP error code : "+ response.getStatusLine().getStatusCode());
		}
		outputFromServer(response);
	}

	public void putMethod(String url, String json) throws Exception
	{
		System.out.println("URL :"+url);
		prettyPrint(json);
		HttpPut putRequest = new HttpPut(url);
		putRequest.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(putRequest);
		if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed : HTTP error code : "+ response.getStatusLine().getStatusCode());
		}
		outputFromServer(response);
	}

	public void getMethod(String url) throws Exception
	{
		System.out.println("URL :"+url);
		HttpGet getRequest = new HttpGet(url);
		getRequest.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed : HTTP error code : "+ response.getStatusLine().getStatusCode());
		}
		outputFromServer(response);	
	}

	public void postMethod(String url,String json) throws Exception
	{
		System.out.println("URL :"+url);
		prettyPrint(json);
		HttpPost postRequest = new HttpPost(url);
		StringEntity input = new StringEntity( json);
		input.setContentType("application/json");
		postRequest.setEntity(input);

		HttpResponse response = httpClient.execute(postRequest);
		int retStatus=response.getStatusLine().getStatusCode();

		String line;
		if (retStatus>=400){
			System.out.println("Failed : HTTP error code : "+ response.getStatusLine().getStatusCode());
		}
		outputFromServer(response);
	}

	public void deleteMethod(String url) throws Exception
	{
		System.out.println("URL :"+url);
		HttpDelete deleteRequest = new HttpDelete(url);

		deleteRequest.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(deleteRequest);

		if (response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed : HTTP error code : "+ response.getStatusLine().getStatusCode());
		}
		outputFromServer(response);
	}

	public void outputFromServer(HttpResponse response) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String line;
		StringBuffer output= new StringBuffer();
		System.out.println("Output from Server ....");
		//output.append("Output from Server ....\n");
		while ((line = br.readLine()) != null) {
			output.append(line+"\n");
		}
		prettyPrint(output.toString());
		Header[] header=response.getAllHeaders();
		//System.out.println("Headers....");
		output.append("Headers...\n");
		for(int i=0;i<header.length;i++){
			System.out.println(header[i].getName()+" :: "+header[i].getValue());
		}
	}
	
	public void prettyPrint(String json){
		JSONObject jobj=null;
		if(json.startsWith("[")){
			JSONArray jarry=new JSONArray(json);
			for(int i=0;i<jarry.length();i++){
				jobj=(JSONObject) jarry.get(i);
				System.out.println(jobj.toString(4));
			}				
		}
		else if(json.startsWith("{")){
			jobj=new JSONObject(json);
			System.out.println(jobj.toString(4));
		}
		else
			System.out.println(json);
	}
}
