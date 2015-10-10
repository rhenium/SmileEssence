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
import net.lacolaco.smileessence.viewmodel.IViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UnorderedCustomListAdapter<T extends IViewModel> extends CustomListAdapter<T> {

    // ------------------------------ FIELDS ------------------------------

    protected final List<T> linkedList;

    // --------------------------- CONSTRUCTORS ---------------------------

    public UnorderedCustomListAdapter(Activity activity) {
        super(activity);
        this.linkedList = new LinkedList<>();
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected List<T> getFrozenList() {
        return Collections.unmodifiableList(new ArrayList<>(linkedList));
    }

    // -------------------------- OTHER METHODS --------------------------

    public void addItemToTop(T item) {
        synchronized (LOCK) {
            linkedList.add(item);
        }
    }

    public void addItemsToTop(List<T> items) {
        synchronized (LOCK) {
            linkedList.addAll(items);
        }
    }

    public void addItemToBottom(T item) {
        synchronized (LOCK) {
            linkedList.add(0, item);
        }
    }

    public void addItemsToBottom(List<T> items) {
        synchronized (LOCK) {
            linkedList.addAll(0, items);
        }
    }

    public void clear() {
        synchronized (LOCK) {
            linkedList.clear();
        }
    }

    public boolean removeItem(T item) {
        synchronized (LOCK) {
            return linkedList.remove(item);
        }
    }
}
