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
import android.preference.PreferenceManager;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.util.Themes;

public class UserPreferenceHelper extends SharedPreferenceHelper {
    // --------------------------- CONSTRUCTORS ---------------------------

    private static final UserPreferenceHelper instance = new UserPreferenceHelper();

    public static UserPreferenceHelper getInstance() {
        return instance;
    }

    private UserPreferenceHelper() {
    }

    // --------------------- GETTER / SETTER METHODS ---------------------
    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
    }

    // --------------------- HELPER METHODS ---------------------
    public int getThemeIndex() {
        return get(R.string.key_setting_theme, Themes.THEME_DARK);
    }

    public int getTextSize() {
        return get(R.string.key_setting_text_size, 10);
    }

    public int getNameStyle() {
        return get(R.string.key_setting_namestyle, 0);
    }

    public int getRequestCountPerPage() {
        return get(R.string.key_setting_timelines, 20);
    }
}
