package net.lacolaco.smileessence.util;

import net.lacolaco.smileessence.entity.RO;

import java.util.EnumSet;

public interface UIObserver {
    void update(UIObservable observable, EnumSet<RO> flags);
}
