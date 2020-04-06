package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.adapter.AnneeAdapter;
import com.jennifertestu.coeffapp.adapter.MatiereAdapter;
import com.jennifertestu.coeffapp.model.Annee;

import java.util.List;

public class AnneeActivity extends AppCompatActivity {

    private EditText editNom, editNb;

    private RecyclerView recyclerView;
    private Annee anneeActive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annee);

        // Emplacement de la liste des matieres
        recyclerView = findViewById(R.id.list_annees_view);

        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = getSharedPreferences("annee_active", MODE_PRIVATE);
        int id = prefs.getInt("id", 0);//"No name defined" is the default value.
        String nom = prefs.getString("nom", "Pas de nom"); //0 is the default value.
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.
        anneeActive = new Annee(nom,nbperiode);
        anneeActive.setId(id);

        // Appel de la fonction pour la création du menu pour naviguer entre les années
        creationBoutonsMenu();
        // Appel de la fonction pour les boutons du menu
        autresBoutonsMenu();

        // Création du bouton pour ajouter une matière
        Button boutonAjouter = (Button)findViewById(R.id.bouton_ajouter);
        boutonAjouter.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupAjout();

            }
        });

    }

    private void creationBoutonsMenu(){

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);
        final SubMenu menu = navView.getMenu().getItem(0).getSubMenu();

        class LesAnnees extends AsyncTask<Void, Void, List<Annee>> {

            @Override
            protected List<Annee> doInBackground(Void... voids) {
                List<Annee> anneeList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .anneeDAO()
                        .getAll();

                return anneeList;
            }

            @Override
            protected void onPostExecute(List<Annee> annees) {
                super.onPostExecute(annees);

                AnneeAdapter adapter = new AnneeAdapter(AnneeActivity.this,getApplicationContext(),annees);
                recyclerView.setAdapter(adapter);

                for(final Annee a : annees) {
                    MenuItem item = menu.add(a.getNom());
                    if(a.getId()==anneeActive.getId()) {
                        item.setIcon(R.drawable.ic_check_circle);
                    }
                    item.setOnMenuItemClickListener (new MenuItem.OnMenuItemClickListener(){
                        @Override
                        public boolean onMenuItemClick (MenuItem item){

                            // Mise a jour de l'année active
                            anneeActive = a;

                            SharedPreferences.Editor editor = getSharedPreferences("annee_active", MODE_PRIVATE).edit();
                            editor.putInt("id", a.getId());
                            editor.putString("nom", a.getNom());
                            editor.putInt("nbperiode", a.getNbPeriodes());
                            editor.apply();

                            // Rafraichir l'activité
                            finish();
                            startActivity(getIntent());
                            return true;
                        }
                    });
                }


            }
        }


        LesAnnees la = new LesAnnees();
        la.execute();


    }

    private void autresBoutonsMenu(){

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();

        MenuItem boutonGererAnnees = menu.findItem(R.id.nav_annee);
        MenuItem boutonPartager = menu.findItem(R.id.nav_share);
        MenuItem boutonEvaluer = menu.findItem(R.id.nav_eval);
        MenuItem boutonAide = menu.findItem(R.id.nav_help);

        boutonGererAnnees.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });

        boutonPartager.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String lien = "https://play.google.com";

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'utilise cette application pour calculer mes notes :  "+lien);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                return false;
            }
        });

        boutonEvaluer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse("https://play.google.com"));
                    startActivity(viewIntent);
                }catch(Exception e) {
                    Toast.makeText(getApplicationContext(),"Impossible d'accèder à la page, essayez plus tard ...",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                return false;
            }
        });

        boutonAide.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
    }

    private void popupAjout(){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        editNom = (EditText) dialogView.findViewById(R.id.editNomAnnee);
        editNb = (EditText) dialogView.findViewById(R.id.editNb);

        Button buttonAjout = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button buttonAnnule = (Button) dialogView.findViewById(R.id.buttonCancel);

        buttonAnnule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        buttonAjout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ajouterAnnee();
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }


    private void ajouterAnnee(){

        if (editNom.getText().toString().isEmpty()) {
            editNom.setError("Un nom pour désigner l'année est requis");
            editNom.requestFocus();
            return;
        }
        if (Integer.parseInt(editNb.getText().toString().trim()) < 1){
            editNb.setError("Le nombre de période doit être au minimum à 1");
            editNb.requestFocus();
            return;
        }
        if (editNb.getText().toString().isEmpty()){
            editNb.setError("Un nombre est requis");
            editNb.requestFocus();
            return;
        }


        final String sNom = editNom.getText().toString().trim();
        final int sNb = Integer.parseInt(editNb.getText().toString().trim());


        class AjoutAnnee extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Annee annee = new Annee(sNom,sNb);

                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .anneeDAO()
                        .insert(annee);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                startActivity(new Intent(getApplicationContext(), AnneeActivity.class));
                Toast.makeText(getApplicationContext(), "Année ajoutée", Toast.LENGTH_LONG).show();
            }
        }

        AjoutAnnee am = new AjoutAnnee();
        am.execute();

    }
}
