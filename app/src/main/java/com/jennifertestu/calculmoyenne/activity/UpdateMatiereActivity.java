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

public class UpdateMatiereActivity extends AppCompatActivity {

    private EditText editNom, editCoef;
    private CheckBox editMoyPond;
    private Spinner s;
    private ArrayList<Integer> arraySpinner;
    private int idAnnee;
    private int nbPeriode;
    private int periodeSelect;
    private Matiere matiereUpdate;
    private Boolean isModule;
    private ArrayList<Module> arraySpinnerModule;
    private ArrayAdapter<Integer> adapter;
    private ArrayAdapter<Module> adapterModule;
    private Module currentModule;

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


        matiereUpdate = (Matiere) getIntent().getExtras().getSerializable("matiereUpdate");

        // Création de la liste déroulante pour naviguer entre les périodes ou modules
        if(isModule==false) {
            arraySpinner = new ArrayList<Integer>();
            for (int compt = 1; compt <= nbPeriode; compt++) {
                arraySpinner.add(compt);
            }
            adapter = new ArrayAdapter<Integer>(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(adapter);
        }else{
            currentModule = DatabaseClient
                    .getInstance(getApplicationContext())
                    .getAppDatabase()
                    .moduleDAO()
                    .getById(matiereUpdate.getIdModule());

            arraySpinnerModule = (ArrayList<Module>) DatabaseClient
                    .getInstance(getApplicationContext())
                    .getAppDatabase()
                    .moduleDAO()
                    .getAllByAnnee(idAnnee);
            adapterModule = new ArrayAdapter<Module>(this,
                    android.R.layout.simple_spinner_item, arraySpinnerModule);
            adapterModule.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(adapterModule);

            TextView tv = findViewById(R.id.textPeriode);
            tv.setText("Matière rattachée au module : ");
        }


        // Bouton pour annuler l'edition et retourner à la liste
        Button boutonAnnuler = (Button)findViewById(R.id.bouton_annuler);
        boutonAnnuler.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });

        // Remplir les champs
        editNom.setText(matiereUpdate.getNom());
        editCoef.setText(Integer.toString(matiereUpdate.getCoef()));
        if(matiereUpdate.isMoyPond()==true) {
            editMoyPond.setChecked(true);
        }
        if(isModule){
            s.setSelection(adapterModule.getPosition(currentModule));
        }else {
            s.setSelection(adapter.getPosition(matiereUpdate.getPeriode()));
        }

        // Bouton pour l'edition
        Button boutonValiderMaj = (Button) findViewById(R.id.bouton_val_ajouter);
        boutonValiderMaj.setText("Modifier");
        boutonValiderMaj.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    updateMatiere();
                }
            });

    }

    // Tache d'edition d'une matiere
    private void updateMatiere(){
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
        int sPeriode = 1;
        Module sModule = null;

        if(isModule==false) {
            sPeriode = (int) s.getSelectedItem();
        }else{
            sModule = (Module) s.getSelectedItem();
        }

        final int finalSPeriode = sPeriode;
        final Module finalSModule = sModule;
        class UpdateMatiere extends AsyncTask<Void, Void, Void> {

            //Edition dans la BDD de la matiere
            @Override
            protected Void doInBackground(Void... voids) {

                matiereUpdate.setNom(sNom);
                matiereUpdate.setCoef(sCoef);
                matiereUpdate.setMoyPond(sMoyPond);

                if(isModule==false) {
                    matiereUpdate.setPeriode(finalSPeriode);
                }else {
                    matiereUpdate.setIdModule(finalSModule.getId());
                }

                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .matiereDAO()
                        .update(matiereUpdate);
                return null;
            }

            // Actualisation et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(getApplicationContext(), "Matière modifiée", Toast.LENGTH_LONG).show();
            }
        }

        UpdateMatiere um = new UpdateMatiere();
        um.execute();

    }

    // Si le bouton précédent est cliqué
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
