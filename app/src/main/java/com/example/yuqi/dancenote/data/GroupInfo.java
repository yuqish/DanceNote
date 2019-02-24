package com.example.yuqi.dancenote.data;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupInfo implements Serializable{
    private ArrayList<Note> note;
    private String title;
    public GroupInfo(ArrayList<Note> note, String title){
        this.note = note;
        this.title = title;
    }

    public void setNote(ArrayList<Note> note) {
        this.note = note;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Note> getNote() {
        return note;
    }

    public String getTitle() {
        return title;
    }
}
