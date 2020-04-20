package com.jennifertestu.calculmoyenne.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jennifertestu.calculmoyenne.DatabaseClient;
import com.jennifertestu.calculmoyenne.activity.MainActivity;
import com.jennifertestu.calculmoyenne.activity.NotesActivity;
import com.jennifertestu.calculmoyenne.R;
import com.jennifertestu.calculmoyenne.activity.UpdateMatiereActivity;
import com.jennifertestu.calculmoyenne.model.Matiere;
import com.jennifertestu.calculmoyenne.model.Module;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

// Permet l'affichage d'une liste de matieres avec ses infos
public class ModuleMatiereAdapter extends BaseExpandableListAdapter {

    private Activity activity;
    private Context mCtx;
    private List<Module> modulesList;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Button button;
    private Boolean toutesPeriodes=false;
    private EditText editNom;
    private Spinner editNb;

    public ModuleMatiereAdapter(Activity activity, Context mCtx, List<Module> modulesList, Button button) {
        this.activity=activity;
        this.mCtx = mCtx;
        this.modulesList = modulesList;
        this.button=button;
    }

    public void setToutesPeriodes(Boolean toutesPeriodes) {
        this.toutesPeriodes = toutesPeriodes;
    }


    @Override
    public int getGroupCount() {
        // Taille de la liste des modules
        return this.modulesList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // Nombre de matières
        return this.modulesList.get(groupPosition).getListeMatieres().size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        // Position du module
        return this.modulesList.get(groupPosition);
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // Retourner la matière
        return this.modulesList.get(groupPosition).getListeMatieres().get(childPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    // Création du groupe
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        final Module entete = (Module) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, parent, false);
        }

        TextView header_text = (TextView) convertView.findViewById(R.id.header);
        if(toutesPeriodes==true){
            header_text.setText(entete.getNom()+" (Période "+entete.getPeriode()+")");
        }else{
            header_text.setText(entete.getNom());
        }

        df.setRoundingMode(RoundingMode.DOWN);

        Button buttonMoyModule = (Button) convertView.findViewById(R.id.rond);
        Double moyM = entete.calculerMoy();

        if(moyM >= 10 && moyM < 12){
            buttonMoyModule.setText(df.format(moyM));
            buttonMoyModule.setBackgroundResource(R.drawable.round_orange);
        }else if (moyM >= 0 && moyM < 10){
            buttonMoyModule.setText(df.format(moyM));
            buttonMoyModule.setBackgroundResource(R.drawable.round_red);
        }else if(moyM >= 12){
            buttonMoyModule.setText(df.format(moyM));
            buttonMoyModule.setBackgroundResource(R.drawable.round_blue);
        }else {
            buttonMoyModule.setText("/");
            buttonMoyModule.setBackgroundResource(R.drawable.round_blue);
        }


        // Création du bouton affichant les actions possibles sur cet élément
        final TextView buttonModuleption = convertView.findViewById(R.id.moduleOptions);
        buttonModuleption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une matiere
                PopupMenu popup = new PopupMenu(buttonModuleption.getContext(), view);
                //Ajout du fichier XML contenant le menu
                popup.inflate(R.menu.options_menu);
                //Ajout de l'écoute du clique
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuEdit:
                                //Si le choix est d'éditer
                                popupUpdateModule(entete,activity);
                                return true;
                            case R.id.menuPartage:
                                //Si le choix est de partager
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai une moyenne de "+entete.getMoy()+" dans le module "+entete.getNom());
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(shareIntent);
                                return true;
                            case R.id.menuSupp:
                                //Si le choix est de supprimer
                                popupSuppModule(entete,activity);
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

