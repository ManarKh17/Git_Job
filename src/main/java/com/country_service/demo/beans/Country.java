package com.country_service.demo.beans;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @Column(name = "id")
    private int idCountry;

    @Column(name = "name_country")
    private String name;

    @Column(name = "capital")
    private String capital;

    // --- Constructeurs ---
    public Country() {
    }

    public Country(int idCountry, String name, String capital) {
        this.idCountry = idCountry;
        this.name = name;
        this.capital = capital;
    }

    // --- Getters et Setters ---
    public int getIdCountry() {
        return idCountry;
    }

    public void setIdCountry(int idCountry) {
        this.idCountry = idCountry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    // --- toString() pour affichage ---
    @Override
    public String toString() {
        return "Country{" +
                "idCountry=" + idCountry +
                ", name='" + name + '\'' +
                ", capital='" + capital + '\'' +
                '}';
    }
}
