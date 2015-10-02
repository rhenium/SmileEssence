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

import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.Themes;

import java.lang.ref.WeakReference;

public class Application extends com.activeandroid.app.Application {

    // ------------------------------ FIELDS ------------------------------

    private static WeakReference<Application> instance;
    private Account currentAccount;
    private int resId = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = new WeakReference<>(this);
        Logger.debug("onCreate");
    }

    // --------------------- STATIC METHODS ---------------------

    public static Application getInstance() {
        Application obj = null;
        if (instance != null) {
            obj = instance.get();
        }
        if (obj == null) {
            throw new IllegalStateException("[BUG] Application is not initialized?");
        } else {
            return obj;
        }
    }

    // --------------------- INSTANCE METHODS ---------------------

    public int getThemeResId() {
        if (resId == -1) {
            Logger.debug("setting theme index: " + UserPreferenceHelper.getInstance().getThemeIndex());
            resId = Themes.getThemeResId(UserPreferenceHelper.getInstance().getThemeIndex());
        }
        return resId;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account val) {
        currentAccount = val;
    }

    public void resetState() {
        currentAccount = null;
        resId = -1;
    }
}
