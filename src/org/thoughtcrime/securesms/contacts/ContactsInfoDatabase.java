/**
 * Copyright (C) 2013 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.NumberUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.textsecure.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Database to supply all types of contacts that TextSecure needs to know about
 *
 * @author Wei.He
 */
public class ContactsInfoDatabase {
    private static final String TAG = ContactsInfoDatabase.class.getSimpleName();
    private final DatabaseOpenHelper dbHelper;
    private final Context context;

    public static final String TABLE_NAME = "CONTACTS_INFO";
    public static final String ID_COLUMN = ContactsContract.CommonDataKinds.Phone._ID;
    public static final String AVATAR_COLUMN = "avatar";
    public static final String GENDER_COLUMN = "gender";
    public static final String AGE_COLUMN = "age";
    public static final String NAME_COLUMN = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    public static final String NUMBER_COLUMN = ContactsContract.CommonDataKinds.Phone.NUMBER;
    public static final String SIGNATURE_COLUMN = "signature";

    private static final String FILTER_SELECTION = NAME_COLUMN + " LIKE ? OR " + NUMBER_COLUMN + " LIKE ?";
    private static final String CONTACT_LIST_SORT = NAME_COLUMN + " COLLATE NOCASE ASC";

    private static final String[] CONTACTS_PROJECTION = new String[]{ID_COLUMN,
            AVATAR_COLUMN,
            GENDER_COLUMN,
            AGE_COLUMN,
            NAME_COLUMN,
            NUMBER_COLUMN,
            SIGNATURE_COLUMN};

    private static ContactsInfoDatabase instance = null;

    public synchronized static ContactsInfoDatabase getInstance(Context context) {
        if (instance == null) instance = new ContactsInfoDatabase(context);
        return instance;
    }

    public synchronized static void destroyInstance() {
        if (instance != null) instance.close();
        instance = null;
    }

    private ContactsInfoDatabase(Context context) {
        this.dbHelper = new DatabaseOpenHelper(context);
        this.context = context;
    }

    public void close() {
        dbHelper.close();
    }

    public Cursor query(String filter) {
        final Cursor localCursor = queryLocalDb(filter);
        return localCursor;
    }

    private Cursor queryLocalDb(String filter) {
        final String selection;
        final String[] selectionArgs;
        final String fuzzyFilter = "%" + filter + "%";
        if (!Util.isEmpty(filter)) {
            selection = FILTER_SELECTION;
            selectionArgs = new String[]{fuzzyFilter, fuzzyFilter};
        } else {
            selection = null;
            selectionArgs = null;
        }
        return queryLocalDb(selection, selectionArgs, null);
    }

    private Cursor queryLocalDb(String selection, String[] selectionArgs, String[] columns) {
        SQLiteDatabase localDb = dbHelper.getReadableDatabase();
        final Cursor localCursor;
        if (localDb != null)
            localCursor = localDb.query(TABLE_NAME, columns, selection, selectionArgs, null, null, CONTACT_LIST_SORT);
        else localCursor = null;
        if (localCursor != null && !localCursor.moveToFirst()) {
            localCursor.close();
            return null;
        }
        return localCursor;
    }

    public void updateContactInfo(ContentValues values, String number) {
        Log.d(TAG, "begin insertOrUpdateIdentity.");
        SQLiteDatabase localDb = dbHelper.getWritableDatabase();
        int i = localDb.update(TABLE_NAME, values, NUMBER_COLUMN + " = '" + number + "'", null);
        Log.d(TAG, "finished insertOrUpdateIdentity. i: " + i);
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context context;
        private SQLiteDatabase mDatabase;

        private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        ID_COLUMN + " INTEGER PRIMARY KEY, " +
                        AVATAR_COLUMN + " BLOB, " +
                        GENDER_COLUMN + " TEXT, " +
                        AGE_COLUMN + " INTEGER, " +
                        NAME_COLUMN + " TEXT, " +
                        NUMBER_COLUMN + " TEXT, " +
                        SIGNATURE_COLUMN + " TEXT);";

        DatabaseOpenHelper(Context context) {
            super(context, "contacts_info", null, 1);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "onCreate called for contacts database.");
            mDatabase = db;
            mDatabase.execSQL(TABLE_CREATE);
            if (TextSecurePreferences.isPushRegistered(context)) {
                try {
                    loadPushUsersIntoContactsInfo();
                } catch (IOException ioe) {
                    Log.e(TAG, "Issue when trying to create contacts_info db.", ioe);
                }
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        private void loadPushUsersIntoContactsInfo() throws IOException {
            Log.d(TAG, "populating push users into loadPushUsersIntoContactsInfo db.");
            Collection<ContactAccessor.ContactData> pushUsers = ContactAccessor.getInstance().getContactsWithPush(context);
            for (ContactAccessor.ContactData user : pushUsers) {
                ContentValues values = new ContentValues();
                values.put(ID_COLUMN, user.id);
                values.put(NAME_COLUMN, user.name);
                values.put(NUMBER_COLUMN, user.numbers.get(0).number);
                mDatabase.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            ContentValues values = new ContentValues();
            values.put(NUMBER_COLUMN, TextSecurePreferences.getLocalNumber(context));
            mDatabase.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //TODO get all the infos except avatar from server
            Log.d(TAG, "finished populating loadPushUsersIntoContactsInfo.");
        }
    }
}
