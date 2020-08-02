package com.sb.movietvdl;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import androidx.appcompat.app.AppCompatDelegate;

import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.StaticResource;
import com.sb.movietvdl.config.MConfig;
import com.sb.movietvdl.helper.Fetcher;
import com.sb.movietvdl.ui.Activities.MainActivity;

import org.wlf.filedownloader.FileDownloadConfiguration;
import org.wlf.filedownloader.FileDownloadConfiguration.Builder;
import org.wlf.filedownloader.FileDownloader;

import org.wlf.filedownloader.FileDownloadConfiguration;

import java.io.File;

/*
    * Created by Sharn25
    * Dated 17-06-2020
    */
public class MovieTVDLApplication extends Application {
    //int
    private int cur_theme;
    private int parallel_download;
    //boolean
    private boolean isDarkTheme;
    private static boolean isThemeChanged;
    public static boolean isupdatechecked;
    //SharedPref
    private SharedPreferences.Editor editor = null;
    //constants
    private final static String TAG = "MovieTVDLApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        initStaticResource();
        initConfig();
        initFileDownloader();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        releaseFileDownloader();
    }

    private void initStaticResource(){
        SharedPreferences MySetting = getSharedPreferences(StaticResource.PREF_SETTING,0);
        editor = MySetting.edit();
        cur_theme = MySetting.getInt("cur_theme",-1);
        isDarkTheme = MySetting.getBoolean("isDarkTheme",false);
        parallel_download = MySetting.getInt("parallel_download",3);
        LogUtil.l(TAG +"_OnCreate","cur_theme: " + cur_theme,true);
        if(cur_theme==-1) {
            editor.putInt("cur_theme", StaticResource.LIGHT_THEME);
            editor.putBoolean("isDarkTheme",false);
            editor.putInt("parallel_download",parallel_download);
            editor.commit();
            cur_theme = StaticResource.LIGHT_THEME;
        }

        StaticResource.CUR_THEME = cur_theme;
        setCur_theme(cur_theme);
        StaticResource.PARALLEL_DOWNLOAD = parallel_download;
        LogUtil.l(TAG,"App cur_theme: " +cur_theme,true);
    }

    private void setCur_theme(int theme){
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    private void initConfig(){
        File fAppDir = new File(this.getExternalFilesDir(".mtvdl"),"");
        File fAppdirconfig =new File(fAppDir,".mtv_dlconfig");
        if(fAppdirconfig.exists()) {
            MConfig.setConfig(MConfig.load(fAppdirconfig));
            LogUtil.l(TAG+"_init","GetFAppDir : " + MConfig.getConfig().appdir,false);
            LogUtil.l(TAG+"_init","GetdestDir : " + MConfig.getConfig().destdir,true);
            LogUtil.i(TAG+"_init","Saved config Loaded.",true);
        }else {
            File destDir=new File(Environment.getExternalStorageDirectory(),"Download");
            if(MConfig.getConfig()!=null){
                MConfig.getConfig().file = fAppdirconfig;
                MConfig.getConfig().appdir = fAppDir.getAbsolutePath();
                MConfig.getConfig().destdir = destDir.getAbsolutePath();
                MConfig.getConfig().source = 0;
                MConfig.getConfig().isDarkTheme = isDarkTheme;
                MConfig.getConfig().isPlayerAsk = true;
                MConfig.getConfig().save();
                

            }/*
            //an_con.tempdir=fTmpDir.getAbsolutePath();
            an_con.Default_Location=true;
            an_con.Default_downloader=true;
            an_con.tablesize=70;
            an_con.maxdownloads=5;
            fetcher.db(this);*/
            LogUtil.l(TAG+"_init","destDir : " + MConfig.getConfig().destdir,true);
            LogUtil.i(TAG+"_init","New Config saved and Loaded",true);
        }
    }

    // init FileDownloader
    private void initFileDownloader() {
        LogUtil.l("AnimeDLApplication","initFileDownloader",true);
        // 1.create FileDownloadConfiguration.Builder
        Builder builder = new Builder(this);

        // 2.config FileDownloadConfiguration.Builder
        builder.configFileDownloadDir(MConfig.getConfig().destdir); // config the download path
        // builder.configFileDownloadDir("/storage/sdcard1/FileDownloader");

        // allow 3 download tasks at the same time
        builder.configDownloadTaskSize(StaticResource.PARALLEL_DOWNLOAD);

        // config retry download times when failed
        builder.configRetryDownloadTimes(5);

        // enable debug mode
        //builder.configDebugMode(true);

        // config connect timeout
        builder.configConnectTimeout(25000); // 25s

        // 3.init FileDownloader with the configuration
        FileDownloadConfiguration configuration = builder.build(); // build FileDownloadConfiguration with the builder
        FileDownloader.init(configuration);

    }

    // release FileDownloader
    private void releaseFileDownloader() {
        FileDownloader.release();
    }

}
