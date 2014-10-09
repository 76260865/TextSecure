package org.thoughtcrime.securesms;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.textsecure.util.InvalidNumberException;
import org.whispersystems.textsecure.util.PhoneNumberFormatter;
import org.thoughtcrime.securesms.contacts.ContactsInfoDatabase;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.Recipients;

import java.lang.String;

/**
 * Activity for displaying recipient infomation.
 *
 * @author Jia Cheng Tian(25/9/2014)
 */
public class RecipientInoActivity extends PassphraseRequiredSherlockFragmentActivity {
    public static final String RECIPIENTS_EXTRA        = "recipients";
    private TextView genderTextview;
    private TextView ageTextview;
    private TextView nameTextview;
    private TextView signatureTextview;

    private Recipients recipients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipient_info_activity);

        getView();
        getIntentData();
        getPersonalInfo();
    }

    private void getView() {
        genderTextview = (TextView) findViewById(R.id.gender_textview);
        ageTextview = (TextView) findViewById(R.id.age_textview);
        nameTextview = (TextView) findViewById(R.id.name_textview);
        signatureTextview = (TextView) findViewById(R.id.signature_textview);
    }

    private void getIntentData()
    {
        recipients = RecipientFactory.getRecipientsForIds(this, getIntent().getStringExtra(RECIPIENTS_EXTRA), true);
    }

    private void getPersonalInfo() {
        Cursor cursor = null;
        String phoneNumber = null;
        //try {
            String localNumber = TextSecurePreferences.getLocalNumber(this);
            phoneNumber = recipients.getPrimaryRecipient().getNumber();
            cursor = ContactsInfoDatabase.getInstance(this).query(phoneNumber);
            Log.d("RecipientInoActivity",cursor== null ? "": "not null");
            if (cursor != null && cursor.moveToFirst()) {
                String strGender = cursor.getString(cursor.getColumnIndexOrThrow(ContactsInfoDatabase.GENDER_COLUMN));
                Toast.makeText(RecipientInoActivity.this,strGender, Toast.LENGTH_LONG).show();
                genderTextview.setText(strGender);
            }
//        } catch (InvalidNumberException e) {
//            e.printStackTrace();
//        } finally {
            if (cursor != null) {
                cursor.close();
            }
        /*}*/
    }

   /* private void initAvatar() {
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
    }*/
}