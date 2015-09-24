package net.lacolaco.smileessence.util;

import java.util.HashMap;
import java.util.Map;

// UIObserver と UIObservable のセットを管理するオブジェクトだよ〜〜
public class UIObserverBundle {
    private Map<UIObservable, UIObserver> map = new HashMap<>();

    public void detachAll() {
        for (Map.Entry<UIObservable, UIObserver> entry : map.entrySet()) {
            entry.getKey().removeObserver(this);
        }
        map.clear();
    }

    public UIObserver attach(UIObservable observable, UIObserver observer) {
        observable.addObserver(this, observer);
        map.put(observable, observer);
        return observer;
    }
}
