package net.lacolaco.smileessence.twitter.task;

import android.os.AsyncTask;
import twitter4j.Twitter;

public abstract class TwitterTaskWithProgress<T, A> extends AsyncTask<Void, A, T> {
    protected Twitter twitter;

    protected TwitterTaskWithProgress(Twitter twitter) {
        this.twitter = twitter;
    }
}

