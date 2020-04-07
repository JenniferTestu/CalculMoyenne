package com.jennifertestu.coeffapp.adapter;

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

import androidx.recyclerview.widget.RecyclerView;

import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.activity.AjoutMatiereActivity;
import com.jennifertestu.coeffapp.activity.NotesActivity;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.activity.UpdateMatiereActivity;
import com.jennifertestu.coeffapp.model.Matiere;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MatiereAdapter extends RecyclerView.Adapter<MatiereAdapter.MatiereViewHolder> {

    private Context mCtx;
    private List<Matiere> matiereList;
    private DecimalFormat df = new DecimalFormat("#.##");


    public MatiereAdapter(Context mCtx, List<Matiere> matiereList) {
        this.mCtx = mCtx;
        this.matiereList = matiereList;
    }

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
        holder.nomView.setText(m.getNom());
        holder.coefView.setText("Coefficient : "+Integer.toString(m.getCoef()));
        if(m.getDateDerniereNote()!=null) {
            SimpleDateFormat formater = new SimpleDateFormat("d MMMM yyyy", Locale.FRANCE);
            holder.derDateView.setText("Date de la dernière note : "+formater.format(m.getDateDerniereNote()));
        }

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
                                Intent intent = new Intent(mCtx, UpdateMatiereActivity.class);
                                intent.putExtra("matiereUpdate", matiereList.get(position));

                                mCtx.startActivity(intent);
                                return true;
                            case R.id.menuPartage:
                                //Si le choix est de partager
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai une moyenne de "+matiereList.get(position).getMoy()+" en "+matiereList.get(position).getNom());
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                mCtx.startActivity(shareIntent);
                                return true;
                            case R.id.menuSupp:
                                //Si le choix est de supprimer
                                Matiere selectMatiere = matiereList.get(position);
                                SuppMatiere(selectMatiere);
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

        @Override
        public void onClick(View view) {
            Matiere matiere = matiereList.get(getAdapterPosition());

            Intent intent = new Intent(mCtx, NotesActivity.class);
            intent.putExtra("matiere", matiere);

            mCtx.startActivity(intent);
        }
    }

    private void SuppMatiere(final Matiere m){


        class SuppMatiere extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                //adding to database
                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .matiereDAO()
                        .delete(m);
                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .noteDAO()
                        .deleteByMatiere(m.getId());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                matiereList.remove(m);
                notifyDataSetChanged();
                Toast.makeText(mCtx, "Matière supprimée", Toast.LENGTH_LONG).show();
            }
        }

        SuppMatiere sm = new SuppMatiere();
        sm.execute();


    }
}
