package com.example.yuqi.dancenote.activities;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuqi.dancenote.data.Content;
import com.example.yuqi.dancenote.adapters.NoteContentAdapter;
import com.example.yuqi.dancenote.R;
import com.example.yuqi.dancenote.Utilities;
import com.example.yuqi.dancenote.data.Note;
import com.example.yuqi.dancenote.data.PathObj;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.yuqi.dancenote.activities.MainActivity.child_pos;
import static com.example.yuqi.dancenote.activities.MainActivity.group_pos;
import static com.example.yuqi.dancenote.activities.MainActivity.note_group;
import static com.example.yuqi.dancenote.Utilities.cp_content;


public class NoteListActivity extends AppCompatActivity {

    private static final String TAG = "NoteListActivity";
    private ListView mNoteList;
    private TextView mTitle;
    private Note note;
    private ArrayList<Content> list_content;
    private NoteContentAdapter na;
    private int isNew;
    private String[] items1;
    private String[] items2 = { "Select Image From Gallery","Select Video From Gallery","Record a Video","URL Resource" };
    private String[] items3 = {"Open","Delete"};
    private static final int CHOOSE_PHOTO = 100;
    private static final int CHOOSE_VIDEO = 200;
    private static final int TAKE_VIDEO = 300;
    private Uri videoUri;
    File outputVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notelist);
        mNoteList = (ListView) findViewById(R.id.note_list);
        mTitle = (TextView) findViewById(R.id.title) ;
        note = (Note)getIntent().getSerializableExtra("NOTE");
        isNew = getIntent().getIntExtra("isNewNote",0);
        refreshList(); /*loading resource list to gallery*/

        if(note != null){
            mTitle.setText(note.getmTitle());
            list_content = note.getmContent();
        }else{
            Log.w(TAG, "error getting note");
        }

        na = new NoteContentAdapter(this, R.layout.item_note_content, list_content, mListener);
        mNoteList.setAdapter(na);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void refreshList(){
        ArrayList<PathObj> pathObjs = note.getFile_path();
        items1 = new String[note.getFile_path().size()+1];

        for (int i=0; i<items1.length-1; i++) {
            if (pathObjs.get(i).getMode() == 0) {   /* file path */
                items1[i] = note.getSingleFilepath(i).substring(note.getSingleFilepath(i).lastIndexOf("/") + 1);
                Log.w(TAG, "file item added, path = " + note.getSingleFilepath(i));
            } else {     /* url path */
                if (note.getSingleFilepath(i).length() > 50){
                    items1[i] = note.getSingleFilepath(i).substring(0,50);
                } else {
                    items1[i] = note.getSingleFilepath(i);
                }
                Log.w(TAG, "URL item added, path = " + note.getSingleFilepath(i));
            }
        }
        items1[note.getFile_path().size()] = "Add New Resource";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_notelist, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_resources:
                AlertDialog.Builder listDialog =
                        new AlertDialog.Builder(this);
                listDialog.setTitle("Resources Option");
                listDialog.setItems(items1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createAnotherDialog(which);
                    }
                });
                listDialog.show();
                break;

            case R.id.action_list_new_note:
                Toast.makeText(this, "create your new note item!", Toast.LENGTH_SHORT).show();
                /* insert note item at the bottom of list */
                list_content.add(new Content("",1));
                Log.w(TAG, "note_content.size:"+ list_content.size());
                na.notifyDataSetChanged();
                break;

            case R.id.action_insert:
                if(na.position_selected < 0 || na.position_selected >= list_content.size()){
                    Toast.makeText(this, "please select a item to insert before", Toast.LENGTH_SHORT).show();
                }else{
                    list_content.add(na.position_selected, new Content("", 1));
                    na.position_selected = na.position_selected + 1;
                    na.notifyDataSetChanged();
                    Toast.makeText(this, "inserted an item before selected item!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_edit:
                if(na.position_selected < 0 || na.position_selected >= list_content.size()){
                    Toast.makeText(this, "please select an item to edit", Toast.LENGTH_SHORT).show();
                }else{
                    Content content = list_content.get(na.position_selected);
                    content.setMode(1);
                    list_content.set(na.position_selected, content);
                    na.position_selected = -1;
                    na.notifyDataSetChanged();
                    Toast.makeText(this, "enter edit mode for selected item!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_delete:
                if(na.position_selected < 0 || na.position_selected >= list_content.size()){
                    Toast.makeText(this, "please select an item to delete", Toast.LENGTH_SHORT).show();
                }else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                            .setTitle("delete?").setMessage("are you sure to delete the selected item? ")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    list_content.remove(na.position_selected);
                                    na.position_selected = -1;
                                    na.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(),
                                            "item deleted!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("no", null);

                    dialog.show();
                }
                break;

            case R.id.action_copy:
                if(na.position_selected < 0 || na.position_selected >= list_content.size()){
                    Toast.makeText(this, "please select an item to copy", Toast.LENGTH_SHORT).show();
                }else{
                    cp_content = list_content.get(na.position_selected);
                    Toast.makeText(this, "selected item was copied successfully! " +
                     "click the paste button to paste", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_cut:
                if(na.position_selected < 0 || na.position_selected >= list_content.size()){
                    Toast.makeText(this, "please select an item to cut", Toast.LENGTH_SHORT).show();
                }else{
                    cp_content = list_content.get(na.position_selected);
                    list_content.remove(na.position_selected);
                    na.position_selected = -1;
                    na.notifyDataSetChanged();
                    Toast.makeText(this, "selected item was cut successfully! " +
                            "click the paste button to paste", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_paste_insert:
                if(cp_content == null){
                    Toast.makeText(this, "please select an item to copy first", Toast.LENGTH_SHORT).show();
                }else if(na.position_selected < 0 || na.position_selected >= list_content.size()){
                    Toast.makeText(this, "please select an item to insert before", Toast.LENGTH_SHORT).show();
                }else{
                    Content content = new Content(cp_content.getContent(), cp_content.getMode());
                    list_content.add(na.position_selected, content);
                    na.position_selected = na.position_selected + 1;
                    na.notifyDataSetChanged();
                    Toast.makeText(this, "item pasted before selected item!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_paste_add:
                if(cp_content == null){
                    Toast.makeText(this, "please select an item to copy first", Toast.LENGTH_SHORT).show();
                } else{
                    Content content = new Content(cp_content.getContent(), cp_content.getMode());
                    list_content.add(content);
                    na.notifyDataSetChanged();
                    Toast.makeText(this, "item pasted at the end!", Toast.LENGTH_SHORT).show();
                }
                break;

            case android.R.id.home:
                this.finish();
                return true;

        }
        return true;
    }

    private void createAnotherDialog(final int n) {
        if(n == items1.length-1){  /* new resource */
            AlertDialog.Builder listDialog =
                    new AlertDialog.Builder(this);
            listDialog.setTitle("Resources Option");
            listDialog.setItems(items2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // ...To-do
                    if (which == 0) {   /* chose photo from album */
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_PHOTO);//open album

                    } else if (which == 1) {     /* chose video from album */
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("video/*");
                        startActivityForResult(intent, CHOOSE_VIDEO);//open album

                    }  else if (which == 2) {    /* take a video */
                        File outputVideoDir = new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera");
                        if (!outputVideoDir.exists()) {
                            outputVideoDir.mkdirs();
                        }

                        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                        String dateStr = dateformat.format(System.currentTimeMillis());
                        outputVideo = new File(outputVideoDir, "dn_"+dateStr+"vid.mp4");
                        Log.w(TAG, "video file path:"+outputVideo.getPath());

                        //above or below android 7.0
                        if (Build.VERSION.SDK_INT >= 24) {
                            videoUri = FileProvider.getUriForFile(NoteListActivity.this, "com.example.yuqi.dancenote.fileprovider", outputVideo);
                        } else {
                            videoUri = Uri.fromFile(outputVideo);
                        }
                        //start camera
                        Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                        startActivityForResult(intent, TAKE_VIDEO);
                    } else if (which == 3) {        /* manually enter an URL */

                        final EditText editText = new EditText(NoteListActivity.this);
                        AlertDialog.Builder inputDialog =
                                new AlertDialog.Builder(NoteListActivity.this);
                        inputDialog.setTitle("Input URL Adress").setView(editText);
                        inputDialog.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String url = editText.getText().toString();
                                        note.addSingleFilePath(url, 1);
                                        refreshList();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                }
            });
            listDialog.show();
        }else{   /* existing resource clicked */
            AlertDialog.Builder listDialog =
                    new AlertDialog.Builder(this);
            listDialog.setTitle("Resources Option");
            listDialog.setItems(items3, new DialogInterface.OnClickListener() {
                String filePath = note.getSingleFilepath(n);
                int mode = note.getFile_path().get(n).getMode();

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) { /* open */
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Log.w(TAG, "opening file path:"+filePath);

                        if (mode == 0) {
                            File file = new File(filePath);
                            Uri uri;
                            if (file.exists()){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Log.w(TAG,"higher version file path:"+filePath);
                                    uri = FileProvider.getUriForFile(NoteListActivity.this, "com.example.yuqi.dancenote.fileprovider", file);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                } else {
                                    Log.w(TAG,"lower version file path:"+filePath);
                                    uri = Uri.fromFile(file);
                                }
                                intent.setData(uri);
                                startActivity(intent);
                            }else{
                                Toast.makeText(NoteListActivity.this, "error: file does not exist anymore! might be deleted", Toast.LENGTH_SHORT).show();
                            }
                        } else {   /* mode == 1 */

                            try {
                                Uri url_uri = Uri.parse(filePath);
                                intent.setData(url_uri);
                                startActivity(intent);

                            } catch (Exception e1) {
                                Log.w(TAG,"Connection fail!");
                                Toast.makeText(NoteListActivity.this, "error: invalid URL", Toast.LENGTH_SHORT).show();
                            }

                        }

                    } else if (which ==1) { /* delete */

                        AlertDialog.Builder dia = new AlertDialog.Builder(NoteListActivity.this)
                                .setTitle("delete?").setMessage("are you sure to delete the selected item? ")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Log.w(TAG, "deleting file path:"+filePath);
                                        note.removeSingleFilePath(n);
                                        refreshList();
                                        Toast.makeText(getApplicationContext(),
                                                "resource deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("no", null);

                        dia.show();
                    }
                }
            });
            listDialog.show();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Log.w(TAG, "resultCode = OK");
                    if(Build.VERSION.SDK_INT >= 19){
                        //above android 4.4
                        handleOnKitKat(data, true);
                    }else{
                        //below android 4.4
                        handleBeforeKitKat(data, true);
                    }
                }
                break;

            case CHOOSE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Log.w(TAG, "resultCode = OK");
                    if(Build.VERSION.SDK_INT >= 19){
                        //above android 4.4
                        handleOnKitKat(data, false);
                    }else{
                        //below android 4.4
                        handleBeforeKitKat(data, false);
                    }
                }
                break;

            case TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Log.w(TAG, "resultCode = OK");
                    note.addSingleFilePath(outputVideo.getPath(), 0);
                    refreshList();
                }
                break;
        }
    }

    @TargetApi(19)
    private void handleOnKitKat(Intent data, boolean is_image) {
        Log.w(TAG, "enter handleOnKitKat");
        String filePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(this, uri)) {
            Log.w(TAG, "is document uri");
            //document uri
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//parse to id
                if (is_image) {
                    String selection = MediaStore.Images.Media._ID + "=" + id;
                    filePath = getFilePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, true);
                } else {
                    String selection = MediaStore.Video.Media._ID + "=" + id;
                    filePath = getFilePath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, selection, false);
                }
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                filePath = getFilePath(contentUri, null, is_image);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Log.w(TAG, "is content uri");
            //content uri
            filePath = getFilePath(uri, null, is_image);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Log.w(TAG, "is file uri");
            //file uri
            filePath = uri.getPath();
        }

        Log.w(TAG, "image/video file path:"+filePath);
        note.addSingleFilePath(filePath,0);
        refreshList();
    }

    private void handleBeforeKitKat(Intent data, boolean is_image){
        Log.w(TAG, "enter handleBeforeKitKat");
        Uri uri = data.getData();
        String filePath = getFilePath(uri, null, is_image);
        note.addSingleFilePath(filePath,0);
        refreshList();
    }

    private String getFilePath(Uri uri, String selection, boolean is_image) {
        String path = null;

        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                if (is_image) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                }else {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                }
            }
            cursor.close();
        }
        return path;
    }

    /**
     * accomplishment class for abstract click listener
     */
    private NoteContentAdapter.MyClickListener mListener = new NoteContentAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, String content, View v) {
            list_content.get(position).setContent(content);
            list_content.get(position).setMode(0);
            na.notifyDataSetChanged();
            InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        }

        @Override
        public void setPositionFlag(int position) {
            na.position_selected = position;
            na.notifyDataSetChanged();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        /*save data when pause*/
        Log.w(TAG, "enter onPause");
        Note note_old = note_group.get(group_pos).getNote().get(child_pos);

        if(!note_old.equals(note) || isNew == 1){
            Note note_new = new Note(note.getmTitle(),new ArrayList<Content>());

            String content_i;
            String filePath_i;
            int mode;
            ArrayList<PathObj> pathObj = note.getFile_path();

            for (int i = 0; i<list_content.size();i++){
                content_i = list_content.get(i).getContent();
                note_new.getmContent().add(new Content(content_i,0));
            }

            for (int j = 0; j<note.getFile_path().size();j++){
                filePath_i = pathObj.get(j).getPath();
                mode = pathObj.get(j).getMode();
                note_new.addSingleFilePath(filePath_i, mode);
            }
            //note_new.setMusic_path(note.getMusic_path());
            note_group.get(group_pos).getNote().set(child_pos, note_new);
            Utilities.saveInfo(this, note_group);
            Log.w(TAG, "note updated");
            Toast.makeText(this, "note updated", Toast.LENGTH_SHORT).show();

            /*Saved: not a new note anymore*/
            isNew = 0;
        }
    }
}

