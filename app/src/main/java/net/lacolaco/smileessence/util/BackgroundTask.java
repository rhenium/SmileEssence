package net.lacolaco.smileessence.util;

import android.os.AsyncTask;

public abstract class BackgroundTask<Result, Progress> extends AsyncTask<Void, Progress, Result> {
    private Consumer<Result> then;
    private Consumer<Progress> progress;

    public BackgroundTask<Result, Progress> onDone(Consumer<Result> cb) {
        this.then = cb;
        return this;
    }

    public BackgroundTask<Result, Progress> onProgress(Consumer<Progress> cb) {
        this.progress = cb;
        return this;
    }

    public BackgroundTask<Result, Progress> onDoneUI(Consumer<Result> cb) {
        return onDone(r -> new UIHandler().post(() -> cb.accept(r)));
    }

    public BackgroundTask<Result, Progress> onProgressUI(Consumer<Progress> cb) {
        return onProgress(p -> new UIHandler().post(() -> cb.accept(p)));
    }

    public boolean cancel() {
        return cancel(true);
    }

    @Override
    protected final void onProgressUpdate(Progress... values) {
        for (Progress value : values) {
            if (!isCancelled()) {
                progress.accept(value);
            }
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (!isCancelled()) {
            then.accept(result);
        }
    }
}
