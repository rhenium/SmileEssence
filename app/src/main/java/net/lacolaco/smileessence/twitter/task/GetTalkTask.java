package net.lacolaco.smileessence.twitter.task;

import net.lacolaco.smileessence.entity.Account;
import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.logging.Logger;
import net.lacolaco.smileessence.util.BackgroundTask;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

public class GetTalkTask extends BackgroundTask<List<Tweet>, Tweet> {
    private final Account account;
    private final long statusId;

    public GetTalkTask(Account account, long statusId) {
        this.account = account;
        this.statusId = statusId;
    }

    @Override
    protected List<Tweet> doInBackground(Void... params) {
        ArrayList<Tweet> list = new ArrayList<>();
        long id = statusId;
        try {
            while (id != -1) {
                Tweet tweet = Tweet.fetch(id);
                if (tweet == null) {
                    tweet = Tweet.fromTwitter(account.getTwitter().showStatus(id));
                }

                if (tweet == null) {
                    break;
                } else {
                    list.add(tweet);
                    publishProgress(tweet);
                    id = tweet.getInReplyTo();
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            Logger.error(e);
        }
        return list;
    }
}
