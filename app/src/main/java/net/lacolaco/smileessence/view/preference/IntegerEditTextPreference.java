package net.lacolaco.smileessence.view.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntegerEditTextPreference extends EditTextPreference {
    public IntegerEditTextPreference(Context context) {
        super(context);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.valueOf(value));
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }
}
