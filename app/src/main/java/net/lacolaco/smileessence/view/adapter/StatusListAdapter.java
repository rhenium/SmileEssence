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
import net.lacolaco.smileessence.entity.MuteUserIds;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class StatusListAdapter extends CustomListAdapter<StatusViewModel> {

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusListAdapter(Activity activity) {
        super(activity);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getLastID() {
        return ((StatusViewModel) getItem(getCount() - 1)).getTweet().getId();
    }

    public long getTopID() {
        return ((StatusViewModel) getItem(0)).getTweet().getId();
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void addToBottom(StatusViewModel... items) {
        for (StatusViewModel item : items) {
            if (!preAdd(item)) {
                continue;
            }
            super.addToBottom(item);
        }
    }

    @Override
    public void addToTop(StatusViewModel... items) {
        for (StatusViewModel item : items) {
            if (!preAdd(item)) {
                continue;
            }
            super.addToTop(item);
        }
    }

    /**
     * Sort list by Status ID
     */
    @Override
    protected void sort() {
        synchronized (LOCK) {
            Collections.sort(list, new Comparator<StatusViewModel>() {
                @Override
                public int compare(StatusViewModel lhs, StatusViewModel rhs) {
                    return Long.valueOf(rhs.getTweet().getId()).compareTo(lhs.getTweet().getId());
                }
            });
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

    private boolean isBlockUser(StatusViewModel item) {
        return MuteUserIds.isMuted(item.getTweet().getOriginalTweet().getUser().getId());
    }

    private boolean preAdd(StatusViewModel item) {
        removeByStatusID(item.getTweet().getId());
        return !isBlockUser(item);
    }
}
