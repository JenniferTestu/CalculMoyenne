package com.jennifertestu.coeffapp.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.adapter.NoteAdapter;
import com.jennifertestu.coeffapp.model.Matiere;
import com.jennifertestu.coeffapp.model.Note;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class NotesActivity extends AppCompatActivity {

    private TextView textMoy;
    private RecyclerView recyclerView;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Button button;
    private Matiere matiere;
    private List<Note> notes;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        df.setRoundingMode(RoundingMode.DOWN);

        matiere = (Matiere) getIntent().getExtras().getSerializable("matiere");

        textMoy = (TextView) findViewById(R.id.moy_text_view);
        textMoy.setText("Moyenne de "+matiere.getNom()+" : ");

        button = (Button) findViewById(R.id.button);
        /*
        button.setText(df.format(matiere.getMoy()));
        if(matiere.getMoy() >= 10 && matiere.getMoy() <= 12){
            button.setBackgroundResource(R.drawable.round_orange);
        }else if (matiere.getMoy() < 10){
            button.setBackgroundResource(R.drawable.round_red);
        }else{
            button.setBackgroundResource(R.drawable.round_blue);
        }
*/
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

        recyclerView = findViewById(R.id.list_notes_view);

        lesNotes();

        Button boutonAjouter = (Button)findViewById(R.id.bouton_ajouter);
        boutonAjouter.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent activityAjoutNote = new Intent(getApplicationContext(), AjoutNoteActivity.class);
                activityAjoutNote.putExtra("matiere",matiere);
                startActivity(activityAjoutNote);

            }
        });

        // Création du bouton pour pour partager la moyenne
        Button boutonPartage = (Button)findViewById(R.id.bouton_share_moy);
        boutonPartage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai une moyenne de "+button.getText()+" en "+matiere.getNom());
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);

            }
        });

    }


    private void lesNotes(){
        class LesNotes extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {

                List<Note> noteList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .noteDAO()
                        .getAllByMatiere(matiere.getId());


                //List<Note> noteList = matiere.getListeNotes();

                for(Note n : noteList) {
                    Log.i("Note de la liste",n.toString());
                }

                matiere.setListeNotes(noteList);
                matiere.calculerMoy();

                return noteList;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                //final ListView notesListView = findViewById(R.id.list_notes_view);
                //notesListView.setAdapter(new MatiereAdapter(getApplicationContext(), matieres));

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

                optionTri(recyclerView,notes);

                adapter = new NoteAdapter(NotesActivity.this,getApplicationContext(),notes,matiere,button);
                recyclerView.setAdapter(adapter);

            }
        }

        LesNotes ln = new LesNotes();
        ln.execute();
    }

    // Dans le cas où on presse le bouton précédent
    public void onRestart() {
        super.onRestart();
        lesNotes();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void optionTri(final RecyclerView recyclerView, final List<Note> liste) {

        Button button_tri = findViewById(R.id.bouton_tri);
        button_tri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Creation du menu pour une matiere
                PopupMenu popup = new PopupMenu(button.getContext(), view);
                //Ajout du fichier XML contenant le menu
                popup.inflate(R.menu.option_tri_2);
                //Ajout de l'écoute du clique
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tri_notes_croissants:
                                Collections.sort(liste, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note o1, Note o2) {
                                        return Double.compare(o1.getValeur(),o2.getValeur());
                                    }
                                });
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_notes_decroissants:
                                Collections.sort(liste, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note o1, Note o2) {
                                        return Double.compare(o1.getValeur(),o2.getValeur());
                                    }
                                });
                                Collections.reverse(liste);
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_coefs_croissants:
                                Collections.sort(liste, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note o1, Note o2) {
                                        return Double.compare(o1.getPoids(),o2.getPoids());
                                    }
                                });
                                recyclerView.getAdapter().notifyDataSetChanged();
                                return true;
                            case R.id.tri_coefs_decroissants:
                                Collections.sort(liste, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note o1, Note o2) {
                                        return Double.compare(o1.getPoids(),o2.getPoids());
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
}
