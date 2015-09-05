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

package net.lacolaco.smileessence.view.dialog;

import android.app.Activity;
import android.os.Bundle;

import net.lacolaco.smileessence.activity.MainActivity;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.command.CommandOpenHashtagDialog;
import net.lacolaco.smileessence.command.CommandOpenURL;
import net.lacolaco.smileessence.command.CommandOpenUserDetail;
import net.lacolaco.smileessence.command.CommandSaveAsTemplate;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.twitter.util.TwitterUtils;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.List;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

public class StatusMenuDialogFragment extends MenuDialogFragment {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_STATUS_ID = "statusID";

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getStatusID() {
        return getArguments().getLong(KEY_STATUS_ID);
    }

    public void setStatusID(long statusID) {
        Bundle args = new Bundle();
        args.putLong(KEY_STATUS_ID, statusID);
        setArguments(args);
    }

    // ------------------------ OVERRIDE METHODS ------------------------
    @Override
    protected void setMenuItems(final CustomListAdapter<Command> adapter) {
        final MainActivity activity = (MainActivity) getActivity();
        Account account = activity.getCurrentAccount();
        Tweet tweet = Tweet.fetch(getStatusID());

        if (tweet != null) {
            List<Command> commands = getCommands(activity, tweet, account);
            Command.filter(commands);
            for (Command command : commands) {
                adapter.addToBottom(command);
            }
            adapter.update();
        } else {
            dismiss();
        }
    }

    // -------------------------- OTHER METHODS --------------------------

    public void addBottomCommands(Activity activity, Tweet tweet, Account account, ArrayList<Command> commands) {
        commands.add(new CommandSaveAsTemplate(activity, tweet.getOriginalTweet().getText()));
        //User
        for (String screenName : TwitterUtils.getScreenNames(tweet, null)) {
            commands.add(new CommandOpenUserDetail(activity, screenName, account));
        }
        for (Command command : getHashtagCommands(activity, tweet)) {
            commands.add(command);
        }
        // Media
        if (tweet.getUrls() != null) {
            for (URLEntity urlEntity : tweet.getUrls()) {
                commands.add(new CommandOpenURL(activity, urlEntity.getExpandedURL()));
            }
        }
        for (MediaEntity mediaEntity : tweet.getMedia()) {
            commands.add(new CommandOpenURL(activity, mediaEntity.getMediaURL()));
        }
    }

    public boolean addMainCommands(Activity activity, Tweet tweet, Account account, ArrayList<Command> commands) {
        return commands.addAll(Command.getStatusCommands(activity, tweet, account));
    }

    public List<Command> getCommands(Activity activity, Tweet tweet, Account account) {
        ArrayList<Command> commands = new ArrayList<>();
        addMainCommands(activity, tweet, account, commands);
        addBottomCommands(activity, tweet, account, commands);
        return commands;
    }

    private ArrayList<Command> getHashtagCommands(Activity activity, Tweet tweet) {
        ArrayList<Command> commands = new ArrayList<>();
        if (tweet.getHashtags() != null) {
            for (HashtagEntity hashtagEntity : tweet.getHashtags()) {
                commands.add(new CommandOpenHashtagDialog(activity, hashtagEntity));
            }
        }
        return commands;
    }
}
