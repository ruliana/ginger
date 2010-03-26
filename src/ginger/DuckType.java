package ginger;

import ginger.ducktype.MethodCache;
import ginger.ducktype.NoMethodCache;
import ginger.ducktype.SimpleMethodCache;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an simplified interface to dynamically execute methods.
 * <p>
 * Examples:
 * </p>
 * 
 * <pre>
 * // Execute it, no return value, no arguments
 * new DuckType(myObject).call("myMethodName");
 * 
 * // We can also have a return value
 * MyReturnType result = new DuckType(myObject).call("myMethodName");
 * 
 * // No problem with arguments in the method
 * MyReturnType result = new DuckType(myObject).call("myMethodName", arg1, arg2, ..., argX);
 * </pre>
 * <p>
 * Using static import, it's possible to shorten the call.
 * </p>
 * 
 * <pre>
 * // &quot;duck&quot; is the same as &quot;new DuckType&quot;, just shorter.
 * // Nice for inline use. 
 * duck(myObject).call(&quot;myMethodName&quot;);
 * 
 * // &quot;d&quot; is the same, but even shorter.
 * // Pretty neat if you want a &quot;embedded in the language&quot; look.
 * d(myObject).call(&quot;myMethodName&quot;);
 * </pre>
 * <p>
 * Why this is better than the Java way?
 * <ul>
 * <li>There is only one object to deal with;</li>
 * <li>It's boring to find methods that use primitive as arguments;</li>
 * <li>It's <em>really</em> boring to find methods that one of the arguments is
 * an ancestor of what you have in hands.</li>
 * </ul>
 * </p>
 * <p>
 * There is a cache for methods inside DuckType. It's static and it's solely
 * purpose is to speed up the lookup process, which can be pretty slow. It's
 * possible to turn the method caching off with {@link #turnMethodCacheOff()}
 * and re-enable it with {@link #turnMethodCacheOn()} (both static methods).
 * Also, you can use your own cache with {@link #useCache(MethodCache)}.
 * </p>
 * 
 * @author Ronie Uliana
 * @since 2010-03
 */
public class DuckType {

    private static final Map<Class<?>, Class<?>> primitiveEquivalents = new HashMap<Class<?>, Class<?>>();
    static {
        primitiveEquivalents.put(Boolean.class, Boolean.TYPE);
        primitiveEquivalents.put(Character.class, Character.TYPE);
        primitiveEquivalents.put(Byte.class, Byte.TYPE);
        primitiveEquivalents.put(Short.class, Short.TYPE);
        primitiveEquivalents.put(Integer.class, Integer.TYPE);
        primitiveEquivalents.put(Long.class, Long.TYPE);
        primitiveEquivalents.put(Float.class, Float.TYPE);
        primitiveEquivalents.put(Double.class, Double.TYPE);
    }

    private static MethodCache cache = new SimpleMethodCache();
    private final Object object;

    /**
     * Turns method caching <strong>off</strong>.
     * <p>
     * The cache is enable by default.
     * </p>
     * <p>
     * If you use your own cache engine, this method will remove it from
     * DuckType and you have to set it again with {@link #useCache(MethodCache)}
     * .
     * </p>
     */
    public static void turnMethodCacheOff() {
        if (isMethodCacheOff()) return;
        cache = new NoMethodCache();
    }

    public static boolean isMethodCacheOff() {
        return cache instanceof NoMethodCache;
    }

    /**
     * Turns method caching <strong>on</strong>.
     * <p>
     * The cache is already enable by default. It has no effect if the cache is
     * already enabled.
     * </p>
     * <p>
     * If you use your own cache engine, this method will revert it to the
     * default engine. If you want your own cache engine back, you must set it
     * again with {@link #useCache(MethodCache)} .
     * </p>
     */
    public static void turnMethodCacheOn() {
        if (isMethodCacheOn()) return;
        cache = new SimpleMethodCache();
    }

    public static boolean isMethodCacheOn() {
        return cache instanceof SimpleMethodCache;
    }

    /**
     * Changes the default cache engine by one provided by the user.
     * <p>
     * The engine provided in this method is lost if at any later time you call
     * {@link #turnMethodCacheOn()} or {@link #turnMethodCacheOff()}.
     * </p>
     */
    public static void useCache(MethodCache cacheEngine) {
        cache = cacheEngine;
    }

    /**
     * Returns the current cache engine. Very useful to get usage stats from it.
     * But you can also use it to operate the cache directly (whatever be the
     * reason of it).
     */
    public static MethodCache getCache() {
        return cache;
    }

    /**
     * Alias method for "new DuckType(object)", intended to me used with static
     * import.
     * 
     * <pre>
     * import static ginger.DuckType.duck;
     * ...
     * duck(object).call("method");
     * </pre>
     */
    public static DuckType duck(Object object) {
        return new DuckType(object);
    }

    /**
     * Alias method for "new DuckType(object)", intended to me used with static
     * import. Think <strong>"d"</strong> for <strong>"dynamic"</strong>.
     * 
     * <pre>
     * import static ginger.DuckType.d;
     * ...
     * d(object).call("method");
     * </pre>
     */
    public static DuckType d(Object object) {
        return new DuckType(object);
    }

    /**
     * You can call any method dynamically on the object passed as parameter
     * using {@link #call(String, Object...)}.
     */
    public DuckType(Object object) {
        this.object = object;
    }

    /**
     * Calls a method dynamically.
     * <p>
     * The <em>methodName</em> can be a {@link CharSequence}, that means it's
     * possible to use {@link String}, {@link StringBuilder} and several other
     * objects as the method name.
     * </p>
     * 
     * @param <T>
     *            Type of return value (this <em>can</em> throws a
     *            {@link ClassCastException}).
     * @param methodName
     *            The name of the method. Still case sensitive.
     * @param arguments
     *            The method arguments, if any.
     * @return The return value of the method or null if the method returns
     *         void.
     * @throws NoSuchMethodException
     *             A custom NoSuchMethodException that's not checked.
     */
    @SuppressWarnings("unchecked")
    public <T> T call(CharSequence methodName, Object... arguments)
            throws NoSuchMethodException {

        // Guard clause
        if (object == null) return null;

        String immutableMethodName = methodName.toString();
        Class<?>[] argumentTypes = classesFor(arguments);
        String objectType = object.getClass().getName();

        Method method = cache.get(objectType, immutableMethodName,
                                  argumentTypes);

        // If no method in cache, go find it and fill my cache
        if (method == null) {
            method = findMethod(immutableMethodName, argumentTypes);
            cache.put(method, objectType, immutableMethodName, argumentTypes);
        }

        try {

            return (T) method.invoke(object, arguments);

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Alias for {@link #callChained(String, Object...)}.
     */
    public DuckType chain(CharSequence methodName, Object... arguments) {
        return callChained(methodName, arguments);
    }

    /**
     * Same as {@link #call(String, Object...)}, but wraps the returning value
     * in another DuckType, allowing chain call.
     */
    public DuckType callChained(CharSequence methodName, Object... arguments)
            throws NoSuchMethodException {

        return new DuckType(call(methodName, arguments));
    }

    /**
     * Try its best to find the right method. First it tries the easiest way
     * possible, if it can't find the method, it tries harder.
     */
    private Method findMethod(String methodName, Class<?>... argumentTypes) {
        try {

            return findMethodEasyWay(methodName, argumentTypes);

            /*
             * I'm not really proud of use a catch to provide an alternative
             * path, but hell, I guess my intention can't be clearer!
             */
        } catch (java.lang.NoSuchMethodException e) {

            Method method = findMethodHardWay(methodName, argumentTypes);

            // Not sure if I should rethrow it... :|
            if (method == null) throw new NoSuchMethodException(e);

            return method;

        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * "Easy way" is to call "getMethod" directly. Unfortunately, there is
     * several cases when this does not match, mainly if the argument types are
     * superclasses of the arguments you have or if they are primitives.
     */
    private Method findMethodEasyWay(String methodName, Class<?>[] argumentTypes)
            throws java.lang.NoSuchMethodException {

        return objectClass().getMethod(methodName, argumentTypes);
    }

    /**
     * "Hard way" means to try the most suitable method (same name and number of
     * arguments) and check if there is any possibility of the arguments to be
     * compatible.
     */
    private Method findMethodHardWay(String methodName, Class<?>[] argumentTypes) {

        nextMethod: for (Method method : objectClass().getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (!methodName.equals(method.getName())) continue nextMethod;
            if (parameterTypes.length != argumentTypes.length)
                continue nextMethod;

            for (int i = 0; i < parameterTypes.length; i++)
                if (notCompatible(parameterTypes[i], argumentTypes[i]))
                    continue nextMethod;

            return method;
        }
        return null;
    }

    /**
     * Check for type compatibility.
     * <p>
     * "Compatibility" means:
     * </p>
     * <ul>
     * <li>Type1 is a primitive and type2 is and object that can be "autoboxed"
     * to the primitive;</li>
     * <li>Type1 is a an ancestor of type2.</li>
     * </ul>
     */
    private boolean isCompatible(Class<?> type1, Class<?> type2) {
        return primitivelyCompatible(type1, type2)
                || assignableCompatible(type1, type2);
    }

    private boolean notCompatible(Class<?> type1, Class<?> type2) {
        return !isCompatible(type1, type2);
    }

    private boolean primitivelyCompatible(Class<?> type1, Class<?> type2) {
        return type1.isPrimitive() && type1.equals(toPrimitive(type2));
    }

    private boolean assignableCompatible(Class<?> type1, Class<?> type2) {
        return !type1.isPrimitive() && type1.isAssignableFrom(type2);
    }

    private Class<?> toPrimitive(Class<?> clazz) {
        return primitiveEquivalents.get(clazz);
    }

    private Class<? extends Object> objectClass() {
        return object.getClass();
    }

    private Class<?>[] classesFor(Object[] arguments) {
        Class<?>[] result = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            result[i] = arguments[i] == null
                    ? Object.class
                    : arguments[i].getClass();
        }
        return result;
    }

    /**
     * RuntimeException version of {@link java.lang.NoSuchMethodException}.
     * <p>
     * It was made to be as transparent as possible.
     * </p>
     */
    @SuppressWarnings("serial")
    public static class NoSuchMethodException extends RuntimeException {
        private final java.lang.NoSuchMethodException e;

        public NoSuchMethodException(java.lang.NoSuchMethodException e) {
            this.e = e;
        }

        public Throwable fillInStackTrace() {
            return e.fillInStackTrace();
        }

        public Throwable getCause() {
            return e.getCause();
        }

        public String getLocalizedMessage() {
            return e.getLocalizedMessage();
        }

        public String getMessage() {
            return e.getMessage();
        }

        public StackTraceElement[] getStackTrace() {
            return e.getStackTrace();
        }

        public Throwable initCause(Throwable cause) {
            return e.initCause(cause);
        }

        public void printStackTrace() {
            e.printStackTrace();
        }

        public void printStackTrace(PrintStream s) {
            e.printStackTrace(s);
        }

        public void printStackTrace(PrintWriter s) {
            e.printStackTrace(s);
        }

        public void setStackTrace(StackTraceElement[] stackTrace) {
            e.setStackTrace(stackTrace);
        }

        public String toString() {
            return e.toString();
        }
    }
}