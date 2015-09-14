package net.lacolaco.smileessence.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    public static <T, R> List<R> map(List<T> orig, Function<T, R> func) {
        List<R> result = new ArrayList<>(orig.size());
        for (T item : orig) {
            result.add(func.apply(item));
        }
        return result;
    }
}
