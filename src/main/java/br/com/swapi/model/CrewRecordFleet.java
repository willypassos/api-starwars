package br.com.swapi.model;

public class CrewRecordFleet {
    private String name;
    private String height;
    private String mass;
    private String gender;
    private int externalId;

    // Construtores, Getters e Setters
    public CrewRecordFleet() {}

    public CrewRecordFleet(String name, String height, String mass, String gender, int externalId) {
        this.name = name;
        this.height = height;
        this.mass = mass;
        this.gender = gender;
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }
}
