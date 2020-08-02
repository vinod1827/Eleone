package com.morgat.eleone.models;

import java.io.Serializable;

/**
 * Created by AQEEL on 2/18/2019.
 */

public class HomeModel implements Serializable {
    public String fb_id,first_name,last_name,profile_pic;
    public String video_id,video_description,video_url,gif,thum,created_date;

    public String sound_id,sound_name,sound_pic;

    public String liked,like_count,video_comment_count,views;

    @Override
    public String toString() {
        return "HomeModel{" +
                "fb_id='" + fb_id + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", profile_pic='" + profile_pic + '\'' +
                ", video_id='" + video_id + '\'' +
                ", video_description='" + video_description + '\'' +
                ", video_url='" + video_url + '\'' +
                ", gif='" + gif + '\'' +
                ", thum='" + thum + '\'' +
                ", created_date='" + created_date + '\'' +
                ", sound_id='" + sound_id + '\'' +
                ", sound_name='" + sound_name + '\'' +
                ", sound_pic='" + sound_pic + '\'' +
                ", liked='" + liked + '\'' +
                ", like_count='" + like_count + '\'' +
                ", video_comment_count='" + video_comment_count + '\'' +
                ", views='" + views + '\'' +
                '}';
    }
}
