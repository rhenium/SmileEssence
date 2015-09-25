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

package net.lacolaco.smileessence.twitter.task;

import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.util.BackgroundTask;
import twitter4j.Relationship;
import twitter4j.TwitterException;

public class ShowFriendshipTask extends BackgroundTask<Relationship, Void> {

    // ------------------------------ FIELDS ------------------------------

    private final Account account;
    private final long userID;
    private final String screenName;

    // --------------------------- CONSTRUCTORS ---------------------------

    public ShowFriendshipTask(Account account, long userID) {
        this.account = account;
        this.userID = userID;
        this.screenName = null;
    }

    public ShowFriendshipTask(Account account, String screenName) {
        this.account = account;
        this.screenName = screenName;
        this.userID = -1;
    }

    @Override
    protected Relationship doInBackground() throws TwitterException {
        if (screenName != null) {
            return account.getTwitter().friendsFollowers().showFriendship(account.getTwitter().getScreenName(), screenName);
        } else {
            return account.getTwitter().friendsFollowers().showFriendship(account.getTwitter().getId(), userID);
        }
    }
}
