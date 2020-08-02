package com.sb.movietvdl.source;

    /*
    * Created by Sharn25
    * dated 12-06-2020
    */

public class MovieTV {
    private String url;
    private String title;
    private String imgicon;
    private String description;
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgicon() {
        return imgicon;
    }

    public void setImgicon(String imgicon) {
        this.imgicon = imgicon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MovieTV(String url, String title, String imgicon, String description){
        this.url = url;
        this.title = title;
        this.imgicon = imgicon;
        this.description = description;
    }
}
