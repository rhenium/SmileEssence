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
import net.lacolaco.smileessence.entity.IdObject;
import net.lacolaco.smileessence.viewmodel.IViewModel;

import java.util.*;

public class OrderedCustomListAdapter<T extends IViewModel & IdObject> extends CustomListAdapter<T> {

    // ------------------------------ FIELDS ------------------------------

    protected final Map<Long, T> treeMap;

    // --------------------------- CONSTRUCTORS ---------------------------

    public OrderedCustomListAdapter(Activity activity) {
        this(activity, Long::compare);
    }

    public OrderedCustomListAdapter(Activity activity, Comparator<Long> comparator) {
        super(activity);
        this.treeMap = new TreeMap<>(Collections.reverseOrder(comparator)); // 降順
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected List<T> getFrozenList() {
        return Collections.unmodifiableList(new ArrayList<>(treeMap.values()));
    }

    // -------------------------- OTHER METHODS --------------------------

    public void addItem(T... items) {
        synchronized (LOCK) {
            for (T item : items) {
                treeMap.put(item.getId(), item);
            }
        }
    }

    public void clear() {
        synchronized (LOCK) {
            treeMap.clear();
        }
    }

    public T removeItem(T item) {
        synchronized (LOCK) {
            return treeMap.remove(item.getId());
        }
    }

    public int removeItemById(long id) {
        synchronized (LOCK) {
            T item = treeMap.remove(id);
            if (item == null) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
