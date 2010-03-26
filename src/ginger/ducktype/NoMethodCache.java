package ginger.ducktype;

import java.lang.reflect.Method;

/**
 * Engine to ignore caching at all.
 */
public class NoMethodCache implements MethodCache {

    private int missesCount = 0;
    
    /**
     * Ignore the method being cached (does NOT cache it).
     */
    public void put(Method method, String objectType, String methodName, Class<?>... argumentTypes) {
    }

    /**
     * Always return null, meaning "not found in cache".
     */
    public Method get(String objectType, String methodName, Class<?>... argumentTypes) {
        missesCount++;
        return null;
    }

    /**
     * Do nothing. A clear on a clean cache is pointless.
     */
    public void clear() {
        missesCount = 0;
    }

    /**
     * Always ZERO. No method can be found here.
     */
    public int hitCount() {
        return 0;
    }

    /**
     *  How many time this cache was asked for a method.
     */
    public int missesCount() {
        return missesCount;
    }

    /**
     * Always ZERO. This is an empty cache.
     */
    public int size() {
        return 0;
    }
}
