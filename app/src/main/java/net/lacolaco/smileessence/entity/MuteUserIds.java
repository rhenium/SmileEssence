package net.lacolaco.smileessence.entity;

import java.util.HashSet;
import java.util.Set;

public class MuteUserIds {
    private static Set<Long> storage = new HashSet<>();

    public synchronized static boolean isMuted(long userId) {
        return storage.contains(userId);
    }

    public synchronized static void remove(long userId) {
        storage.remove(userId);
    }

    public synchronized static void add(long userId) { storage.add(userId); }
}
