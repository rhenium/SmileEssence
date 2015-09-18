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
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.twitter.task.HomeTimelineTask;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import twitter4j.Paging;
import twitter4j.Twitter;

import java.util.List;
import java.util.ListIterator;

public class HomeFragment extends CustomListFragment<StatusListAdapter> {

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    protected PullToRefreshBase.Mode getRefreshMode() {
        return PullToRefreshBase.Mode.BOTH;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    @Override // onCreate って Fragment のインスタンスが作られるときは必ず呼ばれるって認識でいいんだよね？
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusListAdapter adapter = new StatusListAdapter(getActivity());
        setAdapter(adapter);

        StatusFilter.getInstance().register(this, StatusViewModel.class, (StatusViewModel tweet) -> {
            adapter.addToTop(tweet);
            adapter.update();
        });
        final Twitter twitter = ((MainActivity) getActivity()).getCurrentAccount().getTwitter();
        final Paging paging = TwitterUtils.getPaging(((MainActivity) getActivity()).getRequestCountPerPage());
        new HomeTimelineTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<Tweet> tweets) {
                super.onPostExecute(tweets);
                for (Tweet tweet : tweets) {
                    StatusViewModel statusViewModel = new StatusViewModel(tweet);
                    adapter.addToBottom(statusViewModel);
                    StatusFilter.getInstance().filter(statusViewModel);
                }
                adapter.updateForce();
            }
        }.execute();
    }


    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final MainActivity activity = (MainActivity) getActivity();
        final StatusListAdapter adapter = getAdapter();
        if (activity.isStreaming()) {
            new UIHandler(() -> {
                updateListViewWithNotice(refreshView.getRefreshableView(), true);
                refreshView.onRefreshComplete();
            }).post();
            return;
        }
        final Account currentAccount = activity.getCurrentAccount();
        Twitter twitter = currentAccount.getTwitter();
        Paging paging = TwitterUtils.getPaging(activity.getRequestCountPerPage());
        if (adapter.getCount() > 0) {
            paging.setSinceId(adapter.getTopID());
        }
        new HomeTimelineTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<Tweet> tweets) {
                super.onPostExecute(tweets);
                ListIterator<Tweet> li = tweets.listIterator(tweets.size());
                while (li.hasPrevious()) {
                    StatusViewModel viewModel = new StatusViewModel(li.previous());
                    adapter.addToTop(viewModel);
                    StatusFilter.getInstance().filter(viewModel);
                }
                updateListViewWithNotice(refreshView.getRefreshableView(), true);
                refreshView.onRefreshComplete();
            }
        }.execute();
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final MainActivity activity = (MainActivity) getActivity();
        final StatusListAdapter adapter = getAdapter();
        final Account currentAccount = activity.getCurrentAccount();
        Twitter twitter = currentAccount.getTwitter();
        Paging paging = TwitterUtils.getPaging(activity.getRequestCountPerPage());
        if (adapter.getCount() > 0) {
            paging.setMaxId(adapter.getLastID() - 1);
        }
        new HomeTimelineTask(twitter, paging) {
            @Override
            protected void onPostExecute(List<Tweet> tweets) {
                super.onPostExecute(tweets);
                for (Tweet tweet : tweets) {
                    StatusViewModel viewModel = new StatusViewModel(tweet);
                    adapter.addToBottom(viewModel);
                    StatusFilter.getInstance().filter(viewModel);
                }
                updateListViewWithNotice(refreshView.getRefreshableView(), false);
                refreshView.onRefreshComplete();
            }
        }.execute();
    }
}
