package com.example.yuqi.dancenote.data;


import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable{

    private static final String TAG = "Note";
    private String mTitle;
    private ArrayList<Content> mContent;

        //music utilities
        private String music_title;
        private String music_path;
        private int music_length;

    private ArrayList<PathObj> file_path = new ArrayList<>();

    public Note(String mTitle, ArrayList<Content> mContent) {
        this.mTitle = mTitle;
        this.mContent = mContent;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmContent(ArrayList<Content> mContent) {
        this.mContent = mContent;
    }

    public void setMusic_path(String music_path) { this.music_path = music_path; }

    public void setMusic_length(int music_length) { this.music_length = music_length; }

    public void setMusic_title(String music_title) { this.music_title = music_title; }

    public void setFile_path(ArrayList<PathObj> file_path) { this.file_path = file_path; }

    public String getmTitle() {
        return mTitle;
    }

    public ArrayList<Content> getmContent() {
        return mContent;
    }

    public String getMusic_path() { return music_path; }

    public String getMusic_title() { return music_title; }

    public int getMusic_length() { return music_length; }

    public ArrayList<PathObj> getFile_path() {
        return file_path;
    }

    public String getSingleFilepath(int num) {
        String path;
        if(num<file_path.size()){
            path = file_path.get(num).getPath();
        }else{
            return null;
        }

        return path;
    }

    public void addSingleFilePath(String path, int mode){
        file_path.add(new PathObj(path, mode));
    }

    public void removeSingleFilePath(int num){
        file_path.remove(num);
    }

    @Override
    public boolean equals(Object obj) {
        Note note = (Note) obj;
        ArrayList<Content> content = note.getmContent();

        if(content.size() != mContent.size()){
            Log.w(TAG, "enter false for size different");
            return false;
        }
        for (int i=0; i< content.size();i++){
            if(!content.get(i).getContent().equals(mContent.get(i).getContent())){
                Log.w(TAG, "enter false for content different");
                return false;
            }
        }
        ArrayList<PathObj> paths = note.getFile_path();

        if(paths.size() != file_path.size()){
            Log.w(TAG, "enter false for file_path size different");
            return false;
        }
        for (int i=0; i< paths.size();i++){
            if(!paths.get(i).getPath().equals(file_path.get(i).getPath())){
                Log.w(TAG, "enter false for file_path content different");
                return false;
            }
        }

        Log.w(TAG, "no difference encountered");
        return true;
    }
}
