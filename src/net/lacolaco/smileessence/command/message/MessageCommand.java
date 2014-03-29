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

package net.lacolaco.smileessence.command.message;

import android.content.Context;
import net.lacolaco.smileessence.command.Command;
import net.lacolaco.smileessence.data.DirectMessageCache;
import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.twitter.TwitterApi;
import net.lacolaco.smileessence.twitter.task.ShowDirectMessageTask;
import twitter4j.DirectMessage;

import java.util.concurrent.ExecutionException;

public abstract class MessageCommand extends Command
{

    private final Account account;
    private final long messageID;

    public MessageCommand(int key, Context context, Account account, long messageID)
    {
        super(key, context);
        this.account = account;
        this.messageID = messageID;
    }

    public Account getAccount()
    {
        return account;
    }

    public long getMessageID()
    {
        return messageID;
    }

    protected final DirectMessage tryGetMessage()
    {
        DirectMessage message = DirectMessageCache.getInstance().get(messageID);
        if(message != null)
        {
            return message;
        }
        ShowDirectMessageTask task = new ShowDirectMessageTask(new TwitterApi(account).getTwitter(), messageID);
        task.execute();
        try
        {
            message = task.get();
        }
        catch(InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
        return message;
    }
}
