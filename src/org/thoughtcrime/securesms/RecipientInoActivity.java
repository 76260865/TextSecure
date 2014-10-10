package org.thoughtcrime.securesms;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.thoughtcrime.securesms.ConversationActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.contacts.ContactPhotoFactory;
import org.thoughtcrime.securesms.contacts.ContactsInfoDatabase;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.push.PushServiceSocketFactory;
import org.thoughtcrime.securesms.recipients.RecipientFactory;
import org.thoughtcrime.securesms.recipients.Recipients;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.whispersystems.textsecure.crypto.MasterSecret;
import org.whispersystems.textsecure.push.ContactsInfo;
import org.whispersystems.textsecure.push.PushServiceSocket;
import org.whispersystems.textsecure.util.InvalidNumberException;

//import android.widget.Toast;

/**
 * Activity for displaying recipient infomation.
 *
 * @author Jia Cheng Tian(25/9/2014)
 */
public class RecipientInoActivity extends PassphraseRequiredSherlockFragmentActivity {
    public static final String RECIPIENTS_EXTRA = "recipients";
    public static final String THREAD_ID_EXTRA = "thread_id";
    public static final String MASTER_SECRET_EXTRA = "master_secret";
    private TextView genderTextview;
    private TextView ageTextview;
    private TextView nameTextview;
    private TextView signatureTextview;
    private Button sendMessageButton;
    private ImageView imageView;
    private Recipients recipients;
    private long existingThread;
    private MasterSecret masterSecret;
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != recipients) {
                Intent intent = new Intent(RecipientInoActivity.this, ConversationActivity.class);
                intent.putExtra(ConversationActivity.RECIPIENTS_EXTRA, recipients.toIdString());
                intent.putExtra(ConversationActivity.MASTER_SECRET_EXTRA, masterSecret);
                intent.putExtra(ConversationActivity.DRAFT_TEXT_EXTRA, getIntent().getStringExtra(ConversationActivity.DRAFT_TEXT_EXTRA));
                intent.putExtra(ConversationActivity.DRAFT_AUDIO_EXTRA, getIntent().getParcelableExtra(ConversationActivity.DRAFT_AUDIO_EXTRA));
                intent.putExtra(ConversationActivity.DRAFT_VIDEO_EXTRA, getIntent().getParcelableExtra(ConversationActivity.DRAFT_VIDEO_EXTRA));
                intent.putExtra(ConversationActivity.DRAFT_IMAGE_EXTRA, getIntent().getParcelableExtra(ConversationActivity.DRAFT_IMAGE_EXTRA));
                //long existingThread = DatabaseFactory.getThreadDatabase(this).getThreadIdIfExistsFor(recipients);
                intent.putExtra(ConversationActivity.THREAD_ID_EXTRA, existingThread);
                intent.putExtra(ConversationActivity.DISTRIBUTION_TYPE_EXTRA, ThreadDatabase.DistributionTypes.DEFAULT);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipient_info_activity);

        initResources();
        getPersonalInfo();
    }

    private void initResources() {
        recipients = RecipientFactory.getRecipientsForIds(this, getIntent().getStringExtra(RECIPIENTS_EXTRA), true);
        masterSecret = getIntent().getParcelableExtra(MASTER_SECRET_EXTRA);
        existingThread = getIntent().getLongExtra(THREAD_ID_EXTRA, -1);

        imageView = (ImageView) findViewById(R.id.avatar_imageview);
        genderTextview = (TextView) findViewById(R.id.gender_textview);
        ageTextview = (TextView) findViewById(R.id.age_textview);
        nameTextview = (TextView) findViewById(R.id.name_textview);
        signatureTextview = (TextView) findViewById(R.id.signature_textview);
        sendMessageButton = (Button) findViewById(R.id.send_message_button);
        sendMessageButton.setOnClickListener(listener);
    }

    private void getPersonalInfo() {
        Cursor cursor = null;
        String phoneNumber = null;

        //String localNumber = TextSecurePreferences.getLocalNumber(this);
        phoneNumber = recipients.getPrimaryRecipient().getNumber();
        RecipientInfoTask infoTask = new RecipientInfoTask();
        infoTask.execute(phoneNumber);
        cursor = ContactsInfoDatabase.getInstance(this).query(phoneNumber);
        //Log.d("RecipientInoActivity",cursor== null ? "": "not null");

        if (cursor != null && cursor.moveToFirst()) {
            byte[] avatar = cursor.getBlob(cursor.getColumnIndexOrThrow(ContactsInfoDatabase.AVATAR_COLUMN));
            initAvatar(avatar);
        }

        if (cursor != null) {
            cursor.close();
            }
    }

    private void initAvatar(byte[] avatar) {
        Bitmap bitmap = null;

        if (avatar != null && avatar.length > 0) {
            bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
        } else {
            bitmap = ContactPhotoFactory.getDefaultContactPhoto(this);
        }

        imageView.setImageBitmap(BitmapUtil.getCircleCroppedBitmap(bitmap));

        /*if (null != bitmap) {
            bitmap = null;
        }*/
    }

    private class RecipientInfoTask extends AsyncTask<String, Integer, ContactsInfo> {
        @Override
        protected ContactsInfo doInBackground(String... params) {
            PushServiceSocket socket = PushServiceSocketFactory.create(RecipientInoActivity.this);
            ContactsInfo contactsInfo = null;

            try {
                contactsInfo = ContactPhotoFactory.getContactsInfo(RecipientInoActivity.this, socket, params[0]);
            } catch (InvalidNumberException e) {
                Log.d("RecipientInoActivity", e.getMessage());
            } finally {
            }
            return contactsInfo;
        }

        @Override
        protected void onPostExecute(ContactsInfo contractInfo) {
            //super.onPostExecute(result);
            if (null != contractInfo) {
                genderTextview.setText(contractInfo.getGender() == true ? "男" : "女");
                ageTextview.setText(String.valueOf(contractInfo.getAge()));
                nameTextview.setText(contractInfo.getNickname());
                signatureTextview.setText(contractInfo.getSign());
            }
        }
    }
}