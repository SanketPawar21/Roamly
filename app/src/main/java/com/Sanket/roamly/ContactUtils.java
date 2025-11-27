package com.Sanket.roamly;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactUtils {

    public static String getContactName(Context context, Uri uri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        }
        return null;
    }

    public static String getContactPhone(Context context, Uri uri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (index >= 0) {
                    return cursor.getString(index).replace(" ", "");
                }
            }
        }
        return null;
    }
}
