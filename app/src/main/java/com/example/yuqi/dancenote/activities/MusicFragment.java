package com.example.yuqi.dancenote.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.yuqi.dancenote.R;
import com.example.yuqi.dancenote.Utilities;
import com.example.yuqi.dancenote.data.Note;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.yuqi.dancenote.activities.MainActivity.child_pos;
import static com.example.yuqi.dancenote.activities.MainActivity.group_pos;
import static com.example.yuqi.dancenote.activities.MainActivity.note_group;

public class MusicFragment extends Fragment{

    private static final String TAG = "MusicFragment";
    private TextView tv_music_title;
    private TextView tv_current_time;
    private TextView tv_total_time;
    private ImageView start_pause;
    private Note note = note_group.get(group_pos).getNote().get(child_pos);
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private boolean thread_in_use = false;
    private MusicThread musicThread;
    private Thread mThread;

    /*prevent double click in short time*/
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.w(TAG,"enter onCreate for fragment");
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        Button select = (Button) view.findViewById(R.id.select);
        tv_music_title = (TextView) view.findViewById(R.id.music_title);
        tv_current_time = (TextView) view.findViewById(R.id.music_current_tv);
        tv_total_time = (TextView) view.findViewById(R.id.music_total_tv);

        seekBar = view.findViewById(R.id.music_seekbar);
        start_pause = view.findViewById(R.id.imgstart);

        mediaPlayer = new MediaPlayer();
        if (note.getMusic_path() != null && fileExists(note.getMusic_path())) {
            Log.w(TAG,"music path = "+note.getMusic_path());
            setMediaPlayer();
            thread_in_use = true;
        }

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

                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();   /* pause media player */
                    start_pause.setImageResource(R.drawable.play);
                }

                Intent startMusicActivity = new Intent(getActivity(), MusicListActivity.class);
                startActivityForResult(startMusicActivity,1);
            }
        });
        Log.w(TAG,"quiting onCreate for fragment");
        return view;
    }

    private boolean fileExists (String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            Log.w(TAG, "music file is deleted");
            return false;
        }
    }

    private void initMediaPlayer() {
        Log.w(TAG,"enter initMediaPlayer");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.w(TAG, "onCompletion called");
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(note.getMusic_path());
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                seekBar.setProgress(0);
                start_pause.setImageResource(R.drawable.play);

            }
        });

        if (note.getMusic_path() != null) {
            start_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mediaPlayer.isPlaying()) {
                        Log.w(TAG,"is not playing, start");
                        mediaPlayer.start();
                        start_pause.setImageResource(R.drawable.pause);
                    } else {
                        Log.w(TAG,"is playing, pause");
                        mediaPlayer.pause();
                        start_pause.setImageResource(R.drawable.play);
                    }
                }
            });
        } else {          /* for music removal */
            start_pause.setClickable(false);
        }

    }

    private void setMediaPlayer() {
        Log.w(TAG,"enter setMediaPlayer");
        start_pause.setImageResource(R.drawable.play);
        mediaPlayer.reset();
        try {
            if (note.getMusic_path() != null) {
                seekBar.setMax(note.getMusic_length());
                mediaPlayer.setDataSource(note.getMusic_path());
                mediaPlayer.prepare();
                tv_music_title.setText(note.getMusic_title());
                tv_total_time.setText(formatTime(note.getMusic_length()));
                thread_in_use = true;
            } else {         /* for music removal */
                seekBar.setProgress(0);
                tv_music_title.setText("No Music Selected");
                tv_current_time.setText(formatTime(0));
                tv_total_time.setText(formatTime(0));
                thread_in_use = false;
            }

        } catch (IllegalArgumentException | SecurityException | IllegalStateException
                | IOException e) {
            e.printStackTrace();
        }

        initMediaPlayer();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(TAG, "enter onActivityResult");
        if (requestCode == 1 && resultCode == 10) {
            Bundle b = data.getExtras();
            note = note_group.get(group_pos).getNote().get(child_pos);
            try {
                String title = b.getString("MusicTitle");
                String path = b.getString("MusicPath");
                int length = b.getInt("MusicLength");

                note.setMusic_title(title);
                note.setMusic_path(path);
                note.setMusic_length(length);
                Utilities.saveInfo(this.getContext(), note_group);

                Log.w(TAG, "music title:"+ title + ", music path:"+ path + ", music length:"+ length);

                setMediaPlayer();

            } catch (Exception e) {
                Log.w(TAG,"exception found");
            }
        } else if (requestCode == 1 && resultCode == 20) {
            note.setMusic_title(null);
            note.setMusic_path(null);
            note.setMusic_length(0);
            Utilities.saveInfo(this.getContext(), note_group);

            Log.w(TAG, "music removed");
            setMediaPlayer();
        }
    }

    //format time
    private String formatTime(int length) {
        Date date = new Date(length);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        String totaltime = simpleDateFormat.format(date);
        return totaltime;
    }

    //return current music time to main thread
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            seekBar.setProgress(msg.what);
            tv_current_time.setText(formatTime(msg.what));
        }
    };

    class MusicThread implements Runnable {

        @Override
        public void run() {
            while (note.getMusic_path() != null && thread_in_use) {
                try {
                    handler.sendEmptyMessage(mediaPlayer.getCurrentPosition());
                    Thread.sleep(200);
                    Log.w(TAG, "enter run MusicThread");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();   /* pause media player */
            start_pause.setImageResource(R.drawable.play);
        }
        thread_in_use = false;
    }

    @Override
    public void onResume() {
        Log.w(TAG,"enter on resume MusicFragment");
        super.onResume();
        if (note.getMusic_path() != null) {
            musicThread = new MusicThread();
            mThread = new Thread(musicThread);
            mThread.start();
            thread_in_use = true;
        }
    }
}
