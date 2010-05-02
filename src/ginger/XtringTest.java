package ginger;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class XtringTest {

    //===========================
    // High level interface
    //===========================

	@Test
	public void find() throws Exception {
		Xtring result;
		
		result = new Xtring("testing the test").find("t.e");
		assertXtringEquals("the", result);
		
		result = new Xtring("testing the test").find("not exists");
		assertNull(result);
		
		// We get only the first group
		result = new Xtring("testing the test").find("testing (the) (test)");
		assertXtringEquals("the", result);
		
		// More intuitive way to do "lookahead" and "lookbehind"
		result = new Xtring("testing the test").find("testing (.*) test");
		assertXtringEquals("the", result);
	}
	
	@Test
	public void findAll() throws Exception {
		LinkedList<Xtring> result;
		
		result = new Xtring("taste the test").findAll("taste|test");
		assertEquals("Number of matches", 2, result.size());
		assertXtringEquals("First match", "taste", result.getFirst());
		assertXtringEquals("Last match", "test", result.getLast());
		
		result = new Xtring("testing the test").findAll("not exists");
		assertTrue(result.isEmpty());
	}

	@Test
	public void negativeFind() throws Exception {
		Xtring result;
		
		result = new Xtring("testing the test").negativeFind("testing");
		assertXtringEquals(" the test", result);
		
		result = new Xtring("testing the test").negativeFind(" the ");
		assertXtringEquals("testing", result);
	}
	
    //===========================
    // Low level interface
    //===========================
	
    @Test
    public void basicMatching() throws Exception {

        final Boolean[] wasExecuted = new Boolean[] { false };

        new Xtring("testing the xtring").on(" the ").run(new OnMatch() {
            public void execute() {

                wasExecuted[0] = true;

                assertXtringEquals("testing", previous());
                assertEquals(0, previousStart());
                assertEquals(7, previousEnd());
                
                assertXtringEquals(" the ", match());
                assertEquals(7, matchStart());
                assertEquals(12, matchEnd());
                
                assertXtringEquals("xtring", next());
                assertEquals(12, nextStart());
                assertEquals(18, nextEnd());
            }
        });

        assertTrue("Match should be executed", wasExecuted[0]);
    }
    
    @Test
    public void usingSimpleGroupMatch() throws Exception {
        
        final Boolean[] wasExecuted = new Boolean[] { false };
        
        new Xtring("testing the xtring").on(" (\\w+) ").run(new OnMatch() {
            public void execute() {
                
                wasExecuted[0] = true;
                
                assertXtringEquals("testing ", previous());
                assertEquals(0, previousStart());
                assertEquals(8, previousEnd());
                
                assertXtringEquals("the", match());
                assertEquals(8, matchStart());
                assertEquals(11, matchEnd());
                
                assertXtringEquals(" xtring", next());
                assertEquals(11, nextStart());
                assertEquals(18, nextEnd());
            }
        });
        
        assertTrue("Match should be executed", wasExecuted[0]);
    }
    
    @Test
    public void usingMultipleGroupMatch() throws Exception {

        final List<Xtring> result = new LinkedList<Xtring>();
        
        new Xtring("testing the xtring").on("(t.st).*(xt.i)").run(new OnMatch() {
            public void execute() {
                result.add(match());
            }
        });
        
        assertEquals("Match size", 2, result.size());
        assertXtringEquals("xtri", result.get(0));
        assertXtringEquals("test", result.get(1));
    }
    
    @Test
    public void transforming() throws Exception {
    
        Xtring result = new Xtring("testing the xtring").on("\\s*the\\s*").run(new OnMatch() {
            public void execute(StringBuilder result) {
               result.insert(nextEnd(), ")"); 
               result.insert(nextStart(), "("); 
               result.insert(previousEnd(), ")"); 
               result.insert(previousStart(), "("); 
            }
        });
        
        assertXtringEquals("(testing) the (xtring)", result);
        
        Xtring result2 = new Xtring("testing the xtring").on("t").run(new OnMatch() {
            public void execute(StringBuilder result) {
                if (isLast()) {
                    result.insert(nextEnd(), ")"); 
                    result.insert(nextStart(), "(");
                }
                if (previous().isEmpty()) return;
                result.insert(previousEnd(), ")"); 
                result.insert(previousStart(), "("); 
            }
        });
        
        assertXtringEquals("t(es)t(ing )t(he x)t(ring)", result2);
        
    }
    
    @Test
    public void multipleTransformations() throws Exception {
        Xtring matchT = new Xtring("testing the xtring").on("t");
        
        Xtring result1 = matchT.run(new OnMatch() {
            public void execute(StringBuilder result) {
                result.delete(matchStart(), matchEnd());
            }
        });
        
        assertXtringEquals("esing he xring", result1);
        
        Xtring result2 = matchT.run(new OnMatch() {
            public void execute(StringBuilder result) {
                result.insert(matchEnd(), "|");
                result.insert(matchStart(), "|");
            }
        });        
        
        assertXtringEquals("|t|es|t|ing |t|he x|t|ring", result2);
    }
    
    @Test
    public void equals() throws Exception {
        assertTrue(new Xtring("text").equals(new Xtring("text")));
        
        // Xtring is equals to String
        assertTrue(new Xtring("text").equals("text"));
        
        // BEWARE, String is NOT equals Xtring
        assertFalse("text".equals(new Xtring("text")));
    }
    
    protected void assertXtringEquals(String message, String expected, Xtring actual) {
        assertEquals(message, expected, actual.toString());
    }

    protected void assertXtringEquals(String expected, Xtring actual) {
        assertEquals(expected, actual.toString());
    }
}
