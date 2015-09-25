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

package net.lacolaco.smileessence;

import android.content.Context;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.Themes;

public class Application extends com.activeandroid.app.Application {

    // ------------------------------ FIELDS ------------------------------

    private static Context context;
    private static Account currentAccount;
    private int resId = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        // onCreate は一度しか呼ばれないはずだから安全なはず
        context = getApplicationContext();
    }

    // --------------------- HELPER METHODS ---------------------

    public int getThemeResId() {
        if (resId == -1) {
            resId = Themes.getThemeResId(UserPreferenceHelper.getInstance().get(R.string.key_setting_theme, 0));
        }
        return resId;
    }

    // --------------------- STATIC METHODS ---------------------

    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Application is not initialized");
        }
        return context;
    }

    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }


    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(Account val) {
        currentAccount = val;
    }
}
