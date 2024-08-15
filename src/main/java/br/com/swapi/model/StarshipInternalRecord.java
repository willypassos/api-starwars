package br.com.swapi.model;

public class StarshipInternalRecord {
    private String name;
    private String model;
    private String price;
    private String crew;
    private String cargo;
    private String speed;
    private int externalId;
    private String starshipClass;
    private boolean available;

    public StarshipInternalRecord() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCrew() {
        return crew;
    }

    public void setCrew(String crew) {
        this.crew = crew;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public String getStarshipClass() {
        return starshipClass;
    }

    public void setStarshipClass(String starshipClass) {
        this.starshipClass = starshipClass;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public StarshipInternalRecord(String name, String model, String price, String crew, String cargo, String speed, int externalId, String starshipClass, boolean available) {
        this.name = name;
        this.model = model;
        this.price = price;
        this.crew = crew;
        this.cargo = cargo;
        this.speed = speed;
        this.externalId = externalId;
        this.starshipClass = starshipClass;
        this.available = available;
    }

    // Getters e Setters
}
