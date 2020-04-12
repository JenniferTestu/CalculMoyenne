package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;


public class NotesActivity extends AppCompatActivity {

    private TextView textMoy;
    private RecyclerView recyclerView;
    private DecimalFormat df = new DecimalFormat("#.##");
    private Button button;
    private Matiere matiere;
    private List<Note> notes;

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

                NoteAdapter adapter = new NoteAdapter(NotesActivity.this,getApplicationContext(),notes,matiere,button);
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
}
