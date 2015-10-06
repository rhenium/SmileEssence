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
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.util.Collections;
import java.util.Iterator;

public class StatusListAdapter extends CustomListAdapter<StatusViewModel> {

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

    // ------------------------ OVERRIDE METHODS ------------------------

    /**
     * Sort list by Status ID
     */
    @Override
    protected void sort() {
        synchronized (LOCK) {
            Collections.sort(list, (lhs, rhs) -> Long.valueOf(rhs.getTweet().getId()).compareTo(lhs.getTweet().getId()));
        }
    }

    // -------------------------- OTHER METHODS --------------------------

    public void removeByStatusID(long statusID) {
        synchronized (this.LOCK) {
            Iterator<StatusViewModel> iterator = this.list.iterator();
            while (iterator.hasNext()) {
                StatusViewModel statusViewModel = iterator.next();
                if (statusViewModel.getTweet().getOriginalTweet().getId() == statusID) {
                    iterator.remove();
                }
            }
        }
    }
}
