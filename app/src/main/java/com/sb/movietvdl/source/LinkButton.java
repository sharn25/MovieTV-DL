package com.sb.movietvdl.source;

public class LinkButton {
    private String epname;
    private String epurl;
    public LinkButton(String epname, String epurl){
        this.epname = epname;
        this.epurl = epurl;
    }

    public String getEpname() {
        return epname;
    }

    public void setEpname(String epname) {
        this.epname = epname;
    }

    public String getEpurl() {
        return epurl;
    }

    public void setEpurl(String epurl) {
        this.epurl = epurl;
    }
}
