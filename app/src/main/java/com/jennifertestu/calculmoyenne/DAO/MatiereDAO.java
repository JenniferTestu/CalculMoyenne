package com.jennifertestu.calculmoyenne.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jennifertestu.calculmoyenne.model.Matiere;

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

    @Query("DELETE FROM Matiere WHERE id_module = :id_m")
    void deleteByModule(int id_m);

    @Query("SELECT * FROM Matiere WHERE id_module = :id_m")
    List<Matiere> getAllByModule(int id_m);

}
