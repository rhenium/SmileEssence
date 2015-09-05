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

package net.lacolaco.smileessence.twitter;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.data.*;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;
import net.lacolaco.smileessence.view.adapter.EventListAdapter;
import net.lacolaco.smileessence.view.adapter.MessageListAdapter;
import net.lacolaco.smileessence.view.adapter.StatusListAdapter;
import net.lacolaco.smileessence.viewmodel.EnumEvent;
import net.lacolaco.smileessence.viewmodel.EventViewModel;
import net.lacolaco.smileessence.viewmodel.MessageViewModel;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

public class UserStreamListener implements twitter4j.UserStreamListener, ConnectionLifeCycleListener {

    // ------------------------------ FIELDS ------------------------------

    private final MainActivity activity;

    // --------------------------- CONSTRUCTORS ---------------------------

    public UserStreamListener(MainActivity activity) {
        this.activity = activity;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    private long getMyID() {
        return activity.getCurrentAccount().userID;
    }

    private int getPagerCount() {
        return activity.getPagerAdapter().getCount();
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface ConnectionLifeCycleListener ---------------------

    @Override
    public void onConnect() {
        activity.setStreaming(true);
        new Notificator(activity, R.string.notice_stream_connect).publish();
    }

    @Override
    public void onDisconnect() {
        activity.setStreaming(false);
        new Notificator(activity, R.string.notice_stream_disconnect).publish();
    }

    @Override
    public void onCleanUp() {
    }

    // --------------------- Interface StatusListener ---------------------

    @Override
    public void onStatus(Status status) {
        Tweet tweet = Tweet.fromTwitter(status);
        StatusViewModel vm = new StatusViewModel(tweet);
        addToHome(vm);
        if (tweet.isRetweet()) {
            //if (viewModel.isRetweetOfMe()) {
            //    addToHistory(new EventViewModel(EnumEvent.RETWEETED, status.getUser(), status));
            //}
        }// else if (viewModel.isMention()) {
        //    addToMentions(viewModel);
        //    EventViewModel mentioned = new EventViewModel(EnumEvent.MENTIONED, status.getUser(), status);
        //    Notificator.publish(activity, mentioned.getFormattedString(activity));
        //}
        StatusFilter.filter(activity, vm);
        FavoriteCache.getInstance().put(status);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        for (CustomListAdapter<?> adapter : activity.getListAdapters()) {
            if (adapter != null && adapter instanceof StatusListAdapter) {
                StatusListAdapter statusListAdapter = (StatusListAdapter) adapter;
                statusListAdapter.removeByStatusID(statusDeletionNotice.getStatusId());
                statusListAdapter.updateForce();
            }
        }
        Tweet.remove(statusDeletionNotice.getStatusId());
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
    }

    @Override
    public void onStallWarning(twitter4j.StallWarning warning) {
    }

    // --------------------- Interface StreamListener ---------------------

    @Override
    public void onException(Exception ex) {
        net.lacolaco.smileessence.logging.Logger.error(ex.toString());
    }

    // --------------------- Interface UserStreamListener ---------------------

    @Override
    public void onDeletionNotice(long directMessageId, long userId) {
        MessageListAdapter messages = (MessageListAdapter) activity.getListAdapter(MainActivity.AdapterID.Messages);
        messages.removeByMessageID(directMessageId);
        messages.updateForce();
        DirectMessage.remove(directMessageId);
    }

    @Override
    public void onFriendList(long[] friendIds) {
    }

    @Override
    public void onFavorite(twitter4j.User source, twitter4j.User target, Status favoritedStatus) {
        Tweet tweet = Tweet.fromTwitter(favoritedStatus);
        if (isMe(User.fromTwitter(target))) {
            addToHistory(new EventViewModel(EnumEvent.FAVORITED, User.fromTwitter(source), tweet));
        }
        if (isMe(User.fromTwitter(source))) {
            FavoriteCache.getInstance().put(favoritedStatus, true);
            activity.getListAdapter(MainActivity.AdapterID.Home).update();
            activity.getListAdapter(MainActivity.AdapterID.Mentions).update();
        }
    }

    @Override
    public void onUnfavorite(twitter4j.User source, twitter4j.User target, twitter4j.Status unfavoritedStatus) {
        Tweet tweet = Tweet.fromTwitter(unfavoritedStatus);
        boolean unfavNoticeEnabled = activity.getUserPreferenceHelper().getValue(R.string.key_setting_notify_on_unfavorited, true);
        if (isMe(User.fromTwitter(target)) && unfavNoticeEnabled) {
            addToHistory(new EventViewModel(EnumEvent.UNFAVORITED, User.fromTwitter(source), tweet));
        }
        if (isMe(User.fromTwitter(source))) {
            //FavoriteCache.getInstance().put(unfavoritedStatus, false);
            activity.getListAdapter(MainActivity.AdapterID.Home).update();
            activity.getListAdapter(MainActivity.AdapterID.Mentions).update();
        }
    }

    @Override
    public void onFollow(twitter4j.User source, twitter4j.User followedUser) {
        if (isMe(User.fromTwitter(followedUser))) {
            addToHistory(new EventViewModel(EnumEvent.FOLLOWED, User.fromTwitter(source)));
        }
    }

    @Override
    public void onUnfollow(twitter4j.User source, twitter4j.User unfollowedUser) {
    }

    @Override
    public void onDirectMessage(twitter4j.DirectMessage directMessage) {
        DirectMessage message = DirectMessage.fromTwitter(directMessage);
        if (isMe(message.getRecipient())) {
            addToHistory(new EventViewModel(EnumEvent.RECEIVE_MESSAGE, User.fromTwitter(directMessage.getSender())));
        }
        MessageViewModel vm = new MessageViewModel(message);
        addToMessages(vm);
    }

    @Override
    public void onUserListMemberAddition(twitter4j.User addedMember, twitter4j.User listOwner, twitter4j.UserList list) {
    }

    @Override
    public void onUserListMemberDeletion(twitter4j.User deletedMember, twitter4j.User listOwner, twitter4j.UserList list) {
    }

    @Override
    public void onUserListSubscription(twitter4j.User subscriber, twitter4j.User listOwner, twitter4j.UserList list) {
        UserListCache.getInstance().put(list.getFullName());
    }

    @Override
    public void onUserListUnsubscription(twitter4j.User subscriber, twitter4j.User listOwner, twitter4j.UserList list) {
        UserListCache.getInstance().remove(list.getFullName());
    }

    @Override
    public void onUserListCreation(twitter4j.User listOwner, twitter4j.UserList list) {
        UserListCache.getInstance().put(list.getFullName());
    }

    @Override
    public void onUserListUpdate(twitter4j.User listOwner, twitter4j.UserList list) {
    }

    @Override
    public void onUserListDeletion(twitter4j.User listOwner, twitter4j.UserList list) {
        UserListCache.getInstance().remove(list.getFullName());
    }

    @Override
    public void onUserProfileUpdate(twitter4j.User updatedUser) {
    }

    @Override
    public void onUserSuspension(long suspendedUser) {
    }

    @Override
    public void onUserDeletion(long deletedUser) {
    }

    @Override
    public void onBlock(twitter4j.User source, twitter4j.User blockedUser) {
        if (isMe(User.fromTwitter(blockedUser))) {
            addToHistory(new EventViewModel(EnumEvent.BLOCKED, User.fromTwitter(source)));
        }
    }

    @Override
    public void onUnblock(twitter4j.User source, twitter4j.User unblockedUser) {
        if (isMe(User.fromTwitter(unblockedUser))) {
            addToHistory(new EventViewModel(EnumEvent.UNBLOCKED, User.fromTwitter(source)));
        }
    }

    @Override
    public void onRetweetedRetweet(twitter4j.User source, twitter4j.User target, twitter4j.Status retweetedStatus) {
    }

    @Override
    public void onFavoritedRetweet(twitter4j.User source, twitter4j.User target, twitter4j.Status favoritedRetweeet) {
    }

    @Override
    public void onQuotedTweet(twitter4j.User source, twitter4j.User target, twitter4j.Status quotingTweet) {
    }

    private void addToHistory(EventViewModel mentioned) {
        EventListAdapter history = (EventListAdapter) activity.getListAdapter(MainActivity.AdapterID.History);
        Notificator.publish(activity, mentioned.getFormattedString(activity));
        history.addToTop(mentioned);
        history.update();
    }

    private void addToHome(StatusViewModel viewModel) {
        StatusListAdapter home = (StatusListAdapter) activity.getListAdapter(MainActivity.AdapterID.Home);
        home.addToTop(viewModel);
        home.update();
    }

    private void addToMentions(StatusViewModel viewModel) {
        StatusListAdapter mentions = (StatusListAdapter) activity.getListAdapter(MainActivity.AdapterID.Mentions);
        mentions.addToTop(viewModel);
        mentions.update();
    }

    private void addToMessages(MessageViewModel message) {
        MessageListAdapter messages = (MessageListAdapter) activity.getListAdapter(MainActivity.AdapterID.Messages);
        messages.addToTop(message);
        messages.update();
    }

    private boolean isMe(User user) {
        return user.getId() == getMyID();
    }
}