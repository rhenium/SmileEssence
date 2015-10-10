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
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.twitter.task.DirectMessagesTask;
import net.lacolaco.smileessence.twitter.task.SentDirectMessagesTask;
import net.lacolaco.smileessence.twitter.task.TimelineTask;
import net.lacolaco.smileessence.view.adapter.MessageListAdapter;
import net.lacolaco.smileessence.viewmodel.MessageViewModel;

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
            adapter.addItem(message);
            adapter.update();
        }, id -> {
            adapter.removeItemById(id);
            adapter.updateForce();
        });

        if (Application.getInstance().getCurrentAccount() != null) {
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StatusFilter.getInstance().unregister(this);
    }

    @Override
    public void refresh() {
        runRefreshTask(new DirectMessagesTask(Application.getInstance().getCurrentAccount()), () -> getAdapter().updateForce());
        runRefreshTask(new SentDirectMessagesTask(Application.getInstance().getCurrentAccount()), () -> getAdapter().updateForce());
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        runRefreshTask(
                new DirectMessagesTask(Application.getInstance().getCurrentAccount())
                        .setSinceId(getAdapter().getTopID()),
                () -> {
                    updateListViewWithNotice(refreshView.getRefreshableView(), true);
                    refreshView.onRefreshComplete();
                }); // TODO: sent?
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        runRefreshTask(
                new DirectMessagesTask(Application.getInstance().getCurrentAccount())
                        .setMaxId(getAdapter().getLastID() - 1),
                () -> {
                    updateListViewWithNotice(refreshView.getRefreshableView(), false);
                    refreshView.onRefreshComplete();
                }); // TODO: sent?
    }

    private void runRefreshTask(TimelineTask<DirectMessage> task, Runnable onFinish) {
        task
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_error_get_messages))
                .onDoneUI(messages -> {
                    for (DirectMessage message : messages) {
                        StatusFilter.getInstance().filter(new MessageViewModel(message));
                    }
                })
                .onFinishUI(onFinish)
                .execute();
    }
}
