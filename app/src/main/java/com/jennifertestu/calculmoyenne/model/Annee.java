package com.jennifertestu.calculmoyenne.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Annee {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_annee")
    private int id;
    private String nom;
    private Boolean actif;
    private int nbPeriodes;
    @Ignore
    private List<Periode> listePeriodes;

    public Annee(String nom, int nbPeriodes) {
        this.nom = nom;
        this.nbPeriodes = nbPeriodes;
    }

    public int getNbPeriodes() {
        return nbPeriodes;
    }

    public void setNbPeriodes(int nbPeriodes) {
        this.nbPeriodes = nbPeriodes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public List<Periode> getListePeriodes() {
        return listePeriodes;
    }

    public void setListePeriodes(List<Periode> listePeriodes) {
        this.listePeriodes = listePeriodes;
    }
}
