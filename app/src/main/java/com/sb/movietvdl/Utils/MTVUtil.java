package com.sb.movietvdl.Utils;

import java.io.File;

    /*
    * Created by Sharn25
    * Dated 16-06-2020
    */
public class MTVUtil {
    private final static String TAG = "MTVUtil";
    public static String getFormatedString(String s){
        s = s.replaceAll("<li>","");
        s = s.replaceAll("<ul>","");
        s = s.replaceAll("</ul>","");
        s = s.replaceAll("</li>","<br>");
        return s;
    }

    public static void delEmptyDir(String dir) {

        LogUtil.l(TAG+"_delEmptyDir", "Deleting folder: " + dir,true);
        File fdir = new File(dir);
        if(fdir.exists()) {
            File[] files = fdir.listFiles();
            if(files == null || files.length == 0) {
                fdir.delete();
            }
        }
    }
}
