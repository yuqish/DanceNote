package com.example.yuqi.dancenote.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuqi.dancenote.R;
import com.example.yuqi.dancenote.data.Content;

import java.util.ArrayList;


public class NoteContentAdapter extends ArrayAdapter<Content> {
    private final String TAG = "NoteContentAdapter";
    private static final int tag_position = 5<<24;
    private static final int tag_content = 6<<24;
    private MyClickListener mListener;
    private ArrayList<Content> mObjects;
    public int position_selected = -1; /* indicate the position of selected item in the list */
    public int et_focus_position = -1;  /* indicate the position that cursor need to focus*/

    public NoteContentAdapter(Context context, int resource, ArrayList<Content> objects, MyClickListener listener) {
        super(context, resource, objects);
        mListener = listener;
        mObjects = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Log.w(TAG,"get item view type:" + getItemViewType(position) +", position:"+ position);
        final Content content = getItem(position);
        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        switch(getItemViewType(position)){
            case 0:
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_note_content,null);

                holder.tv_content = (TextView) convertView.findViewById(R.id.content_note);
                if (content != null){
                    holder.tv_content.setText(content.getContent());
                }
                holder.tv_content.setTag(position);
                holder.tv_content.setOnClickListener(mListener);

                if(position_selected == position){
                    holder.tv_content.setBackgroundResource(R.drawable.list_background_selected);
                }
                break;
            case 1:
                Log.w(TAG,"et_focus position is:" + et_focus_position +", current position:"+ position);
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_edit_note,null);

                holder.et_content = (EditText) convertView.findViewById(R.id.editText);
                holder.btn_ok = (Button) convertView.findViewById(R.id.button_ok);
                holder.btn_ok.setOnClickListener(mListener);
                holder.btn_ok.setTag(tag_position, position);
                holder.btn_ok.setTag(tag_content, holder.et_content);
                if (content != null) {
                    holder.et_content.setText(content.getContent());
                    if(et_focus_position == position){
                        holder.et_content.requestFocus();
                    }
                }
                /*avoid data lost when view refresh*/
                holder.et_content.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        content.setContent(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                /* update focus position for cursor */
                holder.et_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean has_focus) {
                        Log.w(TAG,"on focus change");
                        if(has_focus){
                            et_focus_position = position;
                        }
                    }
                });

                break;
        }

        convertView.setTag(holder);
        return convertView;
    }


    public class ViewHolder{
        public TextView tv_content;
        public EditText et_content;
        public Button btn_ok;
    }

    @Override
    public int getItemViewType(int position) {
        Content content = getItem(position);
        if(content.getMode() == 1){
            return 1;
        }else{
            return 0;
        }
    }

    public static abstract class MyClickListener implements View.OnClickListener {
        /**
         * Base onClick method
         */
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_ok:
                    EditText et = (EditText)v.getTag(tag_content);
                    myOnClick((Integer) v.getTag(tag_position), et.getText().toString(), v);
                    break;

                case R.id.content_note:
                    setPositionFlag((int)v.getTag());
                    break;
            }
        }
        public abstract void myOnClick(int position, String content, View v);
        public abstract void setPositionFlag(int position);
    }
}
