package com.jennifertestu.calculmoyenne;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.jennifertestu.calculmoyenne.DAO.AnneeDAO;
import com.jennifertestu.calculmoyenne.DAO.MatiereDAO;
import com.jennifertestu.calculmoyenne.DAO.ModuleDAO;
import com.jennifertestu.calculmoyenne.DAO.NoteDAO;
import com.jennifertestu.calculmoyenne.converter.DateConverter;
import com.jennifertestu.calculmoyenne.converter.TypeDeNoteConverter;
import com.jennifertestu.calculmoyenne.model.Annee;
import com.jennifertestu.calculmoyenne.model.Matiere;
import com.jennifertestu.calculmoyenne.model.Module;
import com.jennifertestu.calculmoyenne.model.Note;

@Database(entities = {Matiere.class, Note.class, Annee.class, Module.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class, TypeDeNoteConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MatiereDAO matiereDAO();
    public abstract NoteDAO noteDAO();
    public abstract AnneeDAO anneeDAO();
    public abstract ModuleDAO moduleDAO();

}
