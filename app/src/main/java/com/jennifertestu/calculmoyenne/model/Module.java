package com.jennifertestu.calculmoyenne.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Module {

    // Id du module
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_module")
    private int id;
    // Nom du module
    private String nom;
    // Numéro de la période à laquelle elle est rattachée
    private int periode;
    // Id de l'année à laquelle elle est rattachée
    @ColumnInfo(name="id_annee")
    private int idAnnee;
    // Liste des matières associées à ce module
    @Ignore
    private List<Matiere> listeMatieres;
    // Moyenne du module
    @Ignore
    private double moy = -1;
    // Somme des coefficients
    @Ignore
    private double sumCoef;

    public Module(String nom, int idAnnee, int periode) {
        this.nom = nom;
        this.idAnnee = idAnnee;
        this.periode=periode;
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

    public int getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(int idAnnee) {
        this.idAnnee = idAnnee;
    }

    public int getPeriode() {
        return periode;
    }

    public void setPeriode(int numPeriode) {
        this.periode = numPeriode;
    }

    public double getMoy() {
        return moy;
    }

    public void setMoy(double moy) {
        this.moy = moy;
    }

    public double calculerMoy(){

        if(listeMatieres==null || listeMatieres.isEmpty()){
            moy = -1.0;
            sumCoef = 0.0;
            return moy;
        }else {
            moy = 0.0;
            sumCoef = 0.0;

            for (Matiere m : listeMatieres) {
                m.calculerMoy();
                if (m.getMoy() != -1) {
                    moy += m.calculerMoy() * m.getCoef();
                    sumCoef += m.getCoef();
                }
            }
            moy = moy / sumCoef;

            return moy;
        }

    }

    public double getSumCoef() {
        return sumCoef;
    }

    @Override
    public String toString() {
        return nom +
                " (Période " + periode +
                ')';
    }
}
