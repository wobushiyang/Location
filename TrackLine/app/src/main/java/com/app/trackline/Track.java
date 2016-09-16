package com.app.trackline;

import java.util.ArrayList;

/**
 * Created by ASUS-PC on 2016/8/20.
 */
public class Track {

    private int id;
    private String track_name;
    private String create_date;
    private String start_loc;
    private String end_loc;
    private ArrayList trackDetails;//线路里面的点集合

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTrack_name() {
        return track_name;
    }

    public void setTrack_name(String track_name) {
        this.track_name = track_name;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getStart_loc() {
        return start_loc;
    }

    public void setStart_loc(String start_loc) {
        this.start_loc = start_loc;
    }

    public String getEnd_loc() {
        return end_loc;
    }

    public void setEnd_loc(String end_loc) {
        this.end_loc = end_loc;
    }

    public ArrayList getTrackDetails() {
        return trackDetails;
    }

    public void setTrackDetails(ArrayList trackDetails) {
        this.trackDetails = trackDetails;
    }

    public Track(int id, String track_name, String create_date, String start_loc, String end_loc) {
        this.id = id;
        this.track_name = track_name;
        this.create_date = create_date;
        this.start_loc = start_loc;
        this.end_loc = end_loc;
    }

    public Track() {
    }
}
