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

import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.logging.Logger;

import twitter4j.*;

import java.util.Collections;
import java.util.List;

public class UserTimelineTask extends TwitterTask<List<Tweet>> {

    // ------------------------------ FIELDS ------------------------------

    private final long userID;
    private final Paging paging;

    // --------------------------- CONSTRUCTORS ---------------------------

    public UserTimelineTask(Twitter twitter, long userID) {
        this(twitter, userID, null);
    }

    public UserTimelineTask(Twitter twitter, long userID, Paging paging) {
        super(twitter);
        this.userID = userID;
        this.paging = paging;
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected void onPostExecute(List<Tweet> tweets) {
        for (Tweet tweet : tweets) {
        }
    }

    @Override
    protected List<Tweet> doInBackground(Void... params) {
        try {
            if (paging == null) {
                return Tweet.fromTwitter(twitter.timelines().getUserTimeline(userID));
            } else {
                return Tweet.fromTwitter(twitter.timelines().getUserTimeline(userID, paging));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            Logger.error(e.toString());
            return Collections.emptyList();
        }
    }
}
