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

import android.test.ActivityInstrumentationTestCase2;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.util.TwitterMock;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.util.Arrays;

public class OrderedCustomListAdapterTest extends ActivityInstrumentationTestCase2<MainActivity> {

    TwitterMock mock;
    OrderedCustomListAdapter<StatusViewModel> adapter;
    Account account;

    public OrderedCustomListAdapterTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        mock = new TwitterMock(getInstrumentation().getContext());
        adapter = new OrderedCustomListAdapter<>(getActivity());
        account = mock.getAccount();
    }

    public void testAddItem() throws Exception {
        adapter.addItem(new StatusViewModel(mock.getReplyMock()));
        adapter.notifyDataSetChanged();
        assertEquals(1, adapter.getCount());
    }

    public void testUpdate() throws Exception {
        adapter.addItem(new StatusViewModel(mock.getReplyMock()));
        assertEquals(0, adapter.getCount());
        adapter.notifyDataSetChanged();
        assertEquals(1, adapter.getCount());
    }

    public void testAddItems() throws Exception {
        StatusViewModel viewModel1 = new StatusViewModel(mock.getReplyMock());
        StatusViewModel viewModel2 = new StatusViewModel(mock.getReplyMock());
        adapter.addItems(Arrays.asList(viewModel1, viewModel2));
        adapter.notifyDataSetChanged();
        assertEquals(2, adapter.getCount());
    }

    public void testRemoveItem() throws Exception {
        StatusViewModel viewModel1 = new StatusViewModel(mock.getReplyMock());
        StatusViewModel viewModel2 = new StatusViewModel(mock.getReplyMock());
        adapter.addItems(Arrays.asList(viewModel1, viewModel2));
        adapter.notifyDataSetChanged();
        assertEquals(2, adapter.getCount());
        adapter.removeItem(viewModel1);
        adapter.notifyDataSetChanged();
        assertEquals(1, adapter.getCount());
    }

    @Override
    protected void tearDown() throws Exception {
        getActivity().forceFinish();
    }
}
