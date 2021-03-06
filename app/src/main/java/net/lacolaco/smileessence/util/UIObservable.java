package net.lacolaco.smileessence.util;

import net.lacolaco.smileessence.entity.RBinding;

import java.util.*;

public abstract class UIObservable {
    private Map<Object, UIObserver> observers = new WeakHashMap<>();

    public void addObserver(Object weakKey, UIObserver observer) {
        synchronized(this) {
            observers.put(weakKey, observer);
        }
    }

    public UIObserver removeObserver(Object weakKey) {
        synchronized (this) {
            return observers.remove(weakKey);
        }
    }

    protected void notifyChange(RBinding flag) {
        notifyChange(EnumSet.of(flag));
    }

    protected void notifyChange(EnumSet<RBinding> flags) {
            List<UIObserver> obs = new ArrayList<>();
        synchronized(this) {
            obs.addAll(observers.values());
        }

        new UIHandler().post(() -> {
            for (UIObserver observer : obs) {
                observer.update(flags);
            }
        });
    }
}
