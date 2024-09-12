package br.com.swapi.model;

public class StarshipInternalRecordFleet {
    private String name;
    private String model;
    private String price;
    private String crew;
    private String cargo;
    private String speed;
    private int external_id;
    private String starship_class;
    private boolean available;


    public StarshipInternalRecordFleet(String name, String model, String price, String crew, String cargo, String speed, int external_id, String starship_class,boolean available) {
        this.name = name;
        this.model = model;
        this.price = price;
        this.crew = crew;
        this.cargo = cargo;
        this.speed = speed;
        this.external_id = external_id;
        this.starship_class = starship_class;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
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

    public int getExternal_id() {
        return external_id;
    }

    public void setExternal_id(int external_id) {
        this.external_id = external_id;
    }

    public String getStarship_class() {
        return starship_class;
    }

    public void setStarship_class(String starship_class) {
        this.starship_class = starship_class;
    }
}



