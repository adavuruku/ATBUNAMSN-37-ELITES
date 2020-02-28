package com.example.atbunamsn;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sherif146 on 22/01/2018.
 */

public class saveImage {
    public boolean storeImage(Bitmap imageData, String filename,Context context) {
        //get path to external storage (SD card)
        String Folderpath = Environment.getExternalStorageDirectory() + "/ATBUNAMSN/";
        File createFoledr = new File(Folderpath);
        //create storage directories, if they don't exist
        createFoledr.mkdirs();
        //file to save pics
        File saveFile = new File(Folderpath,filename);
        try {
            //String filePath = sdIconStorageDir.toString() + filename;
            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            String[] paths = {Folderpath};
            String[] mediatype = {"image/jpeg"};
            MediaScannerConnection.scanFile(context,paths,mediatype,null);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
