package ginger;

import static ginger.Seq.s;
import static ginger.Seq.Pair.p;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.junit.Test;

public class SeqTest {

    @Test
    public void zeroElementConstructorsReturnsZeroElementSeq() {
        // Full Constructor
        Seq<Integer> s1 = new Seq<Integer>();
        assertEquals(0, s1.size());

        // Short Constructor (preferred)
        Seq<Integer> s2 = s();
        assertEquals(0, s2.size());
    }

    @Test
    public void oneElementConstructorReturnsOneElementSeq() {
        // Full Constructor
        Seq<String> s1 = new Seq<String>("one");
        assertEquals(1, s1.size());

        // Short Constructor (preferred)
        Seq<Integer> s2 = s(1);
        assertEquals(1, s2.size());
    }

    @Test
    public void oneElementConstructorUsingNullShouldGenerateOneElementSeq() {
        // It generates a "warning", but should works
        Seq<Object> s1 = s(null);
        assertEquals(1, s1.size());

        // No warnings here if you cast "null" to "Object" :p
        Seq<Object> s2 = s((Object) null);
        assertEquals(1, s2.size());
    }

    @SuppressWarnings("unused")
    @Test
    public void seqCanBeUsedAsCollection() {
        Collection<Integer> seq = s(1, 2, 3);
    }

    @SuppressWarnings("unused")
    @Test
    public void seqCanBeUsedAsList() {
        List<Integer> seq = s(1, 2, 3);
    }

    @SuppressWarnings("unused")
    @Test
    public void seqCanBeUsedAsDeque() {
        Deque<Integer> seq = s(1, 2, 3);
    }

    @Test
    public void joinElementsReturnsAllElementsAsString() {
        Seq<Integer> seq = s(1, 2, 3);
        assertEquals("123", seq.join(""));
        assertEquals("1, 2, 3", seq.join(", "));
        assertEquals("1, 2, 3", seq.join());
        assertEquals("1, 2 and 3", seq.join(", ", " and "));

        assertEquals("1 and 2", s(1, 2).join(", ", " and "));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void equalsShouldCompareInnerElements() {
        assertEquals(s(), s());
        assertEquals(s(1), s(1));
        assertEquals(s("a"), s("a"));
        assertEquals(s("a", "b", "c"), s("a", "b", "c"));
        assertFalse(s("a", "b", "c").equals(s(1, 2, 3)));

        assertEquals(s(s(1), s(2)), s(s(1), s(2)));
        assertFalse(s(s(1), s(2)).equals(s(s(2), s(1))));
    }

    @Test
    public void removeNullsAndBlanks() {
        assertEquals(s(), s(null).removeNullsAndBlanks());
        assertEquals(s(), s(null, null).removeNullsAndBlanks());
        assertEquals(s(), s("").removeNullsAndBlanks());
        assertEquals(s(1, 3), s(1, null, 3).removeNullsAndBlanks());
        assertEquals(s("a", "c"), s(null, "a", null, "   ", "c", "").removeNullsAndBlanks());
    }

    @Test
    public void appendIsLikeAddButChained() {
        Seq<Integer> seq = s();
        
        seq.append(1)
           .append(2)
           .append(3);
        
        assertEquals(s(1, 2, 3), seq);
    }

    @Test
    public void prependIsLikeAddFirstButChained() {
        Seq<Integer> seq = s();
        
        seq.prepend(1)
           .prepend(2)
           .prepend(3);
        
        assertEquals(s(3, 2, 1), seq);
    }
    
    @Test
    public void combineShouldterateSimultanouslyInTwoLists() {
        Seq<Integer> numbers = s(1, 2, 3);
        Seq<String> letters = s("a", "b", "c");
        
        assertEquals(s(p(1, "a"), p(2, "b"), p(3, "c")), numbers.combinedWith(letters));
    }
}
