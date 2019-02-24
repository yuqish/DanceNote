package com.example.yuqi.dancenote.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuqi.dancenote.R;
import com.example.yuqi.dancenote.Utilities;
import com.example.yuqi.dancenote.data.GroupInfo;
import com.example.yuqi.dancenote.data.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends BaseExpandableListAdapter {
    private ArrayList<GroupInfo> list;
    private Context         mContext;
    private int group_pos;  //group position intended to move to
    private String[] m;
    private ArrayAdapter<String> spinner_adpt;

    public NoteAdapter(ArrayList<GroupInfo> list, Context context) {
        this.list = list;
        this.mContext = context;
    }

    //组数
    @Override
    public int getGroupCount() {
        return list.size();
    }

    //子数
    @Override
    public int getChildrenCount(int groupPosition) {
        return list.get(groupPosition).getNote().size();
    }

    //组的对象
    @Override
    public Object getGroup(int groupPosition) {
        return list.get(groupPosition);
    }

    //子的对象
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return list.get(groupPosition).getNote().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //当子条目ID相同时是否复用
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //  is Expandad 展开列表
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder view_holder1 = null;
        if (convertView == null){
            view_holder1 = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.main_list_group, null);
            view_holder1.title = convertView.findViewById(R.id.tvml_group);
            view_holder1.item_menu = convertView.findViewById(R.id.popup_menu);
            convertView.setTag(view_holder1);
        }else{
            view_holder1 = (ViewHolder) convertView.getTag();
        }

        view_holder1.title.setText(list.get(groupPosition).getTitle());
        view_holder1.item_menu.setTag(groupPosition);
        view_holder1.item_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu menu = new PopupMenu(mContext, view);
                menu.inflate(R.menu.menu_note_group);
                menu.setOnMenuItemClickListener(
                        new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){
                                    case R.id.edit_name:
                                        final EditText editText = new EditText(mContext);
                                        editText.setText(list.get(groupPosition).getTitle());
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext)
                                                .setTitle("Edit Name").setMessage("Please enter a new name")
                                                .setView(editText)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        GroupInfo gi = list.get(groupPosition);
                                                        gi.setTitle(editText.getText().toString());
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext,
                                                                "group name is updated", Toast.LENGTH_SHORT).show();
                                                        Utilities.saveInfo(mContext, list);
                                                    }
                                                })
                                                .setNegativeButton("Cancel", null);

                                        dialog.show();
                                        break;
                                    case R.id.delete:
                                        dialog = new AlertDialog.Builder(mContext)
                                                .setTitle("Delete?").setMessage("Are you sure to delete the selected group?" +
                                                        " ALL items within the group will be deleted!!!")
                                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        list.remove(groupPosition);
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext,
                                                                "group deleted!", Toast.LENGTH_SHORT).show();
                                                        Utilities.saveInfo(mContext, list);
                                                    }
                                                })
                                                .setNegativeButton("no", null);

                                        dialog.show();
                                        break;
                                }
                                return false;
                            }
                        });
                menu.show();
            }
        });

        if (isExpanded) {
            convertView.setBackgroundColor(Color.LTGRAY);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    //isLastChild 子条目内容
    @Override
    public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder view_holder2 = null;
        if (convertView == null) {
            view_holder2 = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.main_list_child, null);
            view_holder2.title = convertView.findViewById(R.id.tvml_child);
            view_holder2.item_menu = convertView.findViewById(R.id.popup_menu);
            convertView.setTag(view_holder2);
        }else{
            view_holder2 = (ViewHolder) convertView.getTag();
        }

        view_holder2.title.setText(list.get(groupPosition).getNote().get(childPosition).getmTitle());
        view_holder2.item_menu.setTag(childPosition);
        view_holder2.item_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu menu = new PopupMenu(mContext, view);
                menu.inflate(R.menu.menu_note_child);
                menu.setOnMenuItemClickListener(
                        new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch(item.getItemId()){
                                    case R.id.edit_name:
                                        final EditText editText = new EditText(mContext);
                                        editText.setText(list.get(groupPosition).getNote().get(childPosition).getmTitle());
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext)
                                                .setTitle("Edit Name").setMessage("Please enter a new name")
                                                .setView(editText)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Note note = list.get(groupPosition).getNote().get(childPosition);
                                                        note.setmTitle(editText.getText().toString());
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext,
                                                                "note name is updated", Toast.LENGTH_SHORT).show();
                                                        Utilities.saveInfo(mContext, list);
                                                    }
                                                })
                                                .setNegativeButton("Cancel", null);

                                        dialog.show();
                                        break;
                                    case R.id.edit_group:
                                        final View view = View.inflate(mContext, R.layout.spinner_dialog, null);
                                        Spinner spinner = view.findViewById(R.id.spinner_group);
                                        /*init string array for spinner*/
                                        m = new String[list.size()+1];
                                        for (int i=0; i<list.size(); i++){
                                            m[i] = list.get(i).getTitle();
                                        }
                                        m[list.size()] = "<new group>";

                                        /*set spinner adapter*/
                                        spinner_adpt = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,m);
                                        spinner_adpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinner.setAdapter(spinner_adpt);
                                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                group_pos = i;
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> adapterView) {

                                            }
                                        });
                                        /*set default value*/
                                        spinner.setVisibility(View.VISIBLE);

                                        dialog = new AlertDialog.Builder(mContext)
                                                .setTitle("Change Group").setMessage("Please select a group to move into")
                                                .setView(view)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Note note = list.get(groupPosition).getNote().get(childPosition);
                                                        list.get(groupPosition).getNote().remove(childPosition);

                                                        if(group_pos == list.size()){ /* new group */
                                                            ArrayList<Note> note_array = new ArrayList<Note>();
                                                            note_array.add(note);
                                                            list.add(new GroupInfo(note_array,"New Group"));
                                                            notifyDataSetChanged();
                                                        }else{
                                                            list.get(group_pos).getNote().add(note);
                                                        }
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext,
                                                                "note has moved to group selected", Toast.LENGTH_SHORT).show();
                                                        Utilities.saveInfo(mContext, list);
                                                    }
                                                })
                                                .setNegativeButton("Cancel", null);

                                        dialog.show();
                                        break;
                                    case R.id.delete:
                                        dialog = new AlertDialog.Builder(mContext)
                                                .setTitle("Delete?").setMessage("Are you sure to delete the selected item? ")
                                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        ArrayList<Note> note_list = list.get(groupPosition).getNote();
                                                        note_list.remove(childPosition);
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext,
                                                                "item deleted!", Toast.LENGTH_SHORT).show();
                                                        Utilities.saveInfo(mContext, list);
                                                    }
                                                })
                                                .setNegativeButton("no", null);

                                        dialog.show();
                                        break;
                                }
                                return false;
                            }
                        });
                        menu.show();
            }
        });

        return convertView;
    }

    private void InvalidOperation(){
        Toast.makeText(mContext, "invalid operation", Toast.LENGTH_SHORT).show();
    }

    private class ViewHolder{
        TextView title;
        ImageView item_menu;
    }
    // 子条目是否可以被点击/选中/选择
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        Log.d("", "onGroupExpanded() called with: groupPosition = [" + groupPosition + "]");
    }

}
