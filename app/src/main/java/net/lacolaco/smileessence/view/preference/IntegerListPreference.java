package net.lacolaco.smileessence.view.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class IntegerListPreference extends ListPreference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IntegerListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
