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
import net.lacolaco.smileessence.data.UserListCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.viewmodel.EventViewModel;
import net.lacolaco.smileessence.viewmodel.MessageViewModel;
import net.lacolaco.smileessence.viewmodel.StatusViewModel;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

public class UserStreamListener implements twitter4j.UserStreamListener, ConnectionLifeCycleListener {

    // ------------------------------ FIELDS ------------------------------

    private final Account account;
    private boolean connected = false;

    // --------------------------- CONSTRUCTORS ---------------------------

    public UserStreamListener(Account account) {
        this.account = account;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    public boolean isConnected() {
        return connected;
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface ConnectionLifeCycleListener ---------------------

    @Override
    public void onConnect() {
        connected = true;
        Notificator.getInstance().publish(R.string.notice_stream_connect);
    }

    @Override
    public void onDisconnect() {
        connected = false;
        Notificator.getInstance().publish(R.string.notice_stream_disconnect);
    }

    @Override
    public void onCleanUp() {
    }

    // --------------------- Interface StatusListener ---------------------

    @Override
    public void onStatus(Status status) {
        Tweet tweet = Tweet.fromTwitter(status, account.getUserId());
        StatusViewModel vm = new StatusViewModel(tweet);
        StatusFilter.getInstance().filter(vm);
        if (tweet.isRetweet()) {
            if (tweet.getUser().getId() == account.getUserId()) {
                addToHistory(new EventViewModel(EventViewModel.EnumEvent.RETWEETED, tweet.getUser(), tweet));
            }
        } else if (tweet.getMentions().contains(account.getUser().getScreenName())) {
            EventViewModel mentioned = new EventViewModel(EventViewModel.EnumEvent.MENTIONED, tweet.getUser(), tweet);
            Notificator.getInstance().publish(mentioned.getFormattedString());
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        StatusFilter.getInstance().remove(StatusViewModel.class, statusDeletionNotice.getStatusId());
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
        Logger.error(ex.toString());
    }

    // --------------------- Interface UserStreamListener ---------------------

    @Override
    public void onDeletionNotice(long directMessageId, long userId) {
        StatusFilter.getInstance().remove(MessageViewModel.class, directMessageId);
        DirectMessage.remove(directMessageId);
    }

    @Override
    public void onFriendList(long[] friendIds) {
    }

    @Override
    public void onFavorite(twitter4j.User source, twitter4j.User target, Status favoritedStatus) {
        Tweet tweet = Tweet.fromTwitter(favoritedStatus, account.getUserId());
        if (isMe(User.fromTwitter(target))) {
            addToHistory(new EventViewModel(EventViewModel.EnumEvent.FAVORITED, User.fromTwitter(source), tweet));
        }
        // unneeded?
        // if (isMe(User.fromTwitter(source))) {
        //     tweet.addFavoriter(source.getId());
        // }
    }

    @Override
    public void onUnfavorite(twitter4j.User source, twitter4j.User target, twitter4j.Status unfavoritedStatus) {
        Tweet tweet = Tweet.fromTwitter(unfavoritedStatus, account.getUserId());
        boolean unfavNoticeEnabled = UserPreferenceHelper.getInstance().get(R.string.key_setting_notify_on_unfavorited, true);
        if (unfavNoticeEnabled && isMe(User.fromTwitter(target))) {
            addToHistory(new EventViewModel(EventViewModel.EnumEvent.UNFAVORITED, User.fromTwitter(source), tweet));
        }
        // unneeded?
        // if (isMe(User.fromTwitter(source))) {
        //     tweet.removeFavoriter(source.getId());
        // }
    }

    @Override
    public void onFollow(twitter4j.User source, twitter4j.User followedUser) {
        if (isMe(User.fromTwitter(followedUser))) {
            addToHistory(new EventViewModel(EventViewModel.EnumEvent.FOLLOWED, User.fromTwitter(source)));
        }
    }

    @Override
    public void onUnfollow(twitter4j.User source, twitter4j.User unfollowedUser) {
    }

    @Override
    public void onDirectMessage(twitter4j.DirectMessage directMessage) {
        DirectMessage message = DirectMessage.fromTwitter(directMessage);
        if (isMe(message.getRecipient())) {
            addToHistory(new EventViewModel(EventViewModel.EnumEvent.RECEIVE_MESSAGE, User.fromTwitter(directMessage.getSender())));
        }
        MessageViewModel vm = new MessageViewModel(message);
        StatusFilter.getInstance().filter(vm);
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
            addToHistory(new EventViewModel(EventViewModel.EnumEvent.BLOCKED, User.fromTwitter(source)));
        }
    }

    @Override
    public void onUnblock(twitter4j.User source, twitter4j.User unblockedUser) {
        if (isMe(User.fromTwitter(unblockedUser))) {
            addToHistory(new EventViewModel(EventViewModel.EnumEvent.UNBLOCKED, User.fromTwitter(source)));
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
        StatusFilter.getInstance().filter(mentioned);
        Notificator.getInstance().publish(mentioned.getFormattedString());
    }

    private boolean isMe(User user) {
        return user.getId() == account.getUserId();
    }
}
