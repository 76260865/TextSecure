package org.thoughtcrime.securesms.preferences;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.service.RegistrationService;
import org.thoughtcrime.securesms.util.BitmapUtil;

/**
 * Created by Wei.He on 9/10/14.
 */
public class AvatarPreference extends Preference {
    private ImageView mImageContactPhoto;
    public static final String PREF_KEY_AVATAR = "pref_avatar";

    private Bitmap mBitmapAvatar;

    public void setAvatarBitmap(Bitmap bitmap) {
        mBitmapAvatar = bitmap;
    }

    public AvatarPreference(Context context) {
        super(context);
    }

    public AvatarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mImageContactPhoto = (ImageView) view.findViewById(R.id.contact_photo);
        if (mBitmapAvatar != null) {
            mImageContactPhoto.setImageBitmap(mBitmapAvatar);
        }
    }

    public void updateAvatar(Bitmap bitmap) {
        mImageContactPhoto.setImageBitmap(bitmap);
        setAvatarBitmap(bitmap);
        Intent intent = new Intent(getContext(), RegistrationService.class);
        intent.setAction(RegistrationService.UPDATE_CONTACTS_INFO_ACTION);
        intent.putExtra("pref_key", getKey());
        intent.putExtra(PREF_KEY_AVATAR, BitmapUtil.toByteArray(bitmap));
        getContext().startService(intent);
    }
}
