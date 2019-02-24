package com.example.yuqi.dancenote.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.yuqi.dancenote.Utilities;
import com.example.yuqi.dancenote.data.Content;
import com.example.yuqi.dancenote.data.GroupInfo;
import com.example.yuqi.dancenote.data.Note;
import com.example.yuqi.dancenote.adapters.NoteAdapter;
import com.example.yuqi.dancenote.R;

import java.util.ArrayList;
import java.util.Objects;
import com.master.permissionhelper.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static ArrayList<GroupInfo> note_group;
    public static int group_pos;
    public static int child_pos;
    private ExpandableListView eListView;
    private NoteAdapter na;
    private Spinner spinner;
    private String[] m;
    private ArrayAdapter<String> spinner_adpt;
    private PermissionHelper permissionHelper;

    /*prevent double click in short time*/
    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        eListView = findViewById(R.id.mElistview);

        // 关于权限的代码
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Log.d(TAG, "onPermissionGranted() called");
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
                Log.d(TAG, "onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",", grantedPermission) + "]");
            }

            @Override
            public void onPermissionDenied() {
                Log.d(TAG, "onPermissionDenied() called");
            }

            @Override
            public void onPermissionDeniedBySystem() {
                Log.d(TAG, "onPermissionDeniedBySystem() called");
            }
        });
// 权限代码结束
    }

    @Override
    protected void onResume() {
        super.onResume();
        note_group = Utilities.getSavedInfo(this);
        na = new NoteAdapter(note_group, this);
        eListView.setAdapter(na);
        eListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view,
                                        int group_position, int child_position, long l) {

                /*prevent double click in short time*/
                if (System.currentTimeMillis() - lastClickTime < FAST_CLICK_DELAY_TIME){
                    Log.w(TAG, "double clicked in short time");
                    return false;
                }
                lastClickTime = System.currentTimeMillis();
                Log.w(TAG,"set last click time:"+lastClickTime);

                Note note = note_group.get(group_position).getNote().get(child_position);
                child_pos = child_position;
                group_pos = group_position;
                Intent viewNoteIntent = new Intent(getApplicationContext(),NoteListActivity.class);
                viewNoteIntent.putExtra("NOTE",note);
                viewNoteIntent.putExtra("isNewNote",0);
                startActivityForResult(viewNoteIntent,1);
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_main_new_note:
                Toast.makeText(this, "create your new note!", Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Please enter a note name and a group name");

                /*use own layout for alert dialog*/
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_note_dialog, null);
                builder.setView(view);

                final EditText etChild = (EditText)view.findViewById(R.id.edit_child);
                final EditText etGroup = (EditText)view.findViewById(R.id.edit_group);
                spinner = view.findViewById(R.id.spinner_group);

                /*existing group: disable editText*/
                etGroup.setVisibility(View.INVISIBLE);

                /*init string array for spinner*/
                m = new String[note_group.size()+1];
                for (int i=0; i<note_group.size(); i++){
                    m[i] = note_group.get(i).getTitle();
                }
                m[note_group.size()] = "<new group>";

                /*set spinner adapter*/
                spinner_adpt = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
                spinner_adpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinner_adpt);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        group_pos = i;

                        if(i == note_group.size()){   /* new group, enable editText */
                            etGroup.setVisibility(View.VISIBLE);
                        }else{
                            etGroup.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                /*set default value*/
                spinner.setVisibility(View.VISIBLE);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String a = etChild.getText().toString();
                        String b = etGroup.getText().toString();
                        Note note = new Note(a, new ArrayList<Content>());

                        if(group_pos == note_group.size()){ /* new group */
                            ArrayList<Note> note_array = new ArrayList<Note>();
                            note_array.add(note);
                            note_group.add(new GroupInfo(note_array,b));
                            child_pos = 0;
                        }else{
                            note_group.get(group_pos).getNote().add(note);
                            child_pos = note_group.get(group_pos).getNote().size()-1;
                        }

                        /* start NoteListActivity in new note mode */
                        Intent newNoteActivity = new Intent(getApplicationContext(), NoteListActivity.class);
                        newNoteActivity.putExtra("NOTE",note);
                        newNoteActivity.putExtra("isNewNote",1);
                        startActivityForResult(newNoteActivity,1);
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                break;

        }
        return true;
    }

}
