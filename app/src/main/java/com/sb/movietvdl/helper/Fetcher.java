package com.sb.movietvdl.helper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.telecom.Call;


import com.sb.movietvdl.MovieTVDLApplication;
import com.sb.movietvdl.Utils.LogUtil;
import com.sb.movietvdl.Utils.MTVUtil;
import com.sb.movietvdl.config.MConfig;
import com.sb.movietvdl.source.LinkButton;
import com.sb.movietvdl.source.MovieTV;
import com.sb.movietvdl.ui.Activities.DetailsActivity;
import com.sb.movietvdl.ui.Activities.MainActivity;
import com.sb.movietvdl.ui.Activities.PlayerActivity;
import com.sb.movietvdl.ui.Fragment.HomeFragment;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.listener.OnDetectBigUrlFileListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
    * Created by Sharn25
    * dated 13-06-2020
    */
public class Fetcher {
    //String
    private String ERROR_MSG;
    private String url,title,imgicon,descirtion, hdtype, audiotype, season;
    //Object
    private static Fetcher fetcher;
    MovieTV[] movieTV;
    //List array
    List<MovieTV> movieTVList;
    List<LinkButton> linkButttonList;
    //Constants
    public static final String TAG = "Fetcher";

    public static void setConfig(Fetcher a){
        fetcher = a;
    }
    public static Fetcher getConfig(){
        if(fetcher==null){
            fetcher = new Fetcher();
        }
        return fetcher;
    }
    private void sendERROR_MSG(HomeFragment mainwindow){
        mainwindow.msg = new Message();
        mainwindow.msg.arg1 = mainwindow.POP_INFO;
        String[] ary = {ERROR_MSG};
        mainwindow.msg.obj = ary;
        mainwindow.mHandler.sendMessage(mainwindow.msg);
    }
    private void sendERROR_MSG(DetailsActivity mainwindow){
        mainwindow.msg = new Message();
        mainwindow.msg.arg1 = mainwindow.POP_INFO;
        String[] ary = {ERROR_MSG};
        mainwindow.msg.obj = ary;
        mainwindow.mHandler.sendMessage(mainwindow.msg);
    }
    //docparser
    private Document parsehtml(String url,String ref) {
        Document doc = null;
        Connection.Response response = null;
        int responseCode=0;
        try{
            if(ref!=""){
                response = Jsoup.connect(url).ignoreContentType(true).referrer(ref).execute();
            }else{
                response = Jsoup.connect(url).ignoreContentType(true).execute();
            }

            responseCode = response.statusCode();
            String responseMsg = response.statusMessage();
            LogUtil.l(TAG+"_parsehtml","Response Code: " + responseCode+" Response Msg: " + responseMsg,true);
            if(responseCode==200){
                doc = response.parse();
            }
            else{
                ERROR_MSG = "Error! Invalid response code: " + responseCode;
                doc = null;
            }
        }catch(org.jsoup.HttpStatusException e){
            int st = e.getStatusCode();

            if(st==404) {
                ERROR_MSG = "HTTP ERROR " + e.getStatusCode() + "\nAnime not found.";
            }else{
                ERROR_MSG = "HTTP ERROR " + e.getStatusCode() + "\nTry different Anime.";
            }
            LogUtil.e(TAG+"_parsehtml",ERROR_MSG,true);
            doc=null;
        }catch(java.net.SocketTimeoutException ex){
            ERROR_MSG = "Sever Timeout.\nPlease retry.";
            LogUtil.e(TAG+"_parsehtml","Sever Timeout. Please retry.",true);
            doc=null;
        }
        catch (Exception e) {
            e.printStackTrace();
            ERROR_MSG = "Some unexpected error occured.\nTry checking your internet connection.";
            //CustomDialog.popinfo(MainActivity.this,"Error code: 1001.\nTimeout or internet error  ");
            LogUtil.e(TAG+"_parsehtml","Some unexpected error occured. Try checking your internet connection.",true);
            doc=null;
        }
        return doc;
    }
    //Home fetcher
    public List<MovieTV> getdata(Document doc, List<MovieTV> movieTVList1, int cases, HomeFragment mainwindow){
        movieTVList = movieTVList1;
        //movieTVList.clear();
        switch (cases){
            case 0:
                source1_2_helper(doc,mainwindow);
                break;
            case 1:
                source1_2_helper(doc,mainwindow);
                break;
        }

        return movieTVList;
    }

