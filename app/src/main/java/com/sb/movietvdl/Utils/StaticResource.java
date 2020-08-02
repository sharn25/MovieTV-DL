package com.sb.movietvdl.Utils;

import androidx.appcompat.app.AppCompatDelegate;

import com.sb.movietvdl.R;

    /*
    * Created by Sharn25
    * dated 12-06-2020
    */

public class StaticResource {
    //Themes
    public static final int DARK_THEME = AppCompatDelegate.MODE_NIGHT_YES;//R.style.AppTheme_DARK;
    public static final int LIGHT_THEME = AppCompatDelegate.MODE_NIGHT_NO;//R.style.AppTheme_LIGHT;
    public static final int DEFAULT_THEME = R.style.AppTheme;
    public static int CUR_THEME;
    public static int IV_TITLEBG;
    //Shared_pref
    public static final String PREF_SETTING = "MySetting_Pref";
    //int for parallel download
    public static int PARALLEL_DOWNLOAD;
}
