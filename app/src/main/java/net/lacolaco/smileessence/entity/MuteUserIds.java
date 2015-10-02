package net.lacolaco.smileessence.entity;

import net.lacolaco.smileessence.twitter.task.BlockIDsTask;
import net.lacolaco.smileessence.twitter.task.MutesIDsTask;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MuteUserIds {
    private static Set<Long> storage = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static boolean isMuted(long userId) {
        return storage.contains(userId);
    }

    public static void remove(long userId) {
        storage.remove(userId);
    }

    public static void add(long userId) {
        storage.add(userId);
    }

    public static void refresh(Account account) {
        new BlockIDsTask(account).onDone(idList -> storage.addAll(idList)).execute();
        new MutesIDsTask(account).onDone(mutesIDs -> storage.addAll(mutesIDs)).execute();
    }
}
