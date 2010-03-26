package ginger;

import static ginger.DuckType.d;
import static ginger.DuckType.duck;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class DuckTypeTest {

    @Test
    public void shouldExecuteDirectMethodsWithNoArguments() throws Exception {
        assertEquals("test", new DuckType("Test").call("toLowerCase"));
        assertEquals("TEST", new DuckType("Test").call("toUpperCase"));
        assertEquals(4, new DuckType("Test").call("length"));
    }

    @Test
    public void shouldExecuteDirectMethodsWithSimpleArguments()
            throws Exception {
        assertEquals(0, new DuckType("test").call("compareTo", "test"));
        assertEquals("testing", new DuckType("test").call("concat", "ing"));
    }

    @Test
    public void shouldExecuteDirectMethodsWithCompatibleArguments()
            throws Exception {

        // "es" and "oas" can be CharSequence =/
        assertEquals("toast", new DuckType("test").call("replace", "es", "oas"));

        // 0 and 1 are primitives
        assertEquals("T", new DuckType("Test").call("substring", 0, 1));
        assertEquals("est", new DuckType("Test").call("substring", 1));
    }

    @Test
    public void aliasesShouldWorkToo() throws Exception {

        // No alias
        assertEquals("est", new DuckType("Test").call("substring", 1));

        assertEquals("est", duck("Test").call("substring", 1));
        assertEquals("est", d("Test").call("substring", 1));
    }

    @Test
    public void chainCall() throws Exception {
        assertEquals("st", new DuckType("Test").callChained("substring", 1).call("substring", 1));
        assertEquals("st", new DuckType("Test").chain("substring", 1).call("substring", 1));
    }

    @Test
    public void bugShouldNotConfuseMethodsWithSameSignatureInDifferentObjects()
            throws Exception {
        assertEquals("Test1", d(new Test1()).call("toString"));
        try {
            assertEquals("Test2", d(new Test2()).call("toString"));
            // success
        } catch (IllegalArgumentException e) {
            fail("It's confusing methods form different objects");
        }
    }

    /**
     * Slow test.
     */
    @Test
    public void methodCachingShouldMakeDifference() throws Exception {
        int subjectSize = 100000;

        List<String> testSubject = new LinkedList<String>();
        for (int i = 0; i < subjectSize; i++) {
            testSubject.add(format("test %05d", i));
        }

        double startNoCacheTime = currentTimeMillis();
        // No cache
        DuckType.turnMethodCacheOff();
        for (String string : testSubject) {
            new DuckType(string).call("substring", 6, 10);
        }
        double elapsedNoCacheTime = currentTimeMillis() - startNoCacheTime;

        double startCachedTime = currentTimeMillis();
        // Caching
        DuckType.turnMethodCacheOn();
        for (String string : testSubject) {
            new DuckType(string).call("substring", 6, 10);
        }
        double elapsedCachedTime = currentTimeMillis() - startCachedTime;

        double startDirectCallTime = currentTimeMillis();
        // Direct call
        DuckType.turnMethodCacheOn();
        for (String string : testSubject) {
            string.substring(6, 10);
        }
        double elapsedDirectCallTime = currentTimeMillis() - startDirectCallTime;

        System.out.println(format("Time no cache: %4.0f", elapsedNoCacheTime));
        System.out.println(format("Time cached  : %4.0f", elapsedCachedTime));
        System.out.println(format("%.1fx faster than no cache", elapsedNoCacheTime / elapsedCachedTime));
        System.out.println(format("Direct call  : %4.0f", elapsedDirectCallTime));
        System.out.println(format("%.1fx faster than cached", elapsedCachedTime / elapsedDirectCallTime));
        System.out.println(format("Cache hits  : %d", DuckType.getCache().hitCount()));

        int timesFaster = 10;
        assertTrue("Cached should be at least " + timesFaster + "x faster",
                   elapsedNoCacheTime / elapsedCachedTime > timesFaster);
    }

    private static class Test1 {
        public String toString() {
            return "Test1";
        }
    }

    private static class Test2 {
        public String toString() {
            return "Test2";
        }
    }
}
