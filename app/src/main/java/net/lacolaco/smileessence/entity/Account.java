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
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.util.BackgroundTask;
import net.lacolaco.smileessence.util.Consumer;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

@Table(name = "Accounts")
public class Account extends Model {
    private User user;

    @Column(name = "Token", notNull = true)
    private String accessToken;
    @Column(name = "Secret", notNull = true)
    private String accessSecret;
    @Column(name = "UserID", notNull = true)
    private long userID;
    @Column(name = "ScreenName", notNull = true)
    private String screenName;

    // Required by ActiveAndroid
    public Account() { }

    public Account(String token, String tokenSecret, long userID, String screenName) {
        super();
        this.accessToken = token;
        this.accessSecret = tokenSecret;
        this.userID = userID;
        this.screenName = screenName;
    }

    @Deprecated
    public static void deleteAll() {
        new Delete().from(Account.class).execute();
    }

    public long getUserId() {
        return userID;
    }

    public Twitter getTwitter() {
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthAccessToken(new AccessToken(accessToken, accessSecret));
        return twitter;
    }

    public TwitterStream getTwitterStream() {
        TwitterStream stream = new TwitterStreamFactory().getInstance();
        stream.setOAuthAccessToken(new AccessToken(accessToken, accessSecret));
        return stream;
    }

    public User getUser() {
        if (user == null) {
            user = User.fetch(userID); // 強い参照をもたせる
            if (user == null) {
                user = User._makeSkeleton(getUserId(), screenName);
            }
            user.addObserver(this, (user, objs) -> {
                if (!this.screenName.equals(((User) user).getScreenName())) {
                    this.screenName = ((User) user).getScreenName();
                    this.save();
                }
            });
        }

        return user;
    }

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

    //--- helper methods
    public boolean canDelete(Tweet tweet) {
        return tweet.getUser().getId() == getUserId();
    }

    public boolean canRetweet(Tweet tweet) {
        return !tweet.getOriginalTweet().getUser().isProtected() &&
                tweet.getOriginalTweet().getUser().getId() != getUserId();
    }
}
