package com.jennifertestu.calculmoyenne.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
public class Matiere implements Comparable<Matiere>,Serializable {

    // Identifiant de la matiere
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_matiere")
    private int id;

    // Nom de la matiere
    private String nom;

    // Coefficient de la matiere
    private double coef;

    // La moyenne de cette matiere est-elle pondéré ?
    private boolean moyPond;

    // Id de l'année à laquelle la matière est rattachée
    @ColumnInfo(name="id_annee")
    private int idAnnee;

    // Numéro de la période à laquelle la matière est rattachée
    private int periode;

    // Id de l'année à laquelle la matière est rattachée
    @ColumnInfo(name="id_module")
    private int idModule;

    // La moyenne de la matiere
    @Ignore
    private double moy ;

    // Liste des notes associées à cette matière
    @Ignore
    private List<Note> listeNotes;


    public Matiere(int idAnnee, int periode, String nom, double coef, boolean moyPond) {
        this.idAnnee = idAnnee;
        this.periode = periode;
        this.nom = nom;
        this.coef = coef;
        this.moyPond = moyPond; // a enlever
        //this.listeNotes = new ArrayList<>();
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

    public double getCoef() {
        return coef;
    }

    public void setCoef(double coef) {
        this.coef = coef;
    }

    public boolean isMoyPond() {
        return moyPond;
    }

    public void setMoyPond(boolean moyPond) {
        this.moyPond = moyPond;
    }

    @Ignore
    public List<Note> getListeNotes() {
        return listeNotes;
    }

    @Ignore
    public void setListeNotes(List<Note> listeNotes) {
        this.listeNotes = listeNotes;
    }

    public Date getDateDerniereNote(){
        if(listeNotes==null){
            return null;
        }else {
            Collections.sort(listeNotes);
            if (listeNotes.isEmpty()) {
                return null;
            } else {
                return listeNotes.get(listeNotes.size() - 1).getDate();
            }
        }
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

    public void setPeriode(int periode) {
        this.periode = periode;
    }

    public double getMoy() {
        return moy;
    }

    public int getIdModule() {
        return idModule;
    }

    public void setIdModule(int idModule) {
        this.idModule = idModule;
    }

    public double calculerMoy(){

        // Si il n'y a pas de notes, on renvoie une valeur négative
        if(listeNotes.isEmpty() || listeNotes==null){
            moy = -1;
            return moy;
        }else {
            moy = 0;
            double poids = 0.0;

            // Si cette matiere a une moyenne pondérée
            if (moyPond == true) {
                for (Note n : listeNotes) { // Pour chacune de ses notes
                    moy += n.getValeur() * n.getPoids(); // note*poids que l'on ajoute au calcul précedent
                    poids += n.getPoids(); // On ajoute le poids
                }
                moy = moy / poids; // La moyenne est calculée
                return moy;
            } else { // Si cette matiere n'a pas une moyenne pondérée
                for (Note n : listeNotes) { // Pour chacune de ses notes
                    moy += n.getValeur(); // On ajoute la note aux précédentes
                }
                moy = moy / listeNotes.size(); // La somme des notes est divisée par le nombre de notes
                return moy;
            }
        }
    }

    @Override
    public int compareTo(Matiere m) {
        if (getDateDerniereNote() == null || m.getDateDerniereNote() == null) {
            return 0;
        }
        return getDateDerniereNote().compareTo(m.getDateDerniereNote());
    }

}
