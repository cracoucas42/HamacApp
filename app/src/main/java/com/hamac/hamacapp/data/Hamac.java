package com.hamac.hamacapp.data;

import java.io.Serializable;

public class Hamac implements Serializable
{
    private String id;//create to identify the Hamac during time, has to be unique
    private String name;
    private String description;
    private double lat = 0;
    private double lng = 0;
    private String user = "";
    private String photoUrl_1 = "";
    private String photoUrl_2 = "";
    private String photoUrl_3 = "";
    private String photoUrl_4 = "";
    private String photoUrl_5 = "";

    public Hamac() {
    }

    public Hamac(String id, String name, String description, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }

    public Hamac(String id, String name, String description, double lat, double lng, String user, String photoUrl_1, String photoUrl_2, String photoUrl_3, String photoUrl_4, String photoUrl_5) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.user = user;
        this.photoUrl_1 = photoUrl_1;
        this.photoUrl_2 = photoUrl_2;
        this.photoUrl_3 = photoUrl_3;
        this.photoUrl_4 = photoUrl_4;
        this.photoUrl_5 = photoUrl_5;
    }

    public String getPhotoUrl_1() {
        return photoUrl_1;
    }

    public void setPhotoUrl_1(String photoUrl_1) {
        this.photoUrl_1 = photoUrl_1;
    }

    public String getPhotoUrl_2() {
        return photoUrl_2;
    }

    public void setPhotoUrl_2(String photoUrl_2) {
        this.photoUrl_2 = photoUrl_2;
    }

    public String getPhotoUrl_3() {
        return photoUrl_3;
    }

    public void setPhotoUrl_3(String photoUrl_3) {
        this.photoUrl_3 = photoUrl_3;
    }

    public String getPhotoUrl_4() {
        return photoUrl_4;
    }

    public void setPhotoUrl_4(String photoUrl_4) {
        this.photoUrl_4 = photoUrl_4;
    }

    public String getPhotoUrl_5() {
        return photoUrl_5;
    }

    public void setPhotoUrl_5(String photoUrl_5) {
        this.photoUrl_5 = photoUrl_5;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
