package com.boxfox.core.router.model;

import com.google.gson.annotations.SerializedName;

public class CoinPriceNetworkObject {
    @SerializedName("amount") private double amount;
    @SerializedName("unit") private String unit;

    public double getAmount(){
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit(){
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}