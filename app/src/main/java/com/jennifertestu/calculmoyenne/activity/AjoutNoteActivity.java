package com.jennifertestu.calculmoyenne.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jennifertestu.calculmoyenne.DatabaseClient;
import com.jennifertestu.calculmoyenne.R;
import com.jennifertestu.calculmoyenne.model.Matiere;
import com.jennifertestu.calculmoyenne.model.Note;
import com.jennifertestu.calculmoyenne.model.TypeDeNote;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AjoutNoteActivity extends AppCompatActivity {

    private EditText editNote, editCom, editPoids;
    private TextView textPoids;
    private Spinner editType;
    private Button pickDate;
    private int mYear,mMonth,mDay;
    private Date sDate;

    private Matiere matiere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_note);

        matiere = (Matiere) getIntent().getExtras().getSerializable("matiere");

        editNote = (EditText) findViewById(R.id.editNote);
        editType = (Spinner) findViewById(R.id.editType);
        editCom = (EditText) findViewById(R.id.editCom);
        editPoids = (EditText) findViewById(R.id.editPoids);
        textPoids = (TextView) findViewById(R.id.textPoids);

        editType.setAdapter(new ArrayAdapter<TypeDeNote>(this, android.R.layout.simple_spinner_item, TypeDeNote.values()));

        // Bouton pour annuler l'ajout et retourner à la liste
        Button boutonAnnuler = (Button)findViewById(R.id.bouton_annuler);
        boutonAnnuler.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
                intent.putExtra("matiere", matiere);
                startActivity(intent);

            }
        });

        if(matiere.isMoyPond()==false){
            textPoids.setVisibility(View.INVISIBLE);
            editPoids.setVisibility(View.INVISIBLE);
        }

        // Bouton pour l'ajout
        Button boutonValiderAjouter = (Button)findViewById(R.id.bouton_val_ajouter);
        boutonValiderAjouter.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ajouterNote();

            }
        });

        pickDate = (Button) findViewById(R.id.pick_date);


        pickDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Locale.setDefault(Locale.FRANCE);

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(AjoutNoteActivity.this,R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                pickDate.setText(dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);
                                sDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();

                            }
                        }, mYear, mMonth, mDay);
                dpd.show();

            }
        });
    }

    // Tache d'ajout d'une note
    private void ajouterNote(){


        if (editNote.getText().toString().isEmpty()) {
            editNote.setError("Une valeur pour la note est requise");
            editNote.requestFocus();
            return;
        }
        if (Double.parseDouble(editNote.getText().toString().trim()) > 20){
            editNote.setError("La valeur de la note doit être inférieure ou égale à 20");
            editNote.requestFocus();
            return;
        }
        if (editPoids.getText().toString().isEmpty() && matiere.isMoyPond()==true) {
            editPoids.setError("Un poids est requis");
            editPoids.requestFocus();
            return;
        }
        if (matiere.isMoyPond()==true && Double.parseDouble(editPoids.getText().toString().trim()) <1) {
            editPoids.setError("Le poids doit être au minimum à 1");
            editPoids.requestFocus();
            return;
        }
        if (sDate==null) {
            pickDate.setError("Une date est requise");
            pickDate.requestFocus();
            return;
        }


        final double sVal = Double.parseDouble(editNote.getText().toString().trim());
        final TypeDeNote sType = TypeDeNote.valueOf(editType.getSelectedItem().toString().trim());
        final String sCom = editCom.getText().toString().trim();
        final double sPoids;

        if(matiere.isMoyPond()==false){
            sPoids = 1;
        }else {
            sPoids = Double.parseDouble(editPoids.getText().toString().trim());
        }




        class AjoutNote extends AsyncTask<Void, Void, Void> {

            //Ajout dans la BDD de la note
            @Override
            protected Void doInBackground(Void... voids) {

                Note note = new Note(sVal,sType,sDate,matiere.getId());
                note.setCommentaire(sCom);
                note.setPoids(sPoids);

                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .noteDAO()
                        .insert(note);


                return null;
            }

            // Actualisation et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
                intent.putExtra("matiere",matiere);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Note ajoutée", Toast.LENGTH_LONG).show();
            }
        }

        AjoutNote an = new AjoutNote();
        an.execute();

    }

    // Si le bouton précédent est cliqué
    public void onBackPressed() {
        finish();

        Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
        intent.putExtra("matiere", matiere);
        startActivity(intent);

    }
}
