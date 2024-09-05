package br.com.swapi.model;

import org.bson.Document;

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

    public StarshipInternalRecord(String name, String model, String price, String crew, String cargo, String speed,
                                  int externalId, String starshipClass, boolean available) {
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

    public static StarshipInternalRecord fromDocument(Document doc) {
        return new StarshipInternalRecord(
                doc.getString("name"),
                doc.getString("model"),
                doc.getString("price"),
                doc.getString("crew"),
                doc.getString("cargo"),
                doc.getString("speed"),
                doc.getInteger("externalId"),
                doc.getString("starshipClass"),
                doc.getBoolean("available")
        );
    }

    public Document toDocument() {
        return new Document("name", name)
                .append("model", model)
                .append("price", price)
                .append("crew", crew)
                .append("cargo", cargo)
                .append("speed", speed)
                .append("externalId", externalId)
                .append("starshipClass", starshipClass)
                .append("available", available);
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getPrice() {
        return price;
    }

    public String getCrew() {
        return crew;
    }

    public String getCargo() {
        return cargo;
    }

    public String getSpeed() {
        return speed;
    }

    public int getExternalId() {
        return externalId;
    }

    public String getStarshipClass() {
        return starshipClass;
    }

    public boolean isAvailable() {
        return available;
    }
}
