package net.lacolaco.smileessence.view;

import android.app.Fragment;
import android.widget.Adapter;

public abstract class PageFragment<T extends Adapter> extends Fragment {
    private T adapter;

    protected T getAdapter() {
        if (adapter == null) throw new IllegalStateException("adapter is not initialized");
        return adapter;
    }

    protected void setAdapter(T _adapter) {
        adapter = _adapter;
    }

    public abstract void refresh();
}
