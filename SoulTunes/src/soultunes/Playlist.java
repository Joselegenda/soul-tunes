/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soultunes;

import java.util.ArrayList;

/**
 *
 * @author thund
 */
public class Playlist {
    private String name;
    private ArrayList<Track> tracks;
    
    public Playlist(String n, ArrayList<Track> t){
        name = n;
        tracks = t;
    }
    
    public Playlist(){
        name = "";
        tracks = null;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }
    
    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }
    
    
}
