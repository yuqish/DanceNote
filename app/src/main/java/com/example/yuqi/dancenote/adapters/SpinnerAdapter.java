package com.example.yuqi.dancenote.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.example.yuqi.dancenote.data.GroupInfo;

public class SpinnerAdapter extends ArrayAdapter<GroupInfo> {
    public SpinnerAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }
}
