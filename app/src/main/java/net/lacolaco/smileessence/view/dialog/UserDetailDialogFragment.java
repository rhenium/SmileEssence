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
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.data.ImageCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.RBinding;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.task.FollowTask;
import net.lacolaco.smileessence.twitter.task.ShowFriendshipTask;
import net.lacolaco.smileessence.twitter.task.UnfollowTask;
import net.lacolaco.smileessence.twitter.task.UserTimelineTask;
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
        User user = User.fetch(getUserID());
        if (user != null) {
            switch (v.getId()) {
                case R.id.imageview_user_detail_menu: {
                    openUserMenu(user);
                    break;
                }
                case R.id.imageview_user_detail_icon: {
                    openUrl(user.getProfileImageUrlOriginal());
                    break;
                }
                case R.id.textview_user_detail_screenname: {
                    openUrl(user.getUserHomeURL());
                    break;
                }
                case R.id.textview_user_detail_tweet_count: {
                    openUrl(user.getUserHomeURL());
                    break;
                }
                case R.id.textview_user_detail_friend_count: {
                    openUrl(String.format("%s/following", user.getUserHomeURL()));
                    break;
                }
                case R.id.textview_user_detail_follower_count: {
                    openUrl(String.format("%s/followers", user.getUserHomeURL()));
                    break;
                }
                case R.id.textview_user_detail_favorite_count: {
                    openUrl(String.format("%s/favorites", user.getUserHomeURL()));
                    break;
                }
                case R.id.button_user_detail_follow: {
                    ConfirmDialogFragment.show(getActivity(), getString(R.string.dialog_confirm_commands), () -> toggleFollowing(user));
                    break;
                }
            }
        } else {
            dismiss(); // BUG
        }
    }

    // --------------------- Interface OnRefreshListener2 ---------------------

    @Override
    public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
        Account currentAccount = Application.getCurrentAccount();
        new UserTimelineTask(currentAccount, getUserID())
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .setSinceId(adapter.getTopID())
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_get_user_timeline, NotificationType.ALERT))
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
        Account currentAccount = Application.getCurrentAccount();
        new UserTimelineTask(currentAccount, getUserID())
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .setMaxId(adapter.getLastID() - 1)
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_get_user_timeline, NotificationType.ALERT))
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        User user = User.fetch(getUserID());
        if (user == null) {
            return new DisposeDialog(activity);
        }

        observerBundle = new UIObserverBundle();

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

        initUserData(user);

        return new AlertDialog.Builder(activity)
                .setView(v)
                .setCancelable(true)
                .create();
    }

    private void executeUserTimelineTask(final User user, final StatusListAdapter adapter) {
        Account account = Application.getCurrentAccount();
        tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.GONE);
        new UserTimelineTask(account, user.getId())
                .setCount(UserPreferenceHelper.getInstance().getRequestCountPerPage())
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_error_get_user_timeline, NotificationType.ALERT))
                .onDoneUI(tweets -> {
                    for (Tweet tweet : tweets) {
                        adapter.addToBottom(new StatusViewModel(tweet));
                    }
                    adapter.updateForce();
                    tabHost.getTabWidget().getChildTabViewAt(1).setVisibility(View.VISIBLE);
                })
                .execute();
    }

    private String getHtmlDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return "";
        }
        String html = description;
        html = html.replaceAll("https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+", "<a href=\"$0\">$0</a>");
        html = html.replaceAll("@([a-zA-Z0-9_]+)", "<a href=\"https://twitter.com/$1\">$0</a>");
        html = html.replaceAll("\r\n", "<br />");
        return html;
    }

    private void updateUserDataBasic(User user) {
        textViewName.setText(user.getName());
        textViewScreenName.setText(user.getScreenName());
        textViewProtected.setVisibility(user.isProtected() ? View.VISIBLE : View.GONE);
        ImageCache.getInstance().setImageToView(user.getProfileImageUrlOriginal(), imageViewIcon);
    }

    private void updateUserDataDetail(User user) {
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
        String htmlDescription = getHtmlDescription(user.getDescription());
        textViewDescription.setText(Html.fromHtml(htmlDescription));

        textViewTweetCount.setText(String.valueOf(user.getStatusesCount()));
        textViewFriendCount.setText(String.valueOf(user.getFriendsCount()));
        textViewFollowerCount.setText(String.valueOf(user.getFollowersCount()));
        textViewFavoriteCount.setText(String.valueOf(user.getFavoritesCount()));

        ImageCache.getInstance().setImageToView(user.getProfileBannerUrl(), imageViewHeader);
    }

    private void initUserData(User user) {
        updateUserDataBasic(user);
        updateUserDataDetail(user);

        MainActivity activity = (MainActivity) getActivity();
        adapter = new StatusListAdapter(activity);
        listViewTimeline.setAdapter(adapter);
        executeUserTimelineTask(user, adapter);
        updateRelationship(user.getId());

        observerBundle.attach(user, (x, changes) -> {
            if (getActivity() != null) {
                if (changes.contains(RBinding.BASIC))
                    updateUserDataBasic(user);
                if (changes.contains(RBinding.DETAIL))
                    updateUserDataDetail(user);
            }
        });
    }

    private void lockFollowButton() {
        buttonFollow.setText(R.string.user_detail_loading);
        buttonFollow.setBackground(getActivity().getResources().getDrawable(R.drawable.button_round_gray));
        buttonFollow.setEnabled(false);
    }

    private void openUrl(String url) {
        new CommandOpenURL(getActivity(), url).execute();
    }

    private void openUserMenu(final User user) {
        UserMenuDialogFragment menuFragment = new UserMenuDialogFragment() {
            @Override
            protected void executeCommand(Command command) {
                super.executeCommand(command);
                new UIHandler().postDelayed(() -> {
                    if (UserDetailDialogFragment.this.isDetached()) {
                        return;
                    }
                    updateRelationship(user.getId());
                }, 1000);
            }
        };
        menuFragment.setUserID(user.getId());
        DialogHelper.showDialog(getActivity(), menuFragment);
    }

    private void setFollowButtonState(boolean isFollowing, Drawable unfollowColor, Drawable followColor) {
        buttonFollow.setText(isFollowing ? R.string.user_detail_unfollow : R.string.user_detail_follow);
        buttonFollow.setBackground(isFollowing ? unfollowColor : followColor);
        buttonFollow.setTag(isFollowing);
        buttonFollow.setEnabled(true);
    }

    private void toggleFollowing(final User user) {
        Account account = Application.getCurrentAccount();
        lockFollowButton();
        Boolean isFollowing = buttonFollow.getTag() != null ? (Boolean) buttonFollow.getTag() : false;
        if (isFollowing) {
            new UnfollowTask(account, user.getId())
                    .onDoneUI(result -> {
                        Notificator.getInstance().publish(R.string.notice_unfollow_succeeded);
                        updateRelationship(user.getId());
                        buttonFollow.setEnabled(true);
                    })
                    .onFail(x ->
                            Notificator.getInstance().publish(R.string.notice_unfollow_failed, NotificationType.ALERT))
                    .execute();
        } else {
            new FollowTask(account, user.getId())
                    .onDoneUI(result -> {
                        Notificator.getInstance().publish(R.string.notice_follow_succeeded);
                        updateRelationship(user.getId());
                        buttonFollow.setEnabled(true);
                    })
                    .onFail(x -> Notificator.getInstance().publish(R.string.notice_follow_failed, NotificationType.ALERT))
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

    private void updateRelationship(final long userId) {
        MainActivity mainActivity = (MainActivity) getActivity();
        Account account = Application.getCurrentAccount();
        if (userId == account.getUserId()) {
            textViewFollowed.setText(R.string.user_detail_followed_is_me);
            buttonFollow.setVisibility(View.GONE);
        } else {
            int theme = Application.getThemeResId();
            lockFollowButton();
            textViewFollowed.setText(R.string.user_detail_loading);
            final Drawable red = Themes.getStyledDrawable(mainActivity, theme, R.attr.button_round_red);
            final Drawable blue = Themes.getStyledDrawable(mainActivity, theme, R.attr.button_round_blue);
            new ShowFriendshipTask(account, userId).onDoneUI(relationship -> {
                boolean isFollowing = relationship.isSourceFollowingTarget();
                boolean isFollowed = relationship.isSourceFollowedByTarget();
                setFollowButtonState(isFollowing, red, blue);
                textViewFollowed.setText(isFollowed ? R.string.user_detail_followed : R.string.user_detail_not_followed);
            }).execute();
        }
    }
}
