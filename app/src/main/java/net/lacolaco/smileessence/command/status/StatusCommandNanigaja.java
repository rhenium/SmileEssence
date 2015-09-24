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
import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.IConfirmable;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.entity.User;
import net.lacolaco.smileessence.notification.NotificationType;
import net.lacolaco.smileessence.notification.Notificator;
import net.lacolaco.smileessence.twitter.TweetBuilder;
import net.lacolaco.smileessence.twitter.task.FavoriteTask;
import net.lacolaco.smileessence.twitter.task.TweetTask;
import twitter4j.StatusUpdate;

public class StatusCommandNanigaja extends StatusCommand implements IConfirmable {

    // --------------------------- CONSTRUCTORS ---------------------------

    public StatusCommandNanigaja(Activity activity, Tweet tweet) {
        super(R.id.key_command_status_nanigaja, activity, tweet);
    }

    // --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public String getText() {
        return getActivity().getString(R.string.command_status_nanigaja);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // -------------------------- OTHER METHODS --------------------------

    public String build() {
        User user = ((MainActivity) getActivity()).getCurrentAccount().getUser();
        String str = getOriginalStatus().getText();
        String header = "";
        if (str.startsWith(".")) {
            str = str.replaceFirst(".", "");
        }
        if (str.startsWith(String.format("@%s", user.getScreenName()))) {
            str = str.replaceFirst(String.format("@%s", user.getScreenName()), "").trim();
            header = "@" + getOriginalStatus().getUser().getScreenName();
        }
        str = String.format("%s %s", header, String.format(getFormatString(getActivity()), str)).trim();
        return str;
    }

    @Override
    public boolean execute() {
        StatusUpdate update = new TweetBuilder().setText(build())
                .setInReplyToStatusID(getOriginalStatus().getId())
                .build();
        new TweetTask(((MainActivity) getActivity()).getCurrentAccount(), update).execute();
        new FavoriteTask(((MainActivity) getActivity()).getCurrentAccount(), getOriginalStatus().getId())
                .onDone(x -> Notificator.getInstance().publish(R.string.notice_favorite_succeeded))
                .onFail(x -> Notificator.getInstance().publish(R.string.notice_favorite_failed, NotificationType.ALERT))
                .execute();
        return true;
    }

    private String getFormatString(Activity activity) {
        return activity.getString(R.string.format_status_command_nanigaja);
    }
}
