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

import net.lacolaco.smileessence.util.BackgroundTask;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class AccessTokenTask extends BackgroundTask<AccessToken, Void> {

    // ------------------------------ FIELDS ------------------------------

    private final Twitter twitter;
    private final RequestToken requestToken;
    private final String pinCode;

    // --------------------------- CONSTRUCTORS ---------------------------

    public AccessTokenTask(Twitter twitter, RequestToken requestToken, String pinCode) {
        this.twitter = twitter;
        this.requestToken = requestToken;
        this.pinCode = pinCode;
    }

    @Override
    protected AccessToken doInBackground(Void... params) {
        try {
            return twitter.getOAuthAccessToken(requestToken, pinCode);
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
