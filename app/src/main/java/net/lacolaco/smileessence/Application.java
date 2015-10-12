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

import com.activeandroid.ActiveAndroid;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.util.Themes;
import net.lacolaco.smileessence.util.UIHandler;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * プロセスと同期しているオブジェクト
 * 現在のテーマのリソース ID と現在のアカウント（およびアカウント変更イベントリスナー）を保持します
 * MainActivity の onCreate で resetState を呼び、保持しているデータを破棄すること
 */
public class Application extends android.app.Application {
    private static Application instance;
    private Account currentAccount;
    private final Set<OnCurrentAccountChangedListener> currentAccountChangedListeners = Collections.newSetFromMap(new WeakHashMap<>());
    private int resId;
    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this, true);
        instance = this; // プロセスの寿命の間 1 度しか呼ばれないので安全
        refWatcher = LeakCanary.install(this);
        Logger.debug("onCreate");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }

    // --------------------- reset ---------------------
    public void resetState() {
        currentAccount = null;
        currentAccountChangedListeners.clear();
        resId = -1;
    }

    // --------------------- get instance ---------------------

    public static Application getInstance() {
        Application obj = instance;
        if (obj == null) {
            throw new IllegalStateException("[BUG] Application is not initialized?");
        } else {
            return obj;
        }
    }

    // --------------------- theme ---------------------
    public int getThemeResId() {
        if (resId == -1) {
            Logger.debug("setting theme index: " + UserPreferenceHelper.getInstance().getThemeIndex());
            resId = Themes.getThemeResId(UserPreferenceHelper.getInstance().getThemeIndex());
        }
        return resId;
    }

    // --------------------- current account ---------------------
    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account val) {
        Logger.debug(String.format("setCurrentAccount: %s", val.getUser().getScreenName()));
        currentAccount = val;
        for (OnCurrentAccountChangedListener listener : currentAccountChangedListeners) {
            new UIHandler().post(() -> listener.onCurrentAccountChanged(val));
        }
    }

    public void addOnCurrentAccountChangedListener(OnCurrentAccountChangedListener listener) {
        currentAccountChangedListeners.add(listener);
    }

    public interface OnCurrentAccountChangedListener {
        void onCurrentAccountChanged(Account newAccount);
    }

    // --------------------- LeakCanary ---------------------
    public RefWatcher getRefWatcher() {
        return refWatcher;
    }
}
