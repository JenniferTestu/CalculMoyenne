package com.jennifertestu.coeffapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jennifertestu.coeffapp.AppDatabase;
import com.jennifertestu.coeffapp.DatabaseClient;
import com.jennifertestu.coeffapp.R;
import com.jennifertestu.coeffapp.model.Annee;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class BackupActivity extends AppCompatActivity {
    private static final int ACTIVITY_CHOOSE_FILE = 1;
    private Annee anneeActive;
    private Button boutonSave, boutonRest;
    private TextView tv;
    private String DirectoryName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        // Récupération des boutons d'action
        boutonSave = findViewById(R.id.bouton_save);
        boutonRest = findViewById(R.id.bouton_backup);
        // TextView pour afficher les messages
        tv = findViewById(R.id.textview_result);

        // Si on clique sur SAUVEGARDER
        boutonSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sauvegarde();
            }
        });

        // Si on clique sur RESTAURER
        boutonRest.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // On lance l'écran de selection de fichier
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, ACTIVITY_CHOOSE_FILE);
            }
        });

        // Récupération de l'année active dans les préférences
        SharedPreferences prefs = getSharedPreferences("annee_active", MODE_PRIVATE);
        int id = prefs.getInt("id", 0);//"No name defined" is the default value.
        String nom = prefs.getString("nom", "Pas de nom"); //0 is the default value.
        int nbperiode = prefs.getInt("nbperiode", 0);//"No name defined" is the default value.
        anneeActive = new Annee(nom,nbperiode);
        anneeActive.setId(id);

        creationBoutonsMenu();

        autresBoutonsMenu();

    }

    private void sauvegarde(){

        // On ferme la BDD avant de manipuler le fichier
        AppDatabase appDatabase = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        appDatabase.close();

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String  currentDBPath= "//data//" + "com.jennifertestu.coeffapp"
                        + "//databases//" + "CoefApp";
                String backupDBPath  = Environment.DIRECTORY_DOWNLOADS+"/SauvegardeNotes.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString(),
                        Toast.LENGTH_LONG).show();
                tv.setText("Export réussi !");

            }
        } catch (Exception e) {

            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG)
                    .show();

        }

    }

    private void restaurer(String path){

        // On ferme la BDD avant de manipuler le fichier
        AppDatabase appDatabase = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        appDatabase.close();

        try {

            File sd = Environment.getExternalStorageDirectory();
            File data  = Environment.getDataDirectory();

            if(!path.substring(path.lastIndexOf(".")).equals(".db")){
                tv.setText("Ce fichier n'est pas une sauvegarde");
            }else if (sd.canWrite()) {
                String  currentDBPath= "//data//" + "com.jennifertestu.coeffapp"
                        + "//databases//" + "CoefApp";
                //String backupDBPath  = path;
                File  backupDB= new File(data, currentDBPath);
                File currentDB  = new File(path);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), backupDB.toString(),
                        Toast.LENGTH_LONG).show();
                // On actualise le nav menu
                creationBoutonsMenu();
                tv.setText("Import réussi !");
            }
        } catch (Exception e) {

            tv.setText("Impossible d'accèder au fichier, veuillez vérifier la permission d'accès au stockage");

        }
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
                            Intent mainAnnee = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(mainAnnee);
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
        MenuItem boutonSave = menu.findItem(R.id.nav_save);


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
                String lien = getString (R.string.lien_partager);

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
                String lien = getString (R.string.lien_evaluer);

                try {
                    Intent viewIntent =
                            new Intent("android.intent.action.VIEW",
                                    Uri.parse(lien));
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

        boutonSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return false;
            }
        });
    }

    // Quand le fichier de sauvegarde a été choisi
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri uri = data.getData();
            String Fpath = getPathFromUri(this,uri);
            restaurer(Fpath);
        } else {
            tv.setText("Oups impossible de récupérer la sauvegarde");
        }

    }

    // Fonction de convertion du chemin du fichier de Uri a String
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] {
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
