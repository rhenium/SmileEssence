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
import net.lacolaco.smileessence.entity.DirectMessage;
import net.lacolaco.smileessence.view.adapter.CustomListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessageMenuDialogFragment extends MenuDialogFragment {

    // ------------------------------ FIELDS ------------------------------

    private static final String KEY_MESSAGE_ID = "messageID";
    private DirectMessage message;

    // --------------------- GETTER / SETTER METHODS ---------------------

    public long getMessageID() {
        return getArguments().getLong(KEY_MESSAGE_ID);
    }

    public void setMessageID(long messageID) {
        Bundle args = new Bundle();
        args.putLong(KEY_MESSAGE_ID, messageID);
        setArguments(args);
    }

    // ------------------------ OVERRIDE METHODS ------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        message = DirectMessage.fetch(getMessageID());
    }

    @Override
    protected void setMenuItems(final CustomListAdapter<Command> adapter) {
        if (message != null) {
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
        commands.add(new CommandSaveAsTemplate(activity, message.getText()));
        //User
        if (message.getSender() != message.getRecipient()) {
            commands.add(new CommandOpenUserDetail(activity, message.getRecipient().getScreenName()));
        }
        for (String screenName : message.getMentions()) {
            commands.add(new CommandOpenUserDetail(activity, screenName));
        }
        for (Command command : getHashtagCommands()) {
            commands.add(command);
        }
        // Media
        for (String url : message.getUrlsExpanded()) {
            commands.add(new CommandOpenURL(activity, url));
        }
        for (String url : message.getMediaUrls()) {
            commands.add(new CommandOpenURL(activity, url));
        }
    }

    public boolean addMainCommands(ArrayList<Command> commands) {
        Activity activity = getActivity();
        return commands.addAll(Command.getMessageCommands(activity, message));
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
        if (message.getHashtags() != null) {
            for (String hashtag : message.getHashtags()) {
                commands.add(new CommandOpenHashtagDialog(activity, hashtag));
            }
        }
        return commands;
    }
}
