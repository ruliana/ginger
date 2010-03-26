package ginger.ducktype;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * We use a method cache to speed up the method lookup. Very useful when
 * iterating over a collection.
 * <p>
 * The problem here is that the cache has no limit and no automatic purge. I
 * don't believe this is a problem unless we use DuckType with
 * <em>thousands</em> of different methods, what is a problem by itself. Just in
 * case, we provide a method to clean the cache.
 * </p>
 */
public class SimpleMethodCache implements MethodCache {
    private Map<String, Method> cache = new ConcurrentHashMap<String, Method>();
    private int hitCount = 0;
    private int missesCount = 0;

    /**
     * {@inheritDoc}
     */
    public void put(Method method, String objectType, String methodName, Class<?>... argumentTypes) {
        cache.put(toKey(objectType, methodName, argumentTypes), method);
    }

    /**
     * {@inheritDoc}
     */
    public Method get(String objectType, String methodName, Class<?>... argumentTypes) {
        Method result = cache.get(toKey(objectType, methodName, argumentTypes));
        if (result == null) {
            missesCount++;
        } else {
            hitCount++;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        cache.clear();
        /*
         * We can have a problem with concurrency here. Imagine we try to clear
         * both fields when someone is simultaneously getting a method. The
         * hitCount and missesCount can reflect a different number than the
         * real. But... who cares? They should not be precise number, just
         * informative hints. I don't want to implement synchronize here because
         * we would have to implement it on the "get" method also.
         */
        hitCount = 0;
        missesCount = 0;
    }

    /**
     * {@inheritDoc}
     */
    public int hitCount() {
        return hitCount;
    }

    /**
     * {@inheritDoc}
     */
    public int missesCount() {
        return missesCount;
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return cache.size();
    }

    private String toKey(String objectType, String methodName, Class<?>... argumentTypes) {
        StringBuilder result = new StringBuilder();
        result.append(objectType);
        result.append("|");
        result.append(methodName);
        for (Class<?> clazz : argumentTypes) {
            result.append("|");
            result.append(clazz.getName());
        }
        return result.toString();
    }
}