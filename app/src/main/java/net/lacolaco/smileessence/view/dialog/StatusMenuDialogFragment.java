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
import net.lacolaco.smileessence.command.*;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class StatusMenuDialogFragment extends MenuDialogFragment {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_STATUS_ID = "statusID";
    private Tweet tweet;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tweet = Tweet.fetch(getStatusID());
    }

    @Override
    protected void setMenuItems(final CustomListAdapter<Command> adapter) {
        if (tweet != null) {
            List<Command> commands = getCommands();
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

    public void addBottomCommands(ArrayList<Command> commands) {
        Activity activity = getActivity();

        commands.add(new CommandSaveAsTemplate(activity, tweet.getOriginalTweet().getText()));
        //User
        commands.add(new CommandOpenUserDetail(activity, tweet.getUser().getScreenName()));
        for (String screenName : new ArrayList<>(new LinkedHashSet<>(tweet.getMentions()))) { // Array#uniq
            commands.add(new CommandOpenUserDetail(activity, screenName));
        }
        for (Command command : getHashtagCommands()) {
            commands.add(command);
        }
        // Media
        for (String url : tweet.getUrlsExpanded()) {
            commands.add(new CommandOpenURL(activity, url));
        }
        for (String url : tweet.getMediaUrls()) {
            commands.add(new CommandOpenURL(activity, url));
        }
    }

    public boolean addMainCommands(ArrayList<Command> commands) {
        Activity activity = getActivity();
        return commands.addAll(Command.getStatusCommands(activity, tweet));
    }

    public List<Command> getCommands() {
        ArrayList<Command> commands = new ArrayList<>();
        addMainCommands(commands);
        addBottomCommands(commands);
        return commands;
    }

    private ArrayList<Command> getHashtagCommands() {
        Activity activity = getActivity();
        ArrayList<Command> commands = new ArrayList<>();
        for (String hashtag : tweet.getHashtags()) {
            commands.add(new CommandOpenHashtagDialog(activity, hashtag));
        }
        return commands;
    }
}
