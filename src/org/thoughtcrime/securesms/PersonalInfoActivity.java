package org.thoughtcrime.securesms;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.PreferenceScreen;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.widget.ImageView;
import org.thoughtcrime.securesms.service.RegistrationService;
import org.thoughtcrime.securesms.contacts.ContactsInfoDatabase;
import org.thoughtcrime.securesms.preferences.AvatarPreference;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.R;
import java.lang.Override;
import android.widget.Toast;

/**
 * Created by Wei.He on 9/10/14.
 */
public class PersonalInfoActivity extends PreferenceActivity {
    private static final int SELECT_PICTURE = 2;

    private ImageView mImageContactPhoto;

    private AvatarPreference mAvatarPreference;
    private ListPreference mGenderPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.personal_info_preferences);
        mAvatarPreference = (AvatarPreference) findPreference("pref_avatar");
        mGenderPreference = (ListPreference) findPreference("pref_gender");
        initializeListSummary(mGenderPreference);
        mGenderPreference.setOnPreferenceChangeListener(new OnGenderSharedPreferenceChangeListener());

        mAvatarPreference.setOnPreferenceClickListener(mAvatarPrefClickListener);
        initAvatar();
    }

    private class OnGenderSharedPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean blGender = true;
            if (value.equals("1")) {
                blGender = true;
                preference.setSummary(getString(R.string.personal_info_gender_male));
            } else {
                blGender = false;
                preference.setSummary(getString(R.string.personal_info_gender_female));
            }

            Intent intent = new Intent(getApplicationContext(), RegistrationService.class);
            intent.setAction(RegistrationService.UPDATE_CONTACTS_INFO_ACTION);
            intent.putExtra("pref_gender", blGender);
            intent.putExtra("pref_key", "pref_gender");
            getApplicationContext().startService(intent);
            return true;
        }
    }

    private void initializeListSummary(ListPreference pref) {
        pref.setSummary(pref.getEntry());
    }

    private void initAvatar() {
        Cursor cursor = ContactsInfoDatabase.getInstance(getApplicationContext())
                .query(TextSecurePreferences.getLocalNumber(this));
        if (cursor != null && cursor.moveToFirst()) {
            byte[] avatar = cursor.getBlob(cursor.getColumnIndexOrThrow(ContactsInfoDatabase.AVATAR_COLUMN));
            if (avatar != null && avatar.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                mAvatarPreference.setAvatarBitmap(bitmap);
            }
            cursor.close();
        }
    }

    private Preference.OnPreferenceClickListener mAvatarPrefClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("outputX", 100);
            intent.putExtra("outputY", 100);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("return-data", true);

            startActivityForResult(Intent.createChooser(intent,
                    getString(R.string.personal_info_activity__choose_image)), SELECT_PICTURE);
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = data.getParcelableExtra("data");
            if (bitmap != null) {
                mAvatarPreference.updateAvatar(bitmap);
                ContentValues values = new ContentValues();
                values.put(ContactsInfoDatabase.AVATAR_COLUMN, BitmapUtil.toByteArray(bitmap));
                ContactsInfoDatabase.getInstance(getApplicationContext())
                        .updateContactInfo(values, TextSecurePreferences.getLocalNumber(this));
            }
        }
    }
}
