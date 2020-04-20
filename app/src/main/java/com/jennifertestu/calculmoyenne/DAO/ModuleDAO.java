package com.jennifertestu.calculmoyenne.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jennifertestu.calculmoyenne.model.Module;

import java.util.List;

@Dao
public interface ModuleDAO {

    @Insert
    void insert(Module m);

    @Delete
    void delete(Module m);

    @Update
    void update(Module m);

    @Query("SELECT * FROM Module")
    List<Module> getAll();

    @Query("SELECT * FROM Module WHERE id_annee = :id_a ORDER BY periode")
    List<Module> getAllByAnnee(int id_a);

    @Query("SELECT * FROM Module WHERE id_annee = :id_a AND periode = :p")
    List<Module> getAllByAnneeAndPeriode(int id_a, int p);

    @Query("SELECT * FROM Module WHERE id_module = :id_m")
    Module getById(int id_m);
}
