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
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.ExtractionWord;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.twitter.task.MentionsTimelineTask;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

import java.util.regex.Pattern;

public class MentionsFragment extends CustomListFragment<StatusListAdapter> {

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    protected PullToRefreshBase.Mode getRefreshMode() {
        return PullToRefreshBase.Mode.BOTH;
    }

    // ------------------------ INTERFACE METHODS ------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusListAdapter adapter = new StatusListAdapter(getActivity());
        setAdapter(adapter);

        StatusFilter.getInstance().register(this, StatusViewModel.class, (StatusViewModel tweet) -> {
            for (ExtractionWord word : ExtractionWord.getAll()) {
                Pattern pattern = Pattern.compile(word.text);
                if (pattern.matcher(tweet.getTweet().getText()).find()) {
                    adapter.addToTop(tweet);
                    adapter.update();
                    return;
                }
            }
        }, id -> {
            adapter.removeByStatusID(id);
            adapter.updateForce();
        });
        final Account account = Application.getCurrentAccount();
        final StatusListAdapter adapter_ = adapter;
        new MentionsTimelineTask(account)
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_get_mentions, NotificationType.ALERT))
                .onDoneUI(tweets -> {
                    for (Tweet tweet : tweets) {
                        StatusViewModel statusViewModel = new StatusViewModel(tweet);
                        adapter_.addToBottom(statusViewModel);
                        StatusFilter.getInstance().filter(statusViewModel);
                    }
                    adapter_.updateForce();
                }).execute();
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final Account currentAccount = Application.getCurrentAccount();
        final StatusListAdapter adapter = getAdapter();
        new MentionsTimelineTask(currentAccount)
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .setSinceId(adapter.getTopID())
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_get_mentions, NotificationType.ALERT))
                .onDoneUI(tweets -> {
                    for (int i = tweets.size() - 1; i >= 0; i--) {
                        adapter.addToTop(new StatusViewModel(tweets.get(i)));
                    }
                    updateListViewWithNotice(refreshView.getRefreshableView(), true);
                    refreshView.onRefreshComplete();
                }).execute();
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final Account currentAccount = Application.getCurrentAccount();
        final StatusListAdapter adapter = getAdapter();
        new MentionsTimelineTask(currentAccount)
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .setMaxId(adapter.getLastID() - 1)
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_get_mentions, NotificationType.ALERT))
                .onDoneUI(tweets -> {
                    for (Tweet tweet : tweets) {
                        adapter.addToBottom(new StatusViewModel(tweet));
                    }
                    updateListViewWithNotice(refreshView.getRefreshableView(), false);
                    refreshView.onRefreshComplete();
                }).execute();
    }
}
