package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.model.Matiere;

import java.util.ArrayList;

public class AjoutMatiereActivity extends AppCompatActivity {

    private EditText editNom, editCoef;
    private CheckBox editMoyPond;
    private Spinner s;
    private ArrayList<Integer> arraySpinner;
    private int idAnnee;
    private int nbPeriode;
    private int periodeSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_matiere);

        editNom = findViewById(R.id.editNomMatiere);
        editCoef = findViewById(R.id.editCoefMatiere);
        editMoyPond = findViewById(R.id.editMoyPond);
        s = findViewById(R.id.selectPeriode);


        SharedPreferences prefs = getSharedPreferences("annee_active", MODE_PRIVATE);
        idAnnee = prefs.getInt("id", 0);//"No name defined" is the default value.
        nbPeriode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.


        // Création de la liste déroulante pour naviguer entre les périodes
        arraySpinner = new ArrayList<Integer>();
        for(int compt=1;compt <= nbPeriode;compt++){
            arraySpinner.add(compt);
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);/*
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                periodeSelect = position++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
*/
        Button boutonAnnuler = (Button)findViewById(R.id.bouton_annuler);
        boutonAnnuler.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });


        Button boutonValiderAjouter = (Button) findViewById(R.id.bouton_val_ajouter);
        boutonValiderAjouter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(AjoutMatiereActivity.class.getSimpleName(), "Bouton ajouter cliqué");

                    ajouterMatiere();
                }
        });

    }

    private void ajouterMatiere(){

        if (editNom.getText().toString().isEmpty()) {
            editNom.setError("Un nom pour désigner la matière est requis");
            editNom.requestFocus();
            return;
        }
        if (Integer.parseInt(editCoef.getText().toString().trim()) < 1){
            editCoef.setError("Le coefficient doit être au minimum à 1");
            editCoef.requestFocus();
            return;
        }
        if (editCoef.getText().toString().isEmpty()){
            editCoef.setError("Un coefficient est requis");
            editCoef.requestFocus();
            return;
        }


        final String sNom = editNom.getText().toString().trim();
        final int sCoef = Integer.parseInt(editCoef.getText().toString().trim());
        final boolean sMoyPond = editMoyPond.isChecked();
        final int sPeriode = (int) s.getSelectedItem();

        class AjoutMatiere extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Matiere matiere = new Matiere(idAnnee,sPeriode,sNom,sCoef,sMoyPond);

                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .matiereDAO()
                        .insert(matiere);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(getApplicationContext(), "Matière ajoutée", Toast.LENGTH_LONG).show();
            }
        }

        AjoutMatiere am = new AjoutMatiere();
        am.execute();

        finish();

    }
}
