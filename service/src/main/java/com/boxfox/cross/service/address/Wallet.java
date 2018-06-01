package com.boxfox.cross.service.address;

public class Wallet {
    private String uid;
    private String owner;
    private String name;
    private String symbol;
    private String description;
    private String originalAddress;
    private String crossAddress;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginalAddress() {
        return originalAddress;
    }

    public void setOriginalAddress(String originalAddress) {
        this.originalAddress = originalAddress;
    }

    public String getCrossAddress() {
        return crossAddress;
    }

    public void setCrossAddress(String crossAddress) {
        this.crossAddress = crossAddress;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
