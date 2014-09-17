package org.thoughtcrime.securesms.contacts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.push.PushServiceSocketFactory;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.BitmapUtil;
import org.thoughtcrime.securesms.util.LRUCache;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.whispersystems.textsecure.push.ContactsInfo;
import org.whispersystems.textsecure.push.PushServiceSocket;
import org.whispersystems.textsecure.util.InvalidNumberException;
import org.whispersystems.textsecure.util.PhoneNumberFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

public class ContactPhotoFactory {

  private static final Object defaultPhotoLock              = new Object();
  private static final Object defaultGroupPhotoLock         = new Object();
  private static final Object defaultPhotoCroppedLock       = new Object();
  private static final Object defaultGroupPhotoCroppedLock  = new Object();

  private static Bitmap defaultContactPhoto;
  private static Bitmap defaultGroupContactPhoto;
  private static Bitmap defaultContactPhotoCropped;
  private static Bitmap defaultGroupContactPhotoCropped;

  private static final Map<Uri,Bitmap> localUserContactPhotoCache =
      Collections.synchronizedMap(new LRUCache<Uri,Bitmap>(2));

  private static final String[] CONTENT_URI_PROJECTION = new String[] {
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.DISPLAY_NAME,
    ContactsContract.Contacts.LOOKUP_KEY
  };

  public static Bitmap getDefaultContactPhoto(Context context) {
    synchronized (defaultPhotoLock) {
      if (defaultContactPhoto == null)
        defaultContactPhoto =  BitmapFactory.decodeResource(context.getResources(),
                                                            R.drawable.ic_contact_picture);
      return defaultContactPhoto;
    }
  }

  public static Bitmap getDefaultGroupPhoto(Context context) {
    synchronized (defaultGroupPhotoLock) {
      if (defaultGroupContactPhoto == null)
        defaultGroupContactPhoto =  BitmapFactory.decodeResource(context.getResources(),
                                                                 R.drawable.ic_group_photo);
      return defaultGroupContactPhoto;
    }
  }

  public static Bitmap getDefaultContactPhotoCropped(Context context) {
    synchronized (defaultPhotoCroppedLock) {
      if (defaultContactPhotoCropped == null)
        defaultContactPhotoCropped = BitmapUtil.getCircleCroppedBitmap(getDefaultContactPhoto(context));

      return defaultContactPhotoCropped;
    }
  }

  public static Bitmap getDefaultGroupPhotoCropped(Context context) {
    synchronized (defaultGroupPhotoCroppedLock) {
      if (defaultGroupContactPhotoCropped == null)
        defaultGroupContactPhotoCropped = BitmapUtil.getCircleCroppedBitmap(getDefaultGroupPhoto(context));

      return defaultGroupContactPhotoCropped;
    }
  }

  public static Bitmap getLocalUserContactPhoto(Context context, Uri uri) {
    if (uri == null) return getDefaultContactPhoto(context);

    Bitmap contactPhoto = localUserContactPhotoCache.get(uri);

    if (contactPhoto == null) {
      Cursor cursor = context.getContentResolver().query(uri, CONTENT_URI_PROJECTION,
                                                         null, null, null);

      if (cursor != null && cursor.moveToFirst()) {
        contactPhoto = getContactPhoto(context, Uri.withAppendedPath(Contacts.CONTENT_URI,
                                       cursor.getLong(0) + ""), "");
      } else {
        contactPhoto = getDefaultContactPhoto(context);
      }

      localUserContactPhotoCache.put(uri, contactPhoto);
    }

    return contactPhoto;
  }

  public static void clearCache() {
    localUserContactPhotoCache.clear();
  }

  public static void clearCache(Recipient recipient) {
    if (localUserContactPhotoCache.containsKey(recipient.getContactUri()))
    localUserContactPhotoCache.remove(recipient.getContactUri());
  }

    // Modified by Wei.He for get the contact photo first from server
  public static Bitmap getContactPhoto(Context context, Uri uri, String number) {
      Bitmap contactPhoto;
      contactPhoto = retrieveAvatar(context, number);
      if (contactPhoto != null) {
          Log.d("ContactPhotoFactory", "get the contact photo from server");
          return contactPhoto;
      }

      // get photo from local if there is not exits in our server
    InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
    if (inputStream == null) contactPhoto = ContactPhotoFactory.getDefaultContactPhoto(context);
    else                     contactPhoto = BitmapFactory.decodeStream(inputStream);

    return contactPhoto;
  }

    private static ContactsInfo getContactsInfo(Context context, PushServiceSocket socket, String number) throws InvalidNumberException {
        String localNumber = TextSecurePreferences.getLocalNumber(context);
        if (!TextUtils.isEmpty(localNumber)) {
            String phoneNumber = PhoneNumberFormatter.formatNumber(number, localNumber);
            ContactsInfo contactsInfo = socket.getContactsInfo(phoneNumber);
            return contactsInfo;
        }
        return null;
    }

    private static Bitmap retrieveAvatar(Context context, String number) {
        PushServiceSocket socket = PushServiceSocketFactory.create(context);
        FileInputStream fileInputStream = null;
        ContactsInfo contactsInfo;
        try {
            contactsInfo = getContactsInfo(context, socket, number);
            if (contactsInfo != null && contactsInfo.getImageattachmentid() != null) {
                Log.d("ContactPhotoFactory", "contactsInfo:" + contactsInfo + " number : "
                        + contactsInfo.getNumber());
                File avatar = socket.retrieveAttachment(null, contactsInfo.getImageattachmentid());
                if (avatar.exists() && avatar.length() > 0) {
                    fileInputStream = new FileInputStream(avatar);
                    Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                    return bitmap;
                }
            }
        } catch (FileNotFoundException e) {
            Log.d("ContactPhotoFactory", e.getMessage());
        } catch (InvalidNumberException e) {
            Log.d("ContactPhotoFactory", e.getMessage());
        } catch (IOException e) {
            Log.d("ContactPhotoFactory", e.getMessage());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.d("ContactPhotoFactory", e.getMessage());
                }
            }
        }
        return null;
    }
}
