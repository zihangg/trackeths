package com.example.trackeths.Globals;

public class Model {

    private String description, amount;

    public Model(){

    }

    public Model(String description, String amount) {
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

}

