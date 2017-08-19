package com.vinrosa.badtransitapp;

import android.app.Application;

import com.vinrosa.badtransitapp.model.DaoMaster;
import com.vinrosa.badtransitapp.model.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by vinliangx on 8/7/17.
 */

public class App extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "baddriver-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
