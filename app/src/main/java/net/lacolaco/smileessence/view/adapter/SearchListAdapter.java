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

public class SearchListAdapter extends StatusListAdapter {

    // ------------------------------ FIELDS ------------------------------

    //private long topID;
    private String query;
    private OnQueryChangeListener listener;

    // --------------------------- CONSTRUCTORS ---------------------------

    public SearchListAdapter(Activity activity) {
        super(activity);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public OnQueryChangeListener getListener() {
        return listener;
    }

    public String getQuery() {
        return query;
    }

    //@Override
    //public long getTopID() {
    //    return topID;
    //}

    //public void setTopID(long topID) {
    //    this.topID = topID;
    //}

    public void setOnQueryChangeListener(OnQueryChangeListener listener) {
        this.listener = listener;
    }

    // -------------------------- OTHER METHODS --------------------------

    public void initSearch(String query) {
        this.query = query;
        clear();
    //    topID = 0;
        if (listener != null) {
            listener.onQueryChange(query);
        }
    }

    // -------------------------- INNER CLASSES --------------------------

    public interface OnQueryChangeListener {

        void onQueryChange(String newQuery);
    }
}
