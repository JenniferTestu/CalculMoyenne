package com.jennifertestu.coeffapp.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jennifertestu.coeffapp.model.Annee;

import java.util.List;

@Dao
public interface AnneeDAO {

    @Insert
    void insert(Annee a);

    @Delete
    void delete(Annee a);

    @Update
    void update(Annee a);

    @Query("SELECT * FROM Annee")
    List<Annee> getAll();

    //@Query("SELECT Count(*) from Annee")
    @Query("SELECT count(1) WHERE EXISTS (SELECT * FROM Annee)")
    int count();


}
