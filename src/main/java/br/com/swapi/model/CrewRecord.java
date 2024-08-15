package br.com.swapi.model;

public class CrewRecord {
    private String name;
    private String height;
    private String mass;
    private String gender;
    private boolean available;
    private int externalId;

    public CrewRecord() {}

    public CrewRecord(String name, String height, String mass, String gender, boolean available, int externalId) {
        this.name = name;
        this.height = height;
        this.mass = mass;
        this.gender = gender;
        this.available = available;
        this.externalId = externalId;
    }

    public CrewRecord(String name, String birthYear, String eyeColor, String gender, String hairColor, Integer height, Integer mass, String skinColor, Integer homeworld, Object o) {
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }
}
