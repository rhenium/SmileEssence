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

package net.lacolaco.smileessence.twitter.util;

import android.text.TextUtils;

import com.twitter.Validator;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.entity.*;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.task.ShowDirectMessageTask;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.twitter.task.ShowUserTask;

import net.lacolaco.smileessence.twitter.task.TwitterTask;
import twitter4j.Paging;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import java.util.ArrayList;
import java.util.Collection;

public class TwitterUtils {
    // -------------------------- STATIC METHODS --------------------------

    /**
     * Replace urls by entities
     *
     * @param text     raw text
     * @param entities url entities
     * @param expand   if true, use expanded url
     * @return replaced text
     */
    public static String replaceURLEntities(String text, URLEntity[] entities, boolean expand) {
        if (TextUtils.isEmpty(text)) {
            return "";
        } else if (entities == null) {
            return text;
        }
        if (entities.length == 0) {
            return text;
        }
        for (URLEntity entity : entities) {
            text = text.replace(entity.getURL(), expand ? entity.getExpandedURL() : entity.getDisplayURL());
        }
        return text;
    }

    public static Paging getPaging(int count) {
        return new Paging(1).count(count);
    }

    public static int getPagingCount(MainActivity activity) {
        return UserPreferenceHelper.getInstance().get(R.string.key_setting_timelines, 20);
    }
}
