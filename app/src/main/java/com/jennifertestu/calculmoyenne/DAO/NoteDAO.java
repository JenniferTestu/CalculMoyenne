package com.jennifertestu.calculmoyenne.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jennifertestu.calculmoyenne.model.Note;

import java.util.List;

@Dao
public interface NoteDAO {

    @Insert
    void insert(Note n);

    @Delete
    void delete(Note n);

    @Update
    void update(Note n);

    @Query("SELECT * FROM Note WHERE id_matiere = :id_m")
    List<Note> getAllByMatiere(int id_m);

    @Query("DELETE FROM Note WHERE id_matiere = :id_m")
    void deleteByMatiere(int id_m);

}
