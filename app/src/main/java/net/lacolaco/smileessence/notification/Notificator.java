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
import android.widget.Toast;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.util.UIHandler;

public class Notificator {
    private static Notificator instance;
    private static final int DURATION = 1000;
    private Activity activity;
    private boolean isForeground;

    public static void initialize(Activity activity) {
        instance = new Notificator(activity);
    }

    public static Notificator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Notificatior is not initialized");
        }
        return instance;
    }

    private Notificator(Activity a) {
        activity = a;
    }

    public void publish(int resID) {
        publish(activity.getString(resID));
    }

    public void publish(String text) {
        publish(text, NotificationType.INFO);
    }

    public void publish(int resID, NotificationType type) {
        publish(activity.getString(resID), type);
    }

    public void publish(String text, NotificationType type) {
        if (activity.isFinishing()) {
            return;
        }
        new UIHandler(() -> {
            if (isForeground) {
                Logger.debug(String.format("notify by crouton %s", text));
                Crouton.makeText(activity, text, getStyle(type)).show();
            } else {
                Logger.debug(String.format("notify by toast %s", text));
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        }).post();
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
