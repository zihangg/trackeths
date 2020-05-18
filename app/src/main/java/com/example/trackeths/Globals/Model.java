package com.example.trackeths.Globals;

public class Model {

    private String id, description, amount, category, imageUrl;

    public Model(){
    }

    public Model(String id, String description, String amount, String category, String imageUrl) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.imageUrl = imageUrl;
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


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

