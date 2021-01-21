package com.mimdal.blog;

import android.app.Application;

import com.mimdal.blog.Helpers.SingletonSharedPrefs;

public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SingletonSharedPrefs.init(getApplicationContext(), "preference_blog");
    }
}
