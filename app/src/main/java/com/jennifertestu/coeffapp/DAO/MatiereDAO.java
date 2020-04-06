package com.jennifertestu.coeffapp.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jennifertestu.coeffapp.model.Matiere;

import java.util.List;

@Dao
public interface MatiereDAO {

    @Insert
    void insert(Matiere m);

    @Delete
    void delete(Matiere m);

    @Update
    void update(Matiere m);

    @Query("SELECT * FROM Matiere")
    List<Matiere> getAll();

    @Query("SELECT * FROM Matiere WHERE id_annee = :id_a")
    List<Matiere> getAllByAnnee(int id_a);

    @Query("SELECT * FROM Matiere WHERE id_annee = :id_a AND periode = :p")
    List<Matiere> getAllByAnneeAndPeriode(int id_a, int p);

    @Query("DELETE FROM Matiere WHERE id_annee = :id_a")
    void deleteByAnnee(int id_a);

}
