package com.jennifertestu.coeffapp;

import android.content.Context;
import android.util.Log;

import androidx.room.*;

public class DatabaseClient {

    private Context mCtx;
    private static DatabaseClient mInstance;

    //our app database object
    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        Log.d(DatabaseClient.class.getSimpleName(),"Creation de la Database");
        //creating the app database with Room database builder
        //MyToDos is the name of the database
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "CoefApp")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {

        Log.d(DatabaseClient.class.getSimpleName(),"Récup de la Database");

        return appDatabase;
    }

}
