package net.lacolaco.smileessence.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.lacolaco.smileessence.util.ListUtils;
import twitter4j.*;

import java.util.Date;
import java.util.List;

public class DirectMessage {
    // キャッシュ これも weak reference
    private static Cache<Long, DirectMessage> storage = CacheBuilder.newBuilder().weakValues().build();

    public synchronized static DirectMessage fetch(long statusId) {
        return storage.getIfPresent(statusId);
    }

    public synchronized static void remove(long statusId) {
        storage.invalidate(statusId);
    }

    public synchronized static DirectMessage fromTwitter(twitter4j.DirectMessage st) {
        DirectMessage t = fetch(st.getId());
        if (t == null) {
            t = new DirectMessage(st);
            storage.put(st.getId(), t);
        } else {
            t.update(st);
        }
        return t;
    }

    public synchronized static List<DirectMessage> fromTwitter(List<twitter4j.DirectMessage> sts) {
        return ListUtils.map(sts, DirectMessage::fromTwitter);
    }

    // インスタンス
    private long id;
    private User sender;
    private User recipient;
    private String text;
    private Date createdAt;

    private UserMentionEntity[] mentions;
    private HashtagEntity[] hashtags;
    private MediaEntity[] media;
    private URLEntity[] urls;
    private SymbolEntity[] symbols;

    private DirectMessage(twitter4j.DirectMessage st) {
        update(st);
    }

    private void update(twitter4j.DirectMessage message) {
        id = message.getId();
        sender = User.fromTwitter(message.getSender());
        recipient = User.fromTwitter(message.getRecipient());
        text = message.getText();
        createdAt = message.getCreatedAt();

        mentions = message.getUserMentionEntities();
        hashtags = message.getHashtagEntities();
        media = message.getExtendedMediaEntities().length > 0 ? message.getExtendedMediaEntities() : message.getMediaEntities();
        urls = message.getURLEntities();
        symbols = message.getSymbolEntities();
    }

    public long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getText() {
        return text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public UserMentionEntity[] getMentions() {
        return mentions;
    }

    public HashtagEntity[] getHashtags() {
        return hashtags;
    }

    public MediaEntity[] getMedia() {
        return media;
    }

    public URLEntity[] getUrls() {
        return urls;
    }

    public SymbolEntity[] getSymbols() {
        return symbols;
    }
}
