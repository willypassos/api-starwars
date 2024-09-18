package br.com.swapi.model;

public class CrewRecordFleet {
    private String name;
    private String height;
    private String mass;
    private String gender;
    private Integer externalId;
    private boolean available;  // Adicionando o campo 'available'

    // Construtor com todos os parâmetros
    public CrewRecordFleet(String name, String height, String mass, String gender, Integer externalId, boolean available) {
        this.name = name;
        this.height = height;
        this.mass = mass;
        this.gender = gender;
        this.externalId = externalId;
        this.available = available;
    }

    // Getters e Setters
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

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(int externalId) {
        this.externalId = externalId;
    }

    public boolean isAvailable() {
        return available;  // Método isAvailable para verificar a disponibilidade
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
