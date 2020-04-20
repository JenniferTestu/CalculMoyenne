package com.jennifertestu.calculmoyenne.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jennifertestu.calculmoyenne.DatabaseClient;
import com.jennifertestu.calculmoyenne.R;
import com.jennifertestu.calculmoyenne.model.Matiere;
import com.jennifertestu.calculmoyenne.model.Module;

import java.util.ArrayList;

public class AjoutMatiereActivity extends AppCompatActivity {

    private EditText editNom, editCoef;
    private CheckBox editMoyPond;
    private Spinner s;
    private ArrayList<Integer> arraySpinner;
    private int idAnnee;
    private int nbPeriode;
    private boolean isModule;
    private int periodeSelect;
    private ArrayList<Module> arraySpinnerModule;


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
        isModule = prefs.getBoolean("ismodule", false);//"No name defined" is the default value.


        // Création de la liste déroulante pour naviguer entre les périodes ou modules
        if(isModule==false) {
            arraySpinner = new ArrayList<Integer>();
            for (int compt = 1; compt <= nbPeriode; compt++) {
                arraySpinner.add(compt);
            }
            ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(adapter);
        }else{
            arraySpinnerModule = (ArrayList<Module>) DatabaseClient
                    .getInstance(getApplicationContext())
                    .getAppDatabase()
                    .moduleDAO()
                    .getAllByAnnee(idAnnee);
            ArrayAdapter<Module> adapter = new ArrayAdapter<Module>(this,
                    android.R.layout.simple_spinner_item, arraySpinnerModule);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(adapter);

            TextView tv = findViewById(R.id.textPeriode);
            tv.setText("Matière rattachée au module : ");
        }



        // Bouton pour annuler l'ajout et retourner à la liste
        Button boutonAnnuler = (Button)findViewById(R.id.bouton_annuler);
        boutonAnnuler.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });

        // Bouton pour l'ajout
        Button boutonValiderAjouter = (Button) findViewById(R.id.bouton_val_ajouter);
        boutonValiderAjouter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ajouterMatiere();
                }
        });

    }

    // Tache d'ajout d'une matiere
    private void ajouterMatiere(){

        if (editNom.getText().toString().isEmpty()) {
            editNom.setError("Un nom pour désigner la matière est requis");
            editNom.requestFocus();
            return;
        }
        if (Double.parseDouble(editCoef.getText().toString().trim()) < 1){
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
        final double sCoef = Double.parseDouble(editCoef.getText().toString().trim());
        final boolean sMoyPond = editMoyPond.isChecked();
        int sPeriode = 1;
        Module sModule = null;

        if(isModule==false) {
            sPeriode = (int) s.getSelectedItem();
        }else{
            sModule = (Module) s.getSelectedItem();
        }

        final int finalSPeriode = sPeriode;
        final Module finalSModule = sModule;
        class AjoutMatiere extends AsyncTask<Void, Void, Void> {

            //Ajout dans la BDD de la matiere
            @Override
            protected Void doInBackground(Void... voids) {

                Matiere matiere = new Matiere(idAnnee, finalSPeriode,sNom,sCoef,sMoyPond);

                if(isModule==true){
                    matiere.setIdModule(finalSModule.getId());
                }

                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .matiereDAO()
                        .insert(matiere);
                return null;
            }

            // Actualisation et message de confirmation
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


    }

    // Si le bouton précédent est cliqué
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
