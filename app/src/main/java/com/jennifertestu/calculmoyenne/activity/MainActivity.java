package com.jennifertestu.calculmoyenne.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jennifertestu.calculmoyenne.DatabaseClient;
import com.jennifertestu.calculmoyenne.R;
import com.jennifertestu.calculmoyenne.adapter.MatiereAdapter;
import com.jennifertestu.calculmoyenne.adapter.ModuleMatiereAdapter;
import com.jennifertestu.calculmoyenne.model.*;
import com.jennifertestu.calculmoyenne.ui.MenuNav;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText editNom;
    private Spinner editNb;
    private RecyclerView recyclerView;
    private Button button;
    private DecimalFormat df = new DecimalFormat("#.##");
    private double moyG = 0.0;
    private int coefs = 0;
    private ArrayList<String> arraySpinner = new ArrayList<String>();
    private Annee anneeActive;
    private int periodeSelect;
    private MenuNav menuNav;
    private ModuleMatiereAdapter adapter;
    private MatiereAdapter adapterMatiere;
    private static ExpandableListView expandableListView;
    private boolean isModule;


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


        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = getSharedPreferences("annee_active", MODE_PRIVATE);
        int id = prefs.getInt("id", 0);//"No name defined" is the default value.
        String nom = prefs.getString("nom", "Pas de nom"); //0 is the default value.
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.
        isModule = prefs.getBoolean("ismodule", false);//"No name defined" is the default value.

        anneeActive = new Annee(nom,nbperiode,isModule);
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
                if(isModule==false) {
                    lesMatieres();
                }else{
                    lesModules();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        // Appel de la fonction pour la création du menu pour naviguer entre les années
        // Appel de la fonction pour l'affichage des matières de l'année en cours
        if(isModule==false) {
            lesMatieres();
        }else{
            lesModules();
        }
        // Appel de la fonction pour les boutons du menu
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        menuNav = new MenuNav(getApplicationContext(),navView);
        menuNav.creer();


        // Création du bouton pour ajouter une matière
        Button boutonAjouter = (Button)findViewById(R.id.bouton_ajouter);
        boutonAjouter.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isModule==true){
                    popupChoix();
                }else {
                    finish();
                    Intent activityAjoutMatiere = new Intent(getApplicationContext(), AjoutMatiereActivity.class);
                    startActivity(activityAjoutMatiere);
                }

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

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onPostExecute(List<Matiere> matieres) {
                super.onPostExecute(matieres);

                optionTri(recyclerView,matieres);

                adapterMatiere = new MatiereAdapter(MainActivity.this,getApplicationContext(),matieres,button);

                if(periodeSelect==0){
                    adapterMatiere.setToutesPeriodes(true);
                }else{
                    adapterMatiere.setToutesPeriodes(false);
                }

                recyclerView.setAdapter(adapterMatiere);


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

    private void lesModules() {
        class LesModules extends AsyncTask<Void, Void, List<Module>> {

            @Override
            protected List<Module> doInBackground(Void... voids) {
                List<Module> modulesList;
                if(periodeSelect==0) {
                    modulesList = DatabaseClient
                            .getInstance(getApplicationContext())
                            .getAppDatabase()
                            .moduleDAO()
                            .getAllByAnnee(anneeActive.getId());
                }else {
                    modulesList = DatabaseClient
                            .getInstance(getApplicationContext())
                            .getAppDatabase()
                            .moduleDAO()
                            .getAllByAnneeAndPeriode(anneeActive.getId(),periodeSelect);
                }

                for(Module mo : modulesList) {

                    List<Matiere> matieresList = DatabaseClient
                            .getInstance(getApplicationContext())
                            .getAppDatabase()
                            .matiereDAO()
                            .getAllByModule(mo.getId());

                    for(Matiere ma : matieresList) {
                        List<Note> notesList = DatabaseClient
                                .getInstance(getApplicationContext())
                                .getAppDatabase()
                                .noteDAO()
                                .getAllByMatiere(ma.getId());
                        ma.setListeNotes(notesList);
                    }

                    mo.setListeMatieres(matieresList);
                    mo.calculerMoy();
                }

                moyG = 0.0;
                coefs = 0;

                for(Module mo : modulesList) {
                    Double moyMod = mo.getMoy();
                    if(moyMod!=-1.0 && !moyMod.isNaN()) {
                        moyG += mo.getMoy()*mo.getSumCoef();
                        coefs += mo.getSumCoef();
                    }
                }

                moyG = moyG / coefs;

                return modulesList;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onPostExecute(List<Module> modules) {
                super.onPostExecute(modules);


                recyclerView.setVisibility(View.GONE);
                adapter = new ModuleMatiereAdapter(MainActivity.this,getApplicationContext(),modules,button);

                if(periodeSelect==0){
                    adapter.setToutesPeriodes(true);
                }else{
                    adapter.setToutesPeriodes(false);
                }
                optionTriModule(adapter,modules);

                expandableListView = (ExpandableListView) findViewById(R.id.simple_expandable_listview);
                expandableListView.setGroupIndicator(null);
                expandableListView.setAdapter(adapter);


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


        LesModules lm = new LesModules();
        lm.execute();
    }

    // Dans le cas où on presse le bouton précédent
    public void onRestart() {
        super.onRestart();
        if(isModule==false) {
            lesMatieres();
        }else{
            lesModules();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void optionTri(final RecyclerView adapter, final List<Matiere> liste) {

        Button button_tri = findViewById(R.id.bouton_tri);
        button_tri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une matiere
                PopupMenu popup = new PopupMenu(button.getContext(), view);
                //Ajout du fichier XML contenant le menu
                popup.inflate(R.menu.option_tri);
                //Ajout de l'écoute du clique
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tri_notes_croissants:
                                Collections.sort(liste, new Comparator<Matiere>() {
                                    @Override
                                    public int compare(Matiere o1, Matiere o2) {
                                        return Double.compare(o1.getMoy(),o2.getMoy());
                                    }
                                });
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_notes_decroissants:
                                Collections.sort(liste, new Comparator<Matiere>() {
                                    @Override
                                    public int compare(Matiere o1, Matiere o2) {
                                        return Double.compare(o1.getMoy(),o2.getMoy());
                                    }
                                });
                                Collections.reverse(liste);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_coefs_croissants:
                                Collections.sort(liste, new Comparator<Matiere>() {
                                    @Override
                                    public int compare(Matiere o1, Matiere o2) {
                                        return Double.compare(o1.getCoef(),o2.getCoef());
                                    }
                                });
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_coefs_decroissants:
                                Collections.sort(liste, new Comparator<Matiere>() {
                                    @Override
                                    public int compare(Matiere o1, Matiere o2) {
                                        return Double.compare(o1.getCoef(),o2.getCoef());
                                    }
                                });
                                Collections.reverse(liste);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_ancien_recent:
                                Collections.sort(liste);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_recent_ancien:
                                Collections.sort(liste);
                                Collections.reverse(liste);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_alph:
                                Collections.sort(liste, new Comparator<Matiere>() {
                                    @Override
                                    public int compare(Matiere o1, Matiere o2) {
                                        return o1.getNom().compareToIgnoreCase(o2.getNom());
                                    }
                                });
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //Affichage du menu
                popup.show();

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void optionTriModule(final BaseExpandableListAdapter expandableListViewAdapter, final List<Module> liste) {

        Button button_tri = findViewById(R.id.bouton_tri);
        button_tri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une matiere
                final PopupMenu popup = new PopupMenu(button.getContext(), view);
                //Ajout du fichier XML contenant le menu
                popup.inflate(R.menu.option_tri);
                //Ajout de l'écoute du clique
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tri_notes_croissants:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres(), new Comparator<Matiere>() {
                                        @Override
                                        public int compare(Matiere o1, Matiere o2) {
                                            return Double.compare(o1.getMoy(), o2.getMoy());
                                        }
                                    });
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.tri_notes_decroissants:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres(), new Comparator<Matiere>() {
                                        @Override
                                        public int compare(Matiere o1, Matiere o2) {
                                            return Double.compare(o1.getMoy(), o2.getMoy());
                                        }
                                    });
                                    Collections.reverse(m.getListeMatieres());
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.tri_coefs_croissants:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres(), new Comparator<Matiere>() {
                                        @Override
                                        public int compare(Matiere o1, Matiere o2) {
                                            return Double.compare(o1.getCoef(), o2.getCoef());
                                        }
                                    });
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.tri_coefs_decroissants:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres(), new Comparator<Matiere>() {
                                        @Override
                                        public int compare(Matiere o1, Matiere o2) {
                                            return Double.compare(o1.getCoef(), o2.getCoef());
                                        }
                                    });
                                    Collections.reverse(m.getListeMatieres());
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.tri_ancien_recent:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres());
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.tri_recent_ancien:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres());
                                    Collections.reverse(m.getListeMatieres());
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.tri_alph:
                                for (Module m:liste) {
                                    Collections.sort(m.getListeMatieres(), new Comparator<Matiere>() {
                                        @Override
                                        public int compare(Matiere o1, Matiere o2) {
                                            return o1.getNom().compareToIgnoreCase(o2.getNom());
                                        }
                                    });
                                }
                                expandableListViewAdapter.notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //Affichage du menu
                popup.show();

            }
        });
    }

    // Popup pour demander si on ajoute un module ou une matiere
    private void popupChoix(){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.choix_dialog, null);

        TextView tv = (TextView) dialogView.findViewById(R.id.textView);
        tv.setText("Que voulez-vous ajouter ?");

        Button buttonModule = (Button) dialogView.findViewById(R.id.buttonModule);
        Button buttonMatiere = (Button) dialogView.findViewById(R.id.buttonMatiere);

        buttonModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
                popupAjoutModule();
            }
        });
        buttonMatiere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
                finish();
                Intent activityAjoutMatiere = new Intent(getApplicationContext(), AjoutMatiereActivity.class);
                startActivity(activityAjoutMatiere);

            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    private void popupAjoutModule(){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.ajout_module_dialog, null);

        editNom = (EditText) dialogView.findViewById(R.id.editNomModule);
        editNb = (Spinner) dialogView.findViewById(R.id.editNb);

        ArrayList arraySpinner = new ArrayList<Integer>();
        for (int compt = 1; compt <= anneeActive.getNbPeriodes(); compt++) {
            arraySpinner.add(compt);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editNb.setAdapter(adapter);

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

                ajouterModule();
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }


    private void ajouterModule(){

        if (editNom.getText().toString().isEmpty()) {
            editNom.setError("Un nom pour désigner le module est requis");
            editNom.requestFocus();
            return;
        }


        final String sNom = editNom.getText().toString().trim();
        final int sNb = (int) editNb.getSelectedItem();


        class AjoutModule extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Module module = new Module(sNom,anneeActive.getId(),sNb);

                //adding to database
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .moduleDAO()
                        .insert(module);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(getApplicationContext(), "Module ajouté", Toast.LENGTH_LONG).show();
            }
        }

        AjoutModule am = new AjoutModule();
        am.execute();

    }
}
