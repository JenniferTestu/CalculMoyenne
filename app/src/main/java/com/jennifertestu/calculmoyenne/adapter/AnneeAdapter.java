package com.jennifertestu.calculmoyenne.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.jennifertestu.calculmoyenne.DatabaseClient;
import com.jennifertestu.calculmoyenne.R;
import com.jennifertestu.calculmoyenne.activity.AnneeActivity;
import com.jennifertestu.calculmoyenne.activity.MainActivity;
import com.jennifertestu.calculmoyenne.model.Annee;
import com.jennifertestu.calculmoyenne.model.Matiere;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

// Permet l'affichage d'une liste d'années avec ses infos
public class AnneeAdapter extends RecyclerView.Adapter<AnneeAdapter.AnneeViewHolder> {

    private Activity activity;
    private Context mCtx;
    private List<Annee> anneeList;
    private Annee anneeActive;
    private EditText editNom;

    public AnneeAdapter(Activity activity, Context mCtx, List<Annee> anneeList) {
        this.activity = activity;
        this.mCtx = mCtx;
        this.anneeList = anneeList;

        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = mCtx.getSharedPreferences("annee_active", MODE_PRIVATE);
        int id = prefs.getInt("id", 0);//"No name defined" is the default value.
        String nom = prefs.getString("nom", "Pas de nom"); //0 is the default value.
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.
        final boolean isModule = prefs.getBoolean("ismodule", false);//"No name defined" is the default value.

        anneeActive = new Annee(nom,nbperiode,isModule);
        anneeActive.setId(id);
    }

    // Construction d'un ViewHolder qui contient les infos qui composent un élément de la liste.
    // Ici le ViewHolder se base sur un xml
    @Override
    public AnneeAdapter.AnneeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.adapter_annee, null);
        return new AnneeAdapter.AnneeViewHolder(view);
    }

    //Complete les champs pour chaque année
    @Override
    public void onBindViewHolder(final AnneeAdapter.AnneeViewHolder holder, final int position) {

        Annee a = anneeList.get(position);
        holder.nomView.setText(a.getNom());
        if(a.isAvecModule()==true) {
            holder.avecModule.setText("Avec modules");
        }else {
            holder.avecModule.setText("Sans modules");
        }
        holder.nbPeriodeView.setText("Composée de " +Integer.toString(a.getNbPeriodes())+" période(s)");

        if(a.getId()==anneeActive.getId()){
            holder.rond.setBackgroundResource(R.drawable.ic_date_range);
        }else {
            holder.rond.setBackgroundResource(R.drawable.ic_date_range_white);
        }

        // Création du bouton affichant les actions possibles sur cet élément
        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une année
                PopupMenu popup = new PopupMenu(holder.buttonViewOption.getContext(), view);
                //Ajout du fichier XML contenant le menu
                popup.inflate(R.menu.options_menu_2);
                //Ajout de l'écoute du clique
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuEdit:
                                //Si le choix est d'éditer
                                popupUpdate(anneeList.get(position),activity);
                                return true;
                            case R.id.menuSupp:
                                //Si le choix est de supprimer
                                Annee selectAnnee = anneeList.get(position);
                                if (selectAnnee.getId() == anneeActive.getId()){
                                    Toast.makeText(mCtx, "Vous ne pouvez pas supprimer l'année en cours", Toast.LENGTH_LONG).show();
                                }else{
                                    popupSupp(selectAnnee,activity);
                                }
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

    //Comptage du nombre total d'année
    @Override
    public int getItemCount() {
        return anneeList.size();
    }

    // On récupére tous les champs à completer
    class AnneeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nomView, nbPeriodeView, avecModule, buttonViewOption;
        Button rond;

        public AnneeViewHolder(View itemView) {
            super(itemView);

            nomView = itemView.findViewById(R.id.item_name);
            nbPeriodeView = itemView.findViewById(R.id.item_nbPeriode);
            avecModule = itemView.findViewById(R.id.item_avecModule);
            rond = itemView.findViewById(R.id.rond);
            buttonViewOption = itemView.findViewById(R.id.matiereOptions);

            itemView.setOnClickListener(this);
        }

        // Action à réaliser si on clique sur un élément
        @Override
        public void onClick(View view) {
            Annee annee = anneeList.get(getAdapterPosition());

            SharedPreferences.Editor editor = mCtx.getSharedPreferences("annee_active", MODE_PRIVATE).edit();
            editor.putInt("id", annee.getId());
            editor.putString("nom", annee.getNom());
            editor.putInt("nbperiode", annee.getNbPeriodes());
            editor.putBoolean("ismodule", annee.isAvecModule());

            editor.apply();

            Intent intent = new Intent(mCtx, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mCtx.startActivity(intent);
        }
    }

    // Tache de suppression d'une année
    private void SuppAnnee(final Annee a){

        class SuppAnnee extends AsyncTask<Void, Void, Void> {

            //Supression dans la BDD de l'année ainsi que ses matières et ses notes
            @Override
            protected Void doInBackground(Void... voids) {

                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .anneeDAO()
                        .delete(a);
                List<Matiere> listeMatiereSupp = DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .matiereDAO()
                        .getAllByAnnee(a.getId());

                for(Matiere m : listeMatiereSupp) {
                    DatabaseClient.getInstance(mCtx).getAppDatabase()
                            .noteDAO()
                            .deleteByMatiere(m.getId());
                }

                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .matiereDAO()
                        .deleteByAnnee(a.getId());

                return null;
            }

            // Actualisation de l'adapter et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                anneeList.remove(a);
                notifyDataSetChanged();
                Toast.makeText(mCtx, "Année supprimée", Toast.LENGTH_LONG).show();
            }
        }

        SuppAnnee sa = new SuppAnnee();
        sa.execute();


    }

    // Tache d'edition d'une année
    private void updaterAnnee(final Annee a){

        if (editNom.getText().toString().isEmpty()) {
            editNom.setError("Un nom pour désigner l'année est requis");
            editNom.requestFocus();
            return;
        }


        final String sNom = editNom.getText().toString().trim();

        //Edition dans la BDD de l'année
        class UpdateAnnee extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                a.setNom(sNom);
                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .anneeDAO()
                        .update(a);
                return null;
            }

            // Actualisation de l'adapter et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //data.set(updateIndex, newValue);
                activity.finish();
                mCtx.startActivity(new Intent(mCtx.getApplicationContext(), AnneeActivity.class));
                Toast.makeText(mCtx.getApplicationContext(), "Année modifiée", Toast.LENGTH_LONG).show();
            }
        }

        UpdateAnnee um = new UpdateAnnee();
        um.execute();

    }

    // Popup pour actualiser les infos d'une année
    private void popupUpdate(final Annee a, Activity activity){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.custom_dialog_update, null);

        editNom = (EditText) dialogView.findViewById(R.id.editNomAnnee);
        editNom.setText(a.getNom());

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

                updaterAnnee(a);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }


    // Popup pour demander confirmation avant suppression
    private void popupSupp(final Annee a, Activity activity){

        final androidx.appcompat.app.AlertDialog dialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(activity).create();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.confirmation_dialog, null);

        TextView tv = (TextView) dialogView.findViewById(R.id.textView);
        tv.setText("Etes-vous sûrs de vouloir surpprimer l'année \""+a.getNom()+"\" ainsi que toutes ses matières et ses notes ?");

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

                SuppAnnee(a);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }
}