    private List<MovieTV> source1_2_helper(Document doc, HomeFragment mainwindow){
        String newdoc = doc.toString();/*
        int index1 = newdoc.indexOf("<article class=\"latestPost");
        int index2 = index1+1000;//newdoc.indexOf("<footer>");
        LogUtil.l(TAG,"index1: " + index1 + " index2: " + index2,true);
        newdoc = newdoc.substring(index1,index2);
        LogUtil.l(TAG,"out: " + newdoc,true);
        doc = Jsoup.parse("<html><body>" + newdoc + "</body></html>");
        */
        String selector= "article.latestPost > a";
        Elements sel = doc.select(selector);

        LogUtil.l(TAG+"_getdoc","Sel size: " + sel.size(),true);
        if(sel.size()<=0){
            ERROR_MSG="No Movie/TV Series Found.";
            sendERROR_MSG(mainwindow);
            return null;
        }
        movieTV = new MovieTV[sel.size()];
        for(int i =0 ; i<sel.size();i++) {

            String title = sel.get(i).attr("title");
            title = title.replace("\u00a0", " ");
            title = title.replace("Download ","");
            this.title = title;
            this.url = sel.get(i).attr("href");
            Elements img = sel.get(i).select("img");
            String key = ".jpg";
            this.imgicon = img.get(0).toString();
            if(this.imgicon.contains("png")){
                key=".png";
            }
            try {
                this.imgicon = img.get(0).attr("data-lazy-srcset");
                int index_key = this.imgicon.indexOf(key);
                LogUtil.l(TAG + "_getdoc", "index_key: " + index_key, true);
                if(index_key!=-1) {
                    this.imgicon = this.imgicon.substring(0, index_key + key.length());
                }else{
                    imgicon = img.get(0).toString();
                    this.imgicon = this.imgicon.substring(0,this.imgicon.indexOf(key)+key.length());
                    this.imgicon = this.imgicon.substring(this.imgicon.lastIndexOf("\"")+1);
                }
            }catch (Exception e){
                    this.imgicon = "";
            }
            this.descirtion ="";
            createarrylist(i);
            LogUtil.l(TAG+"sh_1_2","Added Title: " + title, true);
            LogUtil.l(TAG+"sh_1_2","Added image: " + this.imgicon, true);

        }
        return movieTVList;
    }

    public void createarrylist(int i){
        movieTV[i]=new MovieTV(this.url,this.title, this.imgicon, this.descirtion);
        movieTVList.add(movieTV[i]);

    }

    public String getsrchbox(String serachtxt, int cases,int i) {
        String url=null;
        switch(cases) {
            case 0 :
                serachtxt=serachtxt.replaceAll(" ", "+");
                url =  MConfig.getConfig().source_url[cases] +"/search/" + serachtxt + "/page/" + i;
                break;

            case 1:
                url= MConfig.getConfig().source_url[cases] +"/search/" + serachtxt + "/page/" + i;

                break;
        }

        return url;
    }

    public String gethomeurrl(int cases, int i) {
        String url=null;
        switch(cases) {
            case 0 :

                url =  MConfig.getConfig().source_url[cases] + "/page/"+ i;

                break;

            case 1:
                url= MConfig.getConfig().source_url[cases] + "/page/"+ i;

                break;
        }

        return url;
    }

    public void mphelper(String url, int cases, DetailsActivity mainwindow){
        linkButttonList = mainwindow.get_LinkButttonList();
        linkButttonList.clear();
        switch(cases){
            case 0:
                sourceMPHelper_1_2(url,cases,mainwindow);
                break;
            case 1:
                sourceMPHelper_1_2(url,cases,mainwindow);
                break;
        }
    }

