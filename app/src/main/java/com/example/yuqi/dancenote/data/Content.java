package com.example.yuqi.dancenote.data;

import java.io.Serializable;

public class Content implements Serializable {
    private String content;
    private int mode;  /* 0 - display mode, 1 - edit mode */

    public Content(String content, int mode) {
        this.content = content;
        this.mode = mode;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
