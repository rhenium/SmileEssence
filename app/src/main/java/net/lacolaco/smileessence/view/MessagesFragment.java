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

package net.lacolaco.smileessence.view;

import android.os.Bundle;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.twitter.task.DirectMessagesTask;
import net.lacolaco.smileessence.twitter.task.SentDirectMessagesTask;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.view.adapter.MessageListAdapter;
import net.lacolaco.smileessence.viewmodel.MessageViewModel;

import net.lacolaco.smileessence.entity.DirectMessage;
import twitter4j.Paging;
import twitter4j.Twitter;

import java.util.List;

/**
 * Fragment of messages list
 */
public class MessagesFragment extends CustomListFragment<MessageListAdapter> {

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    protected PullToRefreshBase.Mode getRefreshMode() {
        return PullToRefreshBase.Mode.BOTH;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageListAdapter adapter = new MessageListAdapter(getActivity());
        setAdapter(adapter);

        StatusFilter.getInstance().register(this, MessageViewModel.class, (MessageViewModel message) -> {
            adapter.addToTop(message);
            adapter.update();
        });
        final Twitter twitter = ((MainActivity) getActivity()).getCurrentAccount().getTwitter();
        final Paging paging = TwitterUtils.getPaging(TwitterUtils.getPagingCount((MainActivity) getActivity()));
        new DirectMessagesTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<DirectMessage> directMessages) {
                super.onPostExecute(directMessages);
                for (DirectMessage message : directMessages) {
                    adapter.addToBottom(new MessageViewModel(message));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
        new SentDirectMessagesTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<DirectMessage> directMessages) {
                super.onPostExecute(directMessages);
                for (DirectMessage message : directMessages) {
                    adapter.addToBottom(new MessageViewModel(message));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account currentAccount = activity.getCurrentAccount();
        Twitter twitter = currentAccount.getTwitter();
        final MessageListAdapter adapter = getAdapter();
        Paging paging = TwitterUtils.getPaging(TwitterUtils.getPagingCount(activity));
        if (adapter.getCount() > 0) {
            paging.setSinceId(adapter.getTopID());
        }
        new DirectMessagesTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<DirectMessage> directMessages) {
                super.onPostExecute(directMessages);
                for (int i = directMessages.size() - 1; i >= 0; i--) {
                    adapter.addToTop(new MessageViewModel(directMessages.get(i)));
                }
                updateListViewWithNotice(refreshView.getRefreshableView(), true);
                refreshView.onRefreshComplete();
            }
        }.execute();
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final MainActivity activity = (MainActivity) getActivity();
        final Account currentAccount = activity.getCurrentAccount();
        Twitter twitter = currentAccount.getTwitter();
        final MessageListAdapter adapter = getAdapter();
        Paging paging = TwitterUtils.getPaging(TwitterUtils.getPagingCount(activity));
        if (adapter.getCount() > 0) {
            paging.setMaxId(adapter.getLastID() - 1);
        }
        new DirectMessagesTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<DirectMessage> directMessages) {
                super.onPostExecute(directMessages);
                for (DirectMessage directMessage : directMessages) {
                    adapter.addToBottom(new MessageViewModel(directMessage));
                }
                updateListViewWithNotice(refreshView.getRefreshableView(), false);
                refreshView.onRefreshComplete();
            }
        }.execute();
    }
}
