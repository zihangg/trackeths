package com.example.trackeths.Globals;

public class User {
    private String email;

    public User(){

    }

    public User(String email) {
        this.email = email;
    }

    public void setEmail(String email){this.email = email;}

    public String getEmail(){return email;}
}
