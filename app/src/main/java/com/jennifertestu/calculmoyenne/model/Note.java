package com.jennifertestu.calculmoyenne.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.jennifertestu.calculmoyenne.converter.DateConverter;
import com.jennifertestu.calculmoyenne.converter.TypeDeNoteConverter;

import java.io.Serializable;
import java.util.Date;

@Entity
public class Note implements Comparable<Note>, Serializable {

    // Identifiant de la note
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_note")
    private int id;
    // Valeur de la note
    private double valeur;
    // Type de controle
    @TypeConverters({TypeDeNoteConverter.class})
    private TypeDeNote typeDeNote;
    // Date a laquelle la note correspond
    @TypeConverters({DateConverter.class})
    private Date date;
    // Commentaire de l'utilisateur à propos de la note
    private String commentaire;
    // Poids de la note
    @Nullable
    private double poids;
    // Identifiant de la matière a laquelle la note est rattachée
    @ColumnInfo(name="id_matiere")
    private int idMatiere;



    public Note(double valeur, TypeDeNote typeDeNote, Date date, int idMatiere) {
        this.valeur = valeur;
        this.typeDeNote = typeDeNote;
        this.date = date;
        this.idMatiere = idMatiere;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public TypeDeNote getTypeDeNote() {
        return typeDeNote;
    }

    public void setTypeDeNote(TypeDeNote typeDeNote) {
        this.typeDeNote = typeDeNote;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public int getIdMatiere() {
        return idMatiere;
    }

    public void setIdMatiere(int id_matiere) {
        this.idMatiere = id_matiere;
    }

    @Override
    public int compareTo(Note n) {
        if (getDate() == null || n.getDate() == null) {
            return 0;
        }
        return getDate().compareTo(n.getDate());
    }
}
