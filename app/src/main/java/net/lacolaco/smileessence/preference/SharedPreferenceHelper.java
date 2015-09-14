/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012-2014 lacolaco.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.lacolaco.smileessence.preference;

import android.content.SharedPreferences;
import net.lacolaco.smileessence.Application;

import java.util.Set;

public abstract class SharedPreferenceHelper {
    // --------------------- GETTER / SETTER METHODS ---------------------

    protected abstract SharedPreferences getPreferences();

    // -------------------------- OTHER METHODS --------------------------

    public String get(int key, String defaultValue) {
        return getPreferences().getString(getString(key), defaultValue);
    }

    // int, long, float value may be stored in String format (old versions)
    public int get(int key, int defaultValue) {
        try {
            return getPreferences().getInt(getString(key), defaultValue);
        } catch (ClassCastException ex) {
            int ret = Integer.parseInt(get(key, String.valueOf(defaultValue)));
            set(key, ret);
            return ret;
        }
    }

    public long get(int key, long defaultValue) {
        try {
            return getPreferences().getLong(getString(key), defaultValue);
        } catch (ClassCastException ex) {
            long ret = Long.parseLong(get(key, String.valueOf(defaultValue)));
            set(key, ret);
            return ret;
        }
    }

    public float get(int key, float defaultValue) {
        try {
            return getPreferences().getFloat(getString(key), defaultValue);
        } catch (ClassCastException ex) {
            float ret = Float.parseFloat(get(key, String.valueOf(defaultValue)));
            set(key, ret);
            return ret;
        }
    }

    public boolean get(int key, boolean defaultValue) {
        return getPreferences().getBoolean(getString(key), defaultValue);
    }

    public Set<String> get(int key, Set<String> defaultValue) {
        return getPreferences().getStringSet(getString(key), defaultValue);
    }

    public boolean set(int key, String value) {
        return getPreferences().edit()
                .putString(getString(key), value)
                .commit();
    }

    public boolean set(int key, int value) {
        return getPreferences().edit()
                .putInt(getString(key), value)
                .commit();
    }

    public boolean set(int key, long value) {
        return getPreferences().edit()
                .putLong(getString(key), value)
                .commit();
    }

    public boolean set(int key, float value) {
        return getPreferences().edit()
                .putFloat(getString(key), value)
                .commit();
    }

    public boolean set(int key, boolean value) {
        return getPreferences().edit()
                .putBoolean(getString(key), value)
                .commit();
    }

    public boolean set(int key, Set<String> value) {
        return getPreferences().edit()
                .putStringSet(getString(key), value)
                .commit();
    }

    private String getString(int resID) {
        try {
            return Application.getContext().getString(resID);
        } catch (Exception e) {
            return null;
        }
    }
}
