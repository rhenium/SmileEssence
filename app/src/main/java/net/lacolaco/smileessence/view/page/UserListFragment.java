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

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.InternalPreferenceHelper;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.StatusFilter;
import net.lacolaco.smileessence.twitter.task.TimelineTask;
import net.lacolaco.smileessence.twitter.task.UserListStatusesTask;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.adapter.UserListListAdapter;
import net.lacolaco.smileessence.view.dialog.SelectUserListDialogFragment;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

public class UserListFragment extends CustomListFragment<UserListListAdapter> implements View.OnClickListener {

    // ------------------------------ FIELDS ------------------------------

    private TextView textListName;

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    protected PullToRefreshBase.Mode getRefreshMode() {
        return PullToRefreshBase.Mode.BOTH;
    }

    // ------------------------ INTERFACE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserListListAdapter adapter = new UserListListAdapter(getActivity());
        setAdapter(adapter);

        if (Application.getInstance().getCurrentAccount() != null) {
            refresh();
        }
    }

    @Override
    public void refresh() {//TODO
        String lastUserList = InternalPreferenceHelper.getInstance().get(R.string.key_last_used_user_list, "");
        if (!TextUtils.isEmpty(lastUserList)) {
            startUserList(lastUserList);
        }
    }

    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button_userlist_lists: {
                openUserListsDialog();
                break;
            }
        }
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final UserListListAdapter adapter = getAdapter();
        String listFullName = adapter.getListFullName();
        if (TextUtils.isEmpty(listFullName)) {
            new UIHandler().post(() -> {
                notifyTextEmpty();
                refreshView.onRefreshComplete();
            });
            return;
        }
        runRefreshTask(
                new UserListStatusesTask(Application.getInstance().getCurrentAccount(), listFullName)
                        .setSinceId(adapter.getTopID()),
                () -> {
                    updateListViewWithNotice(refreshView.getRefreshableView(), true);
                    refreshView.onRefreshComplete();
                });
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        final UserListListAdapter adapter = getAdapter();
        String listFullName = adapter.getListFullName();
        if (TextUtils.isEmpty(listFullName)) {
            new UIHandler().post(() -> {
                notifyTextEmpty();
                refreshView.onRefreshComplete();
            });
            return;
        }
        runRefreshTask(
                new UserListStatusesTask(Application.getInstance().getCurrentAccount(), listFullName)
                        .setMaxId(adapter.getLastID() - 1),
                () -> {
                    updateListViewWithNotice(refreshView.getRefreshableView(), false);
                    refreshView.onRefreshComplete();
                });
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected PullToRefreshListView getListView(View page) {
        return (PullToRefreshListView) page.findViewById(R.id.listview_userlist);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View page = inflater.inflate(R.layout.fragment_userlist, container, false);
        PullToRefreshListView listView = getListView(page);
        UserListListAdapter adapter = getAdapter();
        listView.setAdapter(adapter);
        listView.setOnScrollListener(this);
        listView.setOnRefreshListener(this);
        listView.setMode(getRefreshMode());
        ImageButton buttonUserLists = getUserListsButton(page);
        buttonUserLists.setOnClickListener(this);
        textListName = getTextListName(page);
        textListName.setText(adapter.getListFullName());
        return page;
    }

    private TextView getTextListName(View page) {
        return (TextView) page.findViewById(R.id.textview_userlist_name);
    }

    private ImageButton getUserListsButton(View page) {
        return (ImageButton) page.findViewById(R.id.button_userlist_lists);
    }

    private void notifyTextEmpty() {
        Notificator.getInstance().alert(R.string.notice_userlist_not_selected);
    }

    private void openUserListsDialog() {
        DialogHelper.showDialog(getActivity(), new SelectUserListDialogFragment() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                super.onDismiss(dialog);
                textListName.setText(getAdapter().getListFullName());
            }
        });
    }

    public void startUserList(String listFullName) {
        InternalPreferenceHelper.getInstance().set(R.string.key_last_used_user_list, listFullName);
        final UserListListAdapter adapter = getAdapter();
        adapter.setListFullName(listFullName);
        adapter.clear();
        adapter.updateForce();
        runRefreshTask(
                new UserListStatusesTask(Application.getInstance().getCurrentAccount(), listFullName),
                adapter::updateForce);
    }

    private void runRefreshTask(TimelineTask<Tweet> task, Runnable onFinish) {
        final UserListListAdapter adapter = getAdapter();
        task
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_error_get_list))
                .onDoneUI(tweets -> {
                    for (Tweet tweet : tweets) {
                        StatusViewModel statusViewModel = new StatusViewModel(tweet);
                        StatusFilter.getInstance().filter(statusViewModel);
                        adapter.addItem(statusViewModel);
                    }
                })
                .onFinishUI(onFinish)
                .execute();
    }
}
