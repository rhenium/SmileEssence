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
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.util.BackgroundTask;
import twitter4j.TwitterException;

public class UnfollowTask extends BackgroundTask<User, Void> {

    // ------------------------------ FIELDS ------------------------------

    private final Account account;
    private final long userID;

    // --------------------------- CONSTRUCTORS ---------------------------

    public UnfollowTask(Account account, long userID) {
        this.account = account;
        this.userID = userID;
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    protected User doInBackground() throws TwitterException {
        return User.fromTwitter(account.getTwitter().friendsFollowers().destroyFriendship(userID));
    }
}
