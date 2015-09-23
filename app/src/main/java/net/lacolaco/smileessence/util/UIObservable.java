package net.lacolaco.smileessence.util;

import net.lacolaco.smileessence.entity.RO;

import java.util.*;

public abstract class UIObservable {
    Map<Object, UIObserver> observers = new WeakHashMap<>();

    public void addObserver(Object weakKey, UIObserver observer) {
        synchronized(this) {
            observers.put(weakKey, observer);
        }
    }

    public void notifyChange(RO flag) {
        notifyChange(EnumSet.of(flag));
    }

    public void notifyChange(EnumSet<RO> flags) {
            List<UIObserver> obs = new ArrayList<>();
        synchronized(this) {
            obs.addAll(observers.values());
        }

        new UIHandler().post(() -> {
            for (UIObserver observer : obs) {
                observer.update(this, flags);
            }
        });
    }
}
