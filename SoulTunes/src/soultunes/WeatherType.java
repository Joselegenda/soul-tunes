/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soultunes;

import java.util.Random;

public enum WeatherType{
    RAINY (new String[] {"comfort", "romantic", "classical"}), 
    SNOWY (new String[] {"comfort", "romantic", "christmas"}), 
    SUNNY (new String[] {"happy", "edm", "pop"}),
    CLOUDY (new String[] {"sad", "comfort", "jazz"}),
    THUNDERSTORM (new String[] {"thunderstorm", "angry ", "comfort"}),
    FOGGY (new String[] {"sad", "indie", "classical"});
    
    private final String[] emotion;
    
    WeatherType(String[] arr){
        this.emotion = arr;
    }
    
    public String[] getEmotions(){
        return this.emotion;
    }
    
    public String getMood(){
        Random gen = new Random();
        int random = gen.nextInt(3);
        
        return this.emotion[random];
    }
}