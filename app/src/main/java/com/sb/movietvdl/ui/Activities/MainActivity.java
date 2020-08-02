package com.sb.movietvdl.ui.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.sb.movietvdl.Adapter.MTRecyclerViewAdapter;
import com.sb.movietvdl.MovieTVDLApplication;
import com.sb.movietvdl.R;
import com.sb.movietvdl.Utils.CustomDialog;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.StaticResource;
import com.sb.movietvdl.config.MConfig;
import com.sb.movietvdl.helper.Fetcher;
import com.sb.movietvdl.source.MovieTV;
import com.sb.movietvdl.ui.Fragment.DownloadFragment;
import com.sb.movietvdl.ui.Fragment.HomeFragment;
import com.sb.movietvdl.ui.Fragment.SettingFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/*
    * Created by Sharn25
    * dated 12-06-2020
    */

public class MainActivity extends AppCompatActivity {
    //int
    private int cur_theme;

    //boolean
    private static boolean isThemeChanged;
    //SharedPref
    private SharedPreferences.Editor editor = null;
    //ListArray
    private List<MovieTV> movieTVList;
    //Elements
    final Fragment fHome = new HomeFragment();
    final Fragment fDownload = new DownloadFragment();
    final Fragment fSetting = new SettingFragment();
    final FragmentManager fM = getSupportFragmentManager();
    Fragment active = fHome;
    //Boolean
    private boolean isDarkTheme;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int CURRENT_VERSION=5;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.l(TAG+"_onCreate","MainActivity Cur_theme" + StaticResource.CUR_THEME,true);
        //setTheme(StaticResource.CUR_THEME);
        setContentView(R.layout.activity_main);
        initPermissions();
        //init Config
        //initConfig();
        //Bottom Navigation
        BottomNavigationView bNav = (BottomNavigationView) findViewById(R.id.b_nav);
        bNav.setOnNavigationItemSelectedListener(bNavListener);
        //Load Fragment Transaction Initiate
        fM.beginTransaction().add(R.id.fm_place, fSetting,"3").hide(fSetting).commit();
        fM.beginTransaction().add(R.id.fm_place, fDownload,"2").hide(fDownload).commit();
        fM.beginTransaction().add(R.id.fm_place, fHome,"1").commit();

        if(!MovieTVDLApplication.isupdatechecked) {
            MovieTVDLApplication.isupdatechecked=true;
            checkupdate checkUpdate = new checkupdate();
            checkUpdate.execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.l(TAG+"_onDestroy","Destroyed",true);
    }

    @Override
    public void finish() {
        super.finish();
        LogUtil.l(TAG+"_finish","finished",true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isThemeChanged){
            LogUtil.l(TAG+"_onResume","Themechanged",true);
            MainActivity.this.recreate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        MConfig.getConfig().isPremissionStorage=true;
                        LogUtil.l(TAG+"_OnRequestPermissionResult", "Storage permission Granted.", true);
                    }else {
                        MConfig.getConfig().isPremissionStorage=false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                CustomDialog.popinfo(this,"App require Storage permissions for Downloading animes.\nPremission will be asked again on next start.");
                            }
                        }

                    }
                }


                break;
        }
    }

    private void initPermissions(){
        if(!checkPermission()){
            LogUtil.l(TAG+"_initPermissions","Storage permission not granted", true);
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }else{
            MConfig.getConfig().isPremissionStorage=true;
            LogUtil.l(TAG+"_initPermissions","Storage permission alrerady granted", true);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private BottomNavigationView.OnNavigationItemSelectedListener bNavListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item){

            switch (item.getItemId()){
                case R.id.b_home:
                    fM.beginTransaction().hide(active).show(fHome).commit();
                    active = fHome;

                    break;
                case R.id.b_download:
                    fM.beginTransaction().hide(active).show(fDownload).commit();
                    active = fDownload;
                    break;
                case R.id.b_setting:
                    fM.beginTransaction().hide(active).show(fSetting).commit();
                    active = fSetting;
                    break;
            }
            return true;
        }
    };

    //Check update
    private class checkupdate extends AsyncTask<String, String, String> {
        private boolean updateavaliable=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(updateavaliable) {
                LogUtil.l(TAG+"_checkupdate", "running snackbar",true);
                View view = findViewById(R.id.activity_main);
                Snackbar mySnackbar = Snackbar.make(view,
                        R.string.update, Snackbar.LENGTH_LONG);
                mySnackbar.setAction(R.string.link, new MyActionListener());
                mySnackbar.show();
            }

        }

        @Override
        protected String doInBackground(String... strings) {
            Document doc = null;
            try {
                //System.out.println("entering updateer");
                Thread.sleep(5000);
                //System.out.println("start updater");
                doc= Jsoup.connect("http://a.animedlweb.ga/update/u_movietvdl.html").get();
                String versionstring = doc.body().text();
                LogUtil.l(TAG+"_checkupdate", "running " + versionstring,true);

                int version = Integer.parseInt(versionstring);
                if(CURRENT_VERSION<version) {
                    updateavaliable=true;
                }else {
                    //System.out.println("No update need: " + version);
                }
                // System.out.println(doc.text());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                System.out.println(e);
                //e.printStackTrace();

            }
            return null;
        }
    }
    public class MyActionListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Uri uriUrl = Uri.parse("http://a.animedlweb.ga/mtv_latest_download.html");//require
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            // Code to undo the user's last action
        }
    }

}
