package com.example.yuqi.dancenote.data;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class Music implements Comparable<Music>, Serializable {
    //歌名
    public String title;
    //歌唱者
    public String artist;
    //专辑名
    public  String album;
    public  int length;
    //专辑图片
    public Bitmap albumBip;
    public String path;

    @Override
    public int compareTo(@NonNull Music music) {
        /*for sorting irrespective to Upper/lower case*/
        if(this.title.compareToIgnoreCase(music.title) < 0){
            return -1;
        }else{
            return 1;
        }
    }
}
