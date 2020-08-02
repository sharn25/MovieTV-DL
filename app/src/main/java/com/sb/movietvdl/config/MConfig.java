package com.sb.movietvdl.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Observable;

    /*
    * Created by Sharn25
    * date 13-06-2020
    */

public class MConfig extends Observable implements Serializable {
    //int
    public int source;
    //String Array
    public static String[] source_url = new String[]{"https://themoviesflix.com"};
    public static String[] source_des = new String[]{"Holly-Bolly"};
    //Strings
    public String destdir;
    public String tempdir;
    public String appdir;
    //boolean
    public static boolean isStreamMode;
    public static boolean isPremissionStorage=false;
    public boolean isEPlayer;
    public boolean isPlayerAsk;
    public boolean isDarkTheme;
    //Files
    public File file;
    //Object
    private static MConfig mConfig;

    public MConfig(){

    }

    public static void setConfig(MConfig a){
        mConfig = a;
    }
    public static MConfig getConfig(){
       if(mConfig==null){
          mConfig = new MConfig();
       }
       return mConfig;
    }

    public void save() {
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(new FileOutputStream(this.file));
            out.writeObject(this);
            out.close();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        this.setChanged();
        this.notifyObservers();
    }

    public static MConfig load(File file) {
        ObjectInputStream in = null;

        try {
            in = new ObjectInputStream(new FileInputStream(file));
            MConfig e = (MConfig)in.readObject();
            return e;
        } catch (Exception var5) {
            var5.printStackTrace();

            try {
                in.close();
            } catch (Exception var4) {
                ;
            }
            return new MConfig();
        }
    }
}
