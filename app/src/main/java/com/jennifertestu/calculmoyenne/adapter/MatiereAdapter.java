package com.jennifertestu.calculmoyenne.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jennifertestu.calculmoyenne.DatabaseClient;
import com.jennifertestu.calculmoyenne.activity.NotesActivity;
import com.jennifertestu.calculmoyenne.R;
import com.jennifertestu.calculmoyenne.activity.UpdateMatiereActivity;
import com.jennifertestu.calculmoyenne.model.Matiere;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// Permet l'affichage d'une liste de matieres avec ses infos
public class MatiereAdapter extends RecyclerView.Adapter<MatiereAdapter.MatiereViewHolder> {

    private Activity activity;
    private Context mCtx;
    private List<Matiere> matiereList;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Button button;
    private Boolean toutesPeriodes=false;

    public MatiereAdapter(Activity activity, Context mCtx, List<Matiere> matiereList, Button button) {
        this.activity=activity;
        this.mCtx = mCtx;
        this.matiereList = matiereList;
        this.button=button;
    }

    public void setToutesPeriodes(Boolean toutesPeriodes) {
        this.toutesPeriodes = toutesPeriodes;
    }

    // Construction d'un ViewHolder qui contient les infos qui composent un élément de la liste.
    // Ici le ViewHolder se base sur un xml
    @Override
    public MatiereViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.adapter_item, null);
        return new MatiereViewHolder(view);
    }

    //Complete les champs pour chaque matiere
    @Override
    public void onBindViewHolder(final MatiereViewHolder holder, final int position) {

        df.setRoundingMode(RoundingMode.DOWN);

        Matiere m = matiereList.get(position);

        if(toutesPeriodes==true) {
            holder.nomView.setText(m.getNom()+" (Période "+m.getPeriode()+")");
        }else{
            holder.nomView.setText(m.getNom());
        }

        holder.coefView.setText("Coefficient : "+Double.toString(m.getCoef()));
        if(m.getDateDerniereNote()!=null) {
            SimpleDateFormat formater = new SimpleDateFormat("d MMMM yyyy", Locale.FRANCE);
            holder.derDateView.setText("Date de la dernière note : "+formater.format(m.getDateDerniereNote()));
        }

        // Double moy = m.getMoy();
        Double moy = m.calculerMoy();


        if(moy >= 10 && moy < 12){
            holder.rond.setText(df.format(moy));
            holder.rond.setBackgroundResource(R.drawable.round_orange);
        }else if (moy >= 0 && moy < 10){
            holder.rond.setText(df.format(moy));
            holder.rond.setBackgroundResource(R.drawable.round_red);
        }else if(moy >= 12){
            holder.rond.setText(df.format(moy));
            holder.rond.setBackgroundResource(R.drawable.round_blue);
        }else {
            holder.rond.setText("/");
            holder.rond.setBackgroundResource(R.drawable.round_blue);
        }

        // Création du bouton affichant les actions possibles sur cet élément
        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une matiere
                PopupMenu popup = new PopupMenu(holder.buttonViewOption.getContext(), view);
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
                                intent.putExtra("matiereUpdate", matiereList.get(position));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(intent);
                                return true;
                            case R.id.menuPartage:
                                //Si le choix est de partager
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai une moyenne de "+matiereList.get(position).getMoy()+" en "+matiereList.get(position).getNom());
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(shareIntent);
                                return true;
                            case R.id.menuSupp:
                                //Si le choix est de supprimer
                                Matiere selectMatiere = matiereList.get(position);
                                popupSupp(selectMatiere,activity);
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

    //Comptage du nombre total de matière
    @Override
    public int getItemCount() {
        return matiereList.size();
    }

    // On récupére tous les champs à completer
    class MatiereViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nomView, coefView, derDateView, buttonViewOption;
        Button rond;

        public MatiereViewHolder(View itemView) {
            super(itemView);

            nomView = itemView.findViewById(R.id.item_name);
            coefView = itemView.findViewById(R.id.item_coef);
            derDateView = itemView.findViewById(R.id.item_derDate);
            rond = itemView.findViewById(R.id.rond);
            buttonViewOption = itemView.findViewById(R.id.matiereOptions);

            itemView.setOnClickListener(this);
        }

        // Action à réaliser si on clique sur un élément
        @Override
        public void onClick(View view) {
            Matiere matiere = matiereList.get(getAdapterPosition());

            Intent intent = new Intent(mCtx, NotesActivity.class);
            intent.putExtra("matiere", matiere);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mCtx.startActivity(intent);
        }
    }

    // Tache de suppression d'une matiere
    private void SuppMatiere(final Matiere m){


        class SuppMatiere extends AsyncTask<Void, Void, Void> {

            //Supression dans la BDD de la matiere ainsi que ses notes
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
                matiereList.remove(m);
                notifyDataSetChanged();
                Toast.makeText(mCtx, "Matière supprimée", Toast.LENGTH_LONG).show();


                Double moyG = 0.0;
                int coefs = 0;

                for(Matiere m : matiereList) {
                    if(!m.getListeNotes().isEmpty()) {
                        moyG += m.calculerMoy() * m.getCoef();
                        coefs += m.getCoef();
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
    private void popupSupp(final Matiere m, Activity activity){

        final AlertDialog dialogBuilder = new AlertDialog.Builder(activity).create();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View dialogView = inflater.inflate(R.layout.confirmation_dialog, null);

        TextView tv = (TextView) dialogView.findViewById(R.id.textView);
        tv.setText("Etes-vous sûrs de vouloir supprimer la matière \""+m.getNom()+"\" ainsi que toutes ses notes ?");

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

                SuppMatiere(m);
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }
}
