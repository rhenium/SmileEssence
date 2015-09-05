package net.lacolaco.smileessence.twitter.task;

import net.lacolaco.smileessence.entity.Tweet;
import net.lacolaco.smileessence.logging.Logger;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

public class GetTalkTask extends TwitterTaskWithProgress<List<Tweet>, Tweet> {
    private final long statusId;

    public GetTalkTask(Twitter twitter, long statusId) {
        super(twitter);
        this.statusId = statusId;
    }

    @Override
    protected List<Tweet> doInBackground(Void... params) {
        ArrayList<Tweet> list = new ArrayList<>();
        try {
            long id = statusId;
            while (id != -1) {
                Tweet tweet = Tweet.fromTwitter(twitter.showStatus(id));
                if (tweet != null) {
                    publishProgress(tweet);
                    list.add(tweet);
                } else {
                    break;
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            Logger.error(e);
        }
        return list;
    }
}
