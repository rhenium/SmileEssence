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

package net.lacolaco.smileessence.view.adapter;

import android.app.Activity;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

public class StatusListAdapter extends OrderedCustomListAdapter<StatusViewModel> {

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusListAdapter(Activity activity) {
        super(activity);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getLastID() {
        if (getCount() > 0) {
            return getItem(getCount() - 1).getTweet().getId();
        } else {
            return -1;
        }
    }

    public long getTopID() {
        if (getCount() > 0) {
            return getItem(0).getTweet().getId();
        } else {
            return -1;
        }
    }

    // -------------------------- OTHER METHODS --------------------------

    @Override
    public int removeItemById(long statusID) {
        synchronized (LOCK) {
            int count = 0;
            count += super.removeItemById(statusID);
            Tweet t = Tweet.fetch(statusID);
            if (t != null) {
                for (long retweetId : t.getRetweets().values()) {
                    count += super.removeItemById(retweetId);
                }
            }
            return count;
        }
    }
}
