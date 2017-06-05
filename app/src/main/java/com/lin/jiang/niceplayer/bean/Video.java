package com.lin.jiang.niceplayer.bean;

/**
 * Created by jianglin on 17-6-5.
 */

public class Video {

    private String title;
    private String imgUrl;
    private String videoUrl;

    public Video(String title, String imgUrl, String videoUrl) {
        this.title = title;
        this.imgUrl = imgUrl;
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
