package net.lacolaco.smileessence.util;

import net.lacolaco.smileessence.entity.RBinding;

import java.util.EnumSet;

public interface UIObserver {
    void update(EnumSet<RBinding> flags);
}
