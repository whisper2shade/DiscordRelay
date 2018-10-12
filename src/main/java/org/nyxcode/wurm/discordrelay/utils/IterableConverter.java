package org.nyxcode.wurm.discordrelay.utils;

import java.util.*;

public class IterableConverter {

    public static <T> List<T> toList(Iterable<T> source) {

        if (source instanceof List) {
            return (List<T>) source;
        }

        if (source instanceof Collection) {
            return new ArrayList<>((Collection<T>) source);
        }

        List<T> result = new ArrayList<>();
        for (T value : source) {
            result.add(value);
        }
        return result;
    }

    public static <T> T getFirst(Iterable<T> source) {
        return toList(source).get(0);
    }
}
