package com.jennifertestu.calculmoyenne.model;

import java.util.List;

public class Periode {

    private int id;
    private String nom;
    private List<Matiere> listeMatieres;
    private double moy;

    public Periode(String nom) {
        this.nom = nom;
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

    public List<Matiere> getListeMatieres() {
        return listeMatieres;
    }

    public void setListeMatieres(List<Matiere> listeMatieres) {
        this.listeMatieres = listeMatieres;
    }

    public double calculerMoy(){

        moy = 0;
        int coef = 0;

        for(Matiere m : listeMatieres){
            moy =+m.calculerMoy() * m.getCoef();
            coef =+m.getCoef();
        }
        moy = moy/coef;
        return moy;

    }
}
