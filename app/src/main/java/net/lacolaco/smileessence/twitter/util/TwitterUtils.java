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

package net.lacolaco.smileessence.twitter.util;

import android.text.TextUtils;

import com.twitter.Validator;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.entity.*;
import net.lacolaco.smileessence.preference.UserPreferenceHelper;
import net.lacolaco.smileessence.twitter.task.ShowDirectMessageTask;
import net.lacolaco.smileessence.twitter.task.ShowStatusTask;
import net.lacolaco.smileessence.twitter.task.ShowUserTask;

import net.lacolaco.smileessence.twitter.task.TwitterTask;
import twitter4j.Paging;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import java.util.ArrayList;
import java.util.Collection;

public class TwitterUtils {

    // -------------------------- STATIC METHODS --------------------------

    /**
     * Get twitter style fixed text length
     *
     * @return length
     */
    public static int getFixedTextLength(String str) {
        Validator validator = new Validator();
        return validator.getTweetLength(str);
    }

    /**
     * Get status from api if not cached
     */
    public static TwitterTask<Tweet> tryGetStatus(Account account, long statusID, final StatusCallback callback) {
        ShowStatusTask task;
        Tweet tweet = Tweet.fetch(statusID);
        if (tweet != null) {
            callback.success(tweet);
            //update cache
            task = new ShowStatusTask(account.getTwitter(), statusID);
        } else {
            task = new ShowStatusTask(account.getTwitter(), statusID) {
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

    /**
     * Get status from api if not cached
     */
    public static void tryGetUser(Account account, long userID, final UserCallback callback) {
        User user = User.fetch(userID);
        if (user != null) {
            callback.success(user);
            ShowUserTask task = new ShowUserTask(account.getTwitter(), userID);
            task.execute();
        } else {
            ShowUserTask task = new ShowUserTask(account.getTwitter(), userID) {
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

    /**
     * Get direct message from api if not cached
     */
    public static void tryGetMessage(Account account, long messageID, final MessageCallback callback) {
        DirectMessage message = DirectMessage.fetch(messageID);
        if (message != null) {
            callback.success(message);
        } else {
            ShowDirectMessageTask task = new ShowDirectMessageTask(account.getTwitter(), messageID) {
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
     * Get array of screenName in own text
     *
     * @param tweet            tweet
     * @param excludeScreenName
     * @return
     */
    public static Collection<String> getScreenNames(Tweet tweet, String excludeScreenName) {
        ArrayList<String> names = new ArrayList<>();
        names.add(tweet.getUser().getScreenName());
        if (tweet.getMentions() != null) {
            for (UserMentionEntity entity : tweet.getMentions()) {
                if (names.contains(entity.getScreenName())) {
                    continue;
                }
                names.add(entity.getScreenName());
            }
        }
        if (excludeScreenName != null) {
            names.remove(excludeScreenName);
        }
        return names;
    }

    public static Collection<String> getScreenNames(DirectMessage message, String excludeScreenName) {
        ArrayList<String> names = new ArrayList<>();
        names.add(message.getSender().getScreenName());
        if (!message.getRecipient().getScreenName().equals(message.getSender().getScreenName())) {
            names.add(message.getRecipient().getScreenName());
        }
        if (message.getMentions() != null) {
            for (UserMentionEntity entity : message.getMentions()) {
                if (names.contains(entity.getScreenName())) {
                    continue;
                }
                names.add(entity.getScreenName());
            }
        }
        if (excludeScreenName != null) {
            names.remove(excludeScreenName);
        }
        return names;
    }

    public static String getUserHomeURL(String screenName) {
        return String.format("https://twitter.com/%s", screenName);
    }

    public static String getAclogTimelineURL(String screenName) {
        return String.format("http://aclog.koba789.com/%s/timeline", screenName);
    }

    public static String getFavstarRecentURL(String screenName) {
        return String.format("http://favstar.fm/users/%s/recent", screenName);
    }

    public static String getTwilogURL(String screenName) {
        return String.format("http://twilog.org/%s", screenName);
    }

    /**
     * Replace urls by entities
     *
     * @param text     raw text
     * @param entities url entities
     * @param expand   if true, use expanded url
     * @return replaced text
     */
    public static String replaceURLEntities(String text, URLEntity[] entities, boolean expand) {
        if (TextUtils.isEmpty(text)) {
            return "";
        } else if (entities == null) {
            return text;
        }
        if (entities.length == 0) {
            return text;
        }
        for (URLEntity entity : entities) {
            text = text.replace(entity.getURL(), expand ? entity.getExpandedURL() : entity.getDisplayURL());
        }
        return text;
    }

    public static Paging getPaging(int count) {
        return new Paging(1).count(count);
    }

    public static int getPagingCount(MainActivity activity) {
        return UserPreferenceHelper.getInstance().get(R.string.key_setting_timelines, 20);
    }

    public static String getMessageSummary(DirectMessage message) {
        return String.format("@%s: %s", message.getSender().getScreenName(), message.getText());
    }

    // -------------------------- INNER CLASSES --------------------------

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
