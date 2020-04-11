package com.jennifertestu.coeffapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.activity.AnneeActivity;
import com.jennifertestu.coeffapp.activity.BackupActivity;
import com.jennifertestu.coeffapp.activity.MainActivity;
import com.jennifertestu.coeffapp.model.Annee;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MenuNav {

    private NavigationView navView;
    private Annee anneeActive;
    private Context mCtx;


    public MenuNav(Context mCtx, NavigationView navView) {
        this.navView=navView;
        this.mCtx=mCtx;

        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = mCtx.getSharedPreferences("annee_active", MODE_PRIVATE);
        int id = prefs.getInt("id", 0);//"No name defined" is the default value.
        String nom = prefs.getString("nom", "Pas de nom"); //0 is the default value.
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.
        anneeActive = new Annee(nom,nbperiode);
        anneeActive.setId(id);
    }

    public void creer(){
        creationBoutonsMenu();
        autresBoutonsMenu();
    }

    private void creationBoutonsMenu(){

        navView.setItemIconTintList(null);
        final SubMenu menu = navView.getMenu().getItem(0).getSubMenu();

        class LesAnnees extends AsyncTask<Void, Void, List<Annee>> {

            @Override
            protected List<Annee> doInBackground(Void... voids) {
                List<Annee> anneeList = DatabaseClient
                        .getInstance(mCtx)
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

                            SharedPreferences.Editor editor = mCtx.getSharedPreferences("annee_active", MODE_PRIVATE).edit();
                            editor.putInt("id", a.getId());
                            editor.putString("nom", a.getNom());
                            editor.putInt("nbperiode", a.getNbPeriodes());
                            editor.apply();

                            // Rafraichir l'activité
                            Intent mainAnnee = new Intent(mCtx, MainActivity.class);
                            mainAnnee.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mCtx.startActivity(mainAnnee);
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

        Menu menu = navView.getMenu();

        MenuItem boutonGererAnnees = menu.findItem(R.id.nav_annee);
        MenuItem boutonPartager = menu.findItem(R.id.nav_share);
        MenuItem boutonEvaluer = menu.findItem(R.id.nav_eval);
        MenuItem boutonAide = menu.findItem(R.id.nav_help);
        MenuItem boutonSave = menu.findItem(R.id.nav_save);


        boutonGererAnnees.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent activityAnnee = new Intent(mCtx.getApplicationContext(), AnneeActivity.class);
                activityAnnee.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mCtx.startActivity(activityAnnee);

                return false;
            }
        });

        boutonPartager.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String lien = mCtx.getString (R.string.lien_partager);

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "J'utilise cette application pour calculer mes notes :  "+lien);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mCtx.startActivity(shareIntent);
                return false;
            }
        });

        boutonEvaluer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String lien = mCtx.getString (R.string.lien_evaluer);

                try {
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse(lien));
                    viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mCtx.startActivity(viewIntent);
                }catch(Exception e) {
                    Toast.makeText(mCtx,"Impossible d'accèder à la page, essayez plus tard ...",
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

        boutonSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent activityBackup = new Intent(mCtx, BackupActivity.class);
                activityBackup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mCtx.startActivity(activityBackup);

                return false;
            }
        });
    }

}
