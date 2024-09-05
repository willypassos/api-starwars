package br.com.swapi.model;

import org.bson.Document;

public class CrewRecordFleet {
    private String name;
    private String height;
    private String mass;
    private String gender;
    private int externalId;

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

    // Método para converter para Document do MongoDB
    public Document toDocument() {
        return new Document("name", name)
                .append("height", height)
                .append("mass", mass)
                .append("gender", gender)
                .append("externalId", externalId);
    }

    // Método para criar CrewRecordFleet a partir de Document do MongoDB
    public static CrewRecordFleet fromDocument(Document doc) {
        return new CrewRecordFleet(
                doc.getString("name"),
                doc.getString("height"),
                doc.getString("mass"),
                doc.getString("gender"),
                doc.getInteger("externalId")
        );
    }
}
