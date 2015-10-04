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

package net.lacolaco.smileessence.util;

import android.content.Context;
import android.content.res.AssetManager;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import twitter4j.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class TwitterMock {

    AssetManager assets;

    public TwitterMock(Context context) {
        assets = context.getAssets();
    }

    private String getJson(String fileName) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(assets.open(fileName)));
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {
                builder.append(str);
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

    private String getOAuthToken(String name) throws IOException {
        String filename = "tokens.properties";
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = assets.open(filename);
            props.load(is);
            return props.getProperty(name);
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public Status getTweetRawMock() throws IOException, TwitterException {
        return TwitterObjectFactory.createStatus(getJson("status.json"));
    }

    public Tweet getTweetMock() throws IOException, TwitterException {
        return Tweet.fromTwitter(getTweetRawMock(), getUserMock().getId());
    }

    public Status getReplyRawMock() throws IOException, TwitterException {
        return TwitterObjectFactory.createStatus(getJson("reply.json"));
    }

    public Tweet getReplyMock() throws IOException, TwitterException {
        return Tweet.fromTwitter(getReplyRawMock(), getUserMock().getId());
    }

    public Status getRetweetRawMock() throws IOException, TwitterException {
        return TwitterObjectFactory.createStatus(getJson("retweet.json"));
    }

    public Tweet getRetweetMock() throws IOException, TwitterException {
        return Tweet.fromTwitter(getRetweetRawMock(), getUserMock().getId());
    }

    public twitter4j.User getUserRawMock() throws IOException, TwitterException {
        return TwitterObjectFactory.createUser(getJson("user.json"));
    }

    public User getUserMock() throws IOException, TwitterException {
        return User.fromTwitter(getUserRawMock());
    }

    public twitter4j.DirectMessage getDirectMessageRawMock() throws IOException, TwitterException {
        return TwitterObjectFactory.createDirectMessage(getJson("directmessage.json"));
    }

    public DirectMessage getDirectMessageMock() throws IOException, TwitterException {
        return DirectMessage.fromTwitter(getDirectMessageRawMock());
    }

    public String getAccessToken() throws IOException, JSONException {
        return getOAuthToken("accessToken");
    }

    public String getAccessTokenSecret() throws IOException, JSONException {
        return getOAuthToken("accessTokenSecret");
    }

    public Account getAccount() throws IOException, TwitterException, JSONException {
        return Account.register(getAccessToken(), getAccessTokenSecret(), getUserMock().getId(), getUserMock().getScreenName());
    }
}