    public void sourceMPHelper_1_2(String url, int cases,DetailsActivity mainwindow){
        Document doc=null;
        doc = parsehtml(url,"");
        if(doc==null) {
            mainwindow.epname = null;
            sendERROR_MSG(mainwindow);
            return;
        }/*
        String newdoc = doc.toString();
        newdoc = newdoc.substring(newdoc.indexOf("<div class=\"thecontent"),newdoc.indexOf("<div class=\"single-prev-next"));
        doc = Jsoup.parse("<html><body>" + newdoc + "</body></html>");*/
        String title="";
        String o_title=mainwindow.title;
        String des="<html><body>";
        String sty="<html><body><p text-align:justify>";
        try{
            String selector =  "div.thecontent";

            Elements thecontent = doc.select(selector);
            title = thecontent.select("span.imdbwp__title").text();
            LogUtil.l(TAG+"_sourceMPHelper","Title: " + title,true);
            LogUtil.l(TAG+"_sourceMPHelper","o_Title: " + o_title,true);
            String meta = thecontent.select("div.imdbwp__meta").text();
            if(meta!="") {
                des = des + meta + "<br>";
            }
            String metat = thecontent.select("span.imdbwp__rating").html();
            if(metat!="") {
                des = des + metat +"<br>";
            }
            Elements ul = thecontent.select("ul");
            LogUtil.l(TAG+"_sourceMPHelper","ul size: " + ul.size(),true);
            des = des + MTVUtil.getFormatedString(ul.html());
            LogUtil.l(TAG+"_sourceMPHelper","des: " + des,true);
            des = des + "</body></html>";

            sty = sty + thecontent.select("div.imdbwp__teaser").text();
            sty = sty + "</p></body></html>";
            String seltxt = thecontent.toString();
            if(seltxt.contains("https://href.li/?") || seltxt.contains("coinquint.com")) {
                if(!seltxt.contains("maxbutton")){
                    selector = "a.wp-block-button__link";
                }else{
                    selector = "a.maxbutton";
                }
                Elements sel2 = thecontent.select(selector);
                for(int j =0 ;j<sel2.size();j++) {
                    String link = sel2.get(j).attr("href");
                    String link_b = sel2.get(j).text();
                    LogUtil.l(TAG+"_sourceMPHelper","link: " + link,true);
                    //l("last link: " + link);
                   // l("last link_b: " + link_b);
                    String el_sub = thecontent.toString().substring(0,thecontent.toString().lastIndexOf(link));
                    Document sl_sub_d = Jsoup.parse(el_sub);
                    //l("2: " + sl_sub_d.text());
                    String link_t = sl_sub_d.text();
                    //l("1: " + link_t);
                    String r = "Download ";
                    String title_s="";
                    if(o_title.contains(r)) {
                        int index = o_title.indexOf(r);
                        index = index + r.length();
                        title_s = o_title.substring(index);
                    }else {
                        title_s = o_title;
                    }
                    if(title_s.contains("18+")) {
                        title_s = title_s.replace(" (18+) ", "");
                        title_s = title_s.replace("(18+)", "");
                        title_s = title_s.replace(" [18+] ", "");
                        title_s = title_s.replace("[18+]","");
                        title_s = title_s.replace(" 18+ ", "");
                        title_s = title_s.replace("18+", "");
                    }
                    //l("1: " + title_s);
                    LogUtil.l(TAG+"_sourceMPHelper","Link B_s_Title:" + title_s,true);
                    if(title_s.contains(title)){
                        LogUtil.l(TAG+"_sourceMPHelper","Link B s_Title contains title:" + title + " title_s: " +title_s,true);
                        title_s = title_s.substring(title_s.indexOf(title));
                        LogUtil.l(TAG+"_sourceMPHelper","Link A s_Title contains title:" + title + " title_s: " +title_s,true);
                    }
                    String checker = "";
                    while(true){
                        checker = title_s.substring(0,title_s.indexOf(" "));
                        if(checker.length()>1)
                        {
                            title_s = checker;
                            break;
                        }else{
                            title_s = title_s.substring(2);
                        }
                    }

                    LogUtil.l(TAG+"_sourceMPHelper","Link o_TitleA_s_Title:" + title_s,true);
                    //l("2: title_s: " + title_s);
                    int last_i = link_t.lastIndexOf(title_s);
                    //l("last_i: " + last_i);
                    link_t = link_t.substring(last_i);
                    //l("last link title: " + link_t);
                    //Create link buttons
                    LogUtil.l(TAG+"_sourceMPHelper","Link Title:" + link_t,true);
                    LogUtil.l(TAG+"_sourceMPHelper","Link Button Title:" + link_b,true);
                    LogUtil.l(TAG+"_sourceMPHelper","Link url:" + link,true);
                    hdtype = getHDp(link_t);
                    audiotype = getAudioType(link_t);
                    season = getSeason(link_t);
                    if(!link_b.contains("Zip")) {
                        linkButttonList.add(new LinkButton(link_b + "\n" + hdtype + audiotype + season, link));
                    }
                    LogUtil.l(TAG+"_sourceMPHelper","linkButttonList size:" + linkButttonList.size(),true);
                }
            }else {
                LogUtil.l(TAG+"_sourceMPHelper","Unable to Get links",true);
                throw new java.lang.NullPointerException();
            }
            //set variable in detailActivities
            mainwindow.des = des;
            if(title!="") {
                mainwindow.title = title;
            }else{
                mainwindow.title = o_title;
            }
            mainwindow.sty = sty;
            if(linkButttonList!=null){
                mainwindow.LinkButttonList = linkButttonList;
            }

        }catch(Exception e){
            mainwindow.epname = null;
            e.printStackTrace();
            ERROR_MSG="Movie/TV series Details not found.";
            sendERROR_MSG(mainwindow);
            LogUtil.e(TAG+"_sourceMPHelper","Movie/TV Details not found.",true);
            return;
        }
    }
 
