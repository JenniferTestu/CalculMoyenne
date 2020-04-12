package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.model.Matiere;
import com.jennifertestu.coeffapp.model.Note;
import com.jennifertestu.coeffapp.model.TypeDeNote;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class UpdateNoteActivity extends AppCompatActivity {

    private EditText editNote, editCom, editPoids;
    private TextView textPoids;
    private Spinner editType;
    private Button pickDate;
    private int mYear,mMonth,mDay;
    private Date sDate;
    private Note noteUpdate;

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

        ArrayAdapter<TypeDeNote> adapter = new ArrayAdapter<TypeDeNote>(this, android.R.layout.simple_spinner_item, TypeDeNote.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editType.setAdapter(adapter);

        noteUpdate = (Note) getIntent().getExtras().getSerializable("noteUpdate");

        // Bouton pour annuler l'edition et retourner à la liste
        Button boutonAnnuler = (Button)findViewById(R.id.bouton_annuler);
        boutonAnnuler.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
                startActivity(intent);
            }
        });

        if(matiere.isMoyPond()==false){
            textPoids.setVisibility(View.INVISIBLE);
            editPoids.setVisibility(View.INVISIBLE);
        }

        editNote.setText(Double.toString(noteUpdate.getValeur()));
        editType.setSelection(adapter.getPosition(noteUpdate.getTypeDeNote()));
        editCom.setText(noteUpdate.getCommentaire());
        editPoids.setText(Integer.toString(noteUpdate.getPoids()));

        // Bouton pour l'edition
        Button boutonValiderMaj = (Button)findViewById(R.id.bouton_val_ajouter);
        boutonValiderMaj.setText("Modifier");
        boutonValiderMaj.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(AjoutMatiereActivity.class.getSimpleName(),"Bouton ajouter cliqué");

                updateNote();

            }
        });

        pickDate = (Button) findViewById(R.id.pick_date);

        final Calendar c = Calendar.getInstance();
        c.setTime(noteUpdate.getDate());
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        pickDate.setText(mDay + " / " + (mMonth + 1) + " / " + mYear);

        pickDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Locale.setDefault(Locale.FRANCE);

                // Launch Date Picker Dialog
                DatePickerDialog dpd = new DatePickerDialog(UpdateNoteActivity.this,R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                pickDate.setText(dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);
                                sDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
                                noteUpdate.setDate(sDate);
                            }
                        }, mYear, mMonth, mDay);
                dpd.show();

            }
        });


    }

    // Tache d'edition d'une note
    private void updateNote(){

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
        if (matiere.isMoyPond()==true && Integer.parseInt(editPoids.getText().toString().trim()) <1) {
            editPoids.setError("Le poids doit être au minimum à 1");
            editPoids.requestFocus();
            return;
        }


        final double sVal = Double.parseDouble(editNote.getText().toString().trim());
        final TypeDeNote sType = TypeDeNote.valueOf(editType.getSelectedItem().toString().trim());
        final String sCom = editCom.getText().toString().trim();
        final int sPoids;

        if(matiere.isMoyPond()==false){
            sPoids = 1;
        }else {
            sPoids = Integer.parseInt(editPoids.getText().toString().trim());
        }


        class UpdateNote extends AsyncTask<Void, Void, Void> {

            //Edition dans la BDD de la note
            @Override
            protected Void doInBackground(Void... voids) {

                noteUpdate.setValeur(sVal);
                noteUpdate.setTypeDeNote(sType);
                noteUpdate.setCommentaire(sCom);
                noteUpdate.setPoids(sPoids);

                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .noteDAO()
                        .update(noteUpdate);

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
                Toast.makeText(getApplicationContext(), "Note modifiée", Toast.LENGTH_LONG).show();
            }
        }

        UpdateNote un = new UpdateNote();
        un.execute();

    }


}
