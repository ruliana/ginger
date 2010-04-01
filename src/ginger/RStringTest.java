package ginger;

import static ginger.RString.*;
import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class RStringTest {

    @Test
    public void shortConstructors() throws Exception {
        assertTrue(r("test") instanceof RString);
        assertRStringEquals("test", r("test"));
    }
    
    @Test
    public void delete() throws Exception {
        // Single match
        assertRStringEquals("testing the", r("testing the test").delete().after("the"));
        assertRStringEquals(" the test", r("testing the test").delete().before("\\s+[^\\s]+\\s+"));
        assertRStringEquals("the test", r("testing the test").delete().before("ing (the) te"));
        assertRStringEquals("testing the", r("testing the test").delete().after("ing (the) te"));
        assertRStringEquals("the", r("testing the test").delete().around("ing (the) te"));
        assertRStringEquals("testing  test", r("testing the test").delete().inside("ing (the) te"));

        // Multiple matches
        assertRStringEquals("testingte", r("testing the test").delete().after("(ing).*(te)"));
        assertRStringEquals("teee", r("testing the test").delete().after("e"));
        assertRStringEquals("tetest", r("testing the test").delete().before("te"));
        assertRStringEquals("test the st", r("testing the test").delete().inside("(ing).*(te)"));
        assertRStringEquals("ingth", r("testing the test").delete().around("(i.g).*?(t.)"));
        assertRStringEquals("stst", r("testing the test").delete().around("st"));
        
        // No match
        assertRStringEquals("testing the test", r("testing the test").delete().inside("no match"));
    }

    @Test
    public void insert() throws Exception {
        // Simple match
        assertRStringEquals("testing thewabba test", r("testing the test").insert("wabba").after("the"));
        assertRStringEquals("testingwabba the test", r("testing the test").insert("wabba").before("\\s+[^\\s]+\\s+"));
        assertRStringEquals("testing wabbathe test", r("testing the test").insert("wabba").before("ing (the) te"));
        assertRStringEquals("testing thewabba test", r("testing the test").insert("wabba").after("ing (the) te"));
        assertRStringEquals("testing wabbathewabba test", r("testing the test").insert("wabba").around("ing (the) te"));
        
        // TODO: Not sure if the behavior is the expected one, but it makes sense (and same effect as insert.before)
        assertRStringEquals("testing wabbathe test", new RString("testing the test").insert("wabba").inside("ing (the) te"));
        
        // Multiple matches
        assertRStringEquals("te|sting the te|st", r("testing the test").insert("|").after("te"));
        assertRStringEquals("|testing the |test", r("testing the test").insert("|").before("te"));
        assertRStringEquals("|te|sting the |te|st", r("testing the test").insert("|").around("te"));
        assertRStringEquals("test|ing| |the| test", r("testing the test").insert("|").around("(ing).*(the)"));
    }
    
    @Test
    public void insertTwoStrings() throws Exception {
        // Simple match
        assertRStringEquals("testing the( test)", r("testing the test").insert("(", ")").after("the"));
        assertRStringEquals("(testing) the test", r("testing the test").insert("(", ")").before("\\s+[^\\s]+\\s+"));
        assertRStringEquals("(testing )the test", r("testing the test").insert("(", ")").before("ing (the) te"));
        assertRStringEquals("testing the( test)", r("testing the test").insert("(", ")").after("ing (the) te"));
        assertRStringEquals("(testing )the( test)", r("testing the test").insert("(", ")").around("ing (the) te"));
        assertRStringEquals("testing (the) test", r("testing the test").insert("(", ")").inside("ing (the) te"));
        
        // Multiple matches
        assertRStringEquals("(te)sting the (te)st", r("testing the test").insert("(", ")").inside("te"));
        // TODO: Useful, but not really intuitive, we need to make this better 
        // At least the meaning is compatible with "delete"
        assertRStringEquals("te(sting the )te(st)", r("testing the test").insert("(", ")").around("te"));
        assertRStringEquals("(te)st(ing the te)st", r("testing the test").insert("(", ")").around("st"));
        
        assertRStringEquals("(test)ing( the )te(st)", r("testing the test").insert("(", ")").around("(ing).*(te)"));
        assertRStringEquals("test(ing) the (te)st", r("testing the test").insert("(", ")").inside("(ing).*(te)"));
    }

    @Test
    public void replace() throws Exception {
        // Single match
        assertRStringEquals("testing thewabba", r("testing the test").replace("wabba").after("the"));
        assertRStringEquals("wabba the test", r("testing the test").replace("wabba").before("\\s+[^\\s]+\\s+"));
        assertRStringEquals("wabba the test", r("testing the test").replace("wabba").before("ing( the) te"));
        assertRStringEquals("testing the wabba", r("testing the test").replace("wabba").after("ing (the )te"));
        assertRStringEquals("wabba the wabba", r("testing the test").replace("wabba").around("ing( the )te"));
        assertRStringEquals("testing wabba test", r("testing the test").replace("wabba").inside("ing (the) te"));
        
        // Multiple matches
        assertRStringEquals("xsting the xst", r("testing the test").replace("x").inside("te"));
        assertRStringEquals("tes|es|", r("testing the test").replace("|").after("es"));
        assertRStringEquals("|te|test", r("testing the test").replace("|").before("te"));
        assertRStringEquals("|sting the |st", r("testing the test").replace("|").inside("te"));
        assertRStringEquals("|es|es|", r("testing the test").replace("|").around("es"));
    }
    
    @Test
    public void extract() throws Exception {
        assertRStringEquals("the", new RString("testing the test").extract("t.e"));
        assertRStringEquals("the", new RString("testing the test").extract("testing (.*) test"));
    }
    
    @Test
    public void serialize() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        
        objectOut.writeObject(new RString("test 1"));
        objectOut.writeObject(new RString("test 2"));
 
        objectOut.flush();
        objectOut.close();
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);
        
        assertRStringEquals("test 1", (RString) objectIn.readObject()); 
        assertRStringEquals("test 2", (RString) objectIn.readObject());
        
        objectIn.close();
    }
    
    @Test
    public void interestingUses() throws Exception {
        // This is easier to do with conventional replace, not worth :(
        assertRStringEquals("emphasis on <em>this</em> word", r("emphasis on this word").insert("<em>", "</em>").inside("this"));
        assertEquals("emphasis on <em>this</em> word", "emphasis on this word".replaceAll("(this)", "<em>$1</em>"));
        
        // 
        assertRStringEquals("emphasis on <em>this</em> word", r("emphasis on this word").insert("<em>", "</em>").inside("on (.*?) word"));
        assertRStringEquals("<em>emphasis</em> not <em>on the</em> not <em>word</em>", r("emphasis not on the not word").insert("<em>", "</em>").around("\\s+not\\s+"));
        
        // This is easier to do with conventional replace, not worth :(
        assertRStringEquals("T.I.A.", r("This Is an Acronym").extract("[A-Z]").insert(".").after("\\w"));
        assertEquals("T.I.A.", "This Is an Acronym".replaceAll("([A-Z])[^A-Z]+", "$1."));
        
    }
    
    private void assertRStringEquals(String expected, RString actual) {
        org.junit.Assert.assertEquals(expected, actual.toString());
    }
}