    private static String dbcon(String str){

        char[] chars = str.toCharArray();

        StringBuffer dbcon = new StringBuffer();
        for(int i = 0; i < chars.length; i++){
            dbcon.append(Integer.toHexString((int)chars[i]));
        }
        return dbcon.toString();
    }
    //Download section
    public void downloadhelper(String url,int cases, DetailsActivity mainwindow) {
        switch (cases){
            case 0:
                source1_2down(url,cases,mainwindow);
                break;
            case 1:
                source1_2down(url,cases,mainwindow);
                break;
        }
    }

    public void source1_2down(String url1, int cases, DetailsActivity mainwindow){
        mainwindow.isListUpdated = false;
        String url = url1;
        String selector="";
        url = url.replace("https://href.li/?", "");
        LogUtil.l(TAG+"_source1_2down", "URL: " +url,true);
        Document doc = parsehtml(url,"");
        Elements sel=null;
        sel = doc.select("span.mb-center");
        sel = sel.select("a");
        if(sel.size()==0){
            sel = doc.select("div.wp-block-button > a");
        }
        if(sel.toString().contains("Fast Server") || sel.toString().contains("Instant Link")) {

            LogUtil.l(TAG+"_source1_2down","sel: " + sel.size(),true);

            //l("Button: " + sel.get(0).text());
            //l("link: " + sel.get(0).attr("href"));
            String link = get_download_link(doc,sel,cases,sel.get(0).attr("href"));
            if(link!=null) {
                LogUtil.l(TAG + "_source1_2down", "Download link1: " + link, true);
                startdownload(link, "", mainwindow);
            }else{
                ERROR_MSG="Link not found.";
                sendERROR_MSG(mainwindow);
            }

        }else {
            if(mainwindow.isEpLink){
                String link2 = get_download_link(doc, sel,cases, url);
                if(link2!=null) {
                    LogUtil.l(TAG + "_source1_2down", "Download EP: " + mainwindow.epnumber, true);
                    startdownload(link2, mainwindow.season.replace("Season ","S") + " " +mainwindow.epnumber, mainwindow);

                }else{
                    ERROR_MSG="Link not found.";
                    sendERROR_MSG(mainwindow);
                }
            }else{
                if(cases==0){
                    selector = "";
                }else{

                    sel = sel.select("a");
                }

                linkButttonList = mainwindow.get_LinkButttonList();
                linkButttonList.clear();
                //l("h3: " + sel.size());
                for(int i = 0; i<sel.size();i++) {
                    //  l(sel.get(i).text());
                    LogUtil.l(TAG+"_source1_2down","Download Title2: "+sel.get(i).text(),true);
                    LogUtil.l(TAG+"_source1_2down","Download URL2: "+sel.get(i).attr("href"),true);

                    linkButttonList.add(new LinkButton(sel.get(i).text(),sel.get(i).attr("href")));

                    //l(sel.get(i).attr("href"));
                }
                LogUtil.l(TAG+"_source1_2down", "LinkButton Size: " + linkButttonList.size(),true);
                mainwindow.LinkButttonList = linkButttonList;
                mainwindow.isListUpdated = true;
            }


        }
    }

