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

package net.lacolaco.smileessence.view.page;

import android.os.Bundle;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.view.adapter.EventListAdapter;
import net.lacolaco.smileessence.viewmodel.EventViewModel;

/**
 * Fragment for notice history
 */
public class HistoryFragment extends CustomListFragment<EventListAdapter> {

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    protected PullToRefreshBase.Mode getRefreshMode() {
        return PullToRefreshBase.Mode.DISABLED;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventListAdapter adapter = new EventListAdapter(getActivity());
        setAdapter(adapter);

        StatusFilter.getInstance().register(this, EventViewModel.class, (EventViewModel vm) -> {
            adapter.addItemToTop(vm);
            adapter.update();
        }, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StatusFilter.getInstance().unregister(this);
    }

    @Override
    public void refresh() {
    }
}
