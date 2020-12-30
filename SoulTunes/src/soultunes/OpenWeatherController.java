/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soultunes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author thund
 */
public class OpenWeatherController {
    // Location is hard coded, as proof of concept. 
    private static String CITY_CODE = "Caracas,VEN";
    private static final String APPID = "7feeb2474eb1d9e7b07ddfeea727524d";
    
    private static String getJson(){
        String res = "";
        try
        {
            StringBuilder result = new StringBuilder();
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+CITY_CODE+"&appid="+APPID);
            URLConnection connection = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            String line;
            while((line = rd.readLine()) !=  null){
                result.append(line);
            }
            rd.close();
            res = result.toString();
        }
        catch(Exception e)
        {
            System.out.println("Something wrong here...");
            e.printStackTrace();
        }
        return res;
    }
    public static String getLocation(){
        return CITY_CODE;
    }
    public static String getWeatherLive(){
        
        String json = getJson();
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        JsonArray weatherList = root.get("weather").getAsJsonArray();
        JsonObject weatherObj = weatherList.get(0).getAsJsonObject();
        String weather = weatherObj.get("main").getAsString();
        return weather;
    }
}