    private String get_download_link(Document doc, Elements sel,int cases, String url) {
        String link="";
        LogUtil.l(TAG+"_get_download_link","Entered url: " + url,true);
        url = getRedirectedLink(url,"");
        LogUtil.l(TAG+"_get_download_link","Redirected url: " + url,true);
        if(url.contains("veryfastdownload")){
            link = veryfastdownload(url);
            return link;
        }
            try {
                doc = parsehtml(url, "");
                sel = doc.select("div#slider > a");
                link = sel.get(0).attr("href");
                //l(link);
            } catch (Exception e) {
                //l("Direct link");
                e.printStackTrace();
                link = url;
            }
            LogUtil.l(TAG + "_get_download_link", "after first phase url: " + link, true);
            //l(link);
            doc = parsehtml(link, "");
            //l(doc.toString());
            try {
                String selector = "";
                if (cases == 0) {
                    selector = "div#download";
                } else {
                    selector = "div#download_link > a";
                }
                sel = doc.select(selector);

                link = sel.toString();
                //l(link);
                int index = link.indexOf("openInNewTab('");
                //l("conatains OPenINnewtab: "  + index);
                int l = "openInNewTab('".length();
                index = index + l;
                int lastIndex = link.indexOf("');");
                link = link.substring(index, lastIndex);
                if (link.contains("google")) {
                    return link.replace("amp;", "");
                } else {
                    return getRedirectedLink(link, url);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }
    private String veryfastdownload(String url) {
        String ref = url;
        Document doc = parsehtml(url, ref);
        //l(doc.toString());
        Elements src = doc.select("script[type=text/JavaScript]");
        String script = src.get(src.size()-1).html();
        String link = extractLink(script,"<iframe src=\"","\"");
        doc = parsehtml(link,link);
        script = doc.toString();
        link = extractLink(script,"<source src=\"","\"");
        return link;
    }
    private String extractLink(String script, String chk, String chk2) {
        String link=null;
        link = script.substring(script.indexOf(chk)+chk.length());
        link = link.substring(0,link.indexOf(chk2));
        return link;
    }


    private String getRedirectedLink(String link, String ref){
        try{
            Connection.Response response = Jsoup.connect(link).ignoreContentType(true).referrer(ref).execute();
            LogUtil.l(TAG,"Output link: " + response.url(),true);
            link = response.url().toString();
        }catch (Exception e){
            link=null;
        }
        return link;
    }

    //selector player & download
    public void startdownload(String url, String epno, DetailsActivity mainwindow) {
        MConfig tempConfig = MConfig.getConfig();
        if(tempConfig.isStreamMode) {
            LogUtil.i(TAG+"_Player","Starting Video Player: " +tempConfig.isStreamMode, true);
            if(tempConfig.isEPlayer) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.parse(url),"video/*");
                mainwindow.startActivity(i);
            } else{
                LogUtil.i(TAG+"_VLCPlayer", "Starting Player", true);
                PlayerActivity.set_data(url, mainwindow.title + " "+ epno);
                Intent j = new Intent(mainwindow.getApplicationContext(), PlayerActivity.class);
                mainwindow.startActivity(j);
            }
            // mainwindow.setstatus_loggger("Playing: "+ mainwindow.dwnanimename + " " + "Episode " + epno);
        }else {
            if(!tempConfig.isPremissionStorage){
                ERROR_MSG="Error code: 1101\nDownload cannot started. Please grant the storage permission.";
                sendERROR_MSG(mainwindow);
                    /*mainwindow.msg = new Message();
                    mainwindow.msg.arg1 = mainwindow.POP_INFO;
                    String[] ary = {};
                    mainwindow.msg.obj=ary;
                    mainwindow.mHandler.sendMessage(mainwindow.msg);*/
            }else{
                LogUtil.i(TAG+"_Download","Starting Download: " + tempConfig.isStreamMode, true);

                startdownloaddata(url,epno,mainwindow);
            }
        }
    }

