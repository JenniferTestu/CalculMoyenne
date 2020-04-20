package com.jennifertestu.calculmoyenne.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Annee {

    // Id de l'année
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_annee")
    private int id;
    // Nom pour désigner cette année
    private String nom;
    // Année en cours ou pas
    private Boolean actif;
    // Nombre de période qui la compose
    private int nbPeriodes;
    // Année divisé en module ou pas
    private boolean avecModule;
    // Liste des modules
    @Ignore
    private List<Module> listeModules;

    public Annee(String nom, int nbPeriodes, boolean avecModule) {
        this.nom = nom;
        this.nbPeriodes = nbPeriodes;
        this.avecModule = avecModule;
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

    public List<Module> getListeModules() {
        return listeModules;
    }

    public void setListeModules(List<Module> listeModules) {
        this.listeModules = listeModules;
    }

    public boolean isAvecModule() {
        return avecModule;
    }

    public void setAvecModule(boolean avecModule) {
        this.avecModule = avecModule;
    }
}
