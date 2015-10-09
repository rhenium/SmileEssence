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

package net.lacolaco.smileessence.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.*;
import com.android.volley.toolbox.NetworkImageView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.RBinding;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.task.FollowTask;
import net.lacolaco.smileessence.twitter.task.ShowFriendshipTask;
import net.lacolaco.smileessence.twitter.task.UnfollowTask;
import net.lacolaco.smileessence.twitter.task.UserTimelineTask;
import net.lacolaco.smileessence.util.IntentUtils;
import net.lacolaco.smileessence.util.Themes;
import net.lacolaco.smileessence.util.UIHandler;
import net.lacolaco.smileessence.util.UIObserverBundle;
import net.lacolaco.smileessence.view.DialogHelper;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;

public class UserDetailDialogFragment extends StackableDialogFragment implements View.OnClickListener,
        PullToRefreshBase.OnRefreshListener2<ListView> {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_USER_ID = "userID";
    private StatusListAdapter adapter;
    private TextView textViewScreenName;
    private TextView textViewName;
    private TextView textViewURL;
    private TextView textViewLocate;
    private TextView textViewFollowed;
    private TextView textViewProtected;
    private TextView textViewDescription;
    private TextView textViewTweetCount;
    private TextView textViewFriendCount;
    private TextView textViewFollowerCount;
    private TextView textViewFavoriteCount;
    private NetworkImageView imageViewIcon;
    private NetworkImageView imageViewHeader;
    private Button buttonFollow;
    private PullToRefreshListView listViewTimeline;
    private TabHost tabHost;
    private UIObserverBundle observerBundle;
    private User user;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getUserID() {
        return getArguments().getLong(KEY_USER_ID);
    }

    public void setUserID(long userID) {
        Bundle args = new Bundle();
        args.putLong(KEY_USER_ID, userID);
        setArguments(args);
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface OnClickListener ---------------------

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.imageview_user_detail_menu: {
                openUserMenu();
                break;
            }
            case R.id.imageview_user_detail_icon: {
                IntentUtils.openUri(getActivity(), user.getProfileImageUrlOriginal());
                break;
            }
            case R.id.textview_user_detail_screenname:
            case R.id.textview_user_detail_tweet_count:{
                IntentUtils.openUri(getActivity(), user.getUserHomeURL());
                break;
            }
            case R.id.textview_user_detail_friend_count: {
                IntentUtils.openUri(getActivity(), String.format("%s/following", user.getUserHomeURL()));
                break;
            }
            case R.id.textview_user_detail_follower_count: {
                IntentUtils.openUri(getActivity(), String.format("%s/followers", user.getUserHomeURL()));
                break;
            }
            case R.id.textview_user_detail_favorite_count: {
                IntentUtils.openUri(getActivity(), String.format("%s/favorites", user.getUserHomeURL()));
                break;
            }
            case R.id.button_user_detail_follow: {
                ConfirmDialogFragment.show(getActivity(), getString(R.string.dialog_confirm_commands), this::toggleFollowing);
                break;
            }
        }
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        Account currentAccount = Application.getInstance().getCurrentAccount();
        new UserTimelineTask(currentAccount, getUserID())
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .setSinceId(adapter.getTopID())
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_error_get_user_timeline))
                .onDoneUI(tweets -> {
                    for (int i = tweets.size() - 1; i >= 0; i--) {
                        adapter.addToTop(new StatusViewModel(tweets.get(i)));
                    }
                    updateListView(refreshView.getRefreshableView(), adapter, true);
                    refreshView.onRefreshComplete();
                })
                .execute();
    }

    @Override
    public void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
        Account currentAccount = Application.getInstance().getCurrentAccount();
        new UserTimelineTask(currentAccount, getUserID())
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .setMaxId(adapter.getLastID() - 1)
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_error_get_user_timeline))
                .onDoneUI(tweets -> {
                    for (Tweet tweet : tweets) {
                        adapter.addToBottom(new StatusViewModel(tweet));
                    }
                    updateListView(refreshView.getRefreshableView(), adapter, false);
                    refreshView.onRefreshComplete();
                })
                .execute();
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.fetch(getUserID());
        observerBundle = new UIObserverBundle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        observerBundle.detachAll();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        if (user == null) {
            Notificator.getInstance().publish(R.string.notice_error_show_user);
            return new DisposeDialog(activity);
        }

        View v = activity.getLayoutInflater().inflate(R.layout.dialog_user_detail, null);
        View menu = v.findViewById(R.id.imageview_user_detail_menu);
        menu.setOnClickListener(this);
        textViewScreenName = (TextView) v.findViewById(R.id.textview_user_detail_screenname);
        textViewScreenName.setOnClickListener(this);
        textViewName = (TextView) v.findViewById(R.id.textview_user_detail_name);
        textViewURL = (TextView) v.findViewById(R.id.textview_user_detail_url);
        textViewLocate = (TextView) v.findViewById(R.id.textview_user_detail_locate);
        textViewFollowed = (TextView) v.findViewById(R.id.textview_user_detail_followed);
        textViewProtected = (TextView) v.findViewById(R.id.texttview_user_detail_protected);
        textViewDescription = (TextView) v.findViewById(R.id.textview_user_detail_description);
        textViewDescription.setMovementMethod(LinkMovementMethod.getInstance());
        textViewTweetCount = (TextView) v.findViewById(R.id.textview_user_detail_tweet_count);
        textViewTweetCount.setOnClickListener(this);
        textViewFriendCount = (TextView) v.findViewById(R.id.textview_user_detail_friend_count);
        textViewFriendCount.setOnClickListener(this);
        textViewFollowerCount = (TextView) v.findViewById(R.id.textview_user_detail_follower_count);
        textViewFollowerCount.setOnClickListener(this);
        textViewFavoriteCount = (TextView) v.findViewById(R.id.textview_user_detail_favorite_count);
        textViewFavoriteCount.setOnClickListener(this);
        imageViewIcon = (NetworkImageView) v.findViewById(R.id.imageview_user_detail_icon);
        imageViewIcon.setOnClickListener(this);
        imageViewHeader = (NetworkImageView) v.findViewById(R.id.imageview_user_detail_header);
        buttonFollow = (Button) v.findViewById(R.id.button_user_detail_follow);
        buttonFollow.setOnClickListener(this);
        listViewTimeline = (PullToRefreshListView) v.findViewById(R.id.listview_user_detail_timeline);
        listViewTimeline.setOnRefreshListener(this);

        tabHost = (TabHost) v.findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1").setContent(R.id.tab1).setIndicator(getString(R.string.user_detail_tab_info));
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2").setContent(R.id.tab2).setIndicator(getString(R.string.user_detail_tab_timeline));
        tabHost.addTab(tab2);
        tabHost.setCurrentTab(0);

        initUserData();

        return new AlertDialog.Builder(activity)
                .setView(v)
                .setCancelable(true)
                .create();
    }

    private void executeUserTimelineTask(final StatusListAdapter adapter) {
        Account account = Application.getInstance().getCurrentAccount();
        tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);
        new UserTimelineTask(account, user.getId())
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_error_get_user_timeline))
                .onDoneUI(tweets -> {
                    for (Tweet tweet : tweets) {
                        adapter.addToBottom(new StatusViewModel(tweet));
                    }
                    adapter.updateForce();
                    tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
                })
                .execute();
    }

    private String getHtmlDescription() {
        String description = user.getDescription();
        if (TextUtils.isEmpty(description)) {
            return "";
        }
        String html = description;
        html = html.replaceAll("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+", "<a href=\"$0\">$0</a>");
        html = html.replaceAll("@([a-zA-Z0-9_]+)", "<a href=\"https://twitter.com/$1\">$0</a>");
        html = html.replaceAll("\r\n", "<br />");
        return html;
    }

    private void updateUserDataBasic() {
        textViewName.setText(user.getName());
        textViewScreenName.setText(user.getScreenName());
        textViewProtected.setVisibility(user.isProtected() ? View.VISIBLE : View.GONE);
        ImageCache.getInstance().setImageToView(user.getProfileImageUrlOriginal(), imageViewIcon);
    }

    private void updateUserDataDetail() {
        if (TextUtils.isEmpty(user.getLocation())) {
            textViewLocate.setVisibility(View.GONE);
        } else {
            textViewLocate.setText(user.getLocation());
            textViewLocate.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(user.getUrl())) {
            textViewURL.setVisibility(View.GONE);
        } else {
            textViewURL.setText(user.getUrl());
            textViewURL.setVisibility(View.VISIBLE);
        }
        textViewDescription.setText(Html.fromHtml(getHtmlDescription()));

        textViewTweetCount.setText(String.valueOf(user.getStatusesCount()));
        textViewFriendCount.setText(String.valueOf(user.getFriendsCount()));
        textViewFollowerCount.setText(String.valueOf(user.getFollowersCount()));
        textViewFavoriteCount.setText(String.valueOf(user.getFavoritesCount()));

        ImageCache.getInstance().setImageToView(user.getProfileBannerUrl(), imageViewHeader);
    }

    private void initUserData() {
        updateUserDataBasic();
        updateUserDataDetail();

        MainActivity activity = (MainActivity) getActivity();
        adapter = new StatusListAdapter(activity);
        listViewTimeline.setAdapter(adapter);
        executeUserTimelineTask(adapter);
        updateRelationship();

        observerBundle.attach(user, changes -> {
            if (getActivity() != null) {
                if (changes.contains(RBinding.BASIC))
                    updateUserDataBasic();
                if (changes.contains(RBinding.DETAIL))
                    updateUserDataDetail();
            }
        });
    }

    private void lockFollowButton() {
        buttonFollow.setText(R.string.user_detail_loading);
        buttonFollow.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_round_gray));
        buttonFollow.setEnabled(false);
    }

    private void openUserMenu() {
        UserMenuDialogFragment menuFragment = new UserMenuDialogFragment() {
            @Override
            protected void executeCommand(Command command) {
                super.executeCommand(command);
                new UIHandler().postDelayed(() -> {
                    if (UserDetailDialogFragment.this.isDetached()) {
                        return;
                    }
                    updateRelationship();
                }, 1000);
            }
        };
        menuFragment.setUserID(user.getId());
        DialogHelper.showDialog(getActivity(), menuFragment);
    }

    private void setFollowButtonState(boolean isFollowing) {
        MainActivity mainActivity = (MainActivity) getActivity();
        final Drawable unfollowColor = Themes.getStyledDrawable(mainActivity, R.attr.button_round_red);
        final Drawable followColor = Themes.getStyledDrawable(mainActivity, R.attr.button_round_blue);
        buttonFollow.setText(isFollowing ? R.string.user_detail_unfollow : R.string.user_detail_follow);
        buttonFollow.setBackground(isFollowing ? unfollowColor : followColor);
        buttonFollow.setTag(isFollowing);
        buttonFollow.setEnabled(true);
    }

    private void toggleFollowing() {
        Account account = Application.getInstance().getCurrentAccount();
        lockFollowButton();
        Boolean isFollowing = buttonFollow.getTag() != null ? (Boolean) buttonFollow.getTag() : false;
        if (isFollowing) {
            new UnfollowTask(account, user.getId())
                    .onDoneUI(result -> {
                        Notificator.getInstance().publish(R.string.notice_unfollow_succeeded);
                        updateRelationship();
                        buttonFollow.setEnabled(true);
                    })
                    .onFail(x ->
                            Notificator.getInstance().alert(R.string.notice_unfollow_failed))
                    .execute();
        } else {
            new FollowTask(account, user.getId())
                    .onDoneUI(result -> {
                        Notificator.getInstance().publish(R.string.notice_follow_succeeded);
                        updateRelationship();
                        buttonFollow.setEnabled(true);
                    })
                    .onFail(x -> Notificator.getInstance().alert(R.string.notice_follow_failed))
                    .execute();
        }
    }

    protected void updateListView(AbsListView absListView, CustomListAdapter<?> adapter, boolean addedToTop) {
        int before = adapter.getCount();
        adapter.notifyDataSetChanged(); // synchronized call (not adapter#updateForce())
        int after = adapter.getCount();
        int increments = after - before;
        if (increments > 0) {
            adapter.setNotifiable(false);
            if (addedToTop) {
                absListView.setSelection(increments + 1);
                absListView.smoothScrollToPositionFromTop(increments, 0);
                absListView.setSelection(increments);
            } else {
                absListView.smoothScrollToPositionFromTop(before, 0);
            }

            if (increments == 1) {
                adapter.setNotifiable(true);
            }
        } else {
            adapter.setNotifiable(true);
        }
    }

    private void updateRelationship() {
        Account account = Application.getInstance().getCurrentAccount();
        if (user == account.getUser()) {
            textViewFollowed.setText(R.string.user_detail_followed_is_me);
            buttonFollow.setVisibility(View.GONE);
        } else {
            lockFollowButton();
            textViewFollowed.setText(R.string.user_detail_loading);
            new ShowFriendshipTask(account, user.getId()).onDoneUI(relationship -> {
                boolean isFollowing = relationship.isSourceFollowingTarget();
                boolean isFollowed = relationship.isSourceFollowedByTarget();
                setFollowButtonState(isFollowing);
                textViewFollowed.setText(isFollowed ? R.string.user_detail_followed : R.string.user_detail_not_followed);
            }).execute();
        }
    }
}