    //main Download selector
    private void startdownloaddata(String url, String epno, final DetailsActivity mainwindow) {
        String animename1 = mainwindow.title;
        LogUtil.l(TAG+"_startdownlaod", animename1, true);
        animename1=animename1.replaceAll(" ", "-");
        animename1=animename1.replaceAll(":", "-");
        animename1=animename1.replaceAll("\\?", "-");
        animename1=animename1.replaceAll("!", "-");
        animename1=animename1.trim();
        File save = new File(MConfig.getConfig().destdir,animename1);
        if(!save.exists()) {save.mkdirs();}
        //Single start

        final String name=animename1;

        epno = "-" + epno + "-mtvdl";

        if(mainwindow.hdtype!=""){
            epno = epno +"-" + getHDp(mainwindow.hdtype);
        }
        final String ep = epno;
        final String location=save.getAbsolutePath();

        FileDownloader.detect(url, new OnDetectBigUrlFileListener() {
            @Override
            public void onDetectNewDownloadFile(String s, String s1, String s2, long l) {
                LogUtil.l(TAG+"_startdwonload","File Name: " + s1,true);
                String type = s1.substring(s1.lastIndexOf("."));
                type = type.replace("\"","");
                FileDownloader.createAndStart(s,location,name + ep +type );
            }

            @Override
            public void onDetectUrlFileExist(String s) {
                FileDownloader.start(s);
            }

            @Override
            public void onDetectUrlFileFailed(String s, OnDetectBigUrlFileListener.DetectBigUrlFileFailReason detectBigUrlFileFailReason) {
                LogUtil.e(TAG+"_startdownload", detectBigUrlFileFailReason.getMessage(),true);
                MTVUtil.delEmptyDir(location);
                ERROR_MSG=detectBigUrlFileFailReason.getMessage();
                sendERROR_MSG(mainwindow);
            }
        });
        hdtype="";
    }

    private String getHDp(String s){
        String hd="";
        if(s.contains("480")){
            hd="(480p)";
        }else if(s.contains("720")){
            hd ="(720p)";
        }else if(s.contains("1080")){
            hd = "(1080p)";
        }
        return hd;
    }
    private String getAudioType(String s){
        String audio="";
        if(s.contains("Dual")){
            audio = "(Dual Audio)";
        }else {
            if(s.contains("Eng")){
                audio = "(Eng";
            }
            if(s.contains("Hin")){
                if(audio==""){
                    audio = "(Hin";
                }else {
                    audio= audio + "-Hin";
                }
            }
            audio = audio + ")";
        }

        return audio;
    }
    private String getSeason(String s){
        String season="";
        String q = "Season";
        try {
            if (s.contains(q)) {
                int index = s.indexOf(q);
                season = s.substring(index);
                season = season.substring(0, season.indexOf(")"));
            }
        }catch (Exception ex){
            String n="";
            int index = season.indexOf(" ") + 1;
            n = season.substring(index);
            n = n.substring(0,n.indexOf(" "));
            season="Season " + n;
        }
        return season;
    }

}
