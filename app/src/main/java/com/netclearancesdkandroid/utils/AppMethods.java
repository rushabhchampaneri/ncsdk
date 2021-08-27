package com.netclearancesdkandroid.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.netclearancesdkandroid.R;

import java.util.ArrayList;
import java.util.UUID;

public class AppMethods {
    public static ArrayList<ParcelUuid> getServicePrcelUUIDList(String data, Context context){
        ArrayList<ParcelUuid> serviceList= new ArrayList<>();
        if (data.contains(",")){
            String[] dataString = data.split(",");
            for (int i=0;i<dataString.length;i++){
                String spiltedData = dataString[i].trim();
                if (checkIsValidUUID(spiltedData, context)){
                    try {
                        serviceList.add(ParcelUuid.fromString(spiltedData));
                    }catch (Exception e){
                        return null;
                    }
                }else {
                    return null;
                }
            }
        }else {
            if (checkIsValidUUID(data.trim(), context)){
                try {
                    serviceList.add(ParcelUuid.fromString(data.trim()));
                }catch (Exception e){
                    return null;
                }
            }else {
              return null;
            }
        }
        return serviceList;
    }

    public static ArrayList<UUID> getServiceUUIDList(String data, Context context){
        ArrayList<UUID> serviceList= new ArrayList<>();
        if (data.contains(",")){
            String[] dataString = data.split(",");
            for (int i=0;i<dataString.length;i++){
                String spiltedData = dataString[i].trim();
                if (checkIsValidUUID(spiltedData, context)){
                    try {
                        serviceList.add(UUID.fromString(spiltedData));
                    }catch (Exception e){
                        return null;
                    }
                }else {
                    return null;
                }
            }
        }else {
            if (checkIsValidUUID(data.trim(), context)){
                try {
                    serviceList.add(UUID.fromString(data.trim()));
                }catch (Exception e){
                    return null;
                }
            }else {
                return null;
            }
        }
        return serviceList;
    }

    public static boolean checkIsValidUUID(String uuid, Context context){
        if (!uuid.contains("-")){
            Toast.makeText(context, context.getString(R.string.invalid_uuid), Toast.LENGTH_SHORT).show();
            return false;
        }else if (uuid.length()==36){
            return true;
        }else {
            Toast.makeText(context, context.getString(R.string.invalid_uuid), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean checkLocationPermission(Context context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001
            );
            return false;
        }
        return true;
    }
}
