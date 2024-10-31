package br.com.swapi.model;

import org.bson.Document;

public class CrewRecord {
    private String name;
    private String height;
    private String mass;
    private String gender;
    private boolean available;
    private Integer externalId;

    public CrewRecord() {}

    public CrewRecord(String name, String height, String mass, String gender, boolean available, Integer externalId) {
        this.name = name;
        this.height = height;
        this.mass = mass;
        this.gender = gender;
        this.available = available;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    // Método para converter para Document do MongoDB
    public Document toDocument() {
        return new Document("name", name)
                .append("height", height)
                .append("mass", mass)
                .append("gender", gender)
                .append("available", available)
                .append("externalId", externalId);
    }

    // Método para criar CrewRecord a partir de Document do MongoDB
    public static CrewRecord fromDocument(Document doc) {
        return new CrewRecord(
                doc.getString("name"),
                doc.getString("height"),
                doc.getString("mass"),
                doc.getString("gender"),
                doc.getBoolean("available"),
                doc.getInteger("externalId")
        );
    }
}
