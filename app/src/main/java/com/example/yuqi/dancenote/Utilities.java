package com.example.yuqi.dancenote;

import android.content.Context;

import com.example.yuqi.dancenote.data.Content;
import com.example.yuqi.dancenote.data.GroupInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Utilities {

    public static Content cp_content; /* stores the copied content */

    public static boolean saveInfo(Context context, ArrayList<GroupInfo> gi){
        String fileName = "savedInfo.bin";

        FileOutputStream fos;
        ObjectOutputStream oos;
        try{
            fos = context.openFileOutput(fileName,context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(gi);
            oos.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();;
            return false;  //tell the user sth went wrong (could only be the device run out of space)
        }

        return true;
    }

    public static ArrayList<GroupInfo> getSavedInfo(Context context){
        ArrayList<GroupInfo> gi;
        String fileName = "savedInfo.bin";
        File file = new File(context.getFilesDir(),fileName);

        if(file.exists()){
            FileInputStream fis;
            ObjectInputStream ois;

            try{
                fis = context.openFileInput(fileName);
                ois = new ObjectInputStream(fis);
                gi = (ArrayList<GroupInfo>) ois.readObject();
                fis.close();
                ois.close();
            } catch(IOException | ClassNotFoundException e){
                e.printStackTrace();;
                return (new ArrayList<GroupInfo>());
            }
        }else{
            gi = new ArrayList<GroupInfo>();
        }

        return gi;
    }
}