        // Si le groupe est développé on change l'icone
        if (isExpanded) {
            header_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, (android.R.drawable.arrow_up_float), 0);
        } else {
            // Si le groupe n'est pas développé on change l'icone
            header_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, (android.R.drawable.arrow_down_float), 0);
        }

        return convertView;
    }


    // Création des enfants
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final Matiere matiere = (Matiere) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.adapter_item, parent, false);
        }

        final TextView nomView, coefView, derDateView, buttonViewOption;
        Button rond;



        nomView = convertView.findViewById(R.id.item_name);
        coefView = convertView.findViewById(R.id.item_coef);
        derDateView = convertView.findViewById(R.id.item_derDate);
        rond = convertView.findViewById(R.id.rond);
        buttonViewOption = convertView.findViewById(R.id.matiereOptions);

        convertView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {

                                               Intent intent = new Intent(mCtx, NotesActivity.class);
                                               intent.putExtra("matiere", matiere);
                                               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                               mCtx.startActivity(intent);
                                           }
        });



        df.setRoundingMode(RoundingMode.DOWN);

        nomView.setText(matiere.getNom());

        coefView.setText("Coefficient : "+Double.toString(matiere.getCoef()));
        if(matiere.getDateDerniereNote()!=null) {
            SimpleDateFormat formater = new SimpleDateFormat("d MMMM yyyy", Locale.FRANCE);
            derDateView.setText("Date de la dernière note : "+formater.format(matiere.getDateDerniereNote()));
        }

        //Double moy = matiere.calculerMoy();
        Double moy = matiere.getMoy();


        if(moy >= 10 && moy < 12){
            rond.setText(df.format(moy));
            rond.setBackgroundResource(R.drawable.round_orange);
        }else if (moy >= 0 && moy < 10){
            rond.setText(df.format(moy));
            rond.setBackgroundResource(R.drawable.round_red);
        }else if(moy >= 12){
            rond.setText(df.format(moy));
            rond.setBackgroundResource(R.drawable.round_blue);
        }else {
            rond.setText("/");
            rond.setBackgroundResource(R.drawable.round_blue);
        }

        // Création du bouton affichant les actions possibles sur cet élément
        buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une matiere
                PopupMenu popup = new PopupMenu(buttonViewOption.getContext(), view);
                //Ajout du fichier XML contenant le menu
                popup.inflate(R.menu.options_menu);
                //Ajout de l'écoute du clique
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuEdit:
                                //Si le choix est d'éditer
                                activity.finish();
                                Intent intent = new Intent(mCtx, UpdateMatiereActivity.class);
                                intent.putExtra("matiereUpdate", matiere);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(intent);
                                return true;
                            case R.id.menuPartage:
                                //Si le choix est de partager
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai une moyenne de "+matiere.getMoy()+" en "+matiere.getNom());
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(shareIntent);
                                return true;
                            case R.id.menuSupp:
                                //Si le choix est de supprimer
                                Matiere selectMatiere = matiere;
                                popupSupp(selectMatiere,activity,groupPosition);
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

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    // Tache de suppression d'une matiere
    private void SuppMatiere(final Matiere m, final int groupPosition){


        class SuppMatiere extends AsyncTask<Void, Void, Void> {

            //Suppression dans la BDD de la matiere ainsi que ses notes
            @Override
            protected Void doInBackground(Void... voids) {

                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .matiereDAO()
                        .delete(m);
                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .noteDAO()
                        .deleteByMatiere(m.getId());
                return null;
            }

            // Actualisation de l'adapter et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                modulesList.get(groupPosition).getListeMatieres().remove(m);

                notifyDataSetChanged();
                Toast.makeText(mCtx, "Matière supprimée", Toast.LENGTH_LONG).show();

                double moyG = 0.0;
                int coefs = 0;

                for(Module mo : modulesList) {
                    Double moyMod = mo.calculerMoy();
                    if(moyMod!=-1.0 && !moyMod.isNaN()) {
                        moyG += mo.getMoy()*mo.getSumCoef();
                        coefs += mo.getSumCoef();
                    }
                }

                moyG = moyG / coefs;

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

        SuppMatiere sm = new SuppMatiere();
        sm.execute();

    }


    // Popup pour demander confirmation avant suppression
    private void popupSupp(final Matiere m, Activity activity, final int groupPosition){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.confirmation_dialog, null);

        TextView tv = (TextView) dialogView.findViewById(R.id.textView);
        tv.setText("Etes-vous sûrs de vouloir surpprimer la matière \""+m.getNom()+"\" ainsi que toutes ses notes ?");

        Button buttonConfirm = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button buttonAnnule = (Button) dialogView.findViewById(R.id.buttonCancel);

        buttonAnnule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SuppMatiere(m,groupPosition);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    // Popup pour demander confirmation avant suppression
    private void popupSuppModule(final Module m, Activity activity){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.confirmation_dialog, null);

        TextView tv = (TextView) dialogView.findViewById(R.id.textView);
        tv.setText("Etes-vous sûrs de vouloir supprimer le module \""+m.getNom()+"\" ainsi que toutes ses matières et ses notes ?");

        Button buttonConfirm = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button buttonAnnule = (Button) dialogView.findViewById(R.id.buttonCancel);

        buttonAnnule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SuppModule(m);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    // Tache de suppression d'un module
    private void SuppModule(final Module m){


        class SuppModule extends AsyncTask<Void, Void, Void> {

            //Supression dans la BDD du module ainsi que ses matieres et ses notes
            @Override
            protected Void doInBackground(Void... voids) {

                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .moduleDAO()
                        .delete(m);
                List<Matiere> listeMatiere = DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .matiereDAO()
                        .getAllByModule(m.getId());

                for(Matiere ma : listeMatiere) {
                    DatabaseClient.getInstance(mCtx).getAppDatabase()
                            .noteDAO()
                            .deleteByMatiere(ma.getId());
                }

                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .matiereDAO()
                        .deleteByModule(m.getId());

                return null;
            }

            // Actualisation de l'adapter et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                modulesList.remove(m);
                notifyDataSetChanged();
                Toast.makeText(mCtx, "Module supprimé", Toast.LENGTH_LONG).show();


                double moyG = 0.0;
                int coefs = 0;

                for(Module mo : modulesList) {
                    Double moyMod = mo.getMoy();
                    if(moyMod!=-1.0 && !moyMod.isNaN()) {
                        moyG += mo.getMoy()*mo.getSumCoef();
                        coefs += mo.getSumCoef();
                    }
                }

                moyG = moyG / coefs;

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

        SuppModule sm = new SuppModule();
        sm.execute();

    }

    // Popup pour actualiser les infos d'une année
    private void popupUpdateModule(final Module m, Activity activity){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.ajout_module_dialog, null);

        editNom = (EditText) dialogView.findViewById(R.id.editNomModule);
        editNom.setText(m.getNom());
        editNb = (Spinner) dialogView.findViewById(R.id.editNb);

        SharedPreferences prefs = activity.getSharedPreferences("annee_active", MODE_PRIVATE);
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.

        ArrayList arraySpinner = new ArrayList<Integer>();
        for (int compt = 1; compt <= nbperiode; compt++) {
            arraySpinner.add(compt);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(mCtx,
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editNb.setAdapter(adapter);
        editNb.setSelection(adapter.getPosition(m.getPeriode()));
        editNb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setTextColor(Color.WHITE); //Change selected text color
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button buttonAjout = (Button) dialogView.findViewById(R.id.buttonSubmit);
        buttonAjout.setText("Modifier");
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

                updaterModule(m);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }


    // Tache d'edition d'un module
    private void updaterModule(final Module m){

        if (editNom.getText().toString().isEmpty()) {
            editNom.setError("Un nom pour désigner le module est requis");
            editNom.requestFocus();
            return;
        }


        final String sNom = editNom.getText().toString().trim();
        final int sNb = (int) editNb.getSelectedItem();


        class UpdaterModule extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //creating a task
                Module module = m;
                module.setNom(sNom);
                module.setPeriode(sNb);

                //adding to database
                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .moduleDAO()
                        .update(module);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                activity.finish();
                Intent i = new Intent(mCtx.getApplicationContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mCtx.startActivity(i);
                Toast.makeText(mCtx.getApplicationContext(), "Module modifié", Toast.LENGTH_LONG).show();
            }
        }

        UpdaterModule um = new UpdaterModule();
        um.execute();

    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

}
