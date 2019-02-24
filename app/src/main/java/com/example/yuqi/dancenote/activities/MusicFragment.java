package com.example.yuqi.dancenote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuqi.dancenote.R;
import com.example.yuqi.dancenote.data.Music;

import org.w3c.dom.Text;

public class MusicFragment extends Fragment{

    private static final String TAG = "MusicFragment";
    private Music music_selected;
    private TextView tv_music;

    /*prevent double click in short time*/
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        Button select = (Button) view.findViewById(R.id.select);
        tv_music = (TextView) view.findViewById(R.id.music_title);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*prevent double click in short time*/
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME){
                    Log.w(TAG, "double clicked in short time");
                    return;
                }
                lastClickTime = System.currentTimeMillis();
                Log.w(TAG,"set last click time:"+lastClickTime);

                Intent startMusicActivity = new Intent(getActivity(), MusicListActivity.class);
                startActivityForResult(startMusicActivity,1);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(TAG, "enter onActivityResult");
        if (requestCode == 1 && resultCode == 10) {
            Bundle b = data.getExtras();
            try {
                String music = b.getString("Music");
                Log.w(TAG, "music:"+ music);
                tv_music.setText(music);
            } catch (Exception e) {
                Log.w(TAG,"exception found");
            }
        }
    }
}
