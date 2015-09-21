package net.lacolaco.smileessence.util;

import android.os.AsyncTask;
import net.lacolaco.smileessence.logging.Logger;

public abstract class BackgroundTask<Result, Progress> {
    private Consumer<Result> then;
    private Consumer<Progress> progress;
    private Consumer<Exception> fail;
    private Exception exception;
    private final InnerAsyncTask task;

    public BackgroundTask() {
        this.task = new InnerAsyncTask();
    }

    public BackgroundTask<Result, Progress> onDone(Consumer<Result> cb) {
        this.then = cb;
        return this;
    }

    public BackgroundTask<Result, Progress> onProgress(Consumer<Progress> cb) {
        this.progress = cb;
        return this;
    }

    public BackgroundTask<Result, Progress> onFail(Consumer<Exception> cb) {
        this.fail = cb;
        return this;
    }

    public BackgroundTask<Result, Progress> onDoneUI(Consumer<Result> cb) {
        return onDone(r -> new UIHandler().post(() -> cb.accept(r)));
    }

    public BackgroundTask<Result, Progress> onProgressUI(Consumer<Progress> cb) {
        return onProgress(p -> new UIHandler().post(() -> cb.accept(p)));
    }

    public BackgroundTask<Result, Progress> onFailUI(Consumer<Exception> cb) {
        return onFail(e -> new UIHandler().post(() -> cb.accept(e)));
    }

    public boolean cancel() {
        return task.cancel(true);
    }

    public final BackgroundTask<Result, Progress> execute() {
        task.execute();
        return this;
    }

    public final Result getImmediately() throws Exception {
        Result result = task.get();
        if (exception == null) {
            return result;
        } else {
            throw exception;
        }
    }

    protected void fail(Exception ex) {
        ex.printStackTrace();
        Logger.error(ex);

        if (!task.isCancelled() && fail != null) {
            exception = ex;
            fail.accept(ex);
        }
    }

    protected void progress(Progress value) {
        if (!task.isCancelled() && exception == null && progress != null) {
            progress.accept(value);
        }
    }

    protected abstract Result doInBackground() throws Exception;

    private class InnerAsyncTask extends AsyncTask<Void, Progress, Result> {
        @Override
        protected final void onPostExecute(Result result) {
            if (!isCancelled() && exception == null && then != null) {
                then.accept(result);
            }
        }

        @Override
        protected Result doInBackground(Void... params) {
            try {
                return BackgroundTask.this.doInBackground();
            } catch (Exception ex) {
                fail(ex);
                return null;
            }
        }
    }
}
