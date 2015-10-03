package net.lacolaco.smileessence.entity;

import net.lacolaco.smileessence.util.UIObservable;
import twitter4j.*;

import java.util.*;

public abstract class EntitySupport extends UIObservable {
    private List<String> mentions;
    private List<String> hashtags;
    private List<String> mediaUrls;
    private List<String> urlsExpanded;
    private List<String> symbols;

    protected void updateEntities(twitter4j.EntitySupport status) {
        mentions = extractMentions(status.getUserMentionEntities());
        hashtags = extractHashtags(status.getHashtagEntities());
        mediaUrls = extractMediaUrls(status.getExtendedMediaEntities().length > 0 ? status.getExtendedMediaEntities() : status.getMediaEntities());
        urlsExpanded = extractExpandedUrls(status.getURLEntities());
        symbols = extractSymbols(status.getSymbolEntities());
    }

    public List<String> getMentions() {
        return mentions;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public List<String> getUrlsExpanded() {
        return urlsExpanded;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    private List<String> extractMentions(UserMentionEntity[] entities) {
        List<String> names = new ArrayList<>();
        if (entities != null) {
            for (UserMentionEntity entity : entities) {
                names.add(entity.getScreenName());
            }
        }
        return names;
    }

    private List<String> extractSymbols(SymbolEntity[] entities) {
        List<String> names = new ArrayList<>();
        if (entities != null) {
            for (SymbolEntity entity : entities) {
                names.add(entity.getText());
            }
        }
        return names;
    }

    private List<String> extractExpandedUrls(URLEntity[] entities) {
        List<String> names = new ArrayList<>();
        if (entities != null) {
            for (URLEntity entity : entities) {
                names.add(entity.getExpandedURL());
            }
        }
        return names;
    }

    private List<String> extractMediaUrls(MediaEntity[] entities) {
        List<String> names = new ArrayList<>();
        if (entities != null) {
            for (MediaEntity entity : entities) {
                names.add(entity.getMediaURLHttps());
            }
        }
        return names;
    }

    private List<String> extractHashtags(HashtagEntity[] entities) {
        List<String> names = new ArrayList<>();
        if (entities != null) {
            for (HashtagEntity entity : entities) {
                names.add(entity.getText());
            }
        }
        return names;
    }

    protected String extractText(twitter4j.Status status, boolean expand) {
        return extractText(status, status.getText(), expand);
    }

    protected String extractText(twitter4j.DirectMessage status, boolean expand) {
        return extractText(status, status.getText(), expand);
    }

    private String extractText(twitter4j.EntitySupport status, String text, boolean expand) {
        SortedSet<twitter4j.URLEntity> set = new TreeSet<>((a, b) -> a.getStart() - b.getStart());
        if (status.getURLEntities() != null) {
            Collections.addAll(set, status.getURLEntities());
        }
        if (status.getExtendedMediaEntities() != null) {
            Collections.addAll(set, status.getExtendedMediaEntities());
        } else if (status.getMediaEntities() != null) {
            Collections.addAll(set, status.getMediaEntities());
        }

        for (URLEntity entity : set) {
            String newString = expand ? entity.getExpandedURL() : entity.getDisplayURL();
            text = text.replaceFirst(entity.getText(), newString);
        }

        return text;
    }
}
