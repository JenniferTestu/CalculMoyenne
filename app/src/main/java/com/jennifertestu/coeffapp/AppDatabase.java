package com.jennifertestu.coeffapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.jennifertestu.coeffapp.DAO.AnneeDAO;
import com.jennifertestu.coeffapp.DAO.MatiereDAO;
import com.jennifertestu.coeffapp.DAO.NoteDAO;
import com.jennifertestu.coeffapp.converter.DateConverter;
import com.jennifertestu.coeffapp.converter.TypeDeNoteConverter;
import com.jennifertestu.coeffapp.model.Annee;
import com.jennifertestu.coeffapp.model.Matiere;
import com.jennifertestu.coeffapp.model.Note;

@Database(entities = {Matiere.class, Note.class, Annee.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, TypeDeNoteConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MatiereDAO matiereDAO();
    public abstract NoteDAO noteDAO();
    public abstract AnneeDAO anneeDAO();

}
