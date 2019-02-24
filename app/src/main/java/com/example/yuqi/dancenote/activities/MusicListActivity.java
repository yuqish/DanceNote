package com.example.yuqi.dancenote.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yuqi.dancenote.R;
import com.example.yuqi.dancenote.adapters.MusicAdapter;
import com.example.yuqi.dancenote.data.Music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {
    private String TAG = "MusicListActivity";
    private List<Music> musicList;
    private MusicAdapter ma;
    private Music music_selected;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_list);
        musicList = new ArrayList<>();
        initListView();

        ListView musiclv = findViewById(R.id.list_music);
        ma = new MusicAdapter(this, musicList); //create adapter
        musiclv.setAdapter(ma);
        musiclv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                music_selected = musicList.get(i);

                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putString("Music", music_selected.title);
                intent.putExtras(bundle);
                setResult(10, intent);
                finish();//activity ends
            }
        });
    }

    private void initListView() {

        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null); //创建游标MediaStore.Audio.Media.EXTERNAL_CONTENT_URI获取音频的文件，后面的是关于select筛选条件，这里填土null就可以了
        if(cursor.moveToFirst()) {
            do {
                Log.d(TAG, "enter do");
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));            //获取歌名
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));         //获取歌唱者
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));           //获取专辑名
                int albumID = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));            //获取专辑图片id
                int length = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                //create Music object and give value
                Music music = new Music();
                music.length = length;
                music.title = title;
                music.artist = artist;
                music.album = album;
                music.path = path;
                music.albumBip = getAlbumArt(albumID);
                //put music into list
                musicList.add(music);

                Log.d(TAG, "music path:" + music.path);

            }
            while (cursor.moveToNext()); //when cursor reached bottom: cursor.moveToNext()==false
        }else{
            Toast.makeText(this, "Cannot find any stored music on device", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        sortMusicItems();
    }

    private void sortMusicItems() {
        for(int n=0; n<musicList.size(); n++){
            for(int i=0,j=i+1; i<musicList.size()-1-n; i++,j++){
                if(musicList.get(i).compareTo(musicList.get(j))>0){
                    Music temp = musicList.get(i);
                    musicList.set(i,musicList.get(j));
                    musicList.set(j,temp);
                }
            }
        }
    }

    //get album picture
    private Bitmap getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = this.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            bm = BitmapFactory.decodeResource(getResources(), R.mipmap.cd);
        }
        return bm;
    }
}
