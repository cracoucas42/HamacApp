package com.hamac.hamacapp.data;

import java.util.ArrayList;

public class User
{
    private String id = "";
    private String name = "";
    private String mail = "";
    private String password = "";
    private String description = "";
    private String photoProfil = "";
    private ArrayList<Hamac> hamacList = new ArrayList<Hamac>();
    private ArrayList<String> hamacListComment = new ArrayList<String>();

    public User(String id, String name, String mail, String password)
    {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoProfil() {
        return photoProfil;
    }

    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }

    public ArrayList<Hamac> getHamacList() {
        return hamacList;
    }

    public void setHamacList(ArrayList<Hamac> hamacList) {
        this.hamacList = hamacList;
    }

    public ArrayList<String> getHamacListComment() {
        return hamacListComment;
    }

    public void setHamacListComment(ArrayList<String> hamacListComment) {
        this.hamacListComment = hamacListComment;
    }
}
