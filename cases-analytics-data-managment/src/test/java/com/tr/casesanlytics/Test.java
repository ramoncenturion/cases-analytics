package com.tr.casesanlytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Test {

	
	private final static String USER_AGENT = "Mozilla/5.0";
	
	public static void main(String[] args) {

		String url = "http://localhost:2222/judge?name=Venturini%20Camejo&country=Uruguay&subject=civil";
		StringBuilder response = new StringBuilder();
		try{
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine+"\n");
			}
			in.close();
			
		}catch (Exception e){
			
		}		
		//print result
		System.out.println(response.toString());		
	}
}
