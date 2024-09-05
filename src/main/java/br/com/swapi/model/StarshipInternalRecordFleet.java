package br.com.swapi.model;

import org.bson.Document;

public class StarshipInternalRecordFleet {
    private String name;
    private String model;
    private String starshipClass;
    private int externalId;

    public StarshipInternalRecordFleet(String name, String model, String starshipClass, int externalId) {
        this.name = name;
        this.model = model;
        this.starshipClass = starshipClass;
        this.externalId = externalId;
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

    public String getStarshipClass() {
        return starshipClass;
    }

    public void setStarshipClass(String starshipClass) {
        this.starshipClass = starshipClass;
    }

    public int getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    // Converter para Document do MongoDB
    public Document toDocument() {
        return new Document("name", this.name)
                .append("model", this.model)
                .append("starshipClass", this.starshipClass)
                .append("externalId", this.externalId);
    }

    // Converter de Document para StarshipInternalRecordFleet
    public static StarshipInternalRecordFleet fromDocument(Document doc) {
        return new StarshipInternalRecordFleet(
                doc.getString("name"),
                doc.getString("model"),
                doc.getString("starshipClass"),
                doc.getInteger("externalId")
        );
    }
}
