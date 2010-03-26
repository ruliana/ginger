package ginger.ducktype;

import java.lang.reflect.Method;

/**
 * The interface to a caching engine speed up the method lookup. 
 */
public interface MethodCache {
    
    /**
     * Add a method to cache using the method name and argument types as key to find it later.
     */
    public void put(Method method, String objectType, String methodName, Class<?>... argumentTypes);
    
    /**
     * Gets the method from cache using the method name and argument types.
     */
    public Method get(String objectType, String methodName, Class<?>... argumentTypes);
    
    /**
     * Empties the cache.
     */
    public void clear();
    
    /**
     * How many times we found methods in cache.
     */
    public int hitCount();
    
    /**
     * How many times we didn't find methods in cache.
     */
    public int missesCount();
    
    /**
     * How many methods do we have in cache.
     */
    public int size();
}