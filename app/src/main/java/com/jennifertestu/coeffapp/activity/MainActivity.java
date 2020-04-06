package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.adapter.MatiereAdapter;
import com.jennifertestu.coeffapp.model.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button button;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Double moyG = 0.0;
    private int coefs = 0;
    private ArrayList<String> arraySpinner = new ArrayList<String>();
    private Annee anneeActive;
    private int periodeSelect;

    // A la création de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Définition de l'arrondi de la moyenne générale
        df.setRoundingMode(RoundingMode.DOWN);

        // Emplacement de la liste des matieres
        recyclerView = findViewById(R.id.list_matieres_view);
        // Emplacement de la moyenne générale
        button = findViewById(R.id.button);



/*
        Annee a1 = new Annee("Terminal",3);
        a1.setActif(false);
        Annee a2 = new Annee("L1",2);
        a2.setActif(false);

        DatabaseClient
                .getInstance(getApplicationContext())
                .getAppDatabase()
                .anneeDAO()
                .insert(a1);

        DatabaseClient
                .getInstance(getApplicationContext())
                .getAppDatabase()
                .anneeDAO()
                .insert(a2);
*/

        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = getSharedPreferences("annee_active", MODE_PRIVATE);
        int id = prefs.getInt("id", 0);//"No name defined" is the default value.
        String nom = prefs.getString("nom", "Pas de nom"); //0 is the default value.
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.
        anneeActive = new Annee(nom,nbperiode);
        anneeActive.setId(id);

        // Création de la liste déroulante pour naviguer entre les périodes
        arraySpinner.add("Toutes périodes");
        for(int compt=1;compt <= anneeActive.getNbPeriodes();compt++){
           arraySpinner.add("Période "+compt);
        }

        Spinner s = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                periodeSelect = position;
                Log.e("Num période", String.valueOf(periodeSelect));
                lesMatieres();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        // Appel de la fonction pour la création du menu pour naviguer entre les années
        creationBoutonsMenu();
        // Appel de la fonction pour l'affichage des matières de l'année en cours
        lesMatieres();
        // Appel de la fonction pour les boutons du menu
        autresBoutonsMenu();

        // Création du bouton pour ajouter une matière
        Button boutonAjouter = (Button)findViewById(R.id.bouton_ajouter);
        boutonAjouter.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent activityAjoutMatiere = new Intent(getApplicationContext(), AjoutMatiereActivity.class);
                startActivity(activityAjoutMatiere);

            }
        });

        // Création du bouton pour pour partager la moyenne
        Button boutonPartage = (Button)findViewById(R.id.bouton_share_moy);
        boutonPartage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai une moyenne de "+button.getText());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);

            }
        });
    }

    private void lesMatieres() {
        class LesMatieres extends AsyncTask<Void, Void, List<Matiere>> {

            @Override
            protected List<Matiere> doInBackground(Void... voids) {
                List<Matiere> matiereList;
                if(periodeSelect==0) {
                    matiereList = DatabaseClient
                            .getInstance(getApplicationContext())
                            .getAppDatabase()
                            .matiereDAO()
                            .getAllByAnnee(anneeActive.getId());
                }else {
                    matiereList = DatabaseClient
                            .getInstance(getApplicationContext())
                            .getAppDatabase()
                            .matiereDAO()
                            .getAllByAnneeAndPeriode(anneeActive.getId(),periodeSelect);
                }

                for(Matiere m : matiereList) {
                    Log.i("Matiere de la liste",m.toString());
                    List<Note> notesList = DatabaseClient
                            .getInstance(getApplicationContext())
                            .getAppDatabase()
                            .noteDAO()
                            .getAllByMatiere(m.getId());
                    m.setListeNotes(notesList);
                }

                moyG = 0.0;
                coefs = 0;

                for(Matiere m : matiereList) {
                    if(!m.getListeNotes().isEmpty()) {
                        moyG += m.calculerMoy() * m.getCoef();
                        coefs += m.getCoef();
                    }
                }

                moyG = moyG / coefs;

                return matiereList;
            }

            @Override
            protected void onPostExecute(List<Matiere> matieres) {
                super.onPostExecute(matieres);
                MatiereAdapter adapter = new MatiereAdapter(getApplicationContext(),matieres);
                recyclerView.setAdapter(adapter);

                Log.e("moyenne",moyG.toString());


                if(moyG >= 10 && moyG < 12){
                    button.setText(df.format(moyG));
                    button.setBackgroundResource(R.drawable.round_orange);
                }else if (moyG >= 0 && moyG < 10){
                    button.setText(df.format(moyG));
                    button.setBackgroundResource(R.drawable.round_red);
                }else if(moyG >= 12){
                    button.setText(df.format(moyG));
                    button.setBackgroundResource(R.drawable.round_blue);
                }else {
                    button.setText("/");
                    button.setBackgroundResource(R.drawable.round_blue);
                }

            }
        }


        LesMatieres lm = new LesMatieres();
        lm.execute();
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

                finish();

                Intent activityAnnee = new Intent(getApplicationContext(), AnneeActivity.class);
                startActivity(activityAnnee);

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
}
