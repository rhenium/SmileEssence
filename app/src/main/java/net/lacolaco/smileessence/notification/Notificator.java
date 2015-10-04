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

package net.lacolaco.smileessence.notification;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.util.UIHandler;

import java.lang.ref.WeakReference;

public class Notificator {
    private static final Notificator instance = new Notificator();
    private static final int DURATION = 1000;
    private WeakReference<Activity> weakActivity;
    private boolean isForeground;

    public static Notificator getInstance() {
        return instance;
    }

    private Notificator() {
    }

    public void setDefault(Activity activity) {
        weakActivity = new WeakReference<>(activity);
    }

    public void publish(String text) {
        publish(NotificationType.INFO, text);
    }

    public void publish(@StringRes int resId, Object... formatArgs) {
        publish(NotificationType.INFO, resId, formatArgs);
    }

    public void alert(String text) {
        publish(NotificationType.ALERT, text);
    }

    public void alert(@StringRes int resId, Object... formatArgs) {
        publish(NotificationType.ALERT, resId, formatArgs);
    }

    private void publish(NotificationType type, @StringRes int resId, Object... formatArgs) {
        String text = Application.getInstance().getApplicationContext().getString(resId, formatArgs);
        publish(type, text);
    }

    private void publish(NotificationType type, String text) {
        Activity activity = weakActivity.get();
        if (activity == null || activity.isFinishing()) {
            Logger.debug(String.format("notify(log): %s", text));
        } else {
            new UIHandler().post(() -> {
                if (isForeground) {
                    Logger.debug(String.format("notify(crouton): %s", text));
                    Crouton.makeText(activity, text, getStyle(type)).show();
                } else {
                    Logger.debug(String.format("notify(toast): %s", text));
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void onForeground() {
        isForeground = true;
    }

    public void onBackground() {
        isForeground = false;
        Crouton.cancelAllCroutons();
    }

    private Style getStyle(NotificationType type) {
        Configuration.Builder conf = new Configuration.Builder();
        conf.setDuration(DURATION);
        Style.Builder style = new Style.Builder();
        style.setConfiguration(conf.build());
        switch (type) {
            case INFO: {
                style.setBackgroundColorValue(Style.holoBlueLight);
                break;
            }
            case ALERT: {
                style.setBackgroundColorValue(Style.holoRedLight);
                break;
            }
        }
        return style.build();
    }
}
