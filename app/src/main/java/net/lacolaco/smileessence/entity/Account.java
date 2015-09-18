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

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import net.lacolaco.smileessence.twitter.task.ShowDirectMessageTask;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.twitter.task.ShowUserTask;
import net.lacolaco.smileessence.twitter.task.TwitterTask;
import net.lacolaco.smileessence.util.Consumer;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

@Table(name = "Accounts")
public class Account extends Model {

    // ------------------------------ FIELDS ------------------------------

    @Column(name = "Token", notNull = true)
    public String accessToken;
    @Column(name = "Secret", notNull = true)
    public String accessSecret;
    @Column(name = "UserID", notNull = true)
    public long userID;
    @Column(name = "ScreenName", notNull = true)
    public String screenName;

    private User user;

    // -------------------------- STATIC METHODS --------------------------

    public Account() {
        super();
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    public Account(String token, String tokenSecret, long userID, String screenName) {
        super();
        this.accessToken = token;
        this.accessSecret = tokenSecret;
        this.userID = userID;
        this.screenName = screenName;
    }

    public static void deleteAll() {
        new Delete().from(Account.class).execute();
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

    public User getCachedUser() {
        if (user == null) {
            user = User.fetch(userID); // 強い参照をもたせる
        }
        return user; // null かも
    }

    public void tryGetMessage(long messageID, final MessageCallback callback) {
        DirectMessage message = DirectMessage.fetch(messageID);
        if (message != null) {
            callback.success(message);
        } else {
            ShowDirectMessageTask task = new ShowDirectMessageTask(getTwitter(), messageID) {
                @Override
                protected void onPostExecute(DirectMessage directMessage) {
                    super.onPostExecute(directMessage);
                    if (directMessage != null) {
                        callback.success(directMessage);
                    } else {
                        callback.error();
                    }
                }
            };
            task.execute();
        }
    }

    /**
     * Get status from api if not cached
     */
    public void tryGetUser(long userID, final UserCallback callback) {
        User user = User.fetch(userID);
        if (user != null) {
            callback.success(user);
            ShowUserTask task = new ShowUserTask(getTwitter(), userID);
            task.execute();
        } else {
            ShowUserTask task = new ShowUserTask(getTwitter(), userID) {
                @Override
                protected void onPostExecute(User user) {
                    super.onPostExecute(user);
                    if (user != null) {
                        callback.success(user);
                    } else {
                        callback.error();
                    }

                }
            };
            task.execute();
        }
    }

    public void fetchTweet(long statusId, Consumer<Tweet> callback, boolean forceRetrieve) {
        Tweet tweet = Tweet.fetch(statusId);
        if (forceRetrieve || tweet == null) {
            new ShowStatusTask(getTwitter(), statusId) {
                @Override
                protected void onPostExecute(Tweet tweet) {
                    callback.accept(tweet);
                }
            }.execute();
        } else {
            callback.accept(tweet);
        }
    }
    public void fetchTweet(long statusId, Consumer<Tweet> callback) {
        fetchTweet(statusId, callback, false);
    }

    public TwitterTask<Tweet> tryGetStatus(long statusID, final StatusCallback callback) {
        ShowStatusTask task;
        Tweet tweet = Tweet.fetch(statusID);
        if (tweet != null) {
            callback.success(tweet);
            //update cache
            task = new ShowStatusTask(getTwitter(), statusID);
        } else {
            task = new ShowStatusTask(getTwitter(), statusID) {
                @Override
                protected void onPostExecute(Tweet tweet) {
                    if (tweet != null) {
                        callback.success(tweet);
                    } else {
                        callback.error();
                    }
                }
            };
        }
        return (TwitterTask<Tweet>) task.execute();
    }

    public interface StatusCallback {

        void success(Tweet status);

        void error();
    }

    public interface UserCallback {

        void success(User user);

        void error();
    }

    public interface MessageCallback {

        void success(DirectMessage message);

        void error();
    }
}
