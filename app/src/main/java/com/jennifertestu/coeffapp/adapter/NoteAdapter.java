package com.jennifertestu.coeffapp.adapter;

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
import android.widget.Toast;

import android.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.activity.UpdateNoteActivity;
import com.jennifertestu.coeffapp.model.Matiere;
import com.jennifertestu.coeffapp.model.Note;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

// Permet l'affichage d'une liste de notes avec ses infos
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Activity activity;
    private Context mCtx;
    private List<Note> noteList;
    private Matiere matiere;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Button button;

    public NoteAdapter(Activity activity, Context mCtx, List<Note> noteList, Matiere matiere, Button button) {
        this.activity=activity;
        this.mCtx = mCtx;
        this.noteList = noteList;
        this.matiere=matiere;
        this.button=button;
    }

    // Construction d'un ViewHolder qui contient les infos qui composent un élément de la liste.
    // Ici le ViewHolder se base sur un xml
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.adapter_note, null);
        return new NoteViewHolder(view);
    }

    //Complete les champs pour chaque note
    @Override
    public void onBindViewHolder(final NoteViewHolder holder, final int position) {

        df.setRoundingMode(RoundingMode.DOWN);

        Note n = noteList.get(position);
        holder.typeView.setText(n.getTypeDeNote().toString());
        holder.comView.setText(n.getCommentaire());
        holder.coefView.setText(Integer.toString(n.getPoids()));

        SimpleDateFormat formater = new SimpleDateFormat("d MMMM yyyy", Locale.FRANCE);
        holder.dateView.setText(formater.format(n.getDate()));

        Double val = n.getValeur();

        holder.rond.setText(df.format(val));

        if(val >= 10 && val < 12){
            holder.rond.setBackgroundResource(R.drawable.round_orange);
        }else if (val < 10){
            holder.rond.setBackgroundResource(R.drawable.round_red);
        }else{
            holder.rond.setBackgroundResource(R.drawable.round_blue);
        }

        // Création du bouton affichant les actions possibles sur cet élément
        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une note
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
                                Intent intent = new Intent(mCtx, UpdateNoteActivity.class);
                                intent.putExtra("noteUpdate", noteList.get(position));
                                intent.putExtra("matiere", matiere);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(intent);
                                return true;
                            case R.id.menuPartage:
                                //Si le choix est de partager
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai eu "+noteList.get(position).getValeur()+" en "+matiere.getNom());
                                sendIntent.setType("text/plain");

                                Intent shareIntent = Intent.createChooser(sendIntent, null);
                                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.startActivity(shareIntent);
                                return true;
                            case R.id.menuSupp:
                                //Si le choix est de supprimer
                                Note selectNote = noteList.get(position);
                                SuppNote(selectNote);
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

    //Comptage du nombre total de note
    @Override
    public int getItemCount() {
        if(noteList!=null) {
            return noteList.size();
        }else{
            return 0;
        }
    }

    // On récupére tous les champs à completer
    class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView typeView, comView, dateView, coefView, buttonViewOption;
        Button rond;

        public NoteViewHolder(View itemView) {
            super(itemView);

            typeView = itemView.findViewById(R.id.item_type);
            comView = itemView.findViewById(R.id.item_com);
            coefView = itemView.findViewById(R.id.item_coef);
            dateView = itemView.findViewById(R.id.item_date);
            rond = itemView.findViewById(R.id.rond);
            buttonViewOption = itemView.findViewById(R.id.noteOptions);

            itemView.setOnClickListener(this);
        }

        // Action à réaliser si on clique sur un élément
        @Override
        public void onClick(View view) {
            Note note = noteList.get(getAdapterPosition());

        }
    }

    // Tache de suppression d'une note
    private void SuppNote(final Note n){


        class SuppNote extends AsyncTask<Void, Void, Void> {

            //Supression dans la BDD de la note
            @Override
            protected Void doInBackground(Void... voids) {

                //adding to database
                DatabaseClient.getInstance(mCtx).getAppDatabase()
                        .noteDAO()
                        .delete(n);
                return null;
            }

            // Actualisation de l'adapter et message de confirmation
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                noteList.remove(n);
                notifyDataSetChanged();
                Toast.makeText(mCtx, "Note supprimée", Toast.LENGTH_LONG).show();

                matiere.setListeNotes(noteList);
                matiere.calculerMoy();

                if(matiere.getMoy() >= 10 && matiere.getMoy() < 12){
                    button.setText(df.format(matiere.getMoy()));
                    button.setBackgroundResource(R.drawable.round_orange);
                }else if (matiere.getMoy() >= 0 && matiere.getMoy() < 10){
                    button.setText(df.format(matiere.getMoy()));
                    button.setBackgroundResource(R.drawable.round_red);
                }else if(matiere.getMoy() >= 12){
                    button.setText(df.format(matiere.getMoy()));
                    button.setBackgroundResource(R.drawable.round_blue);
                }else {
                    button.setText("/");
                    button.setBackgroundResource(R.drawable.round_blue);
                }

            }
        }

        SuppNote sn = new SuppNote();
        sn.execute();


    }
}