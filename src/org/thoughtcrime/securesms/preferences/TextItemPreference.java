package org.thoughtcrime.securesms.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.service.RegistrationService;

/**
 * Created by Wei.He on 9/10/14.
 */
public class TextItemPreference extends EditTextPreference {
    private TextView mTxtView;
    private Context mContext;
    public static final String PREF_KEY_GENDER = "pref_gender";
    public static final String PREF_KEY_AGE = "pref_age";
    public static final String PREF_KEY_NAME = "pref_name";
    public static final String PREF_KEY_SIGN = "pref_signature";

    public TextItemPreference(Context context) {
        super(context);
        initialize(context);
    }

    public TextItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public TextItemPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        setOnPreferenceChangeListener(mOnPreferenceChangeListener);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mTxtView = (TextView) view.findViewById(R.id.textView);
        mTxtView.setText(getText());
    }

    private OnPreferenceChangeListener mOnPreferenceChangeListener = new OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            mTxtView.setText(o.toString());
            uploadContactsInfo(getKey());
            return true;
        }
    };

    private void uploadContactsInfo(String key) {
        Intent intent = new Intent(getContext(), RegistrationService.class);
        intent.setAction(RegistrationService.UPDATE_CONTACTS_INFO_ACTION);
        intent.putExtra(key, mTxtView.getText());
        intent.putExtra("pref_key", key);
        getContext().startService(intent);
    }
}
