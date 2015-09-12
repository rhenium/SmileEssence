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

package net.lacolaco.smileessence.twitter.task;

import android.app.Activity;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.data.FavoriteCache;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;

import twitter4j.*;

import java.util.Collections;
import java.util.List;

public class HomeTimelineTask extends TwitterTask<List<Tweet>> {

    // ------------------------------ FIELDS ------------------------------

    private final Paging paging;

    // --------------------------- CONSTRUCTORS ---------------------------

    public HomeTimelineTask(Twitter twitter, Paging paging) {
        super(twitter);
        this.paging = paging;
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void onPostExecute(List<Tweet> tweets) {
        for (Tweet tweet : tweets) {
            // FavoriteCache.getInstance().put(tweet);
        }
    }

    @Override
    protected List<Tweet> doInBackground(Void... params) {
        try {
            return Tweet.fromTwitter(twitter.timelines().getHomeTimeline(paging));
        } catch (TwitterException e) {
            e.printStackTrace();
            Logger.error(e.toString());
            if (e.exceededRateLimitation()) {
                Notificator.getInstance().publish(R.string.notice_error_rate_limit, NotificationType.ALERT);
            } else {
                Notificator.getInstance().publish(R.string.notice_error_get_home, NotificationType.ALERT);
            }
            return Collections.emptyList();
        }
    }
}
