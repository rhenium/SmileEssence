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

package net.lacolaco.smileessence.command.status;

import android.app.Activity;

import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.command.IConfirmable;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.twitter.TweetBuilder;
import net.lacolaco.smileessence.twitter.task.FavoriteTask;
import net.lacolaco.smileessence.twitter.task.TweetTask;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;

import java.util.Random;

public class StatusCommandCongratulate extends StatusCommand implements IConfirmable {

    // ------------------------------ FIELDS ------------------------------

    private final Account account;

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusCommandCongratulate(Activity activity, Tweet tweet, Account account) {
        super(R.id.key_command_status_congratulate, activity, tweet);
        this.account = account;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public String getText() {
        return getActivity().getString(R.string.command_status_congratulate);
    }

    @Override
    public boolean isEnabled() {
        return !getOriginalStatus().getUser().isTweetProtected();
    }

    // -------------------------- OTHER METHODS --------------------------

    public String build() {
        int favCount;
        Random rand = new Random();
        int r = rand.nextInt(100);
        if (r < 50) {
            favCount = 50;
        } else if (r < 80) {
            favCount = 100;
        } else if (r < 90) {
            favCount = 250;
        } else if (r < 99) {
            favCount = 1000;
        } else {
            favCount = 10000;
        }
        return String.format("@%s Congrats on your %s ★ tweet! http://favstar.fm/t/%s",
                getOriginalStatus().getUser().getScreenName(), favCount, getOriginalStatus().getId());
    }

    @Override
    public boolean execute() {
        StatusUpdate update = new TweetBuilder().setText(build())
                .setInReplyToStatusID(getOriginalStatus().getId())
                .build();
        Twitter twitter = account.getTwitter();
        new TweetTask(twitter, update).execute();
        new FavoriteTask(twitter, getOriginalStatus().getId()).execute();
        return true;
    }
}
