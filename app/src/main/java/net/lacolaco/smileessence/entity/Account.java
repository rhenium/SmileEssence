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

package net.lacolaco.smileessence.entity;

import android.os.Handler;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.twitter.task.BlockIDsTask;
import net.lacolaco.smileessence.twitter.task.GetUserListsTask;
import net.lacolaco.smileessence.twitter.task.MutesIDsTask;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.util.BackgroundTask;
import net.lacolaco.smileessence.util.Consumer;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Account {
    private static Map<Long, Account> cache; // model id -> Account
    private User user;
    private Model model;
    private final Set<String> listSubscriptions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Long> muteUserIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // --------------------- static methods ---------------------
    public static synchronized Account get(long i) {
        if (cache == null) {
            throw new IllegalStateException("Load first");
        }
        return cache.get(i);
    }

    public static synchronized int count() {
        return cache.size();
    }

    public static synchronized List<Account> all() {
        return new ArrayList<>(cache.values());
    }

    public static synchronized void load() {
        cache = new LinkedHashMap<>();
        List<Model> all = new Select().from(Model.class).execute();
        for (Model model : all) {
            cache.put(model.getId(), new Account(model));
            Logger.info("lod" + model.userID);
        }
    }

    public static synchronized Account register(String token, String tokenSecret, long userID, String screenName) {
        Account account = null;
        for (Account a : all()) {
            if (a.getUserId() == userID) {
                account = a;
                break;
            }
        }
        if (account == null) {
            Model model = new Model(token, tokenSecret, userID, screenName);
            model.save();
            account = new Account(model);
            cache.put(model.getId(), account);
            Logger.error("new" + model.getId());
        } else {
            Model model = account.model;
            model.accessToken = token;
            model.accessSecret = tokenSecret;
            model.screenName = screenName;
            model.save();
            Logger.error("upd" + model.getId());
        }
        return account;
    }

    public static synchronized Account unregister(long modelId) {
        Account account = cache.remove(modelId);
        if (account != null) {
            Model.delete(Model.class, modelId);
        }
        return account;
    }

    // --------------------- instance methods ---------------------
    private Account(Model model) {
        this.model = model;
    }

    public long getUserId() {
        return model.userID;
    }

    public long getModelId() {
        return model.getId();
    }

    public Twitter getTwitter() {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthAccessToken(new AccessToken(model.accessToken, model.accessSecret));
        return twitter;
    }

    public TwitterStream getTwitterStream() {
        TwitterStream stream = new TwitterStreamFactory().getInstance();
        stream.setOAuthAccessToken(new AccessToken(model.accessToken, model.accessSecret));
        return stream;
    }

    public User getUser() {
        if (user == null) {
            user = User.fetch(model.userID);
            if (user == null) {
                user = User._makeSkeleton(getUserId(), model.screenName);
            }
            user.addObserver(this, (objs) -> {
                if (!model.screenName.equals(user.getScreenName())) {
                    model.screenName = user.getScreenName();
                    model.save();
                }
            });
        }

        return user;
    }
    // --------------------- Helper methods ---------------------

    @Deprecated
    public BackgroundTask<Tweet, Void> fetchTweet(long statusId, Consumer<Tweet> callback) {
        Tweet tweet = Tweet.fetch(statusId);
        if (tweet == null) {
            Handler handler = new Handler(); // get current Looper
            return new ShowStatusTask(this, statusId).onDone(t -> handler.post(() -> callback.accept(t))).execute();
        } else {
            callback.accept(tweet);
            return null;
        }
    }

    public boolean canDelete(Tweet tweet) {
        return tweet.getUser() == getUser() ||
                tweet.getOriginalTweet().getUser() == getUser();
    }

    public boolean canDelete(DirectMessage message) {
        return message.getSender() == getUser() ||
                message.getRecipient() == getUser();
    }

    // --------------------- List subscription cache ---------------------
    public BackgroundTask<List<String>, Void> refreshListSubscriptions() {
        return new GetUserListsTask(this)
                .onDone(lists -> {
                    listSubscriptions.clear();
                    listSubscriptions.addAll(lists);
                })
                // .onFail(x -> { }) // TODO: error message?
                .execute();
    }

    public Set<String> getListSubscriptions() {
        return listSubscriptions;
    }

    public boolean addListSubscription(String fullName) {
        return listSubscriptions.add(fullName);
    }

    public boolean removeListSubscription(String fullName) {
        return listSubscriptions.remove(fullName);
    }

    // --------------------- User mute cache ---------------------
    public List<BackgroundTask> refreshUserMuteList() {
        List<BackgroundTask> tasks = new ArrayList<>();
        tasks.add(new BlockIDsTask(this).onDone(muteUserIds::addAll).execute());
        tasks.add(new MutesIDsTask(this).onDone(muteUserIds::addAll).execute());
        return tasks;
    }

    public boolean isMutedUserListContains(long id) {
        return muteUserIds.contains(id);
    }

    @Table(name = "Accounts")
    public static class Model extends com.activeandroid.Model {
        @Column(name = "Token", notNull = true)
        private String accessToken;
        @Column(name = "Secret", notNull = true)
        private String accessSecret;
        @Column(name = "UserID", notNull = true)
        private long userID;
        @Column(name = "ScreenName", notNull = true)
        private String screenName;

        // Required by ActiveAndroid
        public Model() {
            super();
        }

        public Model(String token, String tokenSecret, long userID, String screenName) {
            this();
            this.accessToken = token;
            this.accessSecret = tokenSecret;
            this.userID = userID;
            this.screenName = screenName;
        }
    }
}