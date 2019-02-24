package com.example.yuqi.dancenote.data;

import java.io.Serializable;

public class PathObj implements Serializable {
    private String path;
    private int mode;  /* 0 - file mode, 1 - url mode */

    public PathObj(String path, int mode) {
        this.path = path;
        this.mode = mode;
    }

    public String getPath() { return path; }

    public int getMode() { return mode; }

}
