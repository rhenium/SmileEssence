package net.lacolaco.smileessence.view.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class IntegerListPreference extends ListPreference {
    public IntegerListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IntegerListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IntegerListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntegerListPreference(Context context) {
        super(context);
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
