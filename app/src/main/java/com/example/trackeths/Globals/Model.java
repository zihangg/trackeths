package com.example.trackeths.Globals;

public class Model {

    private String id, description, amount;

    public Model(){
    }

    public Model(String id, String description, String amount) {
        this.id = id;
        this.description = description;
        this.amount = amount;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}

