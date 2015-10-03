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
import net.lacolaco.smileessence.Application;
import net.lacolaco.smileessence.R;
import net.lacolaco.smileessence.command.IConfirmable;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.TweetBuilder;
import net.lacolaco.smileessence.twitter.task.FavoriteTask;
import net.lacolaco.smileessence.twitter.task.TweetTask;
import twitter4j.StatusUpdate;

public class StatusCommandMakeAnonymous extends StatusCommand implements IConfirmable {

    // -------------------------- STATIC METHODS --------------------------

    public StatusCommandMakeAnonymous(Activity activity, Tweet tweet) {
        super(R.id.key_command_status_make_anonymous, activity, tweet);
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    public static String build(Activity activity, Tweet tweet, Account account) {
        User user = account.getUser();
        String str = tweet.getText();
        if (str.startsWith(".")) {
            str = str.replaceFirst(".", "");
        }
        if (str.startsWith(String.format("@%s", user.getScreenName()))) {
            str = str.replaceFirst(String.format("@%s", user.getScreenName()), "").trim();
        }
        str = activity.getString(R.string.format_status_command_make_anonymous, str).trim();
        return str;
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public String getText() {
        return getActivity().getString(R.string.command_status_make_anonymous);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // -------------------------- OTHER METHODS --------------------------

    @Override
    public boolean execute() {
        StatusUpdate update = new TweetBuilder().setText(build(getActivity(), getOriginalStatus(), Application.getInstance().getCurrentAccount())).build();
        new TweetTask(Application.getInstance().getCurrentAccount(), update).execute();
        new FavoriteTask(Application.getInstance().getCurrentAccount(), getOriginalStatus().getId())
                .onDone(x -> Notificator.getInstance().publish(R.string.notice_favorite_succeeded))
                .onFail(x -> Notificator.getInstance().alert(R.string.notice_favorite_failed))
                .execute();
        return true;
    }
}
